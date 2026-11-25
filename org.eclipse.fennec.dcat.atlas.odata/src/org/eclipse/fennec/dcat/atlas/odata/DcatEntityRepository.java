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
package org.eclipse.fennec.dcat.atlas.odata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicView;
import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatChangeNotifierTopics;
import org.eclipse.fennec.dcat.atlas.api.store.StoreRevision;
import org.eclipse.fennec.odata.persistence.api.EntityRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import dcat.DcatPackage;
import rdf.IdentifiedResource;

/**
 * The data source behind the OData endpoints: an in-memory snapshot of the store that the
 * {@code emf.odata} in-memory {@code QueryService} evaluates its query IR against.
 *
 * <h2>Why a snapshot and not a pass-through</h2>
 *
 * The {@link EntityRepository} contract requires that entities handed out can be read
 * freely — expanded navigations must be materialised, with no per-entity lazy loading and
 * no unresolved proxies escaping. Stored entities are the opposite of that: their
 * cross-resource links are EMF proxies resolved through a resource set that lives only for
 * the duration of one store operation. So the entities served here are detached copies,
 * made once per change and reused by every query.
 *
 * <h2>How the links are stitched</h2>
 *
 * {@link PublicView#render} already replaces every non-containment link with a key-only
 * stub carrying the target's public IRI — it reads the link without resolving it, so no
 * blob is fetched for a link. Stitching is then a lookup by {@code about}: a stub whose
 * target is in the snapshot is replaced by that very object, so {@code $expand=dataset} on
 * a catalog returns the entities the {@code Dataset} set serves. A stub with no match — an
 * external resource, or one deleted since — keeps its {@code about} and loses its proxy
 * URI, which is exactly what a link means to an OData client: the key, and nothing else.
 *
 * <h2>Which sets it supplies</h2>
 *
 * The four collections that have a read service, matched by exact type. In Ecore
 * {@code Catalog} and {@code DatasetSeries} extend {@code Dataset}, but they live in
 * separate stores, so the {@code Dataset} set must not return catalogs — which also makes
 * the cast segment {@code /Dataset/dcat.Catalog} legally empty. Distributions are
 * contained in their dataset and arrive with it; they are reachable as
 * {@code $expand=distribution}, not as a set of their own.
 */
@Component(name = "DcatODataRepository", service = { EntityRepository.class, HealthCheck.class, EventHandler.class },
		property = { HealthCheck.NAME + "=odata", HealthCheck.TAGS + "=ready",
				EventConstants.EVENT_TOPIC + "=" + DcatChangeNotifierTopics.BASE_TOPIC + "*" },
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = ODataConfig.class)
public class DcatEntityRepository implements EntityRepository, HealthCheck, EventHandler {

	private static final Logger LOGGER = Logger.getLogger(DcatEntityRepository.class.getName());

	/** The entity sets this repository answers for, in service-document order. */
	private static final List<EClass> SUPPLIED = List.of(DcatPackage.Literals.CATALOG,
			DcatPackage.Literals.DATASET, DcatPackage.Literals.DATASET_SERIES, DcatPackage.Literals.DATA_SERVICE);

	private final PublicIris publicIris;
	private final CatalogReadOnlyService catalogs;
	private final DatasetReadOnlyService datasets;
	private final DatasetSeriesReadOnlyService datasetSeries;
	private final DataServiceReadOnlyService dataServices;

	private final boolean enabled;

	/**
	 * What queries are answered from. Replaced wholesale by a refresh rather than mutated,
	 * so a query in flight keeps reading a consistent snapshot while the next one is built.
	 */
	private volatile Map<EClass, List<EObject>> snapshot = Map.of();

	/**
	 * How the reconcile pass tells whether anything changed. Optional and dynamic: without
	 * it the pass simply rebuilds, which is correct, just not free. A {@link StoreRevision}
	 * rather than anything storage-specific, so this stays unaware of what the store is.
	 */
	private final AtomicReference<StoreRevision> storeRevision = new AtomicReference<>();

	@Reference(name = "storeRevision", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	void setStoreRevision(StoreRevision revision) {
		storeRevision.set(revision);
	}

	void unsetStoreRevision(StoreRevision revision) {
		storeRevision.compareAndSet(revision, null);
	}

	private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(runnable -> {
		Thread thread = new Thread(runnable, "dcat-odata");
		thread.setDaemon(true);
		return thread;
	});

	private volatile boolean ready;
	private volatile boolean refreshing;
	private volatile String lastFailure;

	/** The store version the snapshot was built from, or {@code null} when unknown. */
	private volatile String builtRevision;

	@Activate
	public DcatEntityRepository( //
			@Reference PublicIris publicIris, //
			@Reference CatalogReadOnlyService catalogs, //
			@Reference DatasetReadOnlyService datasets, //
			@Reference DatasetSeriesReadOnlyService datasetSeries, //
			@Reference DataServiceReadOnlyService dataServices, //
			ODataConfig config) {
		this(publicIris, catalogs, datasets, datasetSeries, dataServices, config.enabled());

		if (!enabled) {
			LOGGER.info("OData is disabled; no snapshot will be built");
			return;
		}
		// Off the activation thread: the initial build is O(store size) and must not hold
		// up the rest of the runtime. Readiness is what keeps traffic away until it is
		// finished, not a blocking activate.
		worker.execute(this::refreshQuietly);
		if (config.reconcileIntervalSeconds() > 0) {
			worker.scheduleWithFixedDelay(this::reconcileQuietly, config.reconcileIntervalSeconds(),
					config.reconcileIntervalSeconds(), TimeUnit.SECONDS);
		}
	}

	/**
	 * Package-visible for tests: plain values instead of a configuration, and neither the
	 * initial build nor the reconcile timer started, so a test drives {@link #refresh()}
	 * itself and stays deterministic.
	 */
	DcatEntityRepository(PublicIris publicIris, CatalogReadOnlyService catalogs, DatasetReadOnlyService datasets,
			DatasetSeriesReadOnlyService datasetSeries, DataServiceReadOnlyService dataServices, boolean enabled) {
		this.publicIris = publicIris;
		this.catalogs = catalogs;
		this.datasets = datasets;
		this.datasetSeries = datasetSeries;
		this.dataServices = dataServices;
		this.enabled = enabled;
	}

	@Deactivate
	void deactivate() {
		worker.shutdownNow();
	}

	// --- EntityRepository ---------------------------------------------------

	@Override
	public boolean supplies(EClass entityType) {
		return SUPPLIED.contains(entityType);
	}

	@Override
	public List<EObject> entities(EClass entityType) {
		return snapshot.getOrDefault(entityType, List.of());
	}

	// --- EventHandler -------------------------------------------------------

	/**
	 * Any announced write or delete makes the snapshot stale. The worker is single
	 * threaded, so a burst of events coalesces into however many refreshes it has time
	 * for rather than running them concurrently; the last one wins and is correct.
	 */
	@Override
	public void handleEvent(Event event) {
		if (enabled) {
			worker.execute(this::refreshQuietly);
		}
	}

	// --- refresh ------------------------------------------------------------

	/**
	 * Rebuilds the snapshot from the stores. Idempotent, and the only way the snapshot
	 * ever changes — the startup build, an announced change and the reconcile pass are all
	 * this same pass, so there is no less-tested repair path.
	 */
	void refresh() {
		refreshing = true;
		try {
			List<EObject> roots = new ArrayList<>();
			Map<EClass, List<EObject>> byType = new LinkedHashMap<>();
			for (EClass type : SUPPLIED) {
				// renderAll hands back an immutable list, so the sort needs its own copy
				List<EObject> rendered = new ArrayList<>(PublicView.renderAll(read(type), publicIris));
				rendered.sort(Comparator.comparing(DcatEntityRepository::aboutOf,
						Comparator.nullsLast(Comparator.naturalOrder())));
				byType.put(type, List.copyOf(rendered));
				roots.addAll(rendered);
			}
			stitch(roots);
			snapshot = Map.copyOf(byType);
			ready = true;
			lastFailure = null;
		} finally {
			refreshing = false;
		}
	}

	/**
	 * Widened to {@code EObject} deliberately: the renderer is generic in the entity type,
	 * and a wildcard capture here would not unify with the one snapshot list.
	 */
	private List<EObject> read(EClass type) {
		if (type == DcatPackage.Literals.CATALOG) {
			return new ArrayList<>(catalogs.listCatalogs());
		}
		if (type == DcatPackage.Literals.DATASET) {
			return new ArrayList<>(datasets.listDatasets());
		}
		if (type == DcatPackage.Literals.DATASET_SERIES) {
			return new ArrayList<>(datasetSeries.listDatasetSeries());
		}
		return new ArrayList<>(dataServices.listDataServices());
	}

	/**
	 * Turns the rendered link stubs into references to the snapshot's own objects.
	 * <p>
	 * Package-visible so the stitching can be tested without a framework.
	 *
	 * @param roots every entity the snapshot serves, already rendered public
	 */
	static void stitch(List<EObject> roots) {
		Map<String, EObject> byAbout = new HashMap<>();
		for (EObject root : roots) {
			String about = aboutOf(root);
			if (about != null) {
				byAbout.putIfAbsent(about, root);
			}
		}
		for (EObject root : roots) {
			relink(root, byAbout);
			root.eAllContents().forEachRemaining(contained -> relink(contained, byAbout));
		}
	}

	@SuppressWarnings("unchecked")
	private static void relink(EObject object, Map<String, EObject> byAbout) {
		for (EReference reference : object.eClass().getEAllReferences()) {
			if (reference.isContainment() || reference.isDerived() || !object.eIsSet(reference)) {
				continue;
			}
			if (reference.isMany()) {
				EList<EObject> values = (EList<EObject>) object.eGet(reference, false);
				List<EObject> linked = new ArrayList<>(values.size());
				for (var it = basicIterator(values); it.hasNext();) {
					linked.add(linkTo(it.next(), byAbout));
				}
				values.clear();
				values.addAll(linked);
			} else {
				object.eSet(reference, linkTo((EObject) object.eGet(reference, false), byAbout));
			}
		}
	}

	/**
	 * The snapshot's own object for a link stub, or the stub itself with its proxy URI
	 * cleared. A proxy must not reach a query: the snapshot is detached, so nothing would
	 * resolve it, and the repository contract forbids handing one out. A stub that keeps
	 * only its {@code about} is the honest answer for a resource this portal does not hold.
	 */
	private static EObject linkTo(EObject stub, Map<String, EObject> byAbout) {
		if (stub == null) {
			return null;
		}
		String about = aboutOf(stub);
		EObject target = about == null ? null : byAbout.get(about);
		if (target != null) {
			return target;
		}
		((InternalEObject) stub).eSetProxyURI(null);
		return stub;
	}

	private static Iterator<EObject> basicIterator(EList<EObject> values) {
		return values instanceof InternalEList ? ((InternalEList<EObject>) values).basicIterator()
				: values.iterator();
	}

	private static String aboutOf(EObject object) {
		return object instanceof IdentifiedResource identified ? identified.getAbout() : null;
	}

	/** A refresh that reports rather than throws: nothing upstream can act on the failure. */
	private void refreshQuietly() {
		try {
			refresh();
			builtRevision = currentRevision();
		} catch (RuntimeException e) {
			LOGGER.log(Level.WARNING, e, () -> "Could not build the OData snapshot from the store");
			lastFailure = e.toString();
			// Leave builtRevision stale so the next reconcile tick retries rather than skips.
			builtRevision = null;
		}
	}

	/**
	 * One tick of the reconcile poll: a refresh, unless the store is demonstrably
	 * unchanged. Only the poll may skip — an announced change and an explicit build always
	 * do the work, because a skip is only ever as good as the revision it trusts.
	 */
	void reconcileQuietly() {
		String current = currentRevision();
		if (current != null && current.equals(builtRevision)) {
			LOGGER.log(Level.FINE, () -> "Store unchanged at " + current + "; skipping the OData reconcile pass");
			return;
		}
		refreshQuietly();
	}

	private String currentRevision() {
		StoreRevision revision = storeRevision.get();
		if (revision == null) {
			return null;
		}
		try {
			return revision.current().orElse(null);
		} catch (RuntimeException e) {
			LOGGER.log(Level.FINE, e, () -> "Could not read the store revision; reconciling in full");
			return null;
		}
	}

	// --- HealthCheck --------------------------------------------------------

	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		if (!enabled) {
			log.info("OData disabled");
		} else if (!ready) {
			// An incomplete snapshot answers successfully with too few results, which is
			// worse than answering not at all - so this gates readiness.
			log.critical("OData snapshot is still being built from the store");
		} else if (lastFailure != null) {
			log.warn("OData snapshot is serving, but the last refresh failed: {}", lastFailure);
		} else if (refreshing) {
			log.warn("OData snapshot is being rebuilt; serving the previous {} entities", count());
		} else {
			log.info("OData snapshot holds {} entities", count());
		}
		return new Result(log);
	}

	private int count() {
		return snapshot.values().stream().mapToInt(List::size).sum();
	}

	/** Package-visible for tests. */
	boolean isReady() {
		return ready;
	}
}
