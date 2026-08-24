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
package org.eclipse.fennec.dcat.atlas.client.osgi.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;
import org.eclipse.fennec.dcat.atlas.client.api.DcatCollection;
import org.eclipse.fennec.dcat.atlas.client.api.DcatModelConstraintException;
import org.eclipse.fennec.dcat.atlas.client.api.Registration;
import org.eclipse.fennec.dcat.atlas.client.osgi.AsyncDcatAtlasClient;
import org.eclipse.fennec.dcat.atlas.client.osgi.tests.helper.AwaitService;
import org.eclipse.fennec.dcat.atlas.client.osgi.tests.helper.PortalReady;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;

import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.DcatFactory;
import dcat.Distribution;
import foaf.Agent;
import foaf.FoafFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;
import terms.LicenseDocument;
import terms.TermsFactory;

/**
 * The OSGi front-end driven the way a consumer drives it: write a configuration, take the
 * service out of the registry, publish through it.
 *
 * <h2>What this suite is for</h2>
 *
 * The plain-Java suite already proves the transport and the error mapping against a stub
 * portal. What only a framework can prove is what P3 adds: that a ConfigAdmin factory
 * configuration becomes a working client, that it is <em>the Whiteboard's</em>
 * {@code ClientBuilder} doing the talking, that the two published faces are one client, and
 * that the readiness gate and the shared-service lifecycle behave. So the portal runs in
 * this same framework and the client reaches it over a real socket at
 * {@code http://localhost:8186/rest/}.
 *
 * <h2>Two things about this runtime worth knowing before reading the assertions</h2>
 *
 * <b>The model's own constraints are enforced, so the fixtures are not minimal.</b>
 * {@code StoreConfig.validateOnWrite()} defaults to {@code true} and nothing in
 * {@code configs/config.json} turns it off, so the OCL delegates are live: every dataset and
 * series here carries a description because {@code dcat:Dataset} requires one
 * ({@code HasDescription}), and a title-only entity is refused {@code 422} before it reaches
 * the store. SHACL enforcement, by contrast, is off — the DCAT-AP.de shapes are AGPL-3.0 and
 * deliberately not vendored (see {@code NOTICE.md}) — so conformance is not asserted here.
 * What a refusal looks like is the plain-Java suite's subject, not this one's.
 * <p>
 * <b>The hosted portal reports itself not ready, and that is the steady state.</b>
 * {@code /health/ready} aggregates a {@code shacl} check that is CRITICAL whenever shapes are
 * configured but none load — "validation would silently pass everything" — and since the
 * shapes cannot be vendored it is CRITICAL in every run, measured: 503 from the first probe
 * onwards, with every other check OK. That is what makes
 * {@link #requireReadyRefusesToActivateAgainstAPortalReportingNotReady()} a real test of the
 * gate rather than a rigged one, and it is why the ready-and-activating case is absent — it is
 * unreachable in this suite by design, not by omission.
 * {@code rest.tests}' {@code HealthEndpointIntegrationTest} pins the same behaviour.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class DcatAtlasClientComponentIntegrationTest {

	/** The component's ConfigAdmin factory PID. */
	private static final String PID = "org.eclipse.fennec.dcat.atlas.client";

	/** The portal hosted in this framework — see {@code configs/config.json}. */
	private static final String BASE_URI = "http://localhost:8186/rest/";

	/** A port nothing in this framework listens on, for the unreachable-portal case. */
	private static final String DEAD_URI = "http://localhost:8199/rest/";

	@InjectBundleContext
	BundleContext ctx;

	@InjectService
	ConfigurationAdmin configAdmin;

	private final List<Configuration> created = new ArrayList<>();

	@BeforeEach
	void portalIsServing() throws Exception {
		assertTrue(PortalReady.awaitStable(ctx, 30_000, 750),
				"the portal's REST whiteboard should be stable within 30s; registered: "
						+ PortalReady.registeredResources(ctx));
	}

	/**
	 * Every test owns its configurations and takes them away again, so the next one starts
	 * against an empty registry rather than inheriting a client from whoever ran first.
	 */
	@AfterEach
	void dropConfigurations() throws Exception {
		for (Configuration configuration : created) {
			try {
				configuration.delete();
			} catch (IllegalStateException alreadyGone) {
				// A test that deleted its own configuration to watch the service go away.
			}
		}
		created.clear();
		assertTrue(AwaitService.disappears(ctx, DcatAtlasClient.class, null),
				"the clients should have been retired with their configurations");
	}

	// --- the configuration is the client ----------------------------------

	/**
	 * One configuration publishes both faces, as one object, tagged with the portal's name —
	 * which is what makes {@code @Reference(target = "(dcat.portal=…)")} work at all.
	 */
	@Test
	void aFactoryConfigurationPublishesBothFacesTaggedWithItsPortalName() throws Exception {
		configure("alpha", Map.of());

		DcatAtlasClient sync = client("alpha");
		AsyncDcatAtlasClient async = async("alpha");

		assertSame(sync, async, "both faces should be the one component instance over one client");

		ServiceReference<DcatAtlasClient> reference = reference("alpha");
		assertEquals("alpha", reference.getProperty("dcat.portal"), "the name must be a service property");
		assertEquals(BASE_URI, reference.getProperty("base.uri"));
	}

	/** Two configurations are two independent clients, and a target filter picks exactly one. */
	@Test
	void twoConfigurationsAreTwoIndependentClients() throws Exception {
		configure("left", Map.of());
		configure("right", Map.of());

		DcatAtlasClient left = client("left");
		DcatAtlasClient right = client("right");

		assertNotSame(left, right, "one client per portal, not one shared between them");
		assertEquals(2, AwaitService.count(ctx, DcatAtlasClient.class, null));
		assertEquals(1, AwaitService.count(ctx, DcatAtlasClient.class, filter("left")),
				"a target filter should select one portal");
	}

	/** Deleting the configuration retires both faces: the client is owned by its configuration. */
	@Test
	void deletingTheConfigurationRetiresBothFaces() throws Exception {
		Configuration configuration = configure("ephemeral", Map.of());
		client("ephemeral");

		// Off the cleanup list *before* deleting: Felix's Configuration.equals() throws
		// IllegalStateException once the configuration is gone, so even a List.remove()
		// looking for it fails.
		created.remove(configuration);
		configuration.delete();

		assertTrue(AwaitService.disappears(ctx, DcatAtlasClient.class, filter("ephemeral")));
		assertTrue(AwaitService.disappears(ctx, AsyncDcatAtlasClient.class, filter("ephemeral")));
	}

	// --- it really talks to the portal ------------------------------------

	/**
	 * The point of the whole bundle: a registration issued through the injected service
	 * reaches the portal and comes back as an EObject. Nothing here builds a
	 * {@code ClientBuilder} — the one the Whiteboard registered did the work.
	 */
	@Test
	void aRegistrationGoesOutThroughTheWhiteboardClientAndComesBack() throws Exception {
		configure("beta", Map.of());
		DcatAtlasClient client = client("beta");

		Registration<Dataset> registration = register(client, "p3-roundtrip", dataset("Luftqualität 2026"));

		assertTrue(registration.applied());
		assertEquals("Luftqualität 2026", titleOf(registration.entity()));
		assertNotNull(registration.etag(), "the portal should hand back a validator");

		Optional<Dataset> readBack = client.dataset("p3-roundtrip");
		assertTrue(readBack.isPresent(), "the dataset should be readable straight after the write");
		assertEquals("Luftqualität 2026", titleOf(readBack.get()));
	}

	/**
	 * A conditional registration works across the service boundary, including the part that
	 * makes it usable after a restart: the validator can come from a {@code HEAD} rather than
	 * only from a previous registration in this process.
	 */
	@Test
	void aConditionalRegistrationGuardsWithTheValidatorAHeadReports() throws Exception {
		configure("etag", Map.of());
		DcatAtlasClient client = client("etag");

		Registration<Dataset> first = register(client, "p3-etag", dataset("v1"));
		Optional<String> fromHead = client.etagOf(DcatCollection.DATASETS, "p3-etag");

		assertEquals(Optional.of(first.etag()), fromHead, "a HEAD should report the validator the PUT returned");

		Registration<Dataset> stale = client.registerDataset("p3-etag", dataset("v2"), "\"not-the-current-one\"");
		assertFalse(stale.applied(), "a mismatch is reported as not applied, not thrown");
		assertEquals("v1", titleOf(client.dataset("p3-etag").orElseThrow()), "and nothing was written");

		Registration<Dataset> fresh = client.registerDataset("p3-etag", dataset("v2"), fromHead.orElseThrow());
		assertTrue(fresh.applied());
		assertEquals("v2", titleOf(client.dataset("p3-etag").orElseThrow()));
	}

	// --- the asynchronous face --------------------------------------------

	/**
	 * The promise resolves when the whole publishing sequence is done, not when the first of
	 * its requests returned — which is the reason {@code submit} takes the sequence rather
	 * than mirroring each operation.
	 */
	@Test
	void theWholePublishingSequenceComposesIntoOnePromise() throws Exception {
		configure("gamma", Map.of());
		DcatAtlasClient client = client("gamma");
		AsyncDcatAtlasClient async = async("gamma");

		Promise<String> published = async.submit(dcat -> {
			dcat.registerDatasetSeries("p3-series", series("Air quality, yearly"));
			dcat.registerDataset("p3-member", dataset("Air quality 2026"));
			dcat.registerDistribution("p3-member", "xmi", distribution());
			dcat.linkDatasetToSeries("p3-series", "p3-member");
			return "p3-member";
		});

		assertEquals("p3-member", published.timeout(60_000).getValue());

		// A PUT replaces, so a distribution and a series link only survive if the sequence
		// really ran in the order the caller wrote it — and all of it had finished before the
		// promise resolved.
		Dataset stored = client.dataset("p3-member").orElseThrow();
		assertEquals(1, stored.getDistribution().size(), "the distribution should be there");
		assertEquals(1, stored.getInSeries().size(), "the series link should be there");
	}

	/** Off the calling thread, on a thread named for its portal — so a stack trace says which. */
	@Test
	void submittedWorkRunsOffTheCallingThread() throws Exception {
		configure("delta", Map.of());
		AsyncDcatAtlasClient async = async("delta");

		String workerThread = async.submit(dcat -> Thread.currentThread().getName()).timeout(30_000).getValue();

		assertNotEquals(Thread.currentThread().getName(), workerThread);
		assertEquals("dcat-atlas-client-delta", workerThread);
	}

	// --- lifecycle --------------------------------------------------------

	/**
	 * A consumer that closes an injected service — a plausible reflex, since
	 * {@link DcatAtlasClient} is {@link AutoCloseable} — must not take the portal away from
	 * everybody else.
	 */
	@Test
	void closingTheSharedServiceIsANoOp() throws Exception {
		configure("epsilon", Map.of());
		DcatAtlasClient client = client("epsilon");

		client.close();

		Registration<Dataset> afterClose = register(client, "p3-after-close", dataset("Still working"));
		assertTrue(afterClose.applied(), "the shared client should still be usable after a consumer closed it");
	}

	// --- the readiness gate -----------------------------------------------

	/**
	 * With {@code require.ready} set, a portal that answers but reports itself not ready
	 * stops the component activating — no half-working client for anyone to find.
	 * <p>
	 * This is the realistic half of the gate: the portal is <em>reachable</em>, the readiness
	 * request succeeds as an HTTP round trip, and it is the 503 in the answer that refuses the
	 * activation. See the class comment for why the hosted portal is reliably 503.
	 */
	@Test
	void requireReadyRefusesToActivateAgainstAPortalReportingNotReady() throws Exception {
		configure("strict", Map.of("check.ready", Boolean.TRUE, "require.ready", Boolean.TRUE));

		assertTrue(AwaitService.staysUnusable(ctx, DcatAtlasClient.class, filter("strict"), 3_000),
				"require.ready should have failed the activation, leaving no usable client");
	}

	/**
	 * And an unreachable portal is refused the same way, which is a different code path:
	 * {@code ready()} maps a refused connection to {@code false} rather than reading a status.
	 */
	@Test
	void requireReadyRefusesToActivateAgainstAnUnreachablePortal() throws Exception {
		configure("unreachable",
				Map.of("base.uri", DEAD_URI, "check.ready", Boolean.TRUE, "require.ready", Boolean.TRUE));

		assertTrue(AwaitService.staysUnusable(ctx, DcatAtlasClient.class, filter("unreachable"), 3_000),
				"an unreachable portal should have failed the activation too");
	}

	/**
	 * Without it the check is advisory: even an unreachable portal only gets a warning, and
	 * the client is published so that a portal starting later can be written to.
	 */
	@Test
	void checkingReadinessAloneOnlyWarnsAndStillActivates() throws Exception {
		configure("lenient", Map.of("base.uri", DEAD_URI, "check.ready", Boolean.TRUE));

		DcatAtlasClient client = client("lenient");

		assertNotNull(client, "an unreachable portal must not stop activation when require.ready is off");
		assertFalse(client.ready(), "and it really is unreachable, so the gate was exercised");
	}

	// --- fixtures ---------------------------------------------------------

	/**
	 * Writes one factory configuration. {@code check.ready} is off unless a test says
	 * otherwise: the readiness probe is a real HTTP round trip and only two tests are about
	 * it.
	 */
	private Configuration configure(String portal, Map<String, Object> overrides) throws IOException {
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("dcat.portal", portal);
		properties.put("base.uri", BASE_URI);
		properties.put("check.ready", Boolean.FALSE);
		properties.putAll(overrides);

		Configuration configuration = configAdmin.getFactoryConfiguration(PID, portal, "?");
		configuration.update(properties);
		created.add(configuration);
		return configuration;
	}

	private DcatAtlasClient client(String portal) throws Exception {
		return AwaitService.appears(ctx, DcatAtlasClient.class, filter(portal))
				.orElseThrow(() -> new AssertionError("no DcatAtlasClient appeared for portal '" + portal + "'"));
	}

	private AsyncDcatAtlasClient async(String portal) throws Exception {
		return AwaitService.appears(ctx, AsyncDcatAtlasClient.class, filter(portal))
				.orElseThrow(() -> new AssertionError("no AsyncDcatAtlasClient appeared for portal '" + portal + "'"));
	}

	private ServiceReference<DcatAtlasClient> reference(String portal) throws Exception {
		Collection<ServiceReference<DcatAtlasClient>> references = ctx.getServiceReferences(DcatAtlasClient.class,
				filter(portal));
		assertEquals(1, references.size(), "expected exactly one client for portal '" + portal + "'");
		return references.iterator().next();
	}

	private static String filter(String portal) {
		return "(dcat.portal=" + portal + ")";
	}

	/**
	 * Registers a dataset, reporting the portal's own reasons if it refuses.
	 * <p>
	 * A {@code 422} otherwise arrives as "refused by a model constraint" with the violation
	 * lines tucked inside the exception, which is a slow way to discover that a fixture is
	 * missing a feature the model requires.
	 */
	private static Registration<Dataset> register(DcatAtlasClient client, String id, Dataset dataset) {
		try {
			return client.registerDataset(id, dataset);
		} catch (DcatModelConstraintException refused) {
			throw new AssertionError(refused.getMessage() + " " + refused.getViolations(), refused);
		}
	}

	private static String titleOf(dcat.DcatResource entity) {
		assertFalse(entity.getTitle().isEmpty(), "expected a title");
		return entity.getTitle().get(0).getValue();
	}

	/**
	 * A dataset the portal will actually accept.
	 *
	 * <h2>Why the fixtures are not minimal</h2>
	 *
	 * {@code StoreConfig.validateOnWrite()} defaults to {@code true}, so a write is checked
	 * against the model's declared multiplicities <em>and</em> its OCL invariants before it
	 * reaches the store. For a {@code dcat:Dataset} that means all three of these, and leaving
	 * any one out is a {@code 422}:
	 * <ul>
	 * <li>{@code title} — required by {@code DcatResource}</li>
	 * <li>{@code publisher} — also required, and a {@code foaf:Agent} whose {@code name} is
	 * required in turn, so an empty Agent does not satisfy it</li>
	 * <li>{@code description} — required by the OCL invariant {@code HasDescription}</li>
	 * </ul>
	 * None of this is the client's doing; a real caller has the same obligations.
	 */
	private static Dataset dataset(String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.getTitle().add(literal(title, "de"));
		dataset.getDescription().add(literal(title + " — published by the client integration suite", "de"));
		dataset.setPublisher(publisher());
		return dataset;
	}

	/** A series is a {@code Dataset}, so it carries exactly the same obligations. */
	private static DatasetSeries series(String title) {
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		series.getTitle().add(literal(title, "en"));
		series.getDescription().add(literal(title + " — published by the client integration suite", "en"));
		series.setPublisher(publisher());
		return series;
	}

	/**
	 * A distribution's required features are its own: {@code accessURL} (at least one) and a
	 * {@code license}. It is not a {@code DcatResource}, so it needs no publisher.
	 */
	private static Distribution distribution() {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setTitle(literal("XMI", "en"));
		distribution.setDescription(literal("The model as XMI", "en"));
		distribution.getAccessURL().add(BASE_URI + "datasets/p3-member");
		distribution.setLicense(license());
		return distribution;
	}

	/**
	 * A fresh Agent per entity: {@code publisher} is a <em>containment</em> reference, so one
	 * shared instance would be moved from entity to entity rather than copied into each.
	 */
	private static Agent publisher() {
		Agent publisher = FoafFactory.eINSTANCE.createAgent();
		publisher.getName().add(literal("DCAT.Atlas client integration suite", "en"));
		return publisher;
	}

	private static LicenseDocument license() {
		LicenseDocument license = TermsFactory.eINSTANCE.createLicenseDocument();
		// AboutIsIri: an identity is optional here, but if present it must be an IRI.
		license.setAbout("http://dcat-ap.de/def/licenses/dl-by-de/2.0");
		return license;
	}

	private static PlainLiteral literal(String value, String lang) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setValue(value);
		literal.setLang(lang);
		return literal;
	}
}
