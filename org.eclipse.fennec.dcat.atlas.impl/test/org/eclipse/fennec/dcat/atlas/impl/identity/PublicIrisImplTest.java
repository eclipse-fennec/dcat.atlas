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
package org.eclipse.fennec.dcat.atlas.impl.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The logical &harr; public mapping. The cases that matter are the ones where
 * getting it wrong is silent: rewriting a third party's IRI into ours, or
 * failing to fold an inbound one and storing a hostname.
 */
class PublicIrisImplTest {

	private static final String PUBLIC = "https://opendata.example.de/dcat/rest/";
	private static final String LOGICAL = "http://dcat.atlas/";

	private final PublicIrisImpl iris = new PublicIrisImpl(PUBLIC);

	@Test
	void aStoredIdentityRendersUnderThePublicBase() {
		assertEquals(PUBLIC + "datasets/air", iris.toPublic(LOGICAL + "datasets/air"));
	}

	@Test
	void aPublicIdentityFoldsBackToLogical() {
		assertEquals(LOGICAL + "datasets/air", iris.toLogical(PUBLIC + "datasets/air"));
	}

	@Test
	void aLogicalIdentityInboundStaysLogical() {
		// A stored file POSTed back is already logical; folding must be idempotent.
		assertEquals(LOGICAL + "datasets/air", iris.toLogical(LOGICAL + "datasets/air"));
	}

	@Test
	void theRoundTripIsLossless() {
		String logical = LOGICAL + "catalogs/c1";
		assertEquals(logical, iris.toLogical(iris.toPublic(logical)));
	}

	// --- foreign IRIs -------------------------------------------------------

	@Test
	void aForeignIriPassesThroughBothWays() {
		// The whole point of the structural rule: a publisher or vocabulary IRI must
		// never be claimed, and there is no list anyone could forget to add it to.
		String publisher = "https://www.umweltbundesamt.de/";
		String theme = "http://publications.europa.eu/resource/authority/data-theme/ENVI";

		assertEquals(publisher, iris.toPublic(publisher));
		assertEquals(publisher, iris.toLogical(publisher));
		assertEquals(theme, iris.toPublic(theme));
		assertEquals(theme, iris.toLogical(theme));
		assertFalse(iris.isOwned(theme));
	}

	@Test
	void aBaseThatMerelyStartsTheSameIsNotOurs() {
		// The trap: a raw startsWith on "https://example.org/dcat" claims
		// "https://example.org/dcatalog/air". Matching on the segment boundary does not.
		PublicIrisImpl narrow = new PublicIrisImpl("https://example.org/dcat");

		assertFalse(narrow.isOwned("https://example.org/dcatalog/air"));
		assertEquals("https://example.org/dcatalog/air", narrow.toLogical("https://example.org/dcatalog/air"));
		assertTrue(narrow.isOwned("https://example.org/dcat/datasets/air"));
	}

	@Test
	void theBareBaseIsNotAnIdentity() {
		// The base names the collection space, not a resource; folding it to the
		// logical base would invent an identity for something that has none.
		assertFalse(iris.isOwned(PUBLIC));
		assertEquals(PUBLIC, iris.toLogical(PUBLIC));
	}

	// --- configuration ------------------------------------------------------

	@Test
	void aBaseWithoutATrailingSlashStillMatchesOnSegments() {
		PublicIrisImpl noSlash = new PublicIrisImpl("https://opendata.example.de/dcat/rest");
		assertEquals(LOGICAL + "datasets/air", noSlash.toLogical(PUBLIC + "datasets/air"));
	}

	@Test
	void anAdditionalOwnedBaseIsFoldedInbound() {
		// The migration case: data written under an old hostname arrives from a client.
		PublicIrisImpl migrating = new PublicIrisImpl(PUBLIC, "http://localhost:8085/dcat/rest/");

		assertEquals(LOGICAL + "datasets/air", migrating.toLogical("http://localhost:8085/dcat/rest/datasets/air"));
		// ...but it is never rendered back out under the legacy base.
		assertEquals(PUBLIC + "datasets/air", migrating.toPublic(LOGICAL + "datasets/air"));
	}

	@Test
	void nullIsNotAnIdentity() {
		assertEquals(null, iris.toPublic(null));
		assertEquals(null, iris.toLogical(null));
		assertFalse(iris.isOwned(null));
	}

	/**
	 * There is no default public base, so an unconfigured deployment arrives in one
	 * of three shapes — absent, empty, or an uninterpolated placeholder — and each
	 * has to fail activation: accepted, any of them renders a well-formed
	 * {@code about} pointing nowhere useful, on every single response, with nothing
	 * downstream able to notice. The three tests below take one shape each, and
	 * assert the message says which one it was.
	 */
	@Test
	void anAbsentPublicBaseIsReportedAsAbsent() {
		// Cast needed: an uncast null would bind to the PublicIrisConfig constructor.
		IllegalArgumentException absent = assertThrows(IllegalArgumentException.class,
				() -> new PublicIrisImpl((String) null));

		assertTrue(absent.getMessage().contains("absent"), absent.getMessage());
	}

	/**
	 * An empty value and an unset one are different faults with the same effect, and
	 * an operator cannot see which they have without being told. An empty
	 * {@code PUBLIC_BASE_URL} is substituted verbatim by the interpolation plugin and
	 * lands here; an unset one leaves the placeholder standing and lands on the test
	 * below. Reporting both as "required and has no default" sends the reader off to
	 * check a variable that is demonstrably set.
	 */
	@Test
	void anEmptyPublicBaseIsReportedAsEmptyRatherThanMissing() {
		for (String empty : new String[] { "", "   " }) {
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new PublicIrisImpl(empty));

			assertTrue(e.getMessage().contains("empty"), e.getMessage());
			assertTrue(e.getMessage().contains("PUBLIC_BASE_URL"), e.getMessage());
			assertFalse(e.getMessage().contains("absent"), e.getMessage());
		}
	}

	@Test
	void anUninterpolatedPlaceholderIsRefusedByName() {
		// The shipped container configuration reads the value from PUBLIC_BASE_URL. When
		// that variable is unset the placeholder can reach the component verbatim, which
		// would otherwise become the literal base of every rendered IRI.
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new PublicIrisImpl("$[env:PUBLIC_BASE_URL]"));

		assertTrue(e.getMessage().contains("PUBLIC_BASE_URL"), e.getMessage());
		assertTrue(e.getMessage().contains("placeholder"), e.getMessage());
	}

	@Test
	void aBaseWithoutASchemeIsRefused() {
		// Relative or scheme-less values yield IRIs that cannot be dereferenced, which is
		// the one thing the public base exists to guarantee.
		assertThrows(IllegalArgumentException.class, () -> new PublicIrisImpl("opendata.example.de/dcat/rest/"));
		assertThrows(IllegalArgumentException.class, () -> new PublicIrisImpl("/dcat/rest/"));
	}
}
