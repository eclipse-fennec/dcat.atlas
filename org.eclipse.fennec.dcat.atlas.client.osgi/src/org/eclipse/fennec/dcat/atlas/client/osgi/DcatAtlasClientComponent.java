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
package org.eclipse.fennec.dcat.atlas.client.osgi;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.dcat.atlas.client.api.ClientConfiguration;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClientFactory;
import org.eclipse.fennec.dcat.atlas.client.api.DcatCollection;
import org.eclipse.fennec.dcat.atlas.client.api.DeleteMode;
import org.eclipse.fennec.dcat.atlas.client.api.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.Distribution;
import jakarta.ws.rs.client.ClientBuilder;

/**
 * The OSGi front-end: one configured portal, published as both a synchronous
 * {@link DcatAtlasClient} and an {@link AsyncDcatAtlasClient} over the same client.
 *
 * <h2>One configuration, one portal</h2>
 *
 * A ConfigAdmin <b>factory</b> PID ({@value #PID}), so several portals can be connected in
 * parallel and each is an independent component instance. Each publishes its
 * {@code dcat.portal} name as a service property, so a consumer says which it means —
 * {@code @Reference(target = "(dcat.portal=jena)")} — and publishing to two is an explicit
 * loop over two references rather than a facade that would have to define what "it worked"
 * means when one portal accepts and another refuses.
 * <p>
 * {@link ConfigurationPolicy#REQUIRE} with no {@code @Modified} method: a configuration
 * change tears the instance down, closing its client, and activates a fresh one. Simpler
 * than mutating a live client, and a client is cheap to rebuild.
 *
 * <h2>Reusing the plain-Java client rather than reimplementing it</h2>
 *
 * The client comes from the {@link DcatAtlasClientFactory} <em>service</em> — the impl bundle
 * registers it through bnd's {@code @ServiceProvider}, so nothing here calls
 * {@code ServiceLoader} inside the framework or touches the impl's unexported classes. Only
 * the Jakarta RS seam differs: {@link WhiteboardJakartaRsClientProvider} hands over the
 * Whiteboard's {@link ClientBuilder}, so the runtime's own HTTP client and registered
 * providers apply while the timeout and authentication wiring is reused as-is. That reference
 * is mandatory: with no Whiteboard there is nothing to build a client with, and failing to
 * activate says so more clearly than a later {@code NullPointerException}.
 *
 * <h2>Why this class delegates instead of registering the inner client directly</h2>
 *
 * Being the service itself is what lets DS derive the service properties from the
 * configuration and publish both interfaces from one instance. The cost is a delegating
 * method per operation, which the compiler keeps honest — a new API method fails to compile
 * here until it is delegated.
 * <p>
 * It also lets {@link #close()} be a <b>no-op</b>, which matters: {@link DcatAtlasClient}
 * extends {@code AutoCloseable}, so a consumer using try-with-resources on an injected
 * service would otherwise close the client out from under every other consumer. The real
 * close happens in {@link #deactivate()}.
 */
@Component(name = DcatAtlasClientComponent.PID, //
		configurationPid = DcatAtlasClientComponent.PID, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		service = { DcatAtlasClient.class, AsyncDcatAtlasClient.class })
@Designate(ocd = DcatAtlasClientConfig.class, factory = true)
public class DcatAtlasClientComponent implements DcatAtlasClient, AsyncDcatAtlasClient {

	/** ConfigAdmin factory PID. */
	public static final String PID = "org.eclipse.fennec.dcat.atlas.client";

	private static final Logger LOGGER = Logger.getLogger(DcatAtlasClientComponent.class.getName());

	private final DcatAtlasClient delegate;
	private final ExecutorService executor;
	private final PromiseFactory promises;
	private final String portal;

	@Activate
	public DcatAtlasClientComponent(@Reference DcatAtlasClientFactory clientFactory,
			@Reference ClientBuilder clientBuilder, DcatAtlasClientConfig config) {
		this.portal = config.dcat_portal();
		this.delegate = clientFactory.builder() //
				.configuration(toConfiguration(config)) //
				.clientProvider(new WhiteboardJakartaRsClientProvider(clientBuilder)) //
				.build();
		// One thread: a publishing sequence is a handful of requests when something is
		// released, and serialising them keeps the order a caller wrote them in.
		this.executor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "dcat-atlas-client-" + portal);
			thread.setDaemon(true);
			return thread;
		});
		this.promises = new PromiseFactory(executor);
		checkReadiness(config);
	}

	/**
	 * Says once, at activation, whether the portal is fit to be written to.
	 * <p>
	 * A portal whose SHACL shapes are not mounted, or whose store is unreachable, reports
	 * CRITICAL and then refuses writes in ways that read like the caller's fault. One line
	 * here is worth more than the confusion later.
	 *
	 * @throws IllegalStateException when {@code require.ready} is set and it is not ready —
	 *                               the component then does not activate, which is what that
	 *                               setting is for
	 */
	private void checkReadiness(DcatAtlasClientConfig config) {
		if (!config.check_ready()) {
			return;
		}
		if (delegate.ready()) {
			LOGGER.log(Level.INFO, () -> "DCAT.Atlas portal '" + portal + "' at " + config.base_uri() + " is ready.");
			return;
		}
		if (config.require_ready()) {
			// Close what we built: DS will not call deactivate for an activation that threw.
			delegate.close();
			executor.shutdownNow();
			throw new IllegalStateException("DCAT.Atlas portal '" + portal + "' at " + config.base_uri()
					+ " is not ready, and require.ready is set. Not activating.");
		}
		LOGGER.log(Level.WARNING,
				() -> "DCAT.Atlas portal '" + portal + "' at " + config.base_uri() + " is not ready (or unreachable). "
						+ "Registrations will fail until it is; check its /health/ready.");
	}

	@Deactivate
	void deactivate() {
		delegate.close();
		executor.shutdown();
	}

	private static ClientConfiguration toConfiguration(DcatAtlasClientConfig config) {
		ClientConfiguration.Builder builder = ClientConfiguration.builder() //
				.baseUri(URI.create(config.base_uri())) //
				.connectTimeoutMs(config.connect_timeout_ms()) //
				.readTimeoutMs(config.read_timeout_ms()) //
				.authType(config.auth_type()) //
				.apiKeyHeader(config.auth_apikey_header()) //
				.keystoreType(config.auth_keystore_type()) //
				.truststoreType(config.auth_truststore_type());
		// Blank is how metatype spells "unset" for a String, and the configuration value type
		// wants null rather than "" so the provider can tell them apart.
		text(config.auth_token_env()).ifPresent(builder::authTokenEnv);
		text(config.auth_apikey_env()).ifPresent(builder::apiKeyEnv);
		text(config.auth_keystore_path()).ifPresent(builder::keystorePath);
		text(config.auth_keystore_password()).ifPresent(builder::keystorePassword);
		text(config.auth_truststore_path()).ifPresent(builder::truststorePath);
		text(config.auth_truststore_password()).ifPresent(builder::truststorePassword);
		return builder.build();
	}

	private static Optional<String> text(String value) {
		return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
	}

	// --- AsyncDcatAtlasClient ---------------------------------------------

	@Override
	public <T> Promise<T> submit(Function<DcatAtlasClient, T> work) {
		return promises.submit(() -> work.apply(delegate));
	}

	// --- DcatAtlasClient, delegated ---------------------------------------

	@Override
	public Registration<Catalog> registerCatalog(String id, Catalog catalog, String ifMatch) {
		return delegate.registerCatalog(id, catalog, ifMatch);
	}

	@Override
	public Registration<Dataset> registerDataset(String id, Dataset dataset, String ifMatch) {
		return delegate.registerDataset(id, dataset, ifMatch);
	}

	@Override
	public Registration<DatasetSeries> registerDatasetSeries(String id, DatasetSeries series, String ifMatch) {
		return delegate.registerDatasetSeries(id, series, ifMatch);
	}

	@Override
	public Registration<DataService> registerDataService(String id, DataService service, String ifMatch) {
		return delegate.registerDataService(id, service, ifMatch);
	}

	@Override
	public Registration<Distribution> registerDistribution(String datasetId, String id, Distribution distribution,
			String ifMatch) {
		return delegate.registerDistribution(datasetId, id, distribution, ifMatch);
	}

	@Override
	public boolean ready() {
		return delegate.ready();
	}

	@Override
	public void linkDatasetToCatalog(String catalogId, String datasetId) {
		delegate.linkDatasetToCatalog(catalogId, datasetId);
	}

	@Override
	public void linkDataServiceToCatalog(String catalogId, String serviceId) {
		delegate.linkDataServiceToCatalog(catalogId, serviceId);
	}

	@Override
	public void linkSubCatalog(String catalogId, String subCatalogId) {
		delegate.linkSubCatalog(catalogId, subCatalogId);
	}

	@Override
	public void linkDatasetToSeries(String seriesId, String datasetId) {
		delegate.linkDatasetToSeries(seriesId, datasetId);
	}

	@Override
	public void linkDatasetToDataService(String serviceId, String datasetId) {
		delegate.linkDatasetToDataService(serviceId, datasetId);
	}

	@Override
	public void linkAccessService(String datasetId, String distributionId, String serviceId) {
		delegate.linkAccessService(datasetId, distributionId, serviceId);
	}

	@Override
	public void unlinkDatasetFromCatalog(String catalogId, String datasetId) {
		delegate.unlinkDatasetFromCatalog(catalogId, datasetId);
	}

	@Override
	public void unlinkDataServiceFromCatalog(String catalogId, String serviceId) {
		delegate.unlinkDataServiceFromCatalog(catalogId, serviceId);
	}

	@Override
	public void unlinkSubCatalog(String catalogId, String subCatalogId) {
		delegate.unlinkSubCatalog(catalogId, subCatalogId);
	}

	@Override
	public void unlinkDatasetFromSeries(String seriesId, String datasetId) {
		delegate.unlinkDatasetFromSeries(seriesId, datasetId);
	}

	@Override
	public void unlinkDatasetFromDataService(String serviceId, String datasetId) {
		delegate.unlinkDatasetFromDataService(serviceId, datasetId);
	}

	@Override
	public void unlinkAccessService(String datasetId, String distributionId, String serviceId) {
		delegate.unlinkAccessService(datasetId, distributionId, serviceId);
	}

	@Override
	public Optional<Catalog> catalog(String id) {
		return delegate.catalog(id);
	}

	@Override
	public Optional<Dataset> dataset(String id) {
		return delegate.dataset(id);
	}

	@Override
	public Optional<DatasetSeries> datasetSeries(String id) {
		return delegate.datasetSeries(id);
	}

	@Override
	public Optional<DataService> dataService(String id) {
		return delegate.dataService(id);
	}

	@Override
	public Optional<Distribution> distribution(String datasetId, String id) {
		return delegate.distribution(datasetId, id);
	}

	@Override
	public Optional<String> etagOf(DcatCollection collection, String id) {
		return delegate.etagOf(collection, id);
	}

	@Override
	public Optional<String> etagOfDistribution(String datasetId, String id) {
		return delegate.etagOfDistribution(datasetId, id);
	}

	@Override
	public List<String> delete(DcatCollection collection, String id, DeleteMode mode) {
		return delegate.delete(collection, id, mode);
	}

	@Override
	public void deleteDistribution(String datasetId, String id) {
		delegate.deleteDistribution(datasetId, id);
	}

	@Override
	public URI aboutFor(DcatCollection collection, String id) {
		return delegate.aboutFor(collection, id);
	}

	/**
	 * Deliberately does nothing.
	 * <p>
	 * This is a shared service: a consumer that closed it — a plausible reflex, since
	 * {@link DcatAtlasClient} is {@link AutoCloseable} and try-with-resources is idiomatic —
	 * would take the portal away from every other consumer, with no error anywhere near the
	 * cause. The client is released in {@link #deactivate()}, when the configuration that
	 * owns it goes away.
	 */
	@Override
	public void close() {
		LOGGER.log(Level.FINE,
				() -> "close() on the shared client for portal '" + portal + "' ignored; it is owned by its "
						+ "configuration and released when that goes away.");
	}
}
