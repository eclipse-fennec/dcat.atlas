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

import org.junit.jupiter.api.Test;

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
	 * There is no default public base, so these are the three shapes an
	 * unconfigured deployment actually arrives in. Each has to fail activation:
	 * accepted, any of them renders a well-formed {@code about} pointing nowhere
	 * useful, on every single response, with nothing downstream able to notice.
	 */
	@Test
	void anUnconfiguredPublicBaseIsRefused() {
		// Cast needed: an uncast null would bind to the PublicIrisConfig constructor.
		assertThrows(IllegalArgumentException.class, () -> new PublicIrisImpl((String) null));
		assertThrows(IllegalArgumentException.class, () -> new PublicIrisImpl(""));
		assertThrows(IllegalArgumentException.class, () -> new PublicIrisImpl("   "));
	}

	@Test
	void anUninterpolatedPlaceholderIsRefusedByName() {
		// The shipped container configuration reads the value from PUBLIC_BASE_URL. When
		// that variable is unset the placeholder can reach the component verbatim, which
		// would otherwise become the literal base of every rendered IRI.
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new PublicIrisImpl("$[env:PUBLIC_BASE_URL]"));

		assertTrue(e.getMessage().contains("PUBLIC_BASE_URL"), e.getMessage());
	}

	@Test
	void aBaseWithoutASchemeIsRefused() {
		// Relative or scheme-less values yield IRIs that cannot be dereferenced, which is
		// the one thing the public base exists to guarantee.
		assertThrows(IllegalArgumentException.class, () -> new PublicIrisImpl("opendata.example.de/dcat/rest/"));
		assertThrows(IllegalArgumentException.class, () -> new PublicIrisImpl("/dcat/rest/"));
	}
}
