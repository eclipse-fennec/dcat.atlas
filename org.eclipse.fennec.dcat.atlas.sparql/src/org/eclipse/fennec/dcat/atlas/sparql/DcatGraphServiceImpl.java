/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.dcat.atlas.sparql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionDatasetBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.system.Txn;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.dcat.atlas.api.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DataServiceReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DatasetSeriesReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.PublicView;
import org.eclipse.fennec.dcat.atlas.api.StoreRevision;
import org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService;
import org.eclipse.fennec.dcat.atlas.msg.body.readerwriter.EObjectToJena;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The in-memory RDF projection of the file store, and the SPARQL engine over it
 * (persistence plan P1-2/P1-3/P1-6/P1-7).
 * <p>
 * One <em>named graph per resource</em>, named by the resource's {@code rdf:about}
 * IRI, held in a {@link DatasetFactory#createTxnMem() transactional} in-memory
 * dataset — a plain {@code Model} would not survive SPARQL reads racing admin
 * writes, which they do by nature. Named graphs also make an update a single
 * idempotent operation (replace this resource's graph), which is what lets every
 * refresh path — startup, a write, reconciliation, an explicit rebuild — be the
 * same code.
 * <p>
 * The projection is derived, never authoritative: it is always rebuilt <em>from</em>
 * the stores, and content only ever enters through
 * {@link EObjectToJena#toModel} so that what SPARQL sees is exactly what
 * the REST layer would serialize.
 */
@Component(name = "DcatGraphService", service = { DcatGraphService.class, SparqlEngine.class,
		HealthCheck.class }, property = { HealthCheck.NAME + "=sparql", HealthCheck.TAGS + "=ready" })
@Designate(ocd = GraphConfig.class)
public class DcatGraphServiceImpl implements DcatGraphService, SparqlEngine, HealthCheck {

	private static final Logger LOGGER = Logger.getLogger(DcatGraphServiceImpl.class.getName());

	/** Identifies a stored resource; {@code parentId} is the dataset of a distribution. */
	private record ResourceRef(DcatEntity entity, String parentId, String id) {
	}

	/** Renders stored identities public, so query results match what a GET returns. */
	private final PublicIris publicIris;
	private final CatalogReadOnlyService catalogs;
	private final DatasetReadOnlyService datasets;
	private final DatasetSeriesReadOnlyService datasetSeries;
	private final DataServiceReadOnlyService dataServices;
	private final DistributionReadOnlyService distributions;

	private final boolean enabled;
	private final long queryTimeoutMillis;
	private final long maxResultRows;

	private final Dataset dataset = DatasetFactory.createTxnMem();

	/**
	 * Which named graph currently holds each resource. Needed because a resource's
	 * {@code about} — and therefore its graph name — can change between refreshes,
	 * and because a delete has to find the graph of a resource that no longer exists.
	 */
	private final Map<ResourceRef, String> graphNames = new ConcurrentHashMap<>();

	private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "dcat-graph");
		thread.setDaemon(true);
		return thread;
	});

	/** Resources the last pass could not project. Reported, never swallowed. */
	private final AtomicInteger skipped = new AtomicInteger();

	private volatile boolean ready;
	private volatile boolean refreshing;
	private volatile String lastFailure;
	/**
	 * The store version the projection was last built from, or {@code null} if that is
	 * unknown — no revision service bound, the store had no content, or the last refresh
	 * failed. Only ever set after a refresh that completed.
	 */
	private volatile String projectedRevision;

	/**
	 * How the reconcile poll tells whether anything has changed (P1-7).
	 * <p>
	 * Optional and dynamic: without it the poll simply re-reads everything, which is what
	 * it did before and is still correct — just not free. Deliberately a
	 * {@link StoreRevision} rather than anything storage-specific, so the projection stays
	 * unaware of what the store actually is.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile StoreRevision storeRevision;

	@Activate
	public DcatGraphServiceImpl( //
			@Reference PublicIris publicIris, //
			@Reference CatalogReadOnlyService catalogs, //
			@Reference DatasetReadOnlyService datasets, //
			@Reference DatasetSeriesReadOnlyService datasetSeries, //
			@Reference DataServiceReadOnlyService dataServices, //
			@Reference DistributionReadOnlyService distributions, //
			GraphConfig config) {
		this(publicIris, catalogs, datasets, datasetSeries, dataServices, distributions, config.enabled(),
				config.queryTimeoutMillis(), config.maxResultRows());

		if (!enabled) {
			LOGGER.info("SPARQL is disabled; no RDF projection will be built");
			return;
		}
		// Off the activation thread: the initial build is O(store size) and must not
		// hold up the rest of the runtime coming up. Readiness (P1-6) is what keeps
		// traffic away until it finishes, not a blocking activate.
		worker.execute(this::refreshQuietly);
		if (config.reconcileIntervalSeconds() > 0) {
			// Cheap safety net (P1-7): bounds how long the projection can be silently
			// wrong to one interval, whatever caused the drift.
			worker.scheduleWithFixedDelay(this::reconcileQuietly, config.reconcileIntervalSeconds(),
					config.reconcileIntervalSeconds(), TimeUnit.SECONDS);
		}
	}

	/**
	 * Package-visible for tests: takes the configuration as plain values and starts
	 * neither the initial build nor the reconciliation timer, so a test drives
	 * {@link #refresh()} itself and stays deterministic.
	 */
	DcatGraphServiceImpl(PublicIris publicIris, CatalogReadOnlyService catalogs, DatasetReadOnlyService datasets,
			DatasetSeriesReadOnlyService datasetSeries, DataServiceReadOnlyService dataServices,
			DistributionReadOnlyService distributions, boolean enabled, long queryTimeoutMillis, long maxResultRows) {
		this.publicIris = publicIris;
		this.catalogs = catalogs;
		this.datasets = datasets;
		this.datasetSeries = datasetSeries;
		this.dataServices = dataServices;
		this.distributions = distributions;
		this.enabled = enabled;
		this.queryTimeoutMillis = queryTimeoutMillis;
		this.maxResultRows = maxResultRows;
	}

	@Deactivate
	void deactivate() {
		worker.shutdownNow();
	}

	// --- DcatGraphService ---------------------------------------------------

	@Override
	public void invalidate(DcatEntity entity, String parentId, String id) {
		if (!enabled || id == null) {
			return;
		}
		if (entity == DcatEntity.DISTRIBUTION && parentId == null) {
			// Without the owning dataset the distribution cannot be re-read (FR-10), so
			// there is nothing correct to do here; the reconciliation pass will settle it.
			LOGGER.log(Level.WARNING,
					"No dataset given for distribution {0}; leaving the graph to the next reconciliation", id);
			return;
		}
		ResourceRef ref = new ResourceRef(entity, parentId, id);
		try {
			// Re-read rather than trusting a passed-in object: the graph must be a
			// function of what is on disk (G1), including when the write path was not REST.
			Optional<? extends EObject> stored = read(ref);
			if (stored.isPresent()) {
				project(ref, stored.get());
			} else {
				unproject(ref);
			}
		} catch (RuntimeException e) {
			// The file is already written; failing the caller now would report a failed
			// write that actually succeeded. Reconciliation repairs the projection.
			LOGGER.log(Level.WARNING, e, () -> "Could not project " + entity + " " + id + " into the RDF graph");
			lastFailure = e.toString();
		}
	}

	@Override
	public void rebuild() {
		if (enabled) {
			worker.execute(this::refreshQuietly);
		}
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	// --- SparqlEngine ------------------------------------------------------

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public long maxResultRows() {
		return maxResultRows;
	}

	@Override
	public <T> T execute(Query query, Function<QueryExecution, T> consumer) {
		return Txn.calculateRead(dataset, () -> {
			QueryExecutionDatasetBuilder builder = QueryExecutionDatasetBuilder.create().dataset(dataset).query(query);
			if (queryTimeoutMillis > 0) {
				builder = builder.timeout(queryTimeoutMillis, TimeUnit.MILLISECONDS);
			}
			try (QueryExecution execution = builder.build()) {
				return consumer.apply(execution);
			}
		});
	}

	// --- projection --------------------------------------------------------

	/**
	 * Brings the whole projection in line with the stores: everything found is
	 * re-projected and anything that has disappeared is dropped. Idempotent (G3), so
	 * the startup build, the periodic reconciliation and an explicit rebuild are all
	 * this same pass — there is no separate, less-tested repair path.
	 * <p>
	 * Deliberately does not clear the dataset first: queries keep being answered from
	 * the previous projection while this runs, and every graph is replaced anyway.
	 */
	void refresh() {
		Set<ResourceRef> seen = new HashSet<>();
		skipped.set(0);

		for (EObject catalog : catalogs.listCatalogs()) {
			projectFound(DcatEntity.CATALOG, null, catalog, seen);
		}
		for (EObject series : datasetSeries.listDatasetSeries()) {
			projectFound(DcatEntity.DATASET_SERIES, null, series, seen);
		}
		for (EObject service : dataServices.listDataServices()) {
			projectFound(DcatEntity.DATA_SERVICE, null, service, seen);
		}
		for (EObject dataset : datasets.listDatasets()) {
			String datasetId = projectFound(DcatEntity.DATASET, null, dataset, seen);
			if (datasetId == null) {
				continue;
			}
			// A distribution is only reachable through its dataset (FR-10); one that no
			// dataset references is orphaned and deliberately stays out of the graph.
			for (EObject distribution : distributions.listDistributionsForDataset(datasetId)) {
				projectFound(DcatEntity.DISTRIBUTION, datasetId, distribution, seen);
			}
		}

		for (ResourceRef stale : new ArrayList<>(graphNames.keySet())) {
			if (!seen.contains(stale)) {
				unproject(stale);
			}
		}

		ready = true;
		lastFailure = null;
	}

	/**
	 * One tick of the reconcile poll: a full refresh, unless the store is demonstrably
	 * unchanged since the projection was built.
	 * <p>
	 * Only the <em>poll</em> may skip. The startup build and {@link #rebuild()} always do
	 * the work, because {@code rebuild} is the repair path an operator reaches for and a
	 * repair that decided it had nothing to do would be useless.
	 * <p>
	 * The skip is safe because every way the projection can drift also moves the store
	 * version. A write that committed but whose {@code invalidate} failed — the one drift
	 * P1-4 tolerates by design — has by definition changed the store, so the next tick sees
	 * a different revision and repairs it. And {@link #projectedRevision} is only recorded
	 * after a refresh that completed, so a failed one is always retried.
	 * <p>
	 * A write therefore costs one full pass at the following tick and nothing after that:
	 * {@link #invalidate} repairs its own graph but does not claim the new store version,
	 * because it has no way to know the commit its write produced. Reads stay correct
	 * throughout — this only decides how often the safety net does redundant work.
	 */
	void reconcileQuietly() {
		String current = currentRevision();
		String projected = projectedRevision;
		if (current != null && current.equals(projected)) {
			LOGGER.log(Level.FINE, () -> "Store unchanged at " + current + "; skipping the reconcile pass");
			return;
		}
		refreshQuietly();
	}

	/** The store's version, or {@code null} when nothing can tell us. */
	private String currentRevision() {
		StoreRevision revision = storeRevision;
		if (revision == null) {
			return null;
		}
		try {
			return revision.current().orElse(null);
		} catch (RuntimeException e) {
			// A fingerprint that cannot be read is not a reason to skip the work it was
			// meant to save; fall through to the full pass.
			LOGGER.log(Level.FINE, e, () -> "Could not read the store revision; reconciling in full");
			return null;
		}
	}

	private void refreshQuietly() {
		refreshing = true;
		// Read before the refresh, not after: anything committed while it runs must leave
		// the recorded revision stale, so the next tick picks it up rather than skipping.
		String revision = currentRevision();
		try {
			refresh();
			projectedRevision = revision;
		} catch (RuntimeException e) {
			LOGGER.log(Level.SEVERE, e, () -> "Could not build the RDF projection from the store");
			lastFailure = e.toString();
			// Leave projectedRevision alone so the next tick retries rather than concluding
			// it is already in step with a store it failed to read.
			projectedRevision = null;
		} finally {
			refreshing = false;
		}
	}

	/**
	 * Projects a resource discovered by a full pass; returns its id, or {@code null}
	 * if it could not be projected.
	 * <p>
	 * Failures are per-resource, never fatal to the pass. One resource the RDF bridge
	 * cannot serialize must not leave the whole projection unbuilt — that would keep
	 * readiness CRITICAL and the endpoint at 503 indefinitely, taking the instance out
	 * of rotation over a single malformed record while REST serves it perfectly well.
	 * Skipped resources are counted and surfaced by the health check, because a
	 * silently partial graph answers queries with too few results.
	 */
	private String projectFound(DcatEntity entity, String parentId, EObject object, Set<ResourceRef> seen) {
		String id = idOf(entity, parentId, object);
		if (id == null) {
			LOGGER.log(Level.WARNING, "Skipping a stored {0} with no rdf:about — it has no RDF identity", entity);
			skipped.incrementAndGet();
			return null;
		}
		ResourceRef ref = new ResourceRef(entity, parentId, id);
		try {
			project(ref, object);
		} catch (RuntimeException e) {
			LOGGER.log(Level.WARNING, e, () -> "Skipping " + entity + " " + id + ": cannot be projected into RDF");
			skipped.incrementAndGet();
			return null;
		}
		// Only mark it seen once projected, so a resource that keeps failing is not
		// treated as present and does not mask a later successful projection.
		seen.add(ref);
		return id;
	}

	/** The store id of {@code object}, from its logical identity. */
	private static String idOf(DcatEntity entity, String parentId, EObject object) {
		String about = aboutOf(object);
		return entity == DcatEntity.DISTRIBUTION ? DcatIds.distributionIdOf(parentId, about)
				: DcatIds.idOf(collectionOf(entity), about);
	}

	private static String collectionOf(DcatEntity entity) {
		return switch (entity) {
		case CATALOG -> DcatIds.CATALOGS;
		case DATASET -> DcatIds.DATASETS;
		case DATASET_SERIES -> DcatIds.DATASET_SERIES;
		case DATA_SERVICE -> DcatIds.DATA_SERVICES;
		case DISTRIBUTION -> DcatIds.DATASETS;
		};
	}

	/**
	 * Projects one resource, named and described by the IRIs clients see.
	 * <p>
	 * The projection is rendered public before it enters the graph, so a SPARQL result
	 * carries the same identity a {@code GET} would return. Projecting the stored
	 * logical form instead would expose the internal base through query results — the
	 * one place it could still leak after the REST layer stopped serving it.
	 */
	private void project(ResourceRef ref, EObject object) {
		EObject rendered = PublicView.render(object, publicIris);
		String about = aboutOf(rendered);
		if (about == null || about.isBlank()) {
			LOGGER.log(Level.WARNING, "Cannot project {0} {1}: no rdf:about", new Object[] { ref.entity(), ref.id() });
			return;
		}
		Model model = EObjectToJena.toModel(rendered);
		String previous = graphNames.get(ref);
		Txn.executeWrite(dataset, () -> {
			// An about change renames the graph, so the old name has to go or the
			// resource would appear twice.
			if (previous != null && !previous.equals(about)) {
				dataset.removeNamedModel(previous);
			}
			dataset.replaceNamedModel(about, model);
		});
		graphNames.put(ref, about);
	}

	private void unproject(ResourceRef ref) {
		String name = graphNames.remove(ref);
		if (name != null) {
			Txn.executeWrite(dataset, () -> dataset.removeNamedModel(name));
		}
	}

	private Optional<? extends EObject> read(ResourceRef ref) {
		return switch (ref.entity()) {
		case CATALOG -> catalogs.getCatalog(ref.id());
		case DATASET -> datasets.getDataset(ref.id());
		case DATASET_SERIES -> datasetSeries.getDatasetSeries(ref.id());
		case DATA_SERVICE -> dataServices.getDataService(ref.id());
		case DISTRIBUTION -> distributions.getDistributionForDataset(ref.parentId(), ref.id());
		};
	}

	/**
	 * Reads {@code rdf:about} reflectively. Every DCAT-AP entity carries the feature
	 * but they share no common interface that declares it, and naming each type here
	 * would mean editing this class whenever the model gains an entity.
	 */
	private static String aboutOf(EObject object) {
		EStructuralFeature feature = object.eClass().getEStructuralFeature("about");
		if (feature == null) {
			return null;
		}
		Object value = object.eGet(feature);
		return value == null ? null : value.toString();
	}

	// --- F-25 readiness ----------------------------------------------------

	/**
	 * CRITICAL until the projection is built. An unbuilt graph does not fail
	 * queries, it answers them with too few results — so readiness, not the query
	 * path, is what has to keep traffic away.
	 */
	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		if (!enabled) {
			log.info("SPARQL disabled");
		} else if (!ready) {
			log.critical("RDF projection is still being built from the store");
		} else if (lastFailure != null) {
			log.warn("RDF projection is serving, but the last refresh failed: {}", lastFailure);
		} else if (skipped.get() > 0) {
			// Deliberately WARN, not CRITICAL: REST serves these resources fine, so taking
			// the whole instance out of rotation would be the wrong trade. But it must be
			// visible — SPARQL is under-reporting.
			log.warn("RDF projection holds {} named graph(s); {} resource(s) could not be projected, "
					+ "so SPARQL results are incomplete", graphCount(), skipped.get());
		} else if (refreshing) {
			// A later rebuild keeps answering from the previous projection, so this is
			// visible but not a reason to take the instance out of rotation.
			log.warn("RDF projection is being rebuilt; serving the previous {} named graph(s)", graphCount());
		} else {
			log.info("RDF projection holds {} named graph(s)", graphCount());
		}
		return new Result(log);
	}

	private long graphCount() {
		return Txn.calculateRead(dataset, () -> {
			List<String> names = new ArrayList<>();
			dataset.listNames().forEachRemaining(names::add);
			return (long) names.size();
		});
	}
}
