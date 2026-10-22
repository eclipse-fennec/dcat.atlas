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
package org.eclipse.fennec.dcat.atlas.impl.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntime;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.FailureDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.FailureReasons;
import org.eclipse.fennec.dcat.atlas.api.runtime.IntegrityDTO;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.store.IntegrityFinding;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreChanges;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreHealth;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreIntrospection;
import org.eclipse.fennec.jgit.api.GitService;
import org.eclipse.fennec.services.runtime.ChangeCountPublisher;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

import dcat.Catalog;

/**
 * Keeps the runtime snapshot current and publishes it with a change count.
 *
 * <p>
 * <strong>It registers the service itself rather than letting DS do it</strong>, and that is
 * the one thing here that is not a preference. {@code service.changecount} can only be moved
 * through {@code ServiceRegistration.setProperties}, and a DS-declared service hands out no
 * registration to call it on. So the component activates, registers through
 * {@link ChangeCountPublisher} — which already does this for the broker and client runtimes
 * and coalesces the updates onto a daemon thread — and keeps the handle. {@code service = {}}
 * on the annotation is load-bearing: without it DS would register a second copy of this
 * service beside the publisher's, one with a change count that moves and one with properties
 * that never do, and a consumer binding the wrong one would be told nothing for the lifetime
 * of the framework.
 * </p>
 *
 * <p>
 * <strong>Everything it reports is optional.</strong> A runtime service that needed a store
 * in order to start would be absent in exactly the situation it exists to report — a portal
 * where nothing came up — and an integration test waiting for it would wait rather than be
 * told.
 * </p>
 *
 * <p>
 * <strong>Reading happens on change; assembly happens per call.</strong>
 * {@link #refresh()} does the work that touches the store, and runs when a write lands or a
 * service comes and goes. {@link #snapshot()} assembles a fresh DTO from what was read, so it
 * costs an allocation and nothing else — which is what makes the telemetry consumer
 * affordable, since it takes one snapshot per gauge per export interval. A fresh DTO per call
 * also means no reader holds the instance another reader could change: a DTO has public
 * fields.
 * </p>
 */
@Component(name = "DcatAtlasRuntime", immediate = true, configurationPid = "DcatAtlasRuntime",
		configurationPolicy = ConfigurationPolicy.OPTIONAL, service = {})
public class DcatAtlasRuntimeComponent implements DcatAtlasRuntime {

	private static final Logger LOG = Logger.getLogger(DcatAtlasRuntimeComponent.class.getName());

	/** Only this portal's own components are reported as unsatisfied; the framework's are not ours. */
	private static final String OUR_BUNDLES = "org.eclipse.fennec.dcat.atlas";

	/**
	 * The Jakarta RS whiteboard's runtime service, named as a string rather than imported.
	 *
	 * <p>
	 * Its {@code osgi.jakartars.endpoint} property is where this portal is actually
	 * reachable, which is otherwise something a deployment has to write down a second time
	 * and keep in step by hand. Tracking it by name keeps the REST stack off this bundle's
	 * class path: a portal with no REST layer reports no endpoints rather than failing to
	 * resolve.
	 * </p>
	 */
	private static final String JAKARTARS_RUNTIME = "(objectClass=org.osgi.service.jakartars.runtime.JakartarsServiceRuntime)";

	/** Where the whiteboard says it serves. */
	private static final String JAKARTARS_ENDPOINT = "osgi.jakartars.endpoint";

	private final AtomicLong changeCount = new AtomicLong();

	/**
	 * The stores, bound as a whole set rather than one by one: {@code validateOnWrite} is
	 * configured once per store, and the disagreement between two of them is what the
	 * snapshot reports.
	 */
	private final List<StoreIntrospection> stores = new CopyOnWriteArrayList<>();

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
			policyOption = ReferencePolicyOption.GREEDY)
	volatile PublicIris publicIris;

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
			policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validation;

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
			policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatGraphService graph;

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
			policyOption = ReferencePolicyOption.GREEDY)
	volatile CatalogReadOnlyService catalogs;

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
			policyOption = ReferencePolicyOption.GREEDY)
	volatile GitService gitService;

	/** Without it the failure half of the snapshot cannot see a component that never started. */
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
			policyOption = ReferencePolicyOption.GREEDY)
	volatile ServiceComponentRuntime componentRuntime;

	private List<String> endpoints = List.of();

	private ChangeCountPublisher<DcatAtlasRuntime> publisher;

	private AutoCloseable commitHook;

	/** Tracked for its properties alone; the service object is never asked for. */
	private ServiceTracker<Object, Object> endpointTracker;

	/** What was last read. Replaced wholesale, never mutated in place. */
	private volatile Readings readings = Readings.empty();

	/**
	 * Something changed and the readings have not caught up.
	 *
	 * <p>
	 * <strong>Why the reading is deferred rather than done when the change arrives.</strong>
	 * The notification fires inside {@code Store.commit}, on the thread that is still
	 * completing the write. Reading the store from there re-enters it — a listing per
	 * collection and a load per catalog, through the same resource set the operation is
	 * still using — and the operation fails with a {@code 500} after its commit has already
	 * been written. Measured, not theorised: it broke six write tests the first time this
	 * was built the other way round.
	 * </p>
	 *
	 * <p>
	 * So a change costs a flag and an increment, and the reading happens on the next
	 * snapshot that needs it. The cost rule survives: a burst of snapshots with nothing
	 * happening in between reads nothing, so an export interval that takes one snapshot per
	 * gauge still reads the store at most once, and only if the portal changed.
	 * </p>
	 */
	private volatile boolean stale = true;

	@Activate
	void activate(BundleContext context, Map<String, Object> properties) {
		this.endpoints = strings(properties.get(DCAT_ATLAS_ENDPOINT));

		try {
			endpointTracker = new ServiceTracker<>(context, context.createFilter(JAKARTARS_RUNTIME),
					new ServiceTrackerCustomizer<Object, Object>() {

						@Override
						public Object addingService(ServiceReference<Object> reference) {
							changed();
							// Never getService: this is tracked for its properties, and taking
							// a reference to the REST runtime only to read one of them would
							// hold a stack this bundle has no business holding open.
							return reference;
						}

						@Override
						public void modifiedService(ServiceReference<Object> reference, Object service) {
							changed();
						}

						@Override
						public void removedService(ServiceReference<Object> reference, Object service) {
							changed();
						}
					});
			endpointTracker.open();
		} catch (InvalidSyntaxException impossible) {
			// A constant built from constants. Reporting no endpoints beats not activating.
			LOG.severe("[DCAT.Atlas] the REST runtime filter is malformed, no endpoints will be reported: "
					+ impossible);
		}

		// Every write in this portal ends in one commit, and that is where the snapshot is
		// told. Polling the store instead would re-read the tree once per gauge.
		commitHook = StoreChanges.onCommit(this::changed);

		// No extra service properties: the endpoint is not one. See DCAT_ATLAS_ENDPOINT —
		// the publisher's properties are fixed at construction, and the effective endpoint
		// can arrive later from the whiteboard, so it travels in the snapshot instead.
		publisher = new ChangeCountPublisher<>(context, DcatAtlasRuntime.class, this, changeCount::get,
				"DCAT.Atlas runtime service");
		publisher.open();
	}

	@Deactivate
	void deactivate() {
		if (endpointTracker != null) {
			endpointTracker.close();
		}
		closeQuietly(commitHook);
		if (publisher != null) {
			publisher.close();
		}
	}

	/**
	 * A store arrived, changed or left. Bound by method so the set can be kept, and counted
	 * so a consumer is told: a store coming up changes what the portal holds.
	 */
	@Reference(name = "stores", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE,
			policyOption = ReferencePolicyOption.GREEDY)
	void addStore(StoreIntrospection store) {
		stores.add(store);
		changed();
	}

	void removeStore(StoreIntrospection store) {
		stores.remove(store);
		changed();
	}

	@Override
	public DcatAtlasRuntimeDTO snapshot() {
		Readings current = currentReadings();
		return SnapshotBuilder.build(changeCount.get(), System.currentTimeMillis(), current.identity(),
				current.endpoints(), current.store(), current.validation(), current.graph(), current.collections(),
				current.catalogs(), current.failures());
	}

	/**
	 * Reads every stored resource and reports what is wrong with the content.
	 *
	 * <p>
	 * Each store scans itself, because each one knows how to read its own collection and
	 * holds the validation service that judges it. The cost is the whole portal: this is the
	 * method the snapshot refuses to be.
	 * </p>
	 */
	@Override
	public IntegrityDTO checkIntegrity() {
		long started = System.currentTimeMillis();
		List<FailureDTO> failures = new ArrayList<>();
		int scanned = 0;
		// Scanned once per collection, not once per store: the admin and read-only services
		// for a collection are two services over the same content, and reporting each defect
		// twice would be a worse answer than reporting it once.
		Map<String, StoreIntrospection> byCollection = new LinkedHashMap<>();
		for (StoreIntrospection store : stores) {
			byCollection.putIfAbsent(store.collection(), store);
		}
		for (StoreIntrospection store : byCollection.values()) {
			scanned += store.ids().size();
			for (IntegrityFinding finding : store.scan()) {
				FailureDTO failure = new FailureDTO();
				failure.reason = finding.reason();
				failure.subject = finding.id() == null ? finding.collection()
						: publicIri(finding.collection(), finding.id());
				failure.message = finding.message();
				failures.add(failure);
			}
		}
		IntegrityDTO integrity = new IntegrityDTO();
		integrity.takenAt = started;
		integrity.durationMillis = System.currentTimeMillis() - started;
		integrity.resourcesScanned = scanned;
		integrity.failures = List.copyOf(failures);
		return integrity;
	}

	/** Named by the identity a client can dereference, the way refusals are (#46). */
	private String publicIri(String collection, String id) {
		String logical = DcatIds.logicalIri(collection, id);
		PublicIris iris = publicIris;
		return iris == null ? logical : iris.toPublic(logical);
	}

	/**
	 * Something changed: raise the count now, read later.
	 *
	 * <p>
	 * Called from the commit hook and whenever a store comes or goes. It does no I/O at all —
	 * see {@link #stale} for why that is not an optimisation.
	 * </p>
	 */
	void changed() {
		stale = true;
		changeCount.incrementAndGet();
	}

	/**
	 * The readings, re-read first if anything has happened since they were taken.
	 *
	 * <p>
	 * The expensive half, and the only half that touches the store: a listing per collection
	 * and one read per catalog, with no dataset ever loaded.
	 * </p>
	 */
	private Readings currentReadings() {
		if (!stale) {
			return readings;
		}
		// Cleared before the read, not after: a change that lands while this one is running
		// has to leave the flag set, or it would be read over and lost.
		stale = false;
		try {
			readings = read();
		} catch (RuntimeException e) {
			// Stale readings beat no snapshot, and beat a component that dies: what was read
			// before stands, and the store's own failure is reported by the next one that
			// succeeds.
			LOG.warning("[DCAT.Atlas] the runtime snapshot could not be refreshed, keeping the previous readings: " + e);
		}
		return readings;
	}

	private Readings read() {
		GitService git = gitService;
		String location = git == null ? null : git.getGitUrl();
		StoreReading store = new StoreReading("git", location, !StoreHealth.heldInMemory(location),
				StoreHealth.ready(git));

		DcatValidationService validationService = validation;
		ValidationReading validationReading = validationService == null
				? new ValidationReading(null, 0, null, 0, false)
				: new ValidationReading(validationService.shapesDirectory(), validationService.shapeFileCount(),
						validationService.vocabularyDirectory(), validationService.vocabularyFileCount(),
						validationService.isWriteEnforced());

		DcatGraphService graphService = graph;
		GraphReading graphReading = graphService == null ? new GraphReading(false, false, 0, 0)
				: new GraphReading(graphService.isEnabled(), graphService.isReady(), graphService.maxResultRows(),
						graphService.queryTimeoutMillis());

		PublicIris iris = publicIris;
		IdentityReading identity = new IdentityReading(DcatIds.LOGICAL_BASE, iris == null ? null : iris.publicBase());

		return new Readings(identity, effectiveEndpoints(), store, validationReading, graphReading, collections(),
				catalogs(iris), unsatisfiedComponents());
	}

	/**
	 * Where this portal serves: what the deployment configured, or what the REST layer says.
	 *
	 * <p>
	 * Configuration wins when it is set, because a portal behind a proxy is reachable at an
	 * address the framework cannot see. Everywhere else the whiteboard already knows, and
	 * nobody should have to write it down twice.
	 * </p>
	 */
	private List<String> effectiveEndpoints() {
		if (!endpoints.isEmpty()) {
			return endpoints;
		}
		ServiceReference<Object>[] references = endpointTracker == null ? null
				: endpointTracker.getServiceReferences();
		if (references == null) {
			return List.of();
		}
		List<Object> announced = new ArrayList<>();
		for (ServiceReference<Object> reference : references) {
			announced.add(reference.getProperty(JAKARTARS_ENDPOINT));
		}
		return endpointsOf(announced);
	}

	/**
	 * The endpoints out of whatever the whiteboard put in that property.
	 *
	 * <p>
	 * Separated out because the property's type is not fixed — the whiteboard may announce
	 * one endpoint as a {@code String}, several as a {@code String[]}, and an
	 * implementation is free to use a {@code Collection} — and because duplicates across
	 * several runtimes would otherwise be reported twice.
	 * </p>
	 */
	static List<String> endpointsOf(List<Object> announced) {
		List<String> endpoints = new ArrayList<>();
		for (Object value : announced) {
			for (String endpoint : strings(value)) {
				if (!endpoint.isBlank() && !endpoints.contains(endpoint)) {
					endpoints.add(endpoint);
				}
			}
		}
		return List.copyOf(endpoints);
	}

	private List<CollectionReading> collections() {
		List<CollectionReading> readings = new ArrayList<>();
		for (StoreIntrospection store : stores) {
			readings.add(new CollectionReading(store.collection(), store.ids(), store.validateOnWrite()));
		}
		return readings;
	}

	/**
	 * The catalogs, which are the one kind of resource a snapshot reads rather than counts.
	 *
	 * <p>
	 * There are tens of them and the question "what is in this portal" cannot be answered
	 * without them. Their members are counted from the catalog that is already in hand —
	 * nothing a catalog points at is resolved, which would load every dataset in the portal.
	 * </p>
	 */
	private List<CatalogReading> catalogs(PublicIris iris) {
		CatalogReadOnlyService service = catalogs;
		if (service == null) {
			return List.of();
		}
		List<CatalogReading> readings = new ArrayList<>();
		for (Catalog catalog : service.listCatalogs()) {
			String about = catalog.getAbout();
			readings.add(new CatalogReading(DcatIds.idOf(DcatIds.CATALOGS, about),
					iris == null || about == null ? about : iris.toPublic(about), firstTitle(catalog),
					catalog.getDataset().size(), catalog.getService().size(), catalog.getCatalog().size(),
					catalog.getRecord().size()));
		}
		return readings;
	}

	private static String firstTitle(Catalog catalog) {
		return catalog.getTitle().isEmpty() ? null : catalog.getTitle().get(0).getValue();
	}

	/**
	 * Components of ours that never started.
	 *
	 * <p>
	 * The only way to see one: a service collector shows what registered, so it can never
	 * name what did not. {@code PublicIris} without its mandatory configuration is the case
	 * this was written for — every IRI the portal serves is then wrong, and nothing else
	 * says so.
	 * </p>
	 */
	private List<FailureDTO> unsatisfiedComponents() {
		ServiceComponentRuntime scr = componentRuntime;
		if (scr == null) {
			return List.of();
		}
		List<FailureDTO> failures = new ArrayList<>();
		for (ComponentDescriptionDTO description : scr.getComponentDescriptionDTOs()) {
			if (description.bundle == null || description.bundle.symbolicName == null
					|| !description.bundle.symbolicName.startsWith(OUR_BUNDLES)) {
				continue;
			}
			for (ComponentConfigurationDTO configuration : scr.getComponentConfigurationDTOs(description)) {
				if (configuration.state == ComponentConfigurationDTO.ACTIVE
						|| configuration.state == ComponentConfigurationDTO.SATISFIED) {
					continue;
				}
				FailureDTO failure = new FailureDTO();
				failure.reason = FailureReasons.UNSATISFIED_COMPONENT;
				failure.subject = description.name;
				failure.message = "the component is " + stateName(configuration.state)
						+ "; it registered nothing, so nothing else can report it";
				failures.add(failure);
			}
		}
		return failures;
	}

	private static String stateName(int state) {
		return switch (state) {
		case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION -> "waiting for its configuration";
		case ComponentConfigurationDTO.UNSATISFIED_REFERENCE -> "waiting for a mandatory reference";
		case ComponentConfigurationDTO.FAILED_ACTIVATION -> "failing to activate";
		default -> "not active (state " + state + ")";
		};
	}

	private static List<String> strings(Object value) {
		if (value == null) {
			return List.of();
		}
		if (value instanceof String[] array) {
			return List.of(array);
		}
		if (value instanceof String single) {
			return single.isBlank() ? List.of() : List.of(single);
		}
		if (value instanceof java.util.Collection<?> many) {
			List<String> all = new ArrayList<>();
			many.forEach(item -> {
				if (item != null) {
					all.add(item.toString());
				}
			});
			return List.copyOf(all);
		}
		return List.of(value.toString());
	}

	private static void closeQuietly(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception ignored) {
			// Deactivation is not a place to fail.
		}
	}

	/** Everything the snapshot reports, as it was last read. */
	record Readings(IdentityReading identity, List<String> endpoints, StoreReading store, ValidationReading validation,
			GraphReading graph, List<CollectionReading> collections, List<CatalogReading> catalogs,
			List<FailureDTO> failures) {

		static Readings empty() {
			return new Readings(new IdentityReading(DcatIds.LOGICAL_BASE, null), List.of(),
					new StoreReading("git", null, false, false), new ValidationReading(null, 0, null, 0, false),
					new GraphReading(false, false, 0, 0), List.of(), List.of(), List.of());
		}
	}
}
