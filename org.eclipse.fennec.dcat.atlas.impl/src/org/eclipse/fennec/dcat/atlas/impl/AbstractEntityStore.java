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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper.Store;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreHealth;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;

import rdf.IdentifiedResource;

/**
 * What every entity store does the same way: read one, read all, report an ETag,
 * and report readiness.
 * <p>
 * The five stores differ only in which collection they hold and which type comes
 * back, so the shared behaviour lives here and each service supplies its
 * collection. Keeping it in one place is what lets a change to the storage
 * contract — the move to XMI, logical identities — land once rather than five
 * times in near-identical copies.
 *
 * @param <T> the entity type this store holds
 */
abstract class AbstractEntityStore<T extends EObject> implements HealthCheck {

	protected final ResourceSetFactory resourceSetFactory;
	protected final Path root;
	/** Which collection this store holds; see {@link StoreLayout}. */
	protected final String collection;
	/**
	 * Whether writes through this store are checked against the model's constraints
	 * ({@link StoreConfig#validateOnWrite()}). Held here rather than in the admin
	 * subclasses because {@link #store()} is what opens the session, and a read-only
	 * service never reaches the write path where it is consulted.
	 */
	protected final boolean validateOnWrite;

	protected AbstractEntityStore(ResourceSetFactory resourceSetFactory, Path root, String collection,
			boolean validateOnWrite) {
		this.resourceSetFactory = resourceSetFactory;
		this.root = root;
		this.collection = collection;
		this.validateOnWrite = validateOnWrite;
		DcatHelper.prepare(root);
	}

	/** A store session; everything read through one resolves against everything else. */
	protected Store store() {
		return DcatHelper.open(resourceSetFactory, root, validateOnWrite, writeValidation());
	}

	/**
	 * The SHACL validation service this store enforces with, or {@code null} for none.
	 * <p>
	 * Overridden by the admin subclasses, which hold the DS reference; a read-only service
	 * never reaches the write path, so the base answers {@code null}. The reference is
	 * declared per subclass rather than here because DS binds fields on the component
	 * class, matching how {@code graphService} is already declared.
	 */
	protected DcatValidationService writeValidation() {
		return null;
	}

	protected Optional<T> getEntity(String id) {
		return store().get(collection, id);
	}

	protected List<T> listEntities() {
		return store().list(collection);
	}

	public Optional<String> etag(String id) {
		return DcatHelper.etag(root, collection, id);
	}

	/**
	 * The id {@code about} names in this collection, or a fresh one when it names
	 * nothing at all (D2/FR-3).
	 * <p>
	 * An {@code about} that is not ours is <em>refused</em>
	 * ({@link org.eclipse.fennec.dcat.atlas.api.ForeignIdentityException}) rather than
	 * quietly replaced by a minted id: filing an entity under a segment carved out of
	 * somebody else's URL would claim a resource we do not own, and minting instead told
	 * the caller nothing. The rule is {@link DcatIds#idForWrite}, shared with the REST
	 * adapter so both doors answer the same way.
	 */
	protected String idOrMint(T entity) {
		String about = entity instanceof IdentifiedResource identified ? identified.getAbout() : null;
		return DcatIds.idForWrite(collection, about);
	}

	// --- F-25 readiness -----------------------------------------------------

	/**
	 * Reports whether this store can serve (F-25). CRITICAL rather than WARN: an
	 * unusable store directory is a misconfiguration that no retry fixes, and the
	 * portal should be taken out of rotation.
	 */
	@Override
	public Result execute() {
		Path directory = StoreLayout.directory(root, collection);
		FormattingResultLog log = new FormattingResultLog();
		if (StoreHealth.ready(directory)) {
			log.info("{}", StoreHealth.detail(directory));
		} else {
			log.critical("{}", StoreHealth.detail(directory));
		}
		return new Result(log);
	}
}
