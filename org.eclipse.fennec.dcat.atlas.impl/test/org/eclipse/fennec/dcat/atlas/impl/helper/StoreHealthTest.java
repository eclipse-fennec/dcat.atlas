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
package org.eclipse.fennec.dcat.atlas.impl.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * F-25 store readiness. The cases that matter operationally are the two that are
 * deliberately <em>not</em> failures — a store that does not exist yet, and one that is
 * mounted read-only — because failing either would take a working portal out of service.
 */
public class StoreHealthTest {

	@TempDir
	Path root;

	@Test
	void existingReadableDirectoryIsReady() {
		assertTrue(StoreHealth.ready(root));
		assertTrue(StoreHealth.detail(root).contains("readable and writable"), StoreHealth.detail(root));
	}

	@Test
	void missingDirectoryUnderAWritableParentIsReady() {
		// Stores are created lazily on first write, so a fresh install has no store
		// directories at all and must still become ready.
		Path store = root.resolve("catalogs");
		assertTrue(StoreHealth.ready(store));
		assertTrue(StoreHealth.detail(store).contains("will be created on first write"), StoreHealth.detail(store));
	}

	@Test
	void missingDirectorySeveralLevelsDownIsReady() {
		// e.g. /data/dcat/catalogs where only /data exists.
		Path store = root.resolve("dcat").resolve("nested").resolve("catalogs");
		assertTrue(StoreHealth.ready(store));
	}

	@Test
	void missingDirectoryWithNoWritableParentIsNotReady() {
		Path store = Path.of("/proc/definitely-not-writable/catalogs");
		assertFalse(StoreHealth.ready(store));
		assertTrue(StoreHealth.detail(store).contains("cannot be created"), StoreHealth.detail(store));
	}

	@Test
	void pathThatIsAFileIsNotReady() throws IOException {
		Path file = Files.createFile(root.resolve("not-a-dir"));
		assertFalse(StoreHealth.ready(file));
		assertTrue(StoreHealth.detail(file).contains("not a directory"), StoreHealth.detail(file));
	}

	@Test
	void nullDirectoryIsNotReady() {
		assertFalse(StoreHealth.ready(null));
		assertTrue(StoreHealth.detail(null).contains("no store directory configured"));
	}

}
