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
package org.eclipse.fennec.dcat.atlas.client.api;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.osgi.annotation.versioning.ProviderType;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.Distribution;

/**
 * Registers DCAT elements with a DCAT.Atlas portal, holding EMF objects rather than
 * composing HTTP requests.
 * <p>
 * Usable as a plain library through {@link #builder()}, or as a DS service published by
 * the OSGi front-end. One client addresses one portal; publishing to two is an explicit
 * loop over two clients, because "it worked" has no single meaning when one portal
 * accepts and another refuses.
 *
 * <h2>Registration is idempotent</h2>
 *
 * Every {@code registerX} is a {@code PUT} to {@code /admin/{collection}/{id}}, where the
 * <em>path</em> decides identity. Consumers arrive with their own stable identifier — a
 * namespace URI, a scope name — so create-or-replace with no bookkeeping is exactly the
 * primitive they need, and re-running a registration loop is free. {@code POST} is the
 * wrong verb here and this interface does not expose it: a repeat {@code POST} carrying an
 * identity the portal already holds is a {@code 409} by design.
 *
 * <h2>{@code about} is normally left unset</h2>
 *
 * The path carries the id, so a body needs no {@code rdf:about}. The portal accepts one
 * that is absent or equal to the path's resource and refuses one that names something else
 * or belongs to somebody else. {@link #aboutFor(DcatCollection, String)} computes the
 * correct value for a caller that wants it set, so no consumer has to learn that rule the
 * hard way.
 *
 * <h2>Re-registration replaces, so the loop has three steps</h2>
 *
 * A {@code PUT} replaces the resource, which has two consequences a consumer has to plan
 * for — both measured against a running portal:
 * <ol>
 * <li>its <b>distributions go away</b>, because {@code dcat:distribution} is containment
 * and a body without them says it has none;</li>
 * <li>its <b>membership links go away</b> — {@code dcat:inSeries}, {@code dcat:dataset},
 * {@code dcat:servesDataset}.</li>
 * </ol>
 * So a registration loop is: register the entity, register its distributions, assert its
 * links. Every step is idempotent, so running the whole loop again is safe and is the
 * intended usage — this is not a workaround for anything.
 * <p>
 * <b>Read-modify-write does not work here</b>, which is why this interface offers no
 * helper for it. Reading a linked resource and {@code PUT}ting it back is refused by
 * SHACL: the body carries {@code dcat:inSeries <series>}, but nothing in that graph says
 * {@code <series>} is a {@code dcat:DatasetSeries}, so the profile's "MUSS auf eine Klasse
 * vom Typ … verweisen" rule fails. Build the entity from your own source of truth and
 * re-register it instead.
 *
 * <h2>What it throws</h2>
 *
 * Everything is a {@link DcatAtlasClientException}. Two of the subtypes are worth handling
 * separately by most callers: {@link DcatShaclException} (the metadata does not conform to
 * DCAT-AP.de) and {@link RetryableException} (the write is durable but the portal's mirror
 * push failed — <em>not</em> a lost registration).
 *
 * @see <a href="https://github.com/eclipse-fennec/dcat.atlas/issues/27">issue #27</a>
 */
@ProviderType
public interface DcatAtlasClient extends AutoCloseable {

	/**
	 * A builder for the plain-Java client.
	 *
	 * @return a new builder
	 * @throws IllegalStateException if no implementation is on the classpath
	 */
	static Builder builder() {
		return ServiceLoader.load(DcatAtlasClientFactory.class, DcatAtlasClient.class.getClassLoader()) //
				.findFirst() //
				.orElseThrow(() -> new IllegalStateException(
						"No DcatAtlasClient implementation found on the classpath (expected a ServiceLoader "
								+ "provider for " + DcatAtlasClientFactory.class.getName()
								+ ", e.g. the client.impl bundle)")) //
				.builder();
	}

	// --- registration -----------------------------------------------------

	/**
	 * Create or replace the catalog at {@code id}, unconditionally.
	 * <p>
	 * "Unconditionally" means last writer wins: whatever the portal holds is replaced. That
	 * is what a single publisher wants. Where something else may also write the resource,
	 * pass the validator from the previous registration to
	 * {@link #registerCatalog(String, Catalog, String)} instead.
	 *
	 * @param id      the caller's stable identifier; becomes the last path segment
	 * @param catalog the entity to store
	 * @return the stored entity and its validator; always {@link Registration#applied()}
	 */
	default Registration<Catalog> registerCatalog(String id, Catalog catalog) {
		return registerCatalog(id, catalog, null);
	}

	/**
	 * Create or replace the catalog at {@code id}, but only if the portal's copy is still
	 * the one {@code ifMatch} identifies.
	 *
	 * <h2>What this is for</h2>
	 *
	 * Without it a registration loop silently overwrites anything another writer did to
	 * the resource in the meantime — an edit made through the portal, or a second
	 * publisher. With it, such a write comes back
	 * {@link Registration#applied() not applied}, the client logs it, and the caller
	 * decides what that means: skip, or re-register unconditionally because it owns the
	 * truth.
	 * <p>
	 * The validator comes from the previous registration's {@link Registration#etag()}, so
	 * no read is involved. It is content-based, so a loop that re-registers identical
	 * content gets the same one back and never invalidates its own token.
	 *
	 * @param id      the caller's stable identifier
	 * @param catalog the entity to store
	 * @param ifMatch the expected validator; {@code null} makes the write unconditional
	 * @return the outcome — {@link Registration#applied()} is {@code false} when the
	 *         resource had moved on and nothing was written
	 */
	Registration<Catalog> registerCatalog(String id, Catalog catalog, String ifMatch);

	/** Create or replace the dataset at {@code id}. @see #registerCatalog(String, Catalog) */
	default Registration<Dataset> registerDataset(String id, Dataset dataset) {
		return registerDataset(id, dataset, null);
	}

	/** Conditionally create or replace the dataset at {@code id}. @see #registerCatalog(String, Catalog, String) */
	Registration<Dataset> registerDataset(String id, Dataset dataset, String ifMatch);

	/** Create or replace the dataset series at {@code id}. @see #registerCatalog(String, Catalog) */
	default Registration<DatasetSeries> registerDatasetSeries(String id, DatasetSeries series) {
		return registerDatasetSeries(id, series, null);
	}

	/** Conditionally create or replace the dataset series. @see #registerCatalog(String, Catalog, String) */
	Registration<DatasetSeries> registerDatasetSeries(String id, DatasetSeries series, String ifMatch);

	/** Create or replace the data service at {@code id}. @see #registerCatalog(String, Catalog) */
	default Registration<DataService> registerDataService(String id, DataService service) {
		return registerDataService(id, service, null);
	}

	/** Conditionally create or replace the data service. @see #registerCatalog(String, Catalog, String) */
	Registration<DataService> registerDataService(String id, DataService service, String ifMatch);

	/**
	 * Create or replace a distribution of {@code datasetId}.
	 * <p>
	 * A distribution has no collection of its own: {@code dcat:distribution} is a
	 * containment reference, so a distribution lives in its dataset and is addressed
	 * through it. That is why this method takes two ids and {@link DcatCollection} has no
	 * constant for it.
	 *
	 * @param datasetId    the owning dataset
	 * @param id           the distribution's own identifier within that dataset
	 * @param distribution the entity to store
	 * @return the stored entity and its validator
	 */
	default Registration<Distribution> registerDistribution(String datasetId, String id,
			Distribution distribution) {
		return registerDistribution(datasetId, id, distribution, null);
	}

	/**
	 * Conditionally create or replace a distribution.
	 * <p>
	 * The validator is the <em>distribution's</em> own, not its dataset's.
	 *
	 * @see #registerCatalog(String, Catalog, String)
	 */
	Registration<Distribution> registerDistribution(String datasetId, String id, Distribution distribution,
			String ifMatch);

	// --- membership -------------------------------------------------------

	/**
	 * Link an existing dataset into an existing catalog ({@code dcat:dataset}).
	 * <p>
	 * These endpoints exist so a member can be attached without re-sending the container,
	 * which matters when the container is large or when two consumers write to it.
	 *
	 * @throws NotFoundException if either the catalog or the dataset does not exist
	 */
	void linkDatasetToCatalog(String catalogId, String datasetId);

	/** Link an existing data service into a catalog ({@code dcat:service}). */
	void linkDataServiceToCatalog(String catalogId, String serviceId);

	/** Link an existing catalog into another as a sub-catalogue ({@code dcat:catalog}). */
	void linkSubCatalog(String catalogId, String subCatalogId);

	/**
	 * Join a dataset to a series ({@code dcat:inSeries}).
	 *
	 * <h2>The asymmetry this hides</h2>
	 *
	 * {@code inSeries} lives on the <em>dataset</em>, not on the series, so this call
	 * edits the dataset — and it is the dataset's ETag that moves, not the series'. The
	 * endpoint is nevertheless under the series, because that is how a caller thinks about
	 * it. A consumer should not have to know either half.
	 */
	void linkDatasetToSeries(String seriesId, String datasetId);

	/** Point a data service at a dataset it serves ({@code dcat:servesDataset}). */
	void linkDatasetToDataService(String serviceId, String datasetId);

	/**
	 * Point a distribution at the service that serves it ({@code dcat:accessService}).
	 *
	 * @param datasetId      the dataset owning the distribution
	 * @param distributionId the distribution
	 * @param serviceId      the data service
	 */
	void linkAccessService(String datasetId, String distributionId, String serviceId);

	/** Remove a {@code dcat:dataset} link; a link that was not there is not an error. */
	void unlinkDatasetFromCatalog(String catalogId, String datasetId);

	/** Remove a {@code dcat:service} link. @see #unlinkDatasetFromCatalog */
	void unlinkDataServiceFromCatalog(String catalogId, String serviceId);

	/** Remove a {@code dcat:catalog} link. @see #unlinkDatasetFromCatalog */
	void unlinkSubCatalog(String catalogId, String subCatalogId);

	/** Remove a {@code dcat:inSeries} link. @see #unlinkDatasetFromCatalog */
	void unlinkDatasetFromSeries(String seriesId, String datasetId);

	/** Remove a {@code dcat:servesDataset} link. @see #unlinkDatasetFromCatalog */
	void unlinkDatasetFromDataService(String serviceId, String datasetId);

	/** Remove a {@code dcat:accessService} link. @see #unlinkDatasetFromCatalog */
	void unlinkAccessService(String datasetId, String distributionId, String serviceId);

	// --- reads ------------------------------------------------------------

	/** @return the catalog, or empty when the portal answers {@code 404} */
	Optional<Catalog> catalog(String id);

	/** @return the dataset, or empty when the portal answers {@code 404} */
	Optional<Dataset> dataset(String id);

	/** @return the dataset series, or empty when the portal answers {@code 404} */
	Optional<DatasetSeries> datasetSeries(String id);

	/** @return the data service, or empty when the portal answers {@code 404} */
	Optional<DataService> dataService(String id);

	/** @return the distribution, or empty when the portal answers {@code 404} */
	Optional<Distribution> distribution(String datasetId, String id);

	// --- validators -------------------------------------------------------

	/**
	 * The current validator of a resource, without fetching the resource.
	 *
	 * <h2>What this is for</h2>
	 *
	 * A conditional registration needs a validator, and the only other source of one is a
	 * previous {@link Registration} — which lives in the caller's memory and does not
	 * survive a restart. Without this method a publisher coming back up could not register
	 * conditionally at all: it would have to write unconditionally first, which is exactly
	 * the overwrite the precondition exists to prevent.
	 * <p>
	 * So the startup pattern is one call of this per resource the publisher manages, and
	 * conditional registrations from there:
	 *
	 * <pre>
	 * String validator = client.etagOf(DcatCollection.DATASETS, id).orElse(null);
	 * Registration&lt;Dataset&gt; result = client.registerDataset(id, dataset, validator);
	 * </pre>
	 *
	 * A {@code null} validator makes that write unconditional, which is the right thing in
	 * both empty cases below — so the pattern above needs no branch.
	 *
	 * <h2>Why it is a HEAD and not a read</h2>
	 *
	 * It takes only the header, never the entity. That keeps it cheap, and it keeps it
	 * clear of the trap that killed read-modify-write: nothing is parsed, so there is no
	 * entity to send back and nothing for SHACL to reject.
	 *
	 * @param collection the collection the resource lives in
	 * @param id         the resource
	 * @return the validator, or empty when the resource does not exist <em>or</em> carries
	 *         no validator — in both cases there is nothing to guard a write with, and the
	 *         caller's next move is the same
	 */
	Optional<String> etagOf(DcatCollection collection, String id);

	/**
	 * The current validator of a distribution — its own, not its dataset's.
	 *
	 * @see #etagOf(DcatCollection, String)
	 */
	Optional<String> etagOfDistribution(String datasetId, String id);

	// --- deletion ---------------------------------------------------------

	/**
	 * Delete a resource.
	 *
	 * @param collection which collection it lives in
	 * @param id         the resource
	 * @param mode       {@link DeleteMode#SINGLE} refuses with {@link ConflictException}
	 *                   while anything still references it;
	 *                   {@link DeleteMode#CASCADE} unlinks the referrers first
	 * @return the public IRIs of the resources a cascade rewrote — empty for a plain
	 *         delete, and empty for a cascade that had nothing to unlink. Worth acting
	 *         on: every one of those resources has a new ETag, so a caller holding any of
	 *         them should invalidate it.
	 * @throws NotFoundException if the resource does not exist
	 */
	List<String> delete(DcatCollection collection, String id, DeleteMode mode);

	/**
	 * Delete a distribution of {@code datasetId}. A distribution is contained in its
	 * dataset, so there is nothing to cascade.
	 *
	 * @throws NotFoundException if the dataset or the distribution does not exist
	 */
	void deleteDistribution(String datasetId, String id);

	// --- portal state -----------------------------------------------------

	/**
	 * Whether the portal is ready to accept registrations
	 * ({@code GET /health/ready}).
	 *
	 * <h2>Call this once before the first registration</h2>
	 *
	 * A portal whose SHACL shapes are not mounted, or whose store is unreachable, reports
	 * CRITICAL and refuses writes in ways that look like the caller's fault. One readiness
	 * check at activation turns a confusing cascade of failures into one clear log line.
	 *
	 * @return {@code true} on {@code 200}; {@code false} on {@code 503} <em>or</em> if the
	 *         portal cannot be reached at all — this is a gate, not a diagnostic, so it
	 *         does not throw
	 */
	boolean ready();

	/**
	 * The {@code rdf:about} this portal would give a resource, for a caller that wants to
	 * set it explicitly or to record what it published.
	 *
	 * @param collection the collection
	 * @param id         the resource's identifier
	 * @return the public IRI, computed from the configured base
	 */
	URI aboutFor(DcatCollection collection, String id);

	/** Releases the underlying HTTP client. Idempotent. */
	@Override
	void close();

	/**
	 * Builds a plain-Java client. Obtain one from {@link DcatAtlasClient#builder()}.
	 * <p>
	 * In OSGi the client comes from a ConfigAdmin factory component instead, so this
	 * builder is not the OSGi entry point.
	 */
	interface Builder {

		/**
		 * Use this complete configuration, replacing anything set so far. Later
		 * convenience setters refine it.
		 */
		Builder configuration(ClientConfiguration configuration);

		/** Required — the portal's REST base ({@code base.uri}). */
		Builder baseUri(URI baseUri);

		/** {@code connect.timeout.ms}. */
		Builder connectTimeoutMs(int connectTimeoutMs);

		/** {@code read.timeout.ms}. */
		Builder readTimeoutMs(int readTimeoutMs);

		/**
		 * Override the Jakarta RS seam. Defaults to the implementation's plain-Java
		 * provider; the OSGi front-end supplies a Whiteboard-backed one here.
		 */
		Builder clientProvider(JakartaRsClientProvider clientProvider);

		/**
		 * @return the client; the caller owns it and closes it
		 * @throws IllegalStateException if {@code base.uri} was never set
		 */
		DcatAtlasClient build();
	}
}
