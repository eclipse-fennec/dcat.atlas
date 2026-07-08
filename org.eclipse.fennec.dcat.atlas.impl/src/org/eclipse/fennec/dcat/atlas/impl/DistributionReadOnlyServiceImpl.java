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

import org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.DcatPackage;
import dcat.Distribution;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
@Component(name = "DistributionReadOnlyService", service = DistributionReadOnlyService.class)
@Designate(ocd = StoreConfig.class)
public class DistributionReadOnlyServiceImpl implements DistributionReadOnlyService {
	
	protected final ResourceSetFactory resourceSetFactory;
	protected final Path directory;

	@Activate
	public DistributionReadOnlyServiceImpl(@Reference ResourceSetFactory resourceSetFactory, StoreConfig config) {
		this(resourceSetFactory, Path.of(config.directory()));
	}

	/** Package-visible for the admin subclass and tests. */
	DistributionReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, Path directory) {
		this.resourceSetFactory = resourceSetFactory;
		this.directory = directory;
		try {
			Files.createDirectories(directory);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create catalog storage directory " + directory, e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService#getDistribution(java.lang.String)
	 */
	@Override
	public Optional<Distribution> getDistribution(String id) {
		return DcatHelper.get(resourceSetFactory, directory, id, DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService#listDistributions()
	 */
	@Override
	public List<Distribution> listDistributions() {
		return DcatHelper.list(resourceSetFactory, directory, DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION);
	}

}
