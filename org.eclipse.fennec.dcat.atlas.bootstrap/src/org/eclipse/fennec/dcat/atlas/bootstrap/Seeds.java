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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.ForeignIdentityException;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import rdf.IdentifiedResource;

/**
 * What a folder of seed files means, and what to do with it.
 *
 * <h2>Reading everything before writing anything</h2>
 *
 * A folder is understood in full first and stored afterwards, so a folder with one
 * unusable file in it stores nothing instead of half of itself. There is no rollback
 * behind that: a write is a commit in a git repository, and undoing one that another
 * system may already have linked into would be worse than leaving it. What makes a
 * retry safe instead is {@link #apply}'s rule — a resource that is there is left alone —
 * so a corrected folder on the next start seeds exactly what the failed run did not.
 */
final class Seeds {

	/**
	 * The order the collections are seeded in.
	 *
	 * <p>
	 * Members before the catalogs that point at them: a write naming a member that is not
	 * stored yet is refused for referential integrity, so without an order a folder would
	 * only work if the operator happened to name the files in one the loader never
	 * promised. Within a collection the folder's own order is kept, which is what orders
	 * a sub-catalog before its parent — the one case left for the file names to settle.
	 * </p>
	 */
	private static final List<String> COLLECTION_ORDER = List.of(DcatIds.DATASETS, DcatIds.DATASET_SERIES,
			DcatIds.DATA_SERVICES, DcatIds.CATALOGS);

	private Seeds() {
	}

	/**
	 * Reads every file and says what each one seeds, or refuses the whole folder.
	 *
	 * @param files what the folder holds, in the order it was listed
	 * @param read  how a file becomes a resource; {@code null} for a file holding none
	 */
	static List<Seed> plan(List<Path> files, Function<Path, EObject> read) {
		Map<String, Path> claimed = new HashMap<>();
		Map<String, List<Seed>> byCollection = new LinkedHashMap<>();
		for (Path file : files) {
			Seed seed = seed(file, read.apply(file));
			Path alreadyClaiming = claimed.putIfAbsent(seed.collection() + "/" + seed.id(), file);
			if (alreadyClaiming != null) {
				throw refuse(file, "seeds " + seed.collection() + "/" + seed.id() + ", which "
						+ alreadyClaiming + " already seeds. Two files cannot be the same resource.");
			}
			byCollection.computeIfAbsent(seed.collection(), collection -> new ArrayList<>()).add(seed);
		}
		List<Seed> ordered = new ArrayList<>();
		for (String collection : COLLECTION_ORDER) {
			ordered.addAll(byCollection.getOrDefault(collection, List.of()));
		}
		return ordered;
	}

	/**
	 * Seeds what is missing and leaves alone what is not.
	 *
	 * <p>
	 * <strong>Create-if-absent, never replace.</strong> A write replaces the stored
	 * resource — only its identity, its {@code managedBy} and its records survive — so a
	 * loader that re-seeded on every start would drop every membership the generating
	 * systems had added since, and the catalog would come back empty. That is the one
	 * thing it exists to prevent, so a seed is an offer rather than an assertion: the
	 * file says what the portal should start out holding, not what it should hold.
	 * </p>
	 *
	 * @return the seeds that were written, in the order they were written
	 */
	static List<Seed> apply(List<Seed> plan, Map<String, SeedTarget> targets) {
		List<Seed> written = new ArrayList<>();
		for (Seed seed : plan) {
			SeedTarget target = targets.get(seed.collection());
			if (target == null) {
				throw new IllegalStateException("No way to seed the " + seed.collection() + " collection.");
			}
			if (target.holds(seed.id())) {
				continue;
			}
			target.write(seed.resource());
			written.add(seed);
		}
		return written;
	}

	private static Seed seed(Path file, EObject resource) {
		if (resource == null) {
			throw refuse(file, "holds no resource.");
		}
		String collection = collectionOf(resource);
		if (collection == null) {
			throw refuse(file, "holds a " + resource.eClass().getName() + ". A seed is a Catalog, a Dataset, a "
					+ "DatasetSeries or a DataService: those are the collections the portal stores in their own "
					+ "right, and a Distribution belongs inside its Dataset.");
		}
		String about = ((IdentifiedResource) resource).getAbout();
		if (about == null || about.isBlank()) {
			throw refuse(file, "has no about. A seed has to state the identity the generating systems will "
					+ "address it by; an absent one is minted fresh, which would differ per deployment.");
		}
		try {
			return new Seed(file, collection, DcatIds.idForWrite(collection, about), resource);
		} catch (ForeignIdentityException notOurs) {
			throw refuse(file, "cannot be seeded: " + notOurs.getMessage());
		}
	}

	/**
	 * Which collection a resource is stored in, or {@code null} for one that is not stored
	 * in its own right.
	 *
	 * <p>
	 * Catalog and DatasetSeries are both Datasets in this model, so they are asked about
	 * first; the other way round every one of them would be filed as a dataset, and the
	 * catalog the deployment is waiting for would be sitting in the wrong collection under
	 * an id nothing dereferences.
	 * </p>
	 */
	private static String collectionOf(EObject resource) {
		if (resource instanceof Catalog) {
			return DcatIds.CATALOGS;
		}
		if (resource instanceof DatasetSeries) {
			return DcatIds.DATASET_SERIES;
		}
		if (resource instanceof DataService) {
			return DcatIds.DATA_SERVICES;
		}
		if (resource instanceof Dataset) {
			return DcatIds.DATASETS;
		}
		return null;
	}

	private static IllegalArgumentException refuse(Path file, String because) {
		return new IllegalArgumentException(file + " " + because);
	}
}
