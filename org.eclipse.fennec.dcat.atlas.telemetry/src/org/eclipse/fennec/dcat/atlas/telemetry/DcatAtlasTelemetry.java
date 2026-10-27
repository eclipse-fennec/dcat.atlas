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
package org.eclipse.fennec.dcat.atlas.telemetry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.logging.Logger;

import org.eclipse.fennec.dcat.atlas.api.runtime.CatalogDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.CollectionDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntime;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableLongGauge;

/**
 * What this portal holds, as metrics.
 *
 * <p>
 * This is the bundle the runtime service was built for. It binds whatever DCAT.Atlas runtime
 * service is in the framework and turns it into a handful of gauges. It reaches into nothing:
 * every number here comes from a DTO the portal itself decided to hand out, which is why the
 * snapshot had to be cheap enough to take once per gauge.
 * </p>
 *
 * <p>
 * <strong>Gauges are observed, not pushed.</strong> An instrument asks for a snapshot when
 * the exporter collects, which is the OpenTelemetry idiom and the honest one: a number
 * recorded when it changed stops being true between changes, and a portal that goes quiet
 * would export a stale reading forever.
 * </p>
 *
 * <p>
 * The change count is still worth exporting, and it is not a cache key here: it says
 * <em>how often</em> the portal changed, which no gauge over the current state can answer — a
 * dataset that is added and removed between two collections looks like nothing happened to
 * both of them.
 * </p>
 */
@Component(configurationPid = "org.eclipse.fennec.dcat.atlas.telemetry", immediate = true,
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class DcatAtlasTelemetry {

	private static final Logger LOG = Logger.getLogger(DcatAtlasTelemetry.class.getName());

	private static final AttributeKey<String> COLLECTION = AttributeKey.stringKey("fennec.collection");
	private static final AttributeKey<String> CATALOG = AttributeKey.stringKey("fennec.catalog");

	/**
	 * Where the instruments come from, taken in the constructor.
	 *
	 * <p>
	 * Not a field reference, and that is the lesson this class inherits from its sibling in
	 * {@code emf.services}: DS binds the dynamic reference below while it is building the
	 * component, and a field reference may not be injected yet at that moment. A constructor
	 * parameter is the one thing that is certainly there before any bind method runs.
	 * </p>
	 */
	private final MeterProvider meters;

	private final Map<DcatAtlasRuntime, Watched> watched = new ConcurrentHashMap<>();

	/**
	 * Initialised rather than left null only so there is something to compare against;
	 * {@link #apply} has set it from configuration before any runtime can be bound.
	 */
	private volatile String scope = "org.eclipse.fennec.dcat.atlas";

	private volatile boolean catalogSeries = false;

	/**
	 * Takes the configuration here rather than in an activate method of its own.
	 *
	 * <p>
	 * It is also the earlier of the two: SCR constructs the instance, <em>then</em> binds the
	 * services, <em>then</em> calls an activate method (112.5.6). So configuring from the
	 * constructor means every {@link Watched} is built with the configured scope to begin
	 * with, where an activate method would have had to repair the ones bound before it ran.
	 * </p>
	 */
	@Activate
	public DcatAtlasTelemetry(@Reference MeterProvider meters, TelemetryConfig config) {
		this.meters = meters;
		apply(config);
	}

	/**
	 * The component's own configuration changed while it was running.
	 *
	 * <p>
	 * Having this means the change costs a rebuild of the instruments rather than a
	 * deactivate and activate, which would unbind every runtime and lose nothing but time.
	 * </p>
	 */
	@Modified
	void modify(TelemetryConfig config) {
		apply(config);
	}

	/**
	 * Takes the configuration, and rebuilds the instruments if it says anything different.
	 *
	 * <p>
	 * The loop does nothing when this runs from the constructor — no service is bound yet —
	 * and everything when it runs from {@link #modify}: a new scope is a new meter, and the
	 * switch decides which instruments exist, so either way they are built again.
	 * </p>
	 */
	private void apply(TelemetryConfig config) {
		if (config.scope().equals(scope) && config.catalogSeries() == catalogSeries) {
			return;
		}
		this.scope = config.scope();
		this.catalogSeries = config.catalogSeries();
		for (Watched entry : watched.values()) {
			entry.rebuild();
		}
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void setRuntime(DcatAtlasRuntime runtime, Map<String, Object> properties) {
		watched.put(runtime, new Watched(() -> gauges(runtime)));
		LOG.info("[DCAT.Atlas] watching the runtime service, which holds "
				+ runtime.snapshot().catalogs.count + " catalog(s)");
	}

	/**
	 * The portal said something changed.
	 *
	 * <p>
	 * Nothing to re-register: the gauges read the current snapshot when the exporter
	 * collects. What this is for is the record that a change happened at all.
	 * </p>
	 *
	 * <p>
	 * Named {@code updated}, not {@code modified}: DS calls the first when a <em>bound
	 * service's</em> properties change and the second when the <em>component's own
	 * configuration</em> does. A watcher that writes {@code modified} here compiles, binds,
	 * and is never told anything.
	 * </p>
	 */
	void updatedRuntime(DcatAtlasRuntime runtime, Map<String, Object> properties) {
		LOG.fine(() -> "[DCAT.Atlas] the runtime changed, now at " + properties.get("service.changecount"));
	}

	void unsetRuntime(DcatAtlasRuntime runtime) {
		Watched entry = watched.remove(runtime);
		if (entry != null) {
			entry.close();
		}
	}

	@Deactivate
	void deactivate() {
		for (DcatAtlasRuntime runtime : List.copyOf(watched.keySet())) {
			unsetRuntime(runtime);
		}
	}

	/**
	 * The instruments over one runtime.
	 *
	 * <p>
	 * Every one of them is a function of the snapshot, so the set can be rebuilt from
	 * configuration without the portal knowing anything about it.
	 * </p>
	 */
	private List<ObservableLongGauge> gauges(DcatAtlasRuntime runtime) {
		Meter meter = meters.get(scope);
		List<ObservableLongGauge> instruments = new ArrayList<>();

		instruments.add(gauge(meter, runtime, "dcat.atlas.changes", "How often the portal has changed since it started",
				snapshot -> snapshot.changeCount));

		// One series per collection rather than one instrument per kind: the collections are
		// a closed set the portal already names, and an attribute is what a backend groups by.
		instruments.add(multiPoint(meter, "dcat.atlas.resources", "Resources held, per collection",
				() -> resourcesPerCollection(runtime.snapshot())));

		if (catalogSeries) {
			instruments.add(multiPoint(meter, "dcat.atlas.catalog.datasets", "Datasets listed, per catalog",
					() -> datasetsPerCatalog(runtime.snapshot())));
		}

		return instruments;
	}

	/**
	 * How much each collection holds — <strong>all four of them, every time</strong>.
	 *
	 * <p>
	 * A zero and a missing series are different things to every metrics backend. A portal
	 * that published {@code datasets} only once it had one would make "no datasets" and "the
	 * exporter died" the same observation, so an empty collection reports its zero and a
	 * collection whose service is not there reports zero too.
	 * </p>
	 */
	static Map<Attributes, Long> resourcesPerCollection(DcatAtlasRuntimeDTO snapshot) {
		Map<Attributes, Long> points = new LinkedHashMap<>();
		points.put(Attributes.of(COLLECTION, "catalogs"), snapshot.catalogs == null ? 0L : snapshot.catalogs.count);
		points.put(Attributes.of(COLLECTION, "datasets"), count(snapshot.datasets));
		points.put(Attributes.of(COLLECTION, "data-services"), count(snapshot.dataServices));
		points.put(Attributes.of(COLLECTION, "dataset-series"), count(snapshot.datasetSeries));
		return points;
	}

	private static long count(CollectionDTO collection) {
		return collection == null ? 0L : collection.count;
	}

	/** What each catalog lists, when the per-catalog series are switched on. */
	static Map<Attributes, Long> datasetsPerCatalog(DcatAtlasRuntimeDTO snapshot) {
		Map<Attributes, Long> points = new LinkedHashMap<>();
		if (snapshot.catalogs != null && snapshot.catalogs.resources != null) {
			for (CatalogDTO catalog : snapshot.catalogs.resources) {
				points.put(Attributes.of(CATALOG, catalog.id), (long) catalog.datasets);
			}
		}
		return points;
	}

	private static ObservableLongGauge multiPoint(Meter meter, String name, String description,
			Supplier<Map<Attributes, Long>> points) {
		return meter.gaugeBuilder(name).setDescription(description).ofLongs()
				.buildWithCallback(measurement -> points.get()
						.forEach((attributes, value) -> measurement.record(value, attributes)));
	}

	private static ObservableLongGauge gauge(Meter meter, DcatAtlasRuntime runtime, String name, String description,
			ToLongFunction<DcatAtlasRuntimeDTO> reading) {
		return meter.gaugeBuilder(name).setDescription(description).ofLongs()
				.buildWithCallback(measurement -> measurement.record(reading.applyAsLong(runtime.snapshot())));
	}

	/** One runtime's instruments, so a configuration change can replace them as a set. */
	private final class Watched implements AutoCloseable {

		private final Supplier<List<ObservableLongGauge>> build;

		private volatile List<ObservableLongGauge> instruments;

		Watched(Supplier<List<ObservableLongGauge>> build) {
			this.build = build;
			this.instruments = build.get();
		}

		void rebuild() {
			close();
			instruments = build.get();
		}

		@Override
		public void close() {
			List<ObservableLongGauge> current = instruments;
			if (current == null) {
				return;
			}
			current.forEach(ObservableLongGauge::close);
			instruments = List.of();
		}
	}
}
