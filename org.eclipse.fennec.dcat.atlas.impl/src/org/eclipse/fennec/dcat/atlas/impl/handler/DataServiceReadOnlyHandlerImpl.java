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
package org.eclipse.fennec.dcat.atlas.impl.handler;

import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.DataService;

/** File-backed, read-only view of the data-service store. */
public class DataServiceReadOnlyHandlerImpl extends AbstractEntityHandler<DataService>
		implements DataServiceReadOnlyHandler {

	public DataServiceReadOnlyHandlerImpl(DcatStoreService storeService) {
		super(storeService, StoreLayout.DATA_SERVICES);
	}

	@Override
	public Optional<DataService> getDataService(String id) {
		return getEntity(id);
	}

	@Override
	public List<DataService> listDataServices() {
		return listEntities();
	}

	@Override
	public Page<DataService> listDataServices(PageRequest page) {
		return listEntities(page);
	}
}
