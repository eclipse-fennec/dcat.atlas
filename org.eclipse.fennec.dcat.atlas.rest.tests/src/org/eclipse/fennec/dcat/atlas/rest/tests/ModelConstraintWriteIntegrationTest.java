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
package org.eclipse.fennec.dcat.atlas.rest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.IntPredicate;

import org.eclipse.fennec.dcat.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.dcat.atlas.rest.tests.helper.RestReady;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * On-write enforcement of the model's own constraints, end to end over HTTP.
 *
 * <h2>What this proves that the unit tests cannot</h2>
 *
 * {@code ModelConstraintValidationTest} installs the OCL delegates by hand, because a plain
 * JUnit test has no whiteboard. This one installs nothing: it proves the delegate really
 * does reach EMF's global registry from the {@code m2x} engine's OSGi service, that
 * {@code @RequireOCL} pulled the engine into the runtime, and that a violation surfaces as
 * {@code 422} rather than a {@code 500}.
 * <p>
 * <h2>Enforcement is already on — this class pins it, it does not switch it on</h2>
 *
 * {@code StoreConfig.validateOnWrite()} defaults to <b>{@code true}</b> and
 * {@code configs/config.json} does not override it, so OCL and the declared multiplicities
 * are enforced throughout this runtime. That is why {@link RestEntities} gives every dataset
 * a title, a publisher and a description: the whole suite writes entities that clear the
 * model's floor because it has to.
 * <p>
 * The {@code validateOnWrite=true} written through {@link ConfigurationAdmin} in
 * {@link #enableEnforcement(BundleContext)} therefore states a precondition rather than
 * changing behaviour — it keeps this class honest if the default ever moves, at the cost of
 * one reconfiguration and the poll that waits for it. {@code @AfterEach} writes the same
 * value back, so the runtime is left exactly as it was found.
 * <p>
 * <b>Do not read this as the arrangement {@link WriteValidationIntegrationTest} uses.</b>
 * That one is about SHACL, which genuinely <em>is</em> off by default
 * ({@code enforceOnWrite} defaults to false) and which it must turn on along with shapes of
 * its own. The two switches are independent, and only one of them starts off.
 * <p>
 * The store is shared with the live runtime, so everything written here is cleaned up.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ModelConstraintWriteIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String ADMIN_DATASETS = BASE + "/admin/datasets";
	private static final String XMI = "application/xmi";
	private static final String PID = "DatasetAdminService";
	/**
	 * {@code validateOnWrite} is {@link org.eclipse.fennec.dcat.atlas.impl.store.StoreConfig}
	 * on <em>each</em> admin service, so switching it for datasets says nothing about
	 * catalogs or distributions. #34's tests seed through those two, and had to learn this
	 * the hard way: turning it off for {@code DatasetAdminService} left the catalog write
	 * still enforcing.
	 */
	private static final String CATALOG_PID = "CatalogAdminService";
	private static final String DISTRIBUTION_PID = "DistributionAdminService";
	/** Fixed id for the idempotent readiness probe so it never mints throw-away resources. */
	private static final String PROBE_ID = "model-constraint-readiness-probe";
	private static final String CONFORMANT_ID = "model-constraint-conformant";
	private static final String ADMIN_CATALOGS = BASE + "/admin/catalogs";
	/** #34: the dataset a distribution write attaches to. */
	private static final String OWNER_ID = "model-constraint-distribution-owner";
	/** #34: a catalog seeded invalid, to prove a membership write validates its container. */
	private static final String INVALID_CATALOG_ID = "model-constraint-invalid-catalog";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@InjectService
	ConfigurationAdmin configAdmin;

	@BeforeEach
	void enableEnforcement(@InjectBundleContext BundleContext context) throws Exception {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
		assertTrue(ResourceAware.create(context, "DatasetAdminResource").waitForResource(15, TimeUnit.SECONDS),
				"DatasetAdminResource should be registered within 15 seconds.");

		setValidateOnWrite(PID, true);

		// The reconfigured service rebinds asynchronously, so poll the write path itself
		// rather than trusting the update to have landed: an idempotent PUT of a title-only
		// dataset must answer 422. It normally does so on the first probe, enforcement being
		// on by default — the loop covers the window in which the rebind is still in flight.
		// A fixed id keeps the probe from minting a resource per attempt.
		HttpResponse<String> ready = awaitEnforcementActive(30_000);
		assertEquals(422, ready.statusCode(), "enforcement did not become active; last probe: " + ready.statusCode()
				+ " " + ready.body());
	}

	@AfterEach
	void restoreEnforcement() throws Exception {
		// Back to the shipped default, which is on: leaving it off would silently disarm
		// every test class that runs after this one.
		setValidateOnWrite(PID, true);
		setValidateOnWrite(CATALOG_PID, true);
		setValidateOnWrite(DISTRIBUTION_PID, true);
		delete(ADMIN_DATASETS + "/" + PROBE_ID);
		delete(ADMIN_DATASETS + "/" + CONFORMANT_ID);
		// #34: cascade, because a linked dataset cannot be deleted while its catalog holds it.
		delete(ADMIN_CATALOGS + "/" + INVALID_CATALOG_ID + "?cascade=true");
		delete(ADMIN_DATASETS + "/" + OWNER_ID + "?cascade=true");
	}

	@Test
	void aDatasetMissingAMandatoryFeatureIsRejected() throws Exception {
		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + PROBE_ID, titleOnly(PROBE_ID));

		assertEquals(422, response.statusCode(), "a title-only Dataset violates publisher [1] and HasDescription");
		assertTrue(response.body().contains("publisher"),
				"the body should name the feature to fix, but was: " + response.body());
	}

	@Test
	void theRejectionNamesTheViolatedOclInvariant() throws Exception {
		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + PROBE_ID, titleOnly(PROBE_ID));

		assertEquals(422, response.statusCode());
		assertTrue(response.body().contains("HasDescription"),
				"the OCL invariant should be named, not just the multiplicities: " + response.body());
	}

	@Test
	void aConformantDatasetIsAccepted() throws Exception {
		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + CONFORMANT_ID, conformant(CONFORMANT_ID));

		assertTrue(response.statusCode() / 100 == 2,
				"a Dataset carrying title, description and publisher should be stored, but was: "
						+ response.statusCode() + " " + response.body());
	}

	@Test
	void anAnyUriThatIsNotAnIriIsRejected() throws Exception {
		String body = conformant(CONFORMANT_ID).replace("<title lang=\"en\"",
				"<theme>not-an-iri</theme>\n  <title lang=\"en\"");

		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + CONFORMANT_ID, body);

		assertEquals(422, response.statusCode(),
				"a bare token in an AnyURI attribute must not reach the store: " + response.body());
		assertTrue(response.body().contains("ThemeIsIri"), "expected ThemeIsIri, but was: " + response.body());
	}

	// --- #34: the same enforcement on distributions and memberships ---------

	/**
	 * A Distribution is written through its Dataset, and that write has to be checked like
	 * any other: {@code accessURL [1..*]} and {@code license [1]} are the model's floor for
	 * one, and a title-only Distribution is under it.
	 * <p>
	 * The {@code 200} this used to answer was the bug in #34 — the same entity was then
	 * reported non-conformant by {@code POST /admin/validate/distributions}, which is how it
	 * was noticed.
	 */
	@Test
	void aDistributionMissingAMandatoryFeatureIsRejectedOnCreate() throws Exception {
		seedOwner();

		HttpResponse<String> response = post(adminDistributions(OWNER_ID), titleOnlyDistribution(OWNER_ID, "csv"));

		assertEquals(422, response.statusCode(),
				"a title-only Distribution violates accessURL [1..*] and license [1], but was: "
						+ response.statusCode() + " " + response.body());
		assertTrue(response.body().contains("accessURL") || response.body().contains("license"),
				"the body should name the feature to fix, but was: " + response.body());
		assertEquals(0, storedDistributions(OWNER_ID), "the refused create must not have stored anything");
	}

	/** The replacing half of the same hole. */
	@Test
	void aDistributionMissingAMandatoryFeatureIsRejectedOnReplace() throws Exception {
		seedOwner();

		HttpResponse<String> response = put(adminDistributions(OWNER_ID) + "/csv",
				titleOnlyDistribution(OWNER_ID, "csv"));

		assertEquals(422, response.statusCode(), "a title-only Distribution must not replace anything, but was: "
				+ response.statusCode() + " " + response.body());
		assertEquals(0, storedDistributions(OWNER_ID), "the refused replace must not have stored anything");
	}

	/** The guard against over-tightening: a complete Distribution still goes in. */
	@Test
	void aConformantDistributionIsAccepted() throws Exception {
		seedOwner();

		HttpResponse<String> response = post(adminDistributions(OWNER_ID), conformantDistribution(OWNER_ID, "csv"));

		assertTrue(response.statusCode() / 100 == 2,
				"a Distribution carrying accessURL and license should be stored, but was: " + response.statusCode()
						+ " " + response.body());
		assertEquals(1, storedDistributions(OWNER_ID));
	}

	/**
	 * A membership write re-persists its container, so it has to clear the floor too.
	 * <p>
	 * The catalog is seeded invalid with enforcement off — which is the only way invalid data
	 * gets in once #34 is fixed — and the link is then refused because it would write that
	 * catalog back. The SHACL half of the membership rules (DCAT-AP.de's "MUSS auf eine
	 * Klasse vom Typ … verweisen") cannot be asserted in this runtime: the shapes are
	 * AGPL-3.0 and deliberately not vendored, so {@code enforceOnWrite} is off here.
	 */
	@Test
	void linkingAMemberIsRejectedWhenItWouldRewriteAnInvalidContainer() throws Exception {
		seedOwner();
		seedInvalidCatalog();

		HttpResponse<String> response = put(ADMIN_CATALOGS + "/" + INVALID_CATALOG_ID + "/datasets/" + OWNER_ID, "");

		assertEquals(422, response.statusCode(),
				"linking must not silently re-persist a catalog that is under the model's floor, but was: "
						+ response.statusCode() + " " + response.body());
	}

	/**
	 * And the other direction, which is the trap a blanket check would set: removal must stay
	 * possible. An operator who has invalid data in the store has to be able to delete their
	 * way out of it, so a write that only takes content away is not validated.
	 */
	@Test
	void removingAnInvalidDistributionIsNotBlocked() throws Exception {
		seedOwner();
		seedInvalidDistribution();

		HttpResponse<String> response = delete(adminDistributions(OWNER_ID) + "/csv");

		assertTrue(response.statusCode() / 100 == 2,
				"a delete must not be refused because what it is deleting is invalid, but was: "
						+ response.statusCode() + " " + response.body());
		assertEquals(0, storedDistributions(OWNER_ID));
	}

	// --- distinct Distribution identities -----------------------------------

	/**
	 * Two Distributions of one Dataset may not claim the same identity.
	 * <p>
	 * Measured before this was enforced: the {@code PUT} was answered {@code 201} with both
	 * stored, {@code GET …/distributions/csv} returned the first, {@code DELETE} of it
	 * answered {@code 204} with one still stored — and the same {@code GET} then answered
	 * {@code 200} again. A client told the resource was gone could fetch it straight back,
	 * because every id-based operation resolves the first match and there were two.
	 */
	@Test
	void twoDistributionsClaimingOneIdentityAreRefused() throws Exception {
		seedOwner();

		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + OWNER_ID, datasetWithDistributions(OWNER_ID,
				distributionElement(OWNER_ID, "csv", "One"), distributionElement(OWNER_ID, "csv", "Two")));

		assertEquals(422, response.statusCode(),
				"two Distributions with one about must not be stored, but was: " + response.statusCode() + " "
						+ response.body());
		// The id, not the whole IRI: a request body's identities are folded to the stored
		// logical form (http://dcat.atlas/…) on the way in, and every model-constraint message
		// quotes that rather than the public base. Pinning the base here would pin that
		// separate quirk instead of what this test is about.
		assertTrue(response.body().contains("distributions/csv"),
				"the repeated identity should be named, but was: " + response.body());
		assertEquals(0, storedDistributions(OWNER_ID), "the refused write must not have stored either of them");
	}

	/**
	 * And the case that must stay possible, which is why EMF's {@code validate_UniqueID} could
	 * not simply be kept: two Distributions <em>sharing a licence</em>.
	 * <p>
	 * Their identities differ; what repeats is a contained {@code LicenseDocument} with one
	 * IRI, which in RDF is the same resource mentioned twice and is the commonest shape in a
	 * catalogue. A fix that refuses this is worse than the gap it closes, so this is pinned
	 * rather than left to be noticed.
	 */
	@Test
	void twoDistributionsSharingALicenceAreAccepted() throws Exception {
		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + OWNER_ID, datasetWithDistributions(OWNER_ID,
				distributionElement(OWNER_ID, "csv", "CSV"), distributionElement(OWNER_ID, "json", "JSON")));

		assertTrue(response.statusCode() / 100 == 2,
				"two Distributions under one licence are ordinary data, but was: " + response.statusCode() + " "
						+ response.body());
		assertEquals(2, storedDistributions(OWNER_ID), "both should be stored");
	}

	/**
	 * #46: a refusal names the resource by the identity the client can dereference.
	 * <p>
	 * Identities are folded to the stored logical form on the way in and validation runs on
	 * that, which is right — but the client never saw {@code http://dcat.atlas/…}, cannot
	 * fetch it, and cannot match it against what it sent. {@code CascadeReport} already
	 * renders its own text body public for exactly this reason.
	 */
	@Test
	void aRefusalNamesTheResourceByItsPublicIdentity() throws Exception {
		seedOwner();

		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + OWNER_ID, datasetWithDistributions(OWNER_ID,
				distributionElement(OWNER_ID, "csv", "One"), distributionElement(OWNER_ID, "csv", "Two")));

		assertEquals(422, response.statusCode(), response.body());
		assertFalse(response.body().contains("http://dcat.atlas/"),
				"a 422 must not hand back the store's internal base: " + response.body());
		assertTrue(response.body().contains(distributionIri(OWNER_ID, "csv")),
				"it should name the IRI the client can dereference: " + response.body());
	}

	// --- payloads -----------------------------------------------------------

	/** Everything DCAT-AP.de makes Pflicht for a Dataset: title, description, publisher. */
	private static String conformant(String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmi:version="2.0" about="%s">
				  <title lang="en" value="Air quality"/>
				  <description lang="en" value="Hourly air quality measurements"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				</dcat:Dataset>""".formatted(logicalIri(id));
	}

	/**
	 * Title only — deliberately under the model's floor. A {@code dcat:Dataset} needs a
	 * {@code publisher} by declared multiplicity and a {@code description} by the OCL
	 * invariant {@code HasDescription}, so this is refused twice over. Everything else in the
	 * suite builds complete entities through {@link RestEntities}.
	 */
	private static String titleOnly(String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmi:version="2.0" about="%s">
				  <title lang="en" value="Probe"/>
				</dcat:Dataset>""".formatted(logicalIri(id));
	}

	private static String logicalIri(String id) {
		return BASE + "/datasets/" + id;
	}

	/** The licence both Distributions carry — one IRI, two mentions, which RDF allows. */
	private static final String LICENCE = "http://dcat-ap.de/def/licenses/dl-by-de/2.0";

	/** A conformant Dataset carrying the given {@code <distribution>} elements verbatim. */
	private static String datasetWithDistributions(String id, String... distributions) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmi:version="2.0" about="%s">
				  <title lang="en" value="Air quality"/>
				  <description lang="en" value="Hourly air quality measurements"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				%s
				</dcat:Dataset>""".formatted(logicalIri(id), String.join("\n", distributions));
	}

	private static String distributionElement(String datasetId, String id, String title) {
		return """
				  <distribution about="%s">
				    <title lang="en" value="%s"/>
				    <accessURL>https://example.de/%s.csv</accessURL>
				    <license about="%s"/>
				  </distribution>""".formatted(distributionIri(datasetId, id), title, id, LICENCE);
	}

	private static String adminDistributions(String datasetId) {
		return ADMIN_DATASETS + "/" + datasetId + "/distributions";
	}

	private static String distributionIri(String datasetId, String id) {
		return BASE + "/datasets/" + datasetId + "/distributions/" + id;
	}

	/** Under the model's floor: a Distribution needs {@code accessURL} and a {@code license}. */
	private static String titleOnlyDistribution(String datasetId, String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Distribution xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="%s">
				  <title lang="en" value="Probe"/>
				</dcat:Distribution>""".formatted(distributionIri(datasetId, id));
	}

	private static String conformantDistribution(String datasetId, String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Distribution xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="%s">
				  <title lang="en" value="CSV"/>
				  <accessURL>https://example.de/data.csv</accessURL>
				  <license about="http://dcat-ap.de/def/licenses/dl-by-de/2.0"/>
				</dcat:Distribution>""".formatted(distributionIri(datasetId, id));
	}

	private static String titleOnlyCatalog(String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Catalog xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmi:version="2.0" about="%s">
				  <title lang="en" value="Probe"/>
				</dcat:Catalog>""".formatted(BASE + "/catalogs/" + id);
	}

	/** A conformant Dataset for distributions to hang off. */
	private void seedOwner() throws Exception {
		HttpResponse<String> seeded = put(ADMIN_DATASETS + "/" + OWNER_ID, conformant(OWNER_ID));
		assertTrue(seeded.statusCode() / 100 == 2, "seeding the owning dataset failed: " + seeded.body());
	}

	/** A Catalog that is under the model's floor, written while its own service is not enforcing. */
	private void seedInvalidCatalog() throws Exception {
		String url = ADMIN_CATALOGS + "/" + INVALID_CATALOG_ID;
		seedUnderTheFloor(CATALOG_PID, url, titleOnlyCatalog(INVALID_CATALOG_ID), "catalog");
	}

	/** A Distribution under the floor, likewise — the only way one gets in once #34 is fixed. */
	private void seedInvalidDistribution() throws Exception {
		String url = adminDistributions(OWNER_ID) + "/csv";
		seedUnderTheFloor(DISTRIBUTION_PID, url, titleOnlyDistribution(OWNER_ID, "csv"), "distribution");
	}

	/**
	 * Writes an entity the model would refuse, by switching that service's
	 * {@code validateOnWrite} off around the one write and back on afterwards.
	 * <p>
	 * Both switches are waited for on the write itself: off until the {@code PUT} is accepted,
	 * on until the same {@code PUT} is refused again. The second poll cannot damage what the
	 * first stored, because a refused write stores nothing.
	 */
	private void seedUnderTheFloor(String pid, String url, String body, String what) throws Exception {
		setValidateOnWrite(pid, false);
		HttpResponse<String> seeded = await(() -> put(url, body), status -> status / 100 == 2, 30_000);
		assertTrue(seeded.statusCode() / 100 == 2, "with " + pid + " not enforcing, the invalid " + what
				+ " should go in, but was: " + seeded.statusCode() + " " + seeded.body());

		setValidateOnWrite(pid, true);
		assertEquals(422, await(() -> put(url, body), status -> status == 422, 30_000).statusCode(),
				pid + " did not start enforcing again, so the rest of the test would prove nothing");
	}

	private int storedDistributions(String datasetId) throws Exception {
		HttpResponse<String> response = http.send(
				HttpRequest.newBuilder(URI.create(BASE + "/datasets/" + datasetId)).header("Accept", XMI).GET().build(),
				BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			return 0;
		}
		// Counting the elements rather than parsing: this asserts "nothing was stored", and a
		// substring count says that without dragging an EMF ResourceSet into an HTTP test.
		return response.body().split("<distribution", -1).length - 1;
	}

	// --- plumbing -----------------------------------------------------------

	private void setValidateOnWrite(String pid, boolean enabled) throws IOException {
		Configuration configuration = configAdmin.getConfiguration(pid, "?");
		Dictionary<String, Object> properties = configuration.getProperties();
		Dictionary<String, Object> updated = properties == null ? new Hashtable<>() : properties;
		updated.put("validateOnWrite", enabled);
		configuration.update(updated);
	}

	private HttpResponse<String> awaitEnforcementActive(long timeoutMillis) throws Exception {
		return await(() -> put(ADMIN_DATASETS + "/" + PROBE_ID, titleOnly(PROBE_ID)), status -> status == 422,
				timeoutMillis);
	}

	private HttpResponse<String> post(String url, String body) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(url)) //
				.header("Content-Type", XMI) //
				.header("Accept", XMI) //
				.POST(BodyPublishers.ofString(body)) //
				.build(), BodyHandlers.ofString());
	}

	/**
	 * Retries {@code attempt} until its status is {@code accepted}, or the deadline passes.
	 * <p>
	 * A ConfigAdmin update rebinds the service asynchronously, so every switch of
	 * {@code validateOnWrite} has to be waited for on the write path it affects rather than
	 * assumed to have landed. The attempts are all idempotent {@code PUT}s, so a poll mints
	 * nothing.
	 */
	private HttpResponse<String> await(Callable<HttpResponse<String>> attempt, IntPredicate accepted,
			long timeoutMillis) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		HttpResponse<String> last = attempt.call();
		while (!accepted.test(last.statusCode()) && System.currentTimeMillis() < deadline) {
			Thread.sleep(250);
			last = attempt.call();
		}
		return last;
	}

	private HttpResponse<String> put(String url, String body) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(url)) //
				.header("Content-Type", XMI) //
				.header("Accept", XMI) //
				.PUT(BodyPublishers.ofString(body)) //
				.build(), BodyHandlers.ofString());
	}

	private HttpResponse<String> delete(String url) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(url)).DELETE().build(), BodyHandlers.ofString());
	}
}
