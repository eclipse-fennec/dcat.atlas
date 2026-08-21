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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.eclipse.fennec.dcat.atlas.api.admin.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import dcat.Catalog;
import dcat.Dataset;
import dcat.DcatFactory;
import foaf.Organization;

public class CatalogResourceIntegrationTest extends AbstractEntityResourceIntegrationTest {

	@InjectService
	CatalogAdminService service;

	/** Membership by reference needs a real, separately stored Dataset to point at. */
	@InjectService
	DatasetAdminService datasetAdminService;

	@Override
	protected String collection() {
		return "catalogs";
	}

	@Override
	protected String typeName() {
		return "Catalog";
	}

	@Override
	protected String readResourceName() {
		return "CatalogReadOnlyResource";
	}

	@Override
	protected String adminResourceName() {
		return "CatalogAdminResource";
	}

	@Override
	protected void seed(String id, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		// Seeded the way the store mints identities: logical, not the request URL.
		catalog.setAbout(DcatIds.logicalIri(DcatIds.CATALOGS, id));
		RestEntities.mandatoryDataset(catalog, title);
		service.upsertCatalog(catalog);
	}

	@Override
	protected boolean storedPresent(String id) {
		return service.getCatalog(id).isPresent();
	}

	@Override
	protected String storedTitle(String id) {
		return service.getCatalog(id).get().getTitle().get(0).getValue();
	}

	@Override
	protected void removeFromStore(String id) {
		service.deleteCatalog(id, false);
	}

	// --- identity on create -------------------------------------------------

	/**
	 * Posting a body whose {@code about} already names one of our catalogs is refused, rather
	 * than quietly minting a second one. Two resources claiming the same identity is exactly
	 * what the logical-identity design exists to prevent, and a client that re-sends a catalog
	 * it was served would have had no way to tell it happened — both POSTs answered 201.
	 */
	@Test
	void postingAnAboutThatAlreadyExistsIsRefused() throws Exception {
		track("dup-cat");
		seed("dup-cat", "GovData");
		String existingAbout = DcatIds.logicalIri(DcatIds.CATALOGS, "dup-cat");

		HttpResponse<String> second = postXmi(writes(), xmiBody("Catalog", existingAbout, "GovData again"));

		assertEquals(409, second.statusCode(), second.body());
		assertTrue(second.body().contains("catalogs/dup-cat"), "the 409 should name what is in the way: " + second.body());
		assertEquals(1, service.listCatalogs().stream().filter(c -> existingAbout.equals(c.getAbout())).count(),
				"exactly one catalog should carry that identity");
		assertEquals("GovData", storedTitle("dup-cat"), "the refused POST must not have overwritten anything");
	}

	/**
	 * The same, in the form the client actually sees: the public IRI we served them. It is
	 * recognised because {@code PublicIriFilter} folds a request body's identities back to
	 * the logical form before the resource method runs.
	 */
	@Test
	void postingAPublicAboutThatAlreadyExistsIsRefused() throws Exception {
		track("dup-cat");
		seed("dup-cat", "GovData");

		HttpResponse<String> second = postXmi(writes(), xmiBody("Catalog", reads() + "/dup-cat", "GovData again"));

		assertEquals(409, second.statusCode(), second.body());
	}

	/**
	 * The carve-out that makes the rule above usable: only the entity being stored has to
	 * carry an identity of ours. The resources contained in it — publisher, license, contact
	 * point — are external things by nature, and are kept exactly as sent. Refusing those
	 * would make it impossible to say who published a catalog.
	 */
	@Test
	void containedResourcesKeepTheirForeignAbout() throws Exception {
		track("gov");

		HttpResponse<String> created = postXmi(writes(), GOVDATA_CATALOG);

		assertEquals(201, created.statusCode(), created.body());
		Catalog stored = service.getCatalog("gov").orElseThrow();
		assertEquals("https://www.umweltbundesamt.de/", stored.getPublisher().getAbout());
		assertEquals("http://dcat-ap.de/def/licenses/dl-by-de/2.0", stored.getLicense().getAbout());
	}

	/**
	 * The client's view of it: the identical request sent twice, exactly as a retry or a
	 * double-click produces it. The first creates, the second conflicts — where both used to
	 * answer 201 with a different {@code Location} and leave two catalogs describing one
	 * thing, because the create minted a fresh UUID before it ever looked at the
	 * {@code about} it was sent.
	 */
	@Test
	void postingTheSameBodyTwiceCreatesOnceAndThenConflicts() throws Exception {
		HttpResponse<String> first = postXmi(writes(), GOVDATA_CATALOG);
		assertEquals(201, first.statusCode(), first.body());
		// The client's own identity is honoured, so Location is the id it named — not a UUID.
		assertEquals(reads() + "/gov", location(first));
		// Counted rather than matched on content: the store this runs against is shared and
		// outlives the run, so what the second POST did is the difference it made, not how
		// many GovData catalogs happen to be lying in it.
		int afterFirst = service.listCatalogs().size();

		HttpResponse<String> second = postXmi(writes(), GOVDATA_CATALOG);

		assertEquals(409, second.statusCode(), second.body());
		// The client came for a URL, and gets the same one either way: it can go straight on to
		// POST .../catalogs/gov/datasets without a GET to find out where "gov" ended up.
		assertEquals(reads() + "/gov", second.headers().firstValue("Location").orElse(null),
				"the conflict should carry the Location the create would have");
		assertEquals(afterFirst, service.listCatalogs().size(), "the refused POST must not have stored anything");
	}

	/**
	 * The body from the issue verbatim: a client-chosen {@code about} under our own catalogs
	 * base, which is an identity we could recognise, plus the nested publisher/license the
	 * reader has to cope with.
	 */
	private static final String GOVDATA_CATALOG = """
			<?xml version="1.0" encoding="UTF-8"?>
			<dcat:Catalog xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:dcat="http://www.w3.org/ns/dcat#"
			    about="http://dcat.atlas/catalogs/gov" homepage="https://www.govdata.de/">
			  <title value="GovData Katalog" lang="de"/>
			  <description value="Der offene Datenkatalog des Portals." lang="de"/>
			  <publisher about="https://www.umweltbundesamt.de/">
			    <name value="Umweltbundesamt" lang="de"/>
			  </publisher>
			  <license about="http://dcat-ap.de/def/licenses/dl-by-de/2.0"/>
			  <themeTaxonomy>http://publications.europa.eu/resource/authority/data-theme</themeTaxonomy>
			</dcat:Catalog>""";

	/**
	 * The create example from {@code docs/opendata-portal-user-guide.md}, verbatim but for
	 * the host in {@code about} — the guide documents the default local runtime
	 * ({@code :8085/dcat/rest}), this suite runs on another port.
	 * <p>
	 * A guide whose first example does not parse is worse than no guide, and every XMI rule
	 * it teaches is in this one body: literals with a language, a repeated URI-valued
	 * property, a date literal, a single-valued URI as a plain attribute, and a nested agent.
	 */
	private static final String USER_GUIDE_CATALOG = """
			<?xml version="1.0" encoding="UTF-8"?>
			<dcat:Catalog xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
			              xmi:version="2.0"
			              about="%s"
			              homepage="https://example.org/opendata">
			  <title lang="en" value="Example Catalog"/>
			  <title lang="de" value="Beispiel-Katalog"/>
			  <description lang="en" value="A catalog created via the admin API."/>
			  <language>http://publications.europa.eu/resource/authority/language/ENG</language>
			  <language>http://publications.europa.eu/resource/authority/language/DEU</language>
			  <issued value="2026-07-14T10:00:00.000+02:00"/>
			  <publisher about="https://data-in-motion.biz">
			    <name lang="en" value="Data In Motion"/>
			  </publisher>
			  <license about="http://dcat-ap.de/def/licenses/dl-by-de/2.0"/>
			</dcat:Catalog>""";

	/**
	 * The other half of the guide's nested-object rule: a bare {@code <publisher>} is the
	 * declared type ({@code foaf:Agent}, which is concrete), and {@code xsi:type} is how you
	 * ask for the narrower one.
	 */
	@Test
	void aNestedObjectTakesTheNarrowerTypeFromXsiType() throws Exception {
		track("xsi-typed");
		String body = """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Catalog xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				              xmlns:foaf="http://xmlns.com/foaf/0.1/"
				              xmi:version="2.0" about="%s/xsi-typed">
				  <title lang="en" value="Typed publisher"/>
				  <description lang="en" value="Typed-publisher fixture"/>
				  <publisher xsi:type="foaf:Organization" about="https://data-in-motion.biz">
				    <name lang="en" value="Data In Motion"/>
				  </publisher>
				</dcat:Catalog>""".formatted(reads());

		HttpResponse<String> created = postXmi(writes(), body);

		assertEquals(201, created.statusCode(), created.body());
		assertTrue(service.getCatalog("xsi-typed").orElseThrow().getPublisher() instanceof Organization,
				"xsi:type should have selected the narrower type");
	}

	/** Keeps the published guide honest: its example must still be one the API accepts. */
	@Test
	void theUserGuidesCreateExampleIsAccepted() throws Exception {
		track("example");

		HttpResponse<String> created = postXmi(writes(),
				USER_GUIDE_CATALOG.formatted(reads() + "/example"));

		assertEquals(201, created.statusCode(), created.body());
		Catalog stored = service.getCatalog("example").orElseThrow();
		assertEquals(2, stored.getTitle().size(), "both titles should have been read");
		assertEquals("Example Catalog", stored.getTitle().get(0).getValue());
		assertEquals(2, stored.getLanguage().size(), "a repeated URI-valued property");
		assertEquals("https://example.org/opendata", stored.getHomepage());
		assertTrue(stored.getIssued() != null && stored.getIssued().getValue() != null, "the date literal");
		assertEquals("Data In Motion", stored.getPublisher().getName().get(0).getValue());
		assertEquals("http://dcat-ap.de/def/licenses/dl-by-de/2.0", stored.getLicense().getAbout());
	}

	/**
	 * The {@code Location} of a create response, with the id also registered for cleanup —
	 * these are minted UUIDs, so the test cannot name them in advance.
	 */
	private String location(HttpResponse<String> created) {
		String value = created.headers().firstValue("Location").orElse(null);
		if (value != null) {
			track(value.substring(value.lastIndexOf('/') + 1));
		}
		return value;
	}

	/**
	 * An {@code about} under our own base that still could not be used as an id — a fragment
	 * here — is refused like a foreign one, and in particular does not reach
	 * {@code requireSafeId} and surface as a 500. {@code DcatIds.idOf} only refuses a tail
	 * containing a slash, so this is the gap between "names an id of ours" and "names one we
	 * can file"; a client that lands in it named an identity we cannot honour, which is the
	 * case the 400 exists for.
	 */
	@Test
	void postingAnUnusableIdUnderOurBaseIsRefused() throws Exception {
		HttpResponse<String> refused = postXmi(writes(),
				xmiBody("Catalog", DcatIds.logicalIri(DcatIds.CATALOGS, "frag") + "#x", "Fragment"));

		assertEquals(400, refused.statusCode(), refused.body());
		assertTrue(service.getCatalog("frag").isEmpty(), "the fragment must not have been filed as an id");
	}

	// --- FR-9 catalog membership -------------------------------------------

	private static final String MEMBER_DATASET_ID = "cat-member-ds";

	@Test
	void addAndRemoveDatasetMembershipOverHttp() throws Exception {
		track("cat1");
		seed("cat1", "GovData");
		// A dataset identity, not a path under the catalog: DcatIds.idOf refuses to carve an
		// id out of an IRI that is not under the datasets base, so a member sent as
		// ".../catalogs/cat1/datasets/ds1" gets a minted UUID instead of "ds1" — and the
		// DELETE below then names a resource that was never created.
		String memberAbout = BASE + "/datasets/" + MEMBER_DATASET_ID;

		HttpResponse<String> add = postXmi(writes() + "/cat1/datasets", xmiBody("Dataset", memberAbout, "Air"));
		assertEquals(200, add.statusCode(), add.body());
		assertEquals(1, service.getCatalog("cat1").get().getDataset().size());

		// Sending it again is refused: this endpoint writes the whole member, so a second
		// POST is a replace wearing the clothes of an add — and the dataset it would
		// overwrite may be a member of other catalogs, series and services that did not ask
		// for it to change. The refusal names both requests that say what they mean.
		HttpResponse<String> reAdd = postXmi(writes() + "/cat1/datasets", xmiBody("Dataset", memberAbout, "Rewritten"));
		assertEquals(409, reAdd.statusCode(), reAdd.body());
		assertTrue(reAdd.body().contains("PUT /admin/catalogs/cat1/datasets/" + MEMBER_DATASET_ID),
				"the 409 should point at the link request: " + reAdd.body());
		assertTrue(reAdd.body().contains("PUT /admin/datasets/" + MEMBER_DATASET_ID),
				"...and at the member's own endpoint for a change: " + reAdd.body());
		// Location is the member's own read URL — where the dataset in the way actually is,
		// not the membership path this request used to reach it.
		assertEquals(BASE + "/datasets/" + MEMBER_DATASET_ID, reAdd.headers().firstValue("Location").orElse(null),
				"the member conflict should point at the member that exists");
		assertEquals(1, service.getCatalog("cat1").get().getDataset().size());
		assertEquals("Air", datasetAdminService.getDataset(MEMBER_DATASET_ID).get().getTitle().get(0).getValue(),
				"the refused POST must not have rewritten the dataset");

		HttpResponse<String> remove = delete(writes() + "/cat1/datasets/" + MEMBER_DATASET_ID);
		assertEquals(204, remove.statusCode());
		assertTrue(service.getCatalog("cat1").get().getDataset().isEmpty());

		datasetAdminService.deleteDataset(MEMBER_DATASET_ID, false);
	}

	@Test
	void addAndRemoveSubCatalogMembershipOverHttp() throws Exception {
		track("cat1");
		// The add below *creates* the sub-catalog, and the store outlives the run: without
		// this the next run POSTs an id that already exists and gets the 409, not the 200.
		track("eu");
		seed("cat1", "GovData");
		String memberAbout = reads() + "/eu";

		HttpResponse<String> add = postXmi(writes() + "/cat1/catalogs", xmiBody("Catalog", memberAbout, "EU"));
		assertEquals(200, add.statusCode(), add.body());
		assertEquals(1, service.getCatalog("cat1").get().getCatalog().size());

		HttpResponse<String> remove = delete(writes() + "/cat1/catalogs/eu");
		assertEquals(204, remove.statusCode());
		assertTrue(service.getCatalog("cat1").get().getCatalog().isEmpty());
	}

	/**
	 * The identity rules of {@code POST /admin/datasets} apply here too, foreign
	 * {@code about} included — otherwise the weaker door is the one every client would use.
	 */
	@Test
	void addingAMemberWithAForeignAboutIsRefused() throws Exception {
		track("cat1");
		seed("cat1", "GovData");

		HttpResponse<String> refused = postXmi(writes() + "/cat1/datasets",
				xmiBody("Dataset", "https://www.govdata.de/datasets/air", "Air"));

		assertEquals(400, refused.statusCode(), refused.body());
		assertTrue(service.getCatalog("cat1").get().getDataset().isEmpty(), "nothing should have been linked");
	}

	@Test
	void addMembershipToUnknownCatalogIsNotFound() throws Exception {
		HttpResponse<String> add = postXmi(writes() + "/missing/datasets",
				xmiBody("Dataset", reads() + "/missing/datasets/ds1", "Air"));
		assertEquals(404, add.statusCode());
	}

	// --- FR-9 membership by reference (link an entity that already exists) ---
	//
	// The counterpart of the DELETE on the same path. POSTing to the collection stores
	// the body first, so it cannot attach an existing member without also rewriting it;
	// these endpoints name both ends in the path and carry no body at all.

	@Test
	void linkExistingDatasetOverHttp() throws Exception {
		track("cat1");
		seed("cat1", "GovData");
		seedDataset("air", "Air quality");
		try {
			HttpResponse<String> link = putEmpty(writes() + "/cat1/datasets/air");

			assertEquals(200, link.statusCode(), link.body());
			assertEquals(1, service.getCatalog("cat1").get().getDataset().size());
			// The point of linking: the dataset itself is untouched. A POST of a stub to
			// the collection would have replaced it, title and all.
			assertEquals("Air quality", datasetAdminService.getDataset("air").get().getTitle().get(0).getValue());

			HttpResponse<String> remove = delete(writes() + "/cat1/datasets/air");
			assertEquals(204, remove.statusCode());
			assertTrue(service.getCatalog("cat1").get().getDataset().isEmpty());
			// Unlinking removes the membership, not the dataset.
			assertTrue(datasetAdminService.getDataset("air").isPresent());
		} finally {
			datasetAdminService.deleteDataset("air", false);
		}
	}

	@Test
	void linkingTwiceIsIdempotent() throws Exception {
		track("cat1");
		seed("cat1", "GovData");
		seedDataset("air", "Air quality");
		try {
			HttpResponse<String> first = putEmpty(writes() + "/cat1/datasets/air");
			assertEquals(200, first.statusCode(), first.body());
			String etag = first.headers().firstValue("ETag").orElseThrow();

			HttpResponse<String> second = putEmpty(writes() + "/cat1/datasets/air");

			assertEquals(200, second.statusCode(), second.body());
			// No second write, so the catalog's ETag is unchanged (F-16).
			assertEquals(etag, second.headers().firstValue("ETag").orElseThrow());
			assertEquals(1, service.getCatalog("cat1").get().getDataset().size());
		} finally {
			// Unlink before deleting: FR-1 refuses to delete a dataset a catalog still
			// references, which is exactly the guarantee the link endpoint relies on.
			service.deleteDatasetFromCatalog("cat1", "air");
			datasetAdminService.deleteDataset("air", false);
		}
	}

	@Test
	void linkUnknownDatasetIsNotFound() throws Exception {
		track("cat1");
		seed("cat1", "GovData");

		// The service signals this with NoSuchElementException; the resource must turn it
		// into a 404 about the dataset, not let it escape as a 500.
		HttpResponse<String> link = putEmpty(writes() + "/cat1/datasets/does-not-exist");

		assertEquals(404, link.statusCode(), link.body());
		assertTrue(service.getCatalog("cat1").get().getDataset().isEmpty());
	}

	@Test
	void linkToUnknownCatalogIsNotFound() throws Exception {
		HttpResponse<String> link = putEmpty(writes() + "/missing/datasets/air");
		assertEquals(404, link.statusCode());
	}

	/** Seeds a Dataset directly through its service, the way {@link #seed} does a Catalog. */
	private void seedDataset(String id, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, id));
		RestEntities.mandatoryDataset(dataset, title);
		datasetAdminService.upsertDataset(dataset);
	}
}
