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
import java.util.UUID;

import org.eclipse.fennec.dcat.atlas.api.DataServiceAdminService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.DataService;
import dcat.DcatPackage;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
@Component(name = "DataServiceAdminService", service = DataServiceAdminService.class)
@Designate(ocd = StoreConfig.class)
public class DataServiceAdminServiceImpl extends DataServiceReadOnlyServiceImpl implements DataServiceAdminService {

	@Activate
	public DataServiceAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory, StoreConfig config) {
		super(resourceSetFactory, config);
	}

	/** Package-visible for tests. */
	DataServiceAdminServiceImpl(ResourceSetFactory resourceSetFactory, Path directory) {
		super(resourceSetFactory, directory);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DataServiceAdminService#upsertDataService(dcat.DataService)
	 */
	@Override
	public DataService upsertDataService(DataService service) {
		String id = DcatHelper.idOf(service.getAbout());
		if (id == null) {
			// Mint an id when the client supplied no about (D2/FR-3).
			id = UUID.randomUUID().toString();
		}
		DcatHelper.write(resourceSetFactory, directory, id, DcatPackage.Literals.DCATAP_ROOT__DATA_SERVICE, service);
		return service;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DataServiceAdminService#deleteDataService(java.lang.String, boolean)
	 */
	@Override
	public void deleteDataService(String id, boolean cascade) {
		// TODO FR-1: 409 when the catalog is still referenced; cascade currently ignored.
		DcatHelper.delete(directory, id);
	}

}
