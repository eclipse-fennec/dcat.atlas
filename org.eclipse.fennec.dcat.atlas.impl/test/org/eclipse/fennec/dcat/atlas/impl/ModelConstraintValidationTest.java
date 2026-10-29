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
package org.eclipse.fennec.dcat.atlas.impl;

import org.eclipse.fennec.dcat.atlas.impl.handler.DataServiceAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DatasetAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import java.nio.file.Path;

import javax.xml.datatype.DatatypeFactory;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.fennec.dcat.atlas.api.validation.ModelConstraintException;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.engine.OclEngines;
import org.eclipse.fennec.m2x.ocl.parser.OclParserSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.CatalogRecord;
import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;
import dcat.Distribution;
import foaf.Agent;
import foaf.FoafFactory;
import rdf.DateOrDateTimeLiteral;
import rdf.PlainLiteral;
import rdf.RdfFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * On-write enforcement of the model's own constraints: the OCL invariants annotated on
 * the ecore and the multiplicities the ecore declares, both evaluated by EMF's
 * {@code Diagnostician} at the persistence boundary.
 *
 * <h2>Why the delegates are installed here</h2>
 *
 * In OSGi the {@code m2x} engine publishes its validation delegate as a service and the
 * emf.osgi whiteboard puts it into EMF's global registry. A plain JUnit test has no
 * whiteboard, so it does the same thing by hand — {@code installDelegates()} — which is
 * the documented standalone path. Without it every annotated constraint would report
 * <em>delegate not found</em>, and the tests here would pass for the wrong reason
 * (see {@link #aMissingDelegateRejectsTheWriteRatherThanSkippingTheConstraint()}).
 */
public class ModelConstraintValidationTest {

	private static final String DATASETS = org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout.LOGICAL_BASE
			+ org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout.DATASETS + "/";
	private static final String SERVICES = org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout.LOGICAL_BASE
			+ org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout.DATA_SERVICES + "/";

	/**
	 * Only for the fail-closed test below, which takes the delegates away and puts them back.
	 * Installing them is {@link TestResourceSets}'s job — an {@code @AfterAll} uninstall here
	 * would leave every later test class in the run without them.
	 */
	private static final OclEngine ENGINE = OclEngines.create(new OclParserSupport());

	@TempDir
	Path storage;

	private DatasetAdminHandlerImpl datasets(boolean validateOnWrite) {
		return new DatasetAdminHandlerImpl(TestNotifiers.discarding(), TestStores.at(storage, validateOnWrite));
	}

	private DataServiceAdminHandlerImpl dataServices(boolean validateOnWrite) {
		return new DataServiceAdminHandlerImpl(TestNotifiers.discarding(), TestStores.at(storage, validateOnWrite));
	}

	// --- what a conformant entity looks like --------------------------------

	@Test
	void aConformantDatasetIsStored() {
		Dataset dataset = dataset("Air quality");
		datasets(true).upsertDataset(dataset, Caller.ANONYMOUS);

		assertTrue(datasets(true).getDataset("air").isPresent());
	}

	// --- multiplicities the ecore declares ----------------------------------

	@Test
	void aDatasetWithoutAPublisherIsRefused() {
		Dataset dataset = dataset("Air quality");
		dataset.setPublisher(null);

		ModelConstraintException refused = assertThrows(ModelConstraintException.class,
				() -> datasets(true).upsertDataset(dataset, Caller.ANONYMOUS));
		assertTrue(refused.getViolations().stream().anyMatch(v -> v.contains("publisher")),
				"the violation should name the missing feature, but was: " + refused.getViolations());
	}

	@Test
	void aRefusedWriteLeavesNothingOnDisk() {
		Dataset dataset = dataset("Air quality");
		dataset.getTitle().clear();

		assertThrows(ModelConstraintException.class, () -> datasets(true).upsertDataset(dataset, Caller.ANONYMOUS));
		assertTrue(datasets(false).getDataset("air").isEmpty(),
				"a rejected entity must not reach the store; validation runs before the file is written");
	}

	// --- OCL invariants -----------------------------------------------------

	@Test
	void aDatasetWithoutADescriptionIsRefused() {
		Dataset dataset = dataset("Air quality");
		dataset.getDescription().clear();

		ModelConstraintException refused = assertThrows(ModelConstraintException.class,
				() -> datasets(true).upsertDataset(dataset, Caller.ANONYMOUS));
		assertTrue(refused.getViolations().stream().anyMatch(v -> v.contains("HasDescription")),
				"expected the HasDescription invariant, but was: " + refused.getViolations());
	}

	/**
	 * The reason {@code description} could not stay a {@code [1..*]} lower bound on the
	 * shared supertype: DCAT-AP.de §4.4 makes it Pflicht for Catalog/Dataset/DatasetSeries
	 * and <em>not</em> for DataService, and an ecore cannot relax a bound in a subclass.
	 * Declaring the invariant on {@code Dataset} is what excludes DataService — this test
	 * is what stops someone "tidying" it up onto {@code DcatResource}.
	 */
	@Test
	void aDataServiceWithoutADescriptionIsStored() {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		service.setAbout(SERVICES + "sparql");
		service.getTitle().add(literal("SPARQL endpoint"));
		service.setPublisher(publisher());
		service.getEndpointURL().add("https://example.de/sparql");

		dataServices(true).upsertDataService(service, Caller.ANONYMOUS);

		assertTrue(dataServices(true).getDataService("sparql").isPresent());
	}

	@Test
	void anAnyUriAttributeThatIsNotAnIriIsRefused() {
		Dataset dataset = dataset("Air quality");
		dataset.getTheme().add("ENVI");

		ModelConstraintException refused = assertThrows(ModelConstraintException.class,
				() -> datasets(true).upsertDataset(dataset, Caller.ANONYMOUS));
		assertTrue(refused.getViolations().stream().anyMatch(v -> v.contains("ThemeIsIri")),
				"expected the ThemeIsIri invariant, but was: " + refused.getViolations());
	}

	@Test
	void anAbsoluteIriWithAFragmentIsAccepted() {
		Dataset dataset = dataset("Air quality");
		dataset.getTheme().add("https://govdata.de/themes#environment");

		datasets(true).upsertDataset(dataset, Caller.ANONYMOUS);

		assertTrue(datasets(true).getDataset("air").isPresent());
	}

	@Test
	void anIriContainingASpaceIsRefused() {
		Dataset dataset = dataset("Air quality");
		dataset.getTheme().add("https://example.de/data theme");

		assertThrows(ModelConstraintException.class, () -> datasets(true).upsertDataset(dataset, Caller.ANONYMOUS));
	}

	/**
	 * A Distribution is contained in its Dataset's file (FR-10), so the Dataset's write is
	 * the only write it ever gets. {@code Diagnostician} walks {@code eAllContents}, which
	 * is what makes the contained entity's own constraints reachable at all.
	 */
	@Test
	void aContainedDistributionIsValidatedWithItsDataset() {
		Dataset dataset = dataset("Air quality");
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(DATASETS + "air/csv");
		dataset.getDistribution().add(distribution);

		ModelConstraintException refused = assertThrows(ModelConstraintException.class,
				() -> datasets(true).upsertDataset(dataset, Caller.ANONYMOUS));
		assertTrue(refused.getViolations().stream().anyMatch(v -> v.contains("accessURL") || v.contains("license")),
				"expected the Distribution's own mandatory features, but was: " + refused.getViolations());
	}

	// --- the switch ---------------------------------------------------------

	@Test
	void withValidationOffTheSameEntityIsStored() {
		Dataset bare = DcatFactory.eINSTANCE.createDataset();
		bare.setAbout(DATASETS + "air");

		datasets(false).upsertDataset(bare, Caller.ANONYMOUS);

		assertTrue(datasets(false).getDataset("air").isPresent(),
				"validateOnWrite=false must leave the store's behaviour exactly as it was");
	}

	// --- fails closed -------------------------------------------------------

	/**
	 * If the OCL engine is absent, EMF reports every annotated constraint as
	 * <em>constraint delegate not found</em> at {@code ERROR} rather than passing it. So a
	 * deployment that forgot the engine bundle refuses writes instead of silently
	 * accepting unvalidated ones — the opposite of the unconfigured-SHACL failure mode,
	 * and the reason {@code @RequireOCL} is a belt-and-braces measure rather than the only
	 * safeguard.
	 */
	@Test
	void aMissingDelegateRejectsTheWriteRatherThanSkippingTheConstraint() {
		Dataset conformant = dataset("Air quality");
		ENGINE.uninstallDelegates();
		try {
			ModelConstraintException refused = assertThrows(ModelConstraintException.class,
					() -> datasets(true).upsertDataset(conformant, Caller.ANONYMOUS));
			assertTrue(refused.getViolations().stream().anyMatch(v -> v.contains("delegate")),
					"expected a delegate-not-found diagnostic, but was: " + refused.getViolations());
			assertFalse(refused.getViolations().isEmpty());
		} finally {
			ENGINE.installDelegates();
		}
	}

	@Test
	void theExceptionCarriesEveryViolationNotJustTheFirst() {
		Dataset dataset = dataset("Air quality");
		dataset.getDescription().clear();
		dataset.setPublisher(null);

		ModelConstraintException refused = assertThrows(ModelConstraintException.class,
				() -> datasets(true).upsertDataset(dataset, Caller.ANONYMOUS));
		assertEquals(2, refused.getViolations().size(),
				"both the missing publisher and the missing description should be reported: "
						+ refused.getViolations());
	}

	// --- fixtures -----------------------------------------------------------

	/** A Dataset carrying exactly what DCAT-AP.de makes Pflicht: title, description, publisher. */
	// --- dcterms:source on a CatalogRecord (#56) ----------------------------

	/**
	 * {@code dcterms:source} names the record this one was harvested from, in the catalogue
	 * it came from. It is an {@code AnyURI} attribute, so without an invariant a value that
	 * is not an absolute IRI would degrade silently to an RDF literal rather than fail
	 * ({@code EObjectToJena} only throws for the subject and for link targets).
	 *
	 * <h2>Why the record is validated directly</h2>
	 *
	 * A record is only ever written as part of its Catalog, and {@code primaryTopic} is a
	 * non-containment reference the store would have to resolve first. Going through a store
	 * write would exercise the store; the invariant is local to the record, and
	 * {@code Diagnostician} is what the persistence boundary calls anyway.
	 */
	@Test
	void aCatalogRecordSourceThatIsNotAnIriIsRefused() {
		CatalogRecord record = catalogRecord();
		record.setSource("record-42");

		Diagnostic diagnostic = Diagnostician.INSTANCE.validate(record);

		assertEquals(Diagnostic.ERROR, diagnostic.getSeverity(), diagnostic.toString());
		assertTrue(diagnostic.getChildren().stream().anyMatch(child -> child.getMessage().contains("SourceIsIri")),
				"expected the SourceIsIri invariant, but was: " + diagnostic);
	}

	@Test
	void aCatalogRecordSourceThatIsAnIriIsAccepted() {
		CatalogRecord record = catalogRecord();
		record.setSource("https://govdata.de/ckan/catalog/records/42");

		assertEquals(Diagnostic.OK, Diagnostician.INSTANCE.validate(record).getSeverity());
	}

	/** The property is Optional [0..1]: saying nothing about provenance is conformant. */
	@Test
	void aCatalogRecordWithoutASourceIsAccepted() {
		assertEquals(Diagnostic.OK, Diagnostician.INSTANCE.validate(catalogRecord()).getSeverity());
	}

	/** A record with the two features DCAT-AP.de makes mandatory, and nothing else. */
	private static CatalogRecord catalogRecord() {
		CatalogRecord record = DcatFactory.eINSTANCE.createCatalogRecord();
		DateOrDateTimeLiteral modified = RdfFactory.eINSTANCE.createDateOrDateTimeLiteral();
		modified.setValue(DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar("2026-10-20"));
		record.setModified(modified);
		Dataset topic = DcatFactory.eINSTANCE.createDataset();
		topic.setAbout(DATASETS + "air");
		record.setPrimaryTopic(topic);
		return record;
	}

	private static Dataset dataset(String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASETS + "air");
		dataset.getTitle().add(literal(title));
		dataset.getDescription().add(literal(title + " measurements"));
		dataset.setPublisher(publisher());
		return dataset;
	}

	private static Agent publisher() {
		Agent agent = FoafFactory.eINSTANCE.createAgent();
		agent.setAbout("https://example.de/organisation/uba");
		agent.getName().add(literal("Umweltbundesamt"));
		return agent;
	}

	private static PlainLiteral literal(String value) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(value);
		return literal;
	}
}
