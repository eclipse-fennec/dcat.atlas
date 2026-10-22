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
package org.eclipse.fennec.dcat.atlas.impl.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.FailureDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.FailureReasons;
import org.junit.jupiter.api.Test;

/**
 * What the snapshot says about a portal that is configured wrongly.
 *
 * <p>
 * These are the six states the runtime service exists for: each one serves {@code 200} today
 * and is invisible without reading logs.
 * </p>
 */
class SnapshotBuilderTest {

	@Test
	void enforcementWithNoShapesIsReportedAsNotEnforcing() {
		DcatAtlasRuntimeDTO snapshot = build(new ValidationReading("/tmp/dcat-shapes-unset", 0, null, 0, true), graph(),
				store());

		assertTrue(snapshot.validation.enforceOnWrite, "the configuration asks for enforcement");
		assertFalse(snapshot.validation.effectivelyEnforcing,
				"with no shapes parsed, every write conforms - the portal is not enforcing anything");
		assertTrue(reasons(snapshot).contains(FailureReasons.SHAPES_NOT_LOADED), reasons(snapshot).toString());
		assertTrue(reasons(snapshot).contains(FailureReasons.ENFORCEMENT_INEFFECTIVE), reasons(snapshot).toString());
	}

	@Test
	void shapesWithoutVocabularyDataAreReported() {
		DcatAtlasRuntimeDTO snapshot = build(new ValidationReading("/opt/shapes", 12, null, 0, true), graph(), store());

		assertTrue(snapshot.validation.effectivelyEnforcing, "shapes are loaded and enforcement is on");
		assertTrue(reasons(snapshot).contains(FailureReasons.VOCABULARY_MISSING),
				"CV shapes with no vocabulary behind them report a false violation for every coded value (F-22)");
	}

	@Test
	void aFullyConfiguredPortalReportsNoFailures() {
		DcatAtlasRuntimeDTO snapshot = build(new ValidationReading("/opt/shapes", 12, "/opt/vocab", 5, true), graph(),
				store());

		assertEquals(List.of(), reasons(snapshot), "nothing is wrong with this one");
	}

	@Test
	void anEnabledGraphThatIsNotReadyIsReported() {
		DcatAtlasRuntimeDTO snapshot = build(validation(), new GraphReading(true, false, 10_000, 30_000), store());

		assertTrue(reasons(snapshot).contains(FailureReasons.GRAPH_NOT_READY),
				"an unready graph answers queries successfully and with too few rows");
	}

	@Test
	void aDisabledGraphIsNotAFailure() {
		DcatAtlasRuntimeDTO snapshot = build(validation(), new GraphReading(false, false, 0, 0), store());

		assertFalse(reasons(snapshot).contains(FailureReasons.GRAPH_NOT_READY),
				"switching the projection off is a deployment's choice, not a fault");
	}

	@Test
	void collectionsThatDisagreeAboutValidateOnWriteAreReported() {
		DcatAtlasRuntimeDTO snapshot = SnapshotBuilder.build(7, 1_760_000_000_000L, identity(), List.of(), store(),
				validation(), graph(),
				List.of(new CollectionReading("datasets", List.of("air"), true),
						new CollectionReading("catalogs", List.of("example"), false)),
				List.of(), List.of());

		assertTrue(reasons(snapshot).contains(FailureReasons.CONFIG_INCONSISTENT),
				"validateOnWrite is configured once per store, and two of them disagreeing is invisible otherwise");
	}

	@Test
	void aStoreThatDoesNotPersistIsReported() {
		DcatAtlasRuntimeDTO snapshot = build(validation(), graph(),
				new StoreReading("git", "memory:store", false, true));

		assertFalse(snapshot.store.persistent);
		assertTrue(reasons(snapshot).contains(FailureReasons.STORE_NOT_PERSISTING),
				"an in-memory store accepts every write and loses them all on restart");
	}

	@Test
	void anUnreadableStoreIsReported() {
		DcatAtlasRuntimeDTO snapshot = build(validation(), graph(), new StoreReading("git", "/srv/store.git", true, false));

		assertTrue(reasons(snapshot).contains(FailureReasons.STORE_UNREADABLE));
	}

	/**
	 * No credential reaches the DTO, in any form.
	 * <p>
	 * Asserted over the whole serialised snapshot rather than over the one field, because the
	 * guarantee is about the document a consumer receives and the next field added should have
	 * to pass it too.
	 */
	@Test
	void noCredentialAppearsAnywhereInTheSerialisedSnapshot() {
		DcatAtlasRuntimeDTO snapshot = build(validation(), graph(),
				new StoreReading("git", "https://portal:hunter2@example.org/store.git", true, true));

		assertEquals("https://example.org/store.git", snapshot.store.location, "the userinfo has to be stripped");

		// The DTO's own rendering, not a converter: this is the document a consumer sees, and
		// it does not depend on which converter implementation is on the class path.
		String serialised = snapshot.toString();
		assertFalse(serialised.contains("hunter2"), "a password reached the snapshot: " + serialised);
		assertFalse(serialised.contains("portal:"), "userinfo reached the snapshot: " + serialised);
	}

	/** An scp-style git remote hides its user in front of the host, where no URI parser looks. */
	@Test
	void anScpStyleRemoteLosesItsUserToo() {
		DcatAtlasRuntimeDTO snapshot = build(validation(), graph(),
				new StoreReading("git", "git@example.org:fennec/store.git", true, true));

		assertEquals("example.org:fennec/store.git", snapshot.store.location);
	}

	@Test
	void collectionsAndCatalogsAreCopiedThrough() {
		DcatAtlasRuntimeDTO snapshot = SnapshotBuilder.build(3, 42L, identity(), List.of("http://localhost:8085/dcat/rest"),
				store(), validation(), graph(),
				List.of(new CollectionReading("datasets", List.of("air", "traffic"), true)),
				List.of(new CatalogReading("example", "http://localhost:8085/dcat/rest/catalogs/example",
						"Example Catalog", 2, 1, 0, 2)),
				List.of());

		assertEquals(3, snapshot.changeCount);
		assertEquals(42L, snapshot.takenAt);
		assertEquals(List.of("http://localhost:8085/dcat/rest"), snapshot.endpoints);
		assertEquals(2, snapshot.collections.get(0).count, "the count comes from the ids, never from loading them");
		assertEquals(List.of("air", "traffic"), snapshot.collections.get(0).ids);
		assertEquals("Example Catalog", snapshot.catalogs.get(0).title);
		assertEquals(2, snapshot.catalogs.get(0).records);
	}

	/**
	 * A collection is listed once, however many stores answer for it.
	 *
	 * <p>
	 * Every collection has two: the read-only component and the admin component, each
	 * registering itself for introspection. Both are needed — {@code validateOnWrite} is
	 * configured on each, and the disagreement between them is a thing worth reporting — but
	 * a reader asking what the portal holds should be told once.
	 * </p>
	 */
	@Test
	void aCollectionIsListedOnceEvenWhenTwoStoresAnswerForIt() {
		DcatAtlasRuntimeDTO snapshot = SnapshotBuilder.build(1, 1L, identity(), List.of(), store(), validation(),
				graph(),
				List.of(new CollectionReading("datasets", List.of("air"), true),
						new CollectionReading("datasets", List.of("air"), true),
						new CollectionReading("catalogs", List.of("example"), true),
						new CollectionReading("catalogs", List.of("example"), true)),
				List.of(), List.of());

		assertEquals(List.of("datasets", "catalogs"),
				snapshot.collections.stream().map(c -> c.name).collect(Collectors.toList()),
				"four stores, two collections");
	}

	/**
	 * Two stores over the <em>same</em> collection that disagree are still reported.
	 *
	 * <p>
	 * This is the case the feature was written for and the one deduplication could hide: a
	 * dataset read through one component and written through another, checked on the way in
	 * by only one of them.
	 * </p>
	 */
	@Test
	void twoStoresOverOneCollectionThatDisagreeAreStillReported() {
		DcatAtlasRuntimeDTO snapshot = SnapshotBuilder.build(1, 1L, identity(), List.of(), store(), validation(),
				graph(),
				List.of(new CollectionReading("datasets", List.of("air"), true),
						new CollectionReading("datasets", List.of("air"), false)),
				List.of(), List.of());

		assertTrue(reasons(snapshot).contains(FailureReasons.CONFIG_INCONSISTENT),
				"the read-only and admin stores for one collection disagreeing is exactly the hidden case");
	}

	/**
	 * No two empty lists in a snapshot are the same object.
	 *
	 * <p>
	 * {@code List.of()} is a singleton, so a DTO whose empty fields all share it serialises
	 * with JSON-pointer back-references — {@code "failures":{"$ref":"#/endpoints"}} — which
	 * reads as though failures pointed at endpoints, and makes a converter round-trip to
	 * {@code Map} throw outright.
	 * </p>
	 */
	@Test
	void anEmptySnapshotSerialisesWithoutBackReferences() {
		DcatAtlasRuntimeDTO snapshot = SnapshotBuilder.build(0, 0, identity(), List.of(),
				new StoreReading("git", "/srv/store.git", true, true),
				new ValidationReading("/opt/shapes", 12, "/opt/vocab", 5, true), graph(),
				List.of(new CollectionReading("datasets", List.of(), true)), List.of(), List.of());

		assertFalse(snapshot.toString().contains("$ref"),
				"an empty list may not be rendered as a pointer at another field: " + snapshot);
	}

	/** Unsatisfied components are passed in rather than derived, and reach the failure list. */
	@Test
	void anUnsatisfiedComponentIsReportedAsItself() {
		FailureDTO unsatisfied = new FailureDTO();
		unsatisfied.reason = FailureReasons.UNSATISFIED_COMPONENT;
		unsatisfied.subject = "PublicIris";
		unsatisfied.message = "missing its mandatory configuration";

		DcatAtlasRuntimeDTO snapshot = SnapshotBuilder.build(0, 0, identity(), List.of(), store(), validation(),
				graph(), List.of(), List.of(), List.of(unsatisfied));

		assertTrue(reasons(snapshot).contains(FailureReasons.UNSATISFIED_COMPONENT),
				"a component that never started is the only failure the snapshot cannot see for itself");
	}

	// --- helpers ------------------------------------------------------------

	private static DcatAtlasRuntimeDTO build(ValidationReading validation, GraphReading graph, StoreReading store) {
		return SnapshotBuilder.build(1, 1_760_000_000_000L, identity(), List.of(), store, validation, graph,
				List.of(new CollectionReading("datasets", List.of("air"), true)), List.of(), List.of());
	}

	private static IdentityReading identity() {
		return new IdentityReading("http://dcat.atlas/", "http://localhost:8085/dcat/rest/");
	}

	private static ValidationReading validation() {
		return new ValidationReading("/opt/shapes", 12, "/opt/vocab", 5, true);
	}

	private static GraphReading graph() {
		return new GraphReading(true, true, 10_000, 30_000);
	}

	private static StoreReading store() {
		return new StoreReading("git", "/srv/store.git", true, true);
	}

	private static List<String> reasons(DcatAtlasRuntimeDTO snapshot) {
		return snapshot.failures.stream().map(f -> f.reason).collect(Collectors.toList());
	}
}
