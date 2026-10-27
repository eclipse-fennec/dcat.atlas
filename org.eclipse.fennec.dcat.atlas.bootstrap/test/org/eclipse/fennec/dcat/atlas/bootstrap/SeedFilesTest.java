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
package org.eclipse.fennec.dcat.atlas.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Catalog;

/**
 * Reading one seed file.
 *
 * <p>
 * A seed is written by hand, so what matters here is that the same XMI a client would
 * {@code PUT} is what the folder takes, and that a file which is not that is refused by
 * name rather than arriving at the store as something odd.
 * </p>
 */
class SeedFilesTest {

	@TempDir
	Path folder;

	private static final String CATALOG = """
			<?xml version="1.0" encoding="UTF-8"?>
			<dcat:Catalog xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
			         xmi:version="2.0" about="http://dcat.atlas/catalogs/example">
			  <title lang="en" value="Example Catalog"/>
			  <description lang="en" value="The catalog the generating systems publish into"/>
			  <publisher about="https://example.de/organisation/uba">
			    <name lang="en" value="Umweltbundesamt"/>
			  </publisher>
			</dcat:Catalog>""";

	@Test
	void theXmiAClientWouldPutIsWhatTheFolderTakes() throws IOException {
		EObject read = SeedFiles.read(TestResourceSets.factory(), file("catalog.xmi", CATALOG));

		Catalog catalog = assertInstanceOf(Catalog.class, read);
		assertEquals("http://dcat.atlas/catalogs/example", catalog.getAbout());
		assertEquals("Example Catalog", catalog.getTitle().get(0).getValue());
	}

	/** A reference to another seed is a proxy, left unresolved for the store to resolve. */
	@Test
	void aReferenceToAnotherResourceSurvivesTheRead() throws IOException {
		String withMember = CATALOG.replace("</dcat:Catalog>",
				"  <dataset href=\"http://dcat.atlas/datasets/air#/\"/>\n</dcat:Catalog>");

		Catalog catalog = (Catalog) SeedFiles.read(TestResourceSets.factory(), file("catalog.xmi", withMember));

		assertEquals(1, catalog.getDataset().size());
	}

	@Test
	void aFileThatIsNotXmiIsRefusedByName() throws IOException {
		Path file = file("broken.xmi", "this is not a catalog");

		IllegalArgumentException refused = assertThrows(IllegalArgumentException.class,
				() -> SeedFiles.read(TestResourceSets.factory(), file));

		assertTrue(refused.getMessage().contains("broken.xmi"), refused.getMessage());
	}

	@Test
	void anEmptyFileIsRefusedByName() throws IOException {
		Path file = file("empty.xmi", "");

		IllegalArgumentException refused = assertThrows(IllegalArgumentException.class,
				() -> SeedFiles.read(TestResourceSets.factory(), file));

		assertTrue(refused.getMessage().contains("empty.xmi"), refused.getMessage());
	}

	/** One file is one resource; two roots would leave it ambiguous what was being seeded. */
	@Test
	void aFileWithMoreThanOneRootIsRefused() throws IOException {
		Path file = file("two.xmi", """
				<?xml version="1.0" encoding="UTF-8"?>
				<xmi:XMI xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0">
				  <dcat:Dataset about="http://dcat.atlas/datasets/one"/>
				  <dcat:Dataset about="http://dcat.atlas/datasets/two"/>
				</xmi:XMI>""");

		IllegalArgumentException refused = assertThrows(IllegalArgumentException.class,
				() -> SeedFiles.read(TestResourceSets.factory(), file));

		assertTrue(refused.getMessage().contains("two.xmi"), refused.getMessage());
	}

	private Path file(String name, String content) throws IOException {
		return Files.writeString(folder.resolve(name), content);
	}
}
