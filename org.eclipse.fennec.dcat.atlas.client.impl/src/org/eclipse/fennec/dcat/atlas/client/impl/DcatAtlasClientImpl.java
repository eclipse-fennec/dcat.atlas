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
package org.eclipse.fennec.dcat.atlas.client.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.client.api.ClientConfiguration;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;
import org.eclipse.fennec.dcat.atlas.client.api.DcatCollection;
import org.eclipse.fennec.dcat.atlas.client.api.DeleteMode;
import org.eclipse.fennec.dcat.atlas.client.api.NotFoundException;
import org.eclipse.fennec.dcat.atlas.client.api.TransportException;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.Distribution;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

/**
 * The plain-Java {@link DcatAtlasClient}.
 *
 * <h2>Everything is a PUT</h2>
 *
 * Registration, membership and unlinking are all {@code PUT}/{@code DELETE} against paths
 * that name both ends, so nothing here needs to track what it has already sent. See
 * {@link DcatAtlasClient} for why {@code POST} is deliberately not exposed.
 *
 * <h2>The write Accept is {@code application/xmi}, and that is not a free choice</h2>
 *
 * The admin endpoints {@code @Produces} XMI, JSON, XML and RDF/XML — <b>not</b> Turtle. A
 * write asking for {@code text/turtle} is answered {@code 406} before the resource is even
 * entered, which looks like a rejected registration and is not one. XMI is also the only
 * response this client can parse back into an {@code EObject}, so it is what every write
 * asks for.
 * <p>
 * That costs nothing on the validation path: when a SHACL report has to be produced, the
 * portal picks its syntax from the request's {@code Accept} and falls back to
 * <b>Turtle</b> when nothing there is an RDF type it can write. So a write asking for XMI
 * still gets a Turtle report — the client does not have to send a compound {@code Accept}
 * to get a readable one.
 */
final class DcatAtlasClientImpl implements DcatAtlasClient {

	private static final String ADMIN = "admin";
	private static final String DISTRIBUTIONS = "distributions";
	private static final String DATASETS = "datasets";

	private final ClientConfiguration configuration;
	private final Client httpClient;
	private final WebTarget root;

	DcatAtlasClientImpl(ClientConfiguration configuration, Client httpClient) {
		this.configuration = Objects.requireNonNull(configuration, "configuration");
		this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
		this.root = httpClient.target(normalise(configuration.getBaseUri()));
	}

	// --- registration -----------------------------------------------------

	@Override
	public Catalog registerCatalog(String id, Catalog catalog) {
		return put(admin(DcatCollection.CATALOGS).path(required(id, "id")), catalog, Catalog.class);
	}

	@Override
	public Dataset registerDataset(String id, Dataset dataset) {
		return put(admin(DcatCollection.DATASETS).path(required(id, "id")), dataset, Dataset.class);
	}

	@Override
	public DatasetSeries registerDatasetSeries(String id, DatasetSeries series) {
		return put(admin(DcatCollection.DATASET_SERIES).path(required(id, "id")), series, DatasetSeries.class);
	}

	@Override
	public DataService registerDataService(String id, DataService service) {
		return put(admin(DcatCollection.DATA_SERVICES).path(required(id, "id")), service, DataService.class);
	}

	@Override
	public Distribution registerDistribution(String datasetId, String id, Distribution distribution) {
		return put(distributions(datasetId).path(required(id, "id")), distribution, Distribution.class);
	}

	// --- membership -------------------------------------------------------

	@Override
	public void linkDatasetToCatalog(String catalogId, String datasetId) {
		link(admin(DcatCollection.CATALOGS).path(required(catalogId, "catalogId")).path(DATASETS)
				.path(required(datasetId, "datasetId")));
	}

	@Override
	public void linkDataServiceToCatalog(String catalogId, String serviceId) {
		link(admin(DcatCollection.CATALOGS).path(required(catalogId, "catalogId")).path("services")
				.path(required(serviceId, "serviceId")));
	}

	@Override
	public void linkSubCatalog(String catalogId, String subCatalogId) {
		link(admin(DcatCollection.CATALOGS).path(required(catalogId, "catalogId")).path("catalogs")
				.path(required(subCatalogId, "subCatalogId")));
	}

	@Override
	public void linkDatasetToSeries(String seriesId, String datasetId) {
		link(admin(DcatCollection.DATASET_SERIES).path(required(seriesId, "seriesId")).path(DATASETS)
				.path(required(datasetId, "datasetId")));
	}

	@Override
	public void linkDatasetToDataService(String serviceId, String datasetId) {
		link(admin(DcatCollection.DATA_SERVICES).path(required(serviceId, "serviceId")).path(DATASETS)
				.path(required(datasetId, "datasetId")));
	}

	@Override
	public void linkAccessService(String datasetId, String distributionId, String serviceId) {
		link(distributions(datasetId).path(required(distributionId, "distributionId")).path("access-service")
				.path(required(serviceId, "serviceId")));
	}

	@Override
	public void unlinkDatasetFromCatalog(String catalogId, String datasetId) {
		unlink(admin(DcatCollection.CATALOGS).path(required(catalogId, "catalogId")).path(DATASETS)
				.path(required(datasetId, "datasetId")));
	}

	@Override
	public void unlinkDataServiceFromCatalog(String catalogId, String serviceId) {
		unlink(admin(DcatCollection.CATALOGS).path(required(catalogId, "catalogId")).path("services")
				.path(required(serviceId, "serviceId")));
	}

	@Override
	public void unlinkSubCatalog(String catalogId, String subCatalogId) {
		unlink(admin(DcatCollection.CATALOGS).path(required(catalogId, "catalogId")).path("catalogs")
				.path(required(subCatalogId, "subCatalogId")));
	}

	@Override
	public void unlinkDatasetFromSeries(String seriesId, String datasetId) {
		unlink(admin(DcatCollection.DATASET_SERIES).path(required(seriesId, "seriesId")).path(DATASETS)
				.path(required(datasetId, "datasetId")));
	}

	@Override
	public void unlinkDatasetFromDataService(String serviceId, String datasetId) {
		unlink(admin(DcatCollection.DATA_SERVICES).path(required(serviceId, "serviceId")).path(DATASETS)
				.path(required(datasetId, "datasetId")));
	}

	@Override
	public void unlinkAccessService(String datasetId, String distributionId, String serviceId) {
		unlink(distributions(datasetId).path(required(distributionId, "distributionId")).path("access-service")
				.path(required(serviceId, "serviceId")));
	}

	// --- reads ------------------------------------------------------------

	@Override
	public Optional<Catalog> catalog(String id) {
		return read(publicPath(DcatCollection.CATALOGS).path(required(id, "id")), Catalog.class);
	}

	@Override
	public Optional<Dataset> dataset(String id) {
		return read(publicPath(DcatCollection.DATASETS).path(required(id, "id")), Dataset.class);
	}

	@Override
	public Optional<DatasetSeries> datasetSeries(String id) {
		return read(publicPath(DcatCollection.DATASET_SERIES).path(required(id, "id")), DatasetSeries.class);
	}

	@Override
	public Optional<DataService> dataService(String id) {
		return read(publicPath(DcatCollection.DATA_SERVICES).path(required(id, "id")), DataService.class);
	}

	@Override
	public Optional<Distribution> distribution(String datasetId, String id) {
		return read(publicPath(DcatCollection.DATASETS).path(required(datasetId, "datasetId")).path(DISTRIBUTIONS)
				.path(required(id, "id")), Distribution.class);
	}

	// --- deletion ---------------------------------------------------------

	@Override
	public List<String> delete(DcatCollection collection, String id, DeleteMode mode) {
		Objects.requireNonNull(collection, "collection");
		Objects.requireNonNull(mode, "mode");
		WebTarget target = admin(collection).path(required(id, "id")) //
				.queryParam("cascade", mode == DeleteMode.CASCADE);
		try (Response response = send(target.request(), Invocation.Builder::delete, "DELETE " + target.getUri())) {
			if (RestSupport.isNotFound(response)) {
				throw new NotFoundException("DELETE " + target.getUri() + " — no such resource");
			}
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, "DELETE " + target.getUri());
			}
			// 204 = nothing else changed. 200 = a cascade rewrote these, one public IRI
			// per line; every one of them has a new ETag.
			if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
				return List.of();
			}
			String body = RestSupport.safeBody(response);
			if (body.isEmpty()) {
				return List.of();
			}
			return Arrays.stream(body.split("\\R")).map(String::strip).filter(line -> !line.isEmpty()).toList();
		}
	}

	@Override
	public void deleteDistribution(String datasetId, String id) {
		WebTarget target = distributions(datasetId).path(required(id, "id"));
		try (Response response = send(target.request(), Invocation.Builder::delete, "DELETE " + target.getUri())) {
			if (RestSupport.isNotFound(response)) {
				throw new NotFoundException("DELETE " + target.getUri() + " — no such distribution");
			}
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, "DELETE " + target.getUri());
			}
		}
	}

	// --- portal state -----------------------------------------------------

	@Override
	public boolean ready() {
		// Health is a sibling of the REST application, not a child of it: with base
		// .../rest/ the checks are at .../health/ready. Resolving "../health/ready"
		// gets there for a base with or without a context path.
		WebTarget target = httpClient.target(normalise(configuration.getBaseUri()).resolve("../health/ready"));
		try (Response response = target.request().get()) {
			return RestSupport.isSuccess(response);
		} catch (ProcessingException e) {
			// A gate, not a diagnostic: unreachable is simply not ready.
			return false;
		}
	}

	@Override
	public URI aboutFor(DcatCollection collection, String id) {
		Objects.requireNonNull(collection, "collection");
		return normalise(configuration.getBaseUri()).resolve(collection.segment() + "/" + required(id, "id"));
	}

	@Override
	public void close() {
		httpClient.close();
	}

	// --- plumbing ---------------------------------------------------------

	private WebTarget admin(DcatCollection collection) {
		return root.path(ADMIN).path(collection.segment());
	}

	/** The public read path of a collection — no {@code /admin} segment. */
	private WebTarget publicPath(DcatCollection collection) {
		return root.path(collection.segment());
	}

	private WebTarget distributions(String datasetId) {
		return admin(DcatCollection.DATASETS).path(required(datasetId, "datasetId")).path(DISTRIBUTIONS);
	}

	private <T extends EObject> T put(WebTarget target, EObject entity, Class<T> type) {
		Objects.requireNonNull(entity, "entity");
		String what = "PUT " + target.getUri();
		try (Response response = invoke(target, null, null, entity)) {
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, what);
			}
			return XmiCodec.read(response.readEntity(byte[].class), type, what);
		}
	}

	/**
	 * A membership link. The path names both ends, so there is no body — the endpoint
	 * declares no {@code @Consumes} at all, and sending a body would be inventing one.
	 * {@code Accept} is XMI because that is what its {@code @Produces} offers.
	 */
	private void link(WebTarget target) {
		String what = "PUT " + target.getUri();
		try (Response response = send(target.request(ClientConfiguration.XMI),
				request -> request.put(Entity.entity(new byte[0], ClientConfiguration.XMI)), what)) {
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, what);
			}
		}
	}

	private void unlink(WebTarget target) {
		String what = "DELETE " + target.getUri();
		try (Response response = send(target.request(), Invocation.Builder::delete, what)) {
			// 204 for "it was not linked" as much as for "it is now unlinked": removing a
			// link that is not there is not a failure.
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, what);
			}
		}
	}

	private <T extends EObject> Optional<T> read(WebTarget target, Class<T> type) {
		String what = "GET " + target.getUri();
		try (Response response = get(target)) {
			if (RestSupport.isNotFound(response)) {
				return Optional.empty();
			}
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, what);
			}
			return Optional.of(XmiCodec.read(response.readEntity(byte[].class), type, what));
		}
	}

	private Response get(WebTarget target) {
		return send(target.request(configuration.getReadAcceptMediaType()), Invocation.Builder::get,
				"GET " + target.getUri());
	}

	private Response invoke(WebTarget target, String conditionalHeader, String validator, EObject entity) {
		Invocation.Builder request = target.request(ClientConfiguration.XMI);
		if (conditionalHeader != null && validator != null) {
			request = request.header(conditionalHeader, validator);
		}
		byte[] body = XmiCodec.write(entity);
		return send(request, builder -> builder.put(Entity.entity(body, ClientConfiguration.XMI)),
				"PUT " + target.getUri());
	}

	/**
	 * Runs a request, turning a transport fault into {@link TransportException}. A
	 * {@code ProcessingException} means no answer at all — refused connection, timeout —
	 * which is a different thing from any status the portal could return, and callers act
	 * on it differently.
	 */
	private Response send(Invocation.Builder request, Call call, String what) {
		try {
			return call.on(request);
		} catch (ProcessingException e) {
			throw new TransportException(what + " — the portal could not be reached", e);
		}
	}

	/** One HTTP method on a prepared request. */
	@FunctionalInterface
	private interface Call {
		Response on(Invocation.Builder request);
	}

	private static String required(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}

	/** A base URI that always ends in {@code /}, so {@code resolve} appends rather than replaces. */
	private static URI normalise(URI baseUri) {
		String text = baseUri.toString();
		return text.endsWith("/") ? baseUri : URI.create(text + "/");
	}
}
