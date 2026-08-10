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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DcatHealthContributor;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreHealth;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Catalog;
import dcat.DcatPackage;

/**
 * File-backed, read-only view of the catalog store (visitor / machine-consumer
 * read side). All persistence goes through {@link DcatHelper}.
 * <p>
 * {@code CatalogAdminServiceImpl} extends this class to inherit the read
 * operations and add the write operations, so the two services share one
 * implementation of {@code getCatalog}/{@code listCatalogs}. The storage
 * location is exposed to that subclass through the {@code protected} fields.
 */
@Component(name = "CatalogReadOnlyService", service = { CatalogReadOnlyService.class, DcatHealthContributor.class })
@Designate(ocd = StoreConfig.class)
public class CatalogReadOnlyServiceImpl implements CatalogReadOnlyService, DcatHealthContributor {

	protected final ResourceSetFactory resourceSetFactory;
	protected final Path directory;

	@Activate
	public CatalogReadOnlyServiceImpl(@Reference ResourceSetFactory resourceSetFactory, StoreConfig config) {
		this(resourceSetFactory, Path.of(config.directory()));
	}

	/** Package-visible for the admin subclass and tests. */
	CatalogReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, Path directory) {
		this.resourceSetFactory = resourceSetFactory;
		this.directory = directory;
		try {
			Files.createDirectories(directory);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create catalog storage directory " + directory, e);
		}
	}

	@Override
	public Optional<Catalog> getCatalog(String id) {
		return DcatHelper.get(resourceSetFactory, directory, id, DcatPackage.Literals.DCATAP_ROOT__CATALOG);
	}

	@Override
	public List<Catalog> listCatalogs() {
		return DcatHelper.list(resourceSetFactory, directory, DcatPackage.Literals.DCATAP_ROOT__CATALOG);
	}

	@Override
	public Optional<String> etag(String id) {
		return DcatHelper.etag(directory, id);
	}

	// --- F-25 readiness -----------------------------------------------------

	@Override
	public String name() {
		return "store:catalogs";
	}

	@Override
	public boolean ready() {
		return StoreHealth.ready(directory);
	}

	@Override
	public String detail() {
		return StoreHealth.detail(directory);
	}

}
