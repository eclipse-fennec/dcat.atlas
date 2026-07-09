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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;

import rdf.RDFRoot;
import rdf.RdfFactory;

/**
 * Shared file-based persistence for the DCAT-AP admin/read services: each object
 * is stored as a single RDF/XML file {@code <directory>/<id>.rdf}, wrapped in an
 * {@code <rdf:RDF>} document so the model's own EMF resource factory can
 * round-trip it. Generic over the DCAT-AP document-root feature (e.g.
 * {@code DCATAP_ROOT__CATALOG}) so the same code serves every entity type.
 */
public final class DcatHelper {

	private DcatHelper() {
	}


	public static final String RDF_EXTENSION = ".rdf";

	private static final Map<Object, Object> RESOURCE_OPTIONS = Map.of(//
			XMLResource.OPTION_ENCODING, "UTF-8", //
			XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);

	// --- CRUD primitives ---------------------------------------------------

	/** Loads the object stored under {@code id}, or empty if there is no such file. */
	public static <T extends EObject> Optional<T> get(ResourceSetFactory resourceSetFactory, Path directory, String id,
			EReference rootFeature) {
		Path file = fileFor(directory, id);
		if (!Files.isRegularFile(file)) {
			return Optional.empty();
		}
		return Optional.of(read(resourceSetFactory.createResourceSet(), file, rootFeature));
	}

	/** Loads every object stored in {@code directory}. */
	public static <T extends EObject> List<T> list(ResourceSetFactory resourceSetFactory, Path directory,
			EReference rootFeature) {
		if (!Files.isDirectory(directory)) {
			return List.of();
		}
		try (Stream<Path> files = Files.list(directory)) {
			List<T> result = new ArrayList<>();
			files.filter(Files::isRegularFile) //
					.filter(p -> p.getFileName().toString().endsWith(RDF_EXTENSION)) //
					.sorted() //
					.forEach(p -> result.add(read(resourceSetFactory.createResourceSet(), p, rootFeature)));
			return result;
		} catch (IOException e) {
			throw new UncheckedIOException("Could not list objects in " + directory, e);
		}
	}

	/** Stores {@code object} under {@code id} (create or replace). */
	public static void write(ResourceSetFactory resourceSetFactory, Path directory, String id, EReference rootFeature,
			EObject object) {
		Resource resource = resourceSetFactory.createResourceSet()
				.createResource(URI.createFileURI(fileFor(directory, id).toAbsolutePath().toString()));

		RDFRoot rdfRoot = RdfFactory.eINSTANCE.createRDFRoot();
		resource.getContents().add(rdfRoot);
		AnyType anyType = XMLTypeFactory.eINSTANCE.createAnyType();
		rdfRoot.getRDF().add(anyType);
		anyType.eSet(rootFeature, ECollections.singletonEList(EcoreUtil.copy(object)));

		try {
			resource.save(RESOURCE_OPTIONS);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not store object " + id, e);
		}
	}

	/**
	 * A strong entity-tag validator for the object stored under {@code id}: a
	 * SHA-256 (hex) digest of the stored file's bytes, or empty if there is no such
	 * file. Because it is computed over the persisted representation it is stable
	 * across serialization formats and changes iff the stored state changes — which
	 * is what conditional requests (ETag / If-Match / If-None-Match, F-16) need.
	 */
	public static Optional<String> etag(Path directory, String id) {
		Path file = fileFor(directory, id);
		if (!Files.isRegularFile(file)) {
			return Optional.empty();
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return Optional.of(HexFormat.of().formatHex(digest.digest(Files.readAllBytes(file))));
		} catch (IOException e) {
			throw new UncheckedIOException("Could not compute ETag for " + id, e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is required but unavailable", e);
		}
	}

	/** Removes the object stored under {@code id}; returns whether a file existed. */
	public static boolean delete(Path directory, String id) {
		try {
			return Files.deleteIfExists(fileFor(directory, id));
		} catch (IOException e) {
			throw new UncheckedIOException("Could not delete object " + id, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends EObject> T read(ResourceSet resourceSet, Path file, EReference rootFeature) {
		Resource resource = resourceSet.createResource(URI.createFileURI(file.toAbsolutePath().toString()));
		try {
			resource.load(RESOURCE_OPTIONS);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read object file " + file, e);
		}
		RDFRoot rdfRoot = (RDFRoot) resource.getContents().get(0);
		AnyType anyType = rdfRoot.getRDF().get(0);
		List<T> objects = (List<T>) anyType.eGet(rootFeature);
		return objects.get(0);
	}

	// --- id / path helpers -------------------------------------------------

	/** Derives the storage id from an object's {@code rdf:about} URI, or {@code null}. */
	public static String idOf(String about) {
		if (about == null || about.isBlank()) {
			return null;
		}
		int slash = about.lastIndexOf('/');
		String candidate = slash >= 0 ? about.substring(slash + 1) : about;
		return candidate.isBlank() ? null : candidate;
	}

	/** Resolves (and validates) the storage file for {@code id}. */
	public static Path fileFor(Path directory, String id) {
		return directory.resolve(requireSafeId(id) + RDF_EXTENSION);
	}

	/** Ensures a client-supplied id cannot escape the storage directory. */
	public static String requireSafeId(String id) {
		if (id == null || id.isBlank() || id.contains("/") || id.contains("\\") || id.contains("..")) {
			throw new IllegalArgumentException("Illegal id: " + id);
		}
		return id;
	}
}
