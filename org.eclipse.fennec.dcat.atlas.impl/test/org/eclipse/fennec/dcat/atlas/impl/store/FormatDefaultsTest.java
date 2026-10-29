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
package org.eclipse.fennec.dcat.atlas.impl.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import dcat.Dataset;
import dcat.DcatFactory;
import dcat.Distribution;

/** The media-type → EU file-type completion of issue #68. */
class FormatDefaultsTest {

	private static final String GEOJSON = FormatDefaults.EU_FILE_TYPES + "GEOJSON";
	private static final String CSV = FormatDefaults.EU_FILE_TYPES + "CSV";

	@Test
	void theIanaIriFormIsRecognised() {
		// What DCAT-AP says dcat:mediaType should look like, and what the WOGE data carries.
		assertEquals(Optional.of(GEOJSON),
				FormatDefaults.formatFor(FormatDefaults.IANA_MEDIA_TYPES + "application/geo+json"));
	}

	@Test
	void theBareMediaTypeIsRecognisedToo() {
		assertEquals(Optional.of(CSV), FormatDefaults.formatFor("text/csv"));
	}

	@Test
	void parametersAndCaseAreIgnored() {
		assertEquals(Optional.of(CSV), FormatDefaults.formatFor("Text/CSV; charset=utf-8"));
		assertEquals(Optional.of(CSV), FormatDefaults.formatFor("  text/csv  "));
	}

	@Test
	void anUnknownMediaTypeYieldsNothing() {
		// Guessing would be worse than leaving the field for the writer or a validator.
		assertEquals(Optional.empty(), FormatDefaults.formatFor("application/x-made-up"));
		assertEquals(Optional.empty(), FormatDefaults.formatFor(null));
		assertEquals(Optional.empty(), FormatDefaults.formatFor(""));
	}

	@Test
	void aMissingFormatIsFilledFromTheMediaType() {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setMediaType(FormatDefaults.IANA_MEDIA_TYPES + "application/geo+json");

		assertTrue(FormatDefaults.apply(distribution));
		assertEquals(GEOJSON, distribution.getFormat());
	}

	@Test
	void anExplicitFormatIsNeverTouched() {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setMediaType("text/csv");
		distribution.setFormat("https://example.org/my-own-format");

		assertFalse(FormatDefaults.apply(distribution));
		assertEquals("https://example.org/my-own-format", distribution.getFormat());
	}

	@Test
	void noMediaTypeMeansNoFormat() {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();

		assertFalse(FormatDefaults.apply(distribution));
		assertNull(distribution.getFormat());
	}

	@Test
	void everyDistributionOfADatasetIsCompleted() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		Distribution geo = DcatFactory.eINSTANCE.createDistribution();
		geo.setMediaType("application/geo+json");
		Distribution csv = DcatFactory.eINSTANCE.createDistribution();
		csv.setMediaType("text/csv");
		dataset.getDistribution().add(geo);
		dataset.getDistribution().add(csv);

		assertEquals(2, FormatDefaults.apply(dataset));

		assertEquals(GEOJSON, geo.getFormat());
		assertEquals(CSV, csv.getFormat());
	}

	@Test
	void aRootWithoutDistributionsIsLeftAlone() {
		// Any EObject may reach the write floor; only Datasets (and their subtypes) have
		// Distributions, everything else must pass through untouched.
		assertEquals(0, FormatDefaults.apply(DcatFactory.eINSTANCE.createDataService()));
	}
}
