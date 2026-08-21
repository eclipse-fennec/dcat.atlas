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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.eclipse.fennec.dcat.atlas.api.ModelConstraintException;
import org.eclipse.fennec.m2x.ocl.engine.OclEngineImpl;
import org.eclipse.fennec.m2x.ocl.parser.OclParserSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;
import dcat.Distribution;
import foaf.Agent;
import foaf.FoafFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

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

	private static final String DATASETS = org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.LOGICAL_BASE
			+ org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.DATASETS + "/";
	private static final String SERVICES = org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.LOGICAL_BASE
			+ org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.DATA_SERVICES + "/";

	/**
	 * Only for the fail-closed test below, which takes the delegates away and puts them back.
	 * Installing them is {@link TestResourceSets}'s job — an {@code @AfterAll} uninstall here
	 * would leave every later test class in the run without them.
	 */
	private static final OclEngineImpl ENGINE = new OclEngineImpl(new OclParserSupport());

	@TempDir
	Path storage;

	private DatasetAdminServiceImpl datasets(boolean validateOnWrite) {
		return new DatasetAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH, validateOnWrite);
	}

	private DataServiceAdminServiceImpl dataServices(boolean validateOnWrite) {
		return new DataServiceAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH, validateOnWrite);
	}

	// --- what a conformant entity looks like --------------------------------

	@Test
	void aConformantDatasetIsStored() {
		Dataset dataset = dataset("Air quality");
		datasets(true).upsertDataset(dataset);

		assertTrue(datasets(true).getDataset("air").isPresent());
	}

	// --- multiplicities the ecore declares ----------------------------------

	@Test
	void aDatasetWithoutAPublisherIsRefused() {
		Dataset dataset = dataset("Air quality");
		dataset.setPublisher(null);

		ModelConstraintException refused = assertThrows(ModelConstraintException.class,
				() -> datasets(true).upsertDataset(dataset));
		assertTrue(refused.getViolations().stream().anyMatch(v -> v.contains("publisher")),
				"the violation should name the missing feature, but was: " + refused.getViolations());
	}

	@Test
	void aRefusedWriteLeavesNothingOnDisk() {
		Dataset dataset = dataset("Air quality");
		dataset.getTitle().clear();

		assertThrows(ModelConstraintException.class, () -> datasets(true).upsertDataset(dataset));
		assertTrue(datasets(false).getDataset("air").isEmpty(),
				"a rejected entity must not reach the store; validation runs before the file is written");
	}

	// --- OCL invariants -----------------------------------------------------

	@Test
	void aDatasetWithoutADescriptionIsRefused() {
		Dataset dataset = dataset("Air quality");
		dataset.getDescription().clear();

		ModelConstraintException refused = assertThrows(ModelConstraintException.class,
				() -> datasets(true).upsertDataset(dataset));
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

		dataServices(true).upsertDataService(service);

		assertTrue(dataServices(true).getDataService("sparql").isPresent());
	}

	@Test
	void anAnyUriAttributeThatIsNotAnIriIsRefused() {
		Dataset dataset = dataset("Air quality");
		dataset.getTheme().add("ENVI");

		ModelConstraintException refused = assertThrows(ModelConstraintException.class,
				() -> datasets(true).upsertDataset(dataset));
		assertTrue(refused.getViolations().stream().anyMatch(v -> v.contains("ThemeIsIri")),
				"expected the ThemeIsIri invariant, but was: " + refused.getViolations());
	}

	@Test
	void anAbsoluteIriWithAFragmentIsAccepted() {
		Dataset dataset = dataset("Air quality");
		dataset.getTheme().add("https://govdata.de/themes#environment");

		datasets(true).upsertDataset(dataset);

		assertTrue(datasets(true).getDataset("air").isPresent());
	}

	@Test
	void anIriContainingASpaceIsRefused() {
		Dataset dataset = dataset("Air quality");
		dataset.getTheme().add("https://example.de/data theme");

		assertThrows(ModelConstraintException.class, () -> datasets(true).upsertDataset(dataset));
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
				() -> datasets(true).upsertDataset(dataset));
		assertTrue(refused.getViolations().stream().anyMatch(v -> v.contains("accessURL") || v.contains("license")),
				"expected the Distribution's own mandatory features, but was: " + refused.getViolations());
	}

	// --- the switch ---------------------------------------------------------

	@Test
	void withValidationOffTheSameEntityIsStored() {
		Dataset bare = DcatFactory.eINSTANCE.createDataset();
		bare.setAbout(DATASETS + "air");

		datasets(false).upsertDataset(bare);

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
					() -> datasets(true).upsertDataset(conformant));
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
				() -> datasets(true).upsertDataset(dataset));
		assertEquals(2, refused.getViolations().size(),
				"both the missing publisher and the missing description should be reported: "
						+ refused.getViolations());
	}

	// --- fixtures -----------------------------------------------------------

	/** A Dataset carrying exactly what DCAT-AP.de makes Pflicht: title, description, publisher. */
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
