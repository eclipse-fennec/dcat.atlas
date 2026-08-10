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

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Readiness of one file-backed store directory (F-25).
 * <p>
 * Each entity has its own store directory (the {@code directory} property of the
 * per-entity component configuration), so readiness is reported per store rather than
 * once for the whole portal.
 * <p>
 * Three deliberate decisions about what counts as ready:
 * <ul>
 * <li><b>A missing directory is ready if it can still be created.</b> Stores are created
 * lazily — {@link DcatHelper#write} does not mkdir, EMF's file URI handler creates the
 * parent on first save. So a freshly installed portal has no store directories at all,
 * and failing readiness there would mean it never starts serving.</li>
 * <li><b>Not writable is still ready.</b> A read-only mount is a legitimate deployment
 * for a read-only runtime; reads work, so traffic should be routed. It is reported in the
 * detail text.</li>
 * <li><b>A path that exists but is not a directory, or is unreadable, is not ready.</b>
 * Those are misconfigurations that no amount of waiting fixes.</li>
 * </ul>
 */
public final class StoreHealth {

	private StoreHealth() {
	}

	/** Whether {@code directory} can serve this store: it is usable now, or creatable on first write. */
	public static boolean ready(Path directory) {
		if (directory == null) {
			return false;
		}
		if (Files.exists(directory)) {
			return Files.isDirectory(directory) && Files.isReadable(directory);
		}
		return isCreatable(directory);
	}

	/** What was checked and what was found, for an operator reading the readiness response. */
	public static String detail(Path directory) {
		if (directory == null) {
			return "no store directory configured";
		}
		if (!Files.exists(directory)) {
			return isCreatable(directory)
					? "store directory does not exist yet, will be created on first write: " + directory
					: "store directory does not exist and cannot be created (no writable parent): " + directory;
		}
		if (!Files.isDirectory(directory)) {
			return "store path exists but is not a directory: " + directory;
		}
		if (!Files.isReadable(directory)) {
			return "store directory is not readable: " + directory;
		}
		return Files.isWritable(directory) ? "store directory readable and writable: " + directory
				: "store directory readable, NOT writable (reads only): " + directory;
	}

	/**
	 * Whether an absent directory could be created: the nearest existing ancestor must be
	 * a writable directory. Walking up matters because a configured store may be several
	 * levels below anything that exists yet, e.g. {@code /data/dcat/catalogs} under an
	 * empty {@code /data}.
	 */
	private static boolean isCreatable(Path directory) {
		for (Path parent = directory.getParent(); parent != null; parent = parent.getParent()) {
			if (Files.exists(parent)) {
				return Files.isDirectory(parent) && Files.isWritable(parent);
			}
		}
		return false;
	}

}
