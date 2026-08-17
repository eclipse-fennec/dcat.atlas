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
package org.eclipse.fennec.dcat.atlas.sparql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.hc.api.Result;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.eclipse.fennec.dcat.atlas.api.DcatEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dcat.Catalog;
import dcat.DcatFactory;
import dcat.Dataset;
import dcat.Distribution;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * Unit tests for the RDF projection (persistence plan P1-8).
 * <p>
 * The store is faked so the tests can put it in states a running system reaches
 * only rarely — a resource vanishing behind the projection's back, an
 * {@code rdf:about} changing — which is exactly where a projection goes wrong.
 */
class DcatGraphServiceImplTest {

	// The store mints logical identities, and the projection derives ids from them, so
	// fixtures have to look like stored data rather than like a request URL.
	private static final String BASE = org.eclipse.fennec.dcat.atlas.api.DcatIds.LOGICAL_BASE;

	/** Leaves every IRI as it is, so a projection assertion reads as what was stored. */
	private static final org.eclipse.fennec.dcat.atlas.api.PublicIris IDENTITY_IRIS = new org.eclipse.fennec.dcat.atlas.api.PublicIris() {
		@Override
		public String toPublic(String iri) {
			return iri;
		}

		@Override
		public String toLogical(String iri) {
			return iri;
		}

		@Override
		public boolean isOwned(String iri) {
			return false;
		}

		@Override
		public String publicBase() {
			return BASE;
		}
	};

	private FakeStores stores;

	@BeforeEach
	void setUp() {
		stores = new FakeStores();
	}

	private DcatGraphServiceImpl graph() {
		return graph(true);
	}

	private DcatGraphServiceImpl graph(boolean enabled) {
		// Identity mapping: this test asserts projection, not rebasing, so the public and
		// logical forms are deliberately the same and graph names stay readable.
		return new DcatGraphServiceImpl(IDENTITY_IRIS, stores.catalogService(), stores.datasetService(),
				stores.seriesService(), stores.dataServiceService(), stores.distributionService(), enabled, 0L, 0L);
	}

	// --- building ----------------------------------------------------------

	@Test
	void refreshProjectsEveryStoreAndReportsReady() {
		stores.catalogs.put("c1", catalog(BASE + "catalogs/c1", "Catalogue"));
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));

		DcatGraphServiceImpl graph = graph();
		assertFalse(graph.isReady(), "must not claim readiness before the first build");

		graph.refresh();

		assertTrue(graph.isReady());
		assertEquals(2, namedGraphs(graph).size());
		assertTrue(namedGraphs(graph).contains(BASE + "catalogs/c1"));
		assertTrue(namedGraphs(graph).contains(BASE + "datasets/d1"));
	}

	@Test
	void graphIsNamedByTheResourceAbout() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		DcatGraphServiceImpl graph = graph();
		graph.refresh();

		assertEquals(List.of(BASE + "datasets/d1"), namedGraphs(graph));
	}

	@Test
	void distributionsAreProjectedUnderTheirDataset() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		stores.putDistribution("d1", "x1", distribution(BASE + "datasets/d1/distributions/x1", "CSV export"));

		DcatGraphServiceImpl graph = graph();
		graph.refresh();

		assertEquals(2, namedGraphs(graph).size());
		assertTrue(namedGraphs(graph).contains(BASE + "datasets/d1/distributions/x1"));
	}

	@Test
	void orphanedDistributionStaysOutOfTheGraph() {
		// No dataset references it, so DistributionReadOnlyService cannot resolve it
		// (FR-10) and it must not appear — otherwise SPARQL would answer with data the
		// REST API refuses to serve.
		stores.putDistribution("gone", "x1", distribution(BASE + "datasets/gone/distributions/x1", "CSV"));

		DcatGraphServiceImpl graph = graph();
		graph.refresh();

		assertEquals(List.of(), namedGraphs(graph));
	}

	// --- idempotence and drift (G3) ----------------------------------------

	@Test
	void repeatedRefreshIsIdempotent() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		DcatGraphServiceImpl graph = graph();

		graph.refresh();
		long first = tripleCount(graph);
		graph.refresh();
		graph.refresh();

		assertEquals(first, tripleCount(graph), "re-projecting must converge, not accumulate");
		assertEquals(1, namedGraphs(graph).size());
	}

	@Test
	void repeatedInvalidateIsIdempotent() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		DcatGraphServiceImpl graph = graph();
		graph.refresh();
		long first = tripleCount(graph);

		graph.invalidate(DcatEntity.DATASET, "d1");
		graph.invalidate(DcatEntity.DATASET, "d1");

		assertEquals(first, tripleCount(graph));
		assertEquals(1, namedGraphs(graph).size());
	}

	@Test
	void invalidateReflectsAnUpdate() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Old title"));
		DcatGraphServiceImpl graph = graph();
		graph.refresh();

		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "New title"));
		graph.invalidate(DcatEntity.DATASET, "d1");

		assertTrue(ask(graph, "ASK { GRAPH ?g { ?s <http://purl.org/dc/terms/title> \"New title\"@en } }"));
		assertFalse(ask(graph, "ASK { GRAPH ?g { ?s <http://purl.org/dc/terms/title> \"Old title\"@en } }"),
				"the previous graph contents must be replaced, not merged");
	}

	@Test
	void invalidateRemovesAResourceThatHasGoneFromTheStore() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		DcatGraphServiceImpl graph = graph();
		graph.refresh();
		assertEquals(1, namedGraphs(graph).size());

		stores.datasets.remove("d1");
		graph.invalidate(DcatEntity.DATASET, "d1");

		assertEquals(List.of(), namedGraphs(graph));
		assertEquals(0, tripleCount(graph));
	}

	@Test
	void refreshDropsResourcesDeletedBehindItsBack() {
		// The reconciliation path (P1-7): nothing told the projection, it has to notice.
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		stores.datasets.put("d2", dataset(BASE + "datasets/d2", "Water quality"));
		DcatGraphServiceImpl graph = graph();
		graph.refresh();
		assertEquals(2, namedGraphs(graph).size());

		stores.datasets.remove("d2");
		graph.refresh();

		assertEquals(List.of(BASE + "datasets/d1"), namedGraphs(graph));
	}

	@Test
	void changingAboutRenamesTheGraphRatherThanDuplicating() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		DcatGraphServiceImpl graph = graph();
		graph.refresh();

		// Same storage id, different about: the old graph name must not survive, or the
		// resource would be in the graph twice under two identities.
		stores.datasets.put("d1", dataset(BASE + "datasets/renamed", "Air quality"));
		graph.invalidate(DcatEntity.DATASET, "d1");

		assertEquals(List.of(BASE + "datasets/renamed"), namedGraphs(graph));
	}

	@Test
	void resourceWithoutAboutIsSkippedRatherThanFailing() {
		stores.datasets.put("d1", dataset(null, "No identity"));
		DcatGraphServiceImpl graph = graph();

		graph.refresh();

		assertTrue(graph.isReady(), "one unusable resource must not stop the build");
		assertEquals(List.of(), namedGraphs(graph));
	}

	// --- content fidelity --------------------------------------------------

	@Test
	void projectedTriplesMatchTheRestRdfRepresentation() {
		Dataset stored = dataset(BASE + "datasets/d1", "Air quality");
		stores.datasets.put("d1", stored);
		DcatGraphServiceImpl graph = graph();
		graph.refresh();

		// Same bridge the REST writers use, so SPARQL cannot disagree with a GET.
		Model expected = org.eclipse.fennec.dcat.atlas.msg.body.readerwriter.EObjectToJena.toModel(stored);
		Model projected = graph.execute(QueryFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }"),
				execution -> execution.execConstruct());

		assertTrue(expected.isIsomorphicWith(projected));
	}

	// --- disabled and readiness -------------------------------------------

	@Test
	void disabledProjectionIgnoresInvalidateAndReportsOk() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		DcatGraphServiceImpl graph = graph(false);

		graph.invalidate(DcatEntity.DATASET, "d1");

		assertFalse(graph.isEnabled());
		assertEquals(List.of(), namedGraphs(graph));
		// Not CRITICAL: a deployment that switched SPARQL off is fit to serve.
		assertEquals(Result.Status.OK, graph.execute().getStatus());
	}

	@Test
	void readinessIsCriticalUntilTheProjectionIsBuilt() {
		DcatGraphServiceImpl graph = graph();
		// The dangerous state: queries would succeed and return too little.
		assertEquals(Result.Status.CRITICAL, graph.execute().getStatus());

		graph.refresh();

		assertEquals(Result.Status.OK, graph.execute().getStatus());
	}

	@Test
	void distributionInvalidateWithoutItsDatasetIsANoOp() {
		stores.datasets.put("d1", dataset(BASE + "datasets/d1", "Air quality"));
		stores.putDistribution("d1", "x1", distribution(BASE + "datasets/d1/distributions/x1", "CSV"));
		DcatGraphServiceImpl graph = graph();
		graph.refresh();

		// No dataset id: it cannot be re-read, so the projection must leave what it has
		// alone rather than guess and drop it.
		graph.invalidate(DcatEntity.DISTRIBUTION, "x1");

		assertEquals(2, namedGraphs(graph).size());
	}

	// --- helpers -----------------------------------------------------------

	private static List<String> namedGraphs(DcatGraphServiceImpl graph) {
		return graph.execute(QueryFactory.create("SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } }"), execution -> {
			List<String> names = new ArrayList<>();
			execution.execSelect().forEachRemaining(row -> names.add(row.getResource("g").getURI()));
			return names;
		});
	}

	private static long tripleCount(DcatGraphServiceImpl graph) {
		return graph.execute(QueryFactory.create("SELECT (COUNT(*) AS ?n) WHERE { GRAPH ?g { ?s ?p ?o } }"),
				execution -> execution.execSelect().next().getLiteral("n").getLong());
	}

	private static boolean ask(DcatGraphServiceImpl graph, String query) {
		return graph.execute(QueryFactory.create(query), execution -> execution.execAsk());
	}

	private static Catalog catalog(String about, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(about);
		catalog.getTitle().add(literal(title));
		return catalog;
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(about);
		dataset.getTitle().add(literal(title));
		return dataset;
	}

	private static Distribution distribution(String about, String title) {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(about);
		// Single-valued on Distribution, unlike Catalog/Dataset.
		distribution.setTitle(literal(title));
		return distribution;
	}

	private static PlainLiteral literal(String value) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setValue(value);
		literal.setLang("en");
		return literal;
	}
}
