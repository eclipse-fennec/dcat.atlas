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
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;

import rdf.IdentifiedResource;

/**
 * File-based persistence for the DCAT-AP stores: one XMI file per resource, the
 * entity itself as the file's sole root object.
 *
 * <h2>Sessions, and why they exist</h2>
 *
 * Links between entities are EMF cross-resource references, so two entities can
 * only be linked while they are loaded in the <em>same</em> {@link ResourceSet} —
 * that is what lets EMF write {@code href="http://dcat.atlas/datasets/air#/"}
 * rather than inlining a copy. {@link #open} hands out a {@link Store} holding one
 * such resource set; everything read through it resolves against everything else.
 * <p>
 * A store is scoped to one operation and is not thread-safe, matching EMF's own
 * guarantees — see {@code StoreResourceSets} for why that is preferred over one
 * shared set.
 */
public final class DcatHelper {

	private DcatHelper() {
	}

	/** Opens a store session rooted at {@code root}. */
	public static Store open(ResourceSetFactory resourceSetFactory, Path root) {
		return new Store(StoreResourceSets.create(resourceSetFactory, root), root);
	}

	/**
	 * Creates the store subdirectories. Called on service activation so a fresh
	 * deployment does not fail its first write.
	 */
	public static void prepare(Path root) {
		for (String collection : StoreLayout.COLLECTIONS) {
			Path directory = StoreLayout.directory(root, collection);
			try {
				Files.createDirectories(directory);
			} catch (IOException e) {
				throw new UncheckedIOException("Could not create store directory " + directory, e);
			}
		}
	}

	/** One operation's view of the store. */
	public static final class Store {

		private final ResourceSet resourceSet;
		private final Path root;

		private Store(ResourceSet resourceSet, Path root) {
			this.resourceSet = resourceSet;
			this.root = root;
		}

		/** The object stored under {@code id}, or empty if there is no such file. */
		@SuppressWarnings("unchecked")
		public <T extends EObject> Optional<T> get(String collection, String id) {
			if (!Files.isRegularFile(StoreLayout.file(root, collection, id))) {
				return Optional.empty();
			}
			Resource resource = resourceSet.getResource(StoreResourceSets.resourceUri(collection, id), true);
			return resource.getContents().isEmpty() ? Optional.empty()
					: Optional.of((T) resource.getContents().get(0));
		}

		/** Every object in {@code collection}, ordered by id. */
		public <T extends EObject> List<T> list(String collection) {
			Path directory = StoreLayout.directory(root, collection);
			if (!Files.isDirectory(directory)) {
				return List.of();
			}
			try (Stream<Path> files = Files.list(directory)) {
				List<String> ids = files.filter(Files::isRegularFile) //
						.map(p -> p.getFileName().toString()) //
						.sorted() //
						.toList();
				List<T> result = new ArrayList<>(ids.size());
				for (String id : ids) {
					this.<T>get(collection, id).ifPresent(result::add);
				}
				return result;
			} catch (IOException e) {
				throw new UncheckedIOException("Could not list " + collection + " in " + directory, e);
			}
		}

		/**
		 * Stores {@code object} under {@code id}, creating or replacing, and stamps it
		 * with its logical identity.
		 * <p>
		 * The identity is minted here rather than taken from the caller, because it is
		 * the store that decides what a resource is called. Accepting an {@code about}
		 * from the request is how the writer's hostname used to end up frozen into the
		 * file.
		 */
		public <T extends EObject> T put(String collection, String id, T object) {
			StoreLayout.requireSafeId(id);
			if (object instanceof IdentifiedResource identified) {
				identified.setAbout(StoreLayout.logicalIri(collection, id));
			}
			// Refuse a link to an identity of ours that is not there, before anything is
			// written. Every write funnels through here, so this is the one place the
			// invariant has to hold — and it is the write-side half of the rule
			// References.detach enforces on delete.
			References.requireResolvable(this, object);
			URI uri = StoreResourceSets.resourceUri(collection, id);
			Resource resource = resourceSet.getResource(uri, false);
			if (resource == null) {
				resource = resourceSet.createResource(uri);
			}
			resource.getContents().clear();
			resource.getContents().add(object);
			save(resource);
			return object;
		}

		/** Re-saves an object previously {@link #get}. */
		public void save(EObject object) {
			Resource resource = object.eResource();
			if (resource == null) {
				throw new IllegalArgumentException(
						"Cannot save a " + object.eClass().getName() + " that is not in the store");
			}
			save(resource);
		}

		/** Removes the object stored under {@code id}; returns whether a file existed. */
		public boolean delete(String collection, String id) {
			URI uri = StoreResourceSets.resourceUri(collection, id);
			Resource loaded = resourceSet.getResource(uri, false);
			if (loaded != null) {
				resourceSet.getResources().remove(loaded);
			}
			try {
				return Files.deleteIfExists(StoreLayout.file(root, collection, id));
			} catch (IOException e) {
				throw new UncheckedIOException("Could not delete " + collection + "/" + id, e);
			}
		}

		private void save(Resource resource) {
			try {
				resource.save(StoreResourceSets.saveOptions());
			} catch (IOException e) {
				throw new UncheckedIOException("Could not store " + resource.getURI(), e);
			}
		}
	}

	// --- ETag ---------------------------------------------------------------

	/**
	 * A strong entity-tag validator for the object stored under {@code id}: a
	 * SHA-256 (hex) digest of the stored file's bytes, or empty if there is no such
	 * file. Computed over the persisted representation, so it changes iff the stored
	 * state changes — which is what conditional requests need (F-16).
	 * <p>
	 * Note it digests the <em>stored</em> (logical) bytes, not what a client is
	 * served. Two deployments rendering different public IRIs therefore agree on the
	 * ETag, which is correct: they are serving the same resource at the same version.
	 */
	public static Optional<String> etag(Path root, String collection, String id) {
		Path file = StoreLayout.file(root, collection, id);
		if (!Files.isRegularFile(file)) {
			return Optional.empty();
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return Optional.of(HexFormat.of().formatHex(digest.digest(Files.readAllBytes(file))));
		} catch (IOException e) {
			throw new UncheckedIOException("Could not compute ETag for " + collection + "/" + id, e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is required but unavailable", e);
		}
	}
}
