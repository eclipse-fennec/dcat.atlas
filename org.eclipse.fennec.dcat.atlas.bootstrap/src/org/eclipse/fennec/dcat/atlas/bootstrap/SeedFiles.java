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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;

/**
 * Reading one seed file.
 *
 * <p>
 * A seed file is the same XMI a client would send to the admin API, which is what makes
 * the folder writable by hand: copy what the portal served, or write it from the user
 * guide's example. Nothing here resolves a reference — a {@code dcat:dataset} pointing at
 * another resource stays a proxy, and the store resolves it against what it holds, the
 * same way it resolves one that arrived over HTTP.
 * </p>
 */
final class SeedFiles {

	private static final Map<Object, Object> LOAD_OPTIONS = Map.of(XMLResource.OPTION_ENCODING, "UTF-8");

	private SeedFiles() {
	}

	/**
	 * What {@code file} holds, or an {@link IllegalArgumentException} naming it.
	 *
	 * <p>
	 * One resource set per file, so neither a package registration nor a half-read
	 * resource from a broken file can reach the next one.
	 * </p>
	 */
	static EObject read(ResourceSetFactory resourceSetFactory, Path file) {
		ResourceSet resourceSet = resourceSetFactory.createResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI.createFileURI(file.toAbsolutePath().toString()));
		try {
			resource.load(LOAD_OPTIONS);
		} catch (IOException | RuntimeException unreadable) {
			throw new IllegalArgumentException(file + " could not be read as XMI: " + unreadable.getMessage(),
					unreadable);
		}
		List<EObject> roots = resource.getContents();
		if (roots.size() != 1) {
			throw new IllegalArgumentException(file + " holds " + roots.size()
					+ " root objects. A seed file holds exactly one resource, as a request body does.");
		}
		return roots.get(0);
	}
}
