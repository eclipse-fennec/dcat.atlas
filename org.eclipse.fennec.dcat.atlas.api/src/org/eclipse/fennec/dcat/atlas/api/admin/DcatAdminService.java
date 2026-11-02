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
package org.eclipse.fennec.dcat.atlas.api.admin;


import org.eclipse.fennec.dcat.atlas.api.read.DcatReadOnlyService;

/**
 * The write side of the portal, as one service.
 * <p>
 * It extends the read side because anything allowed to write is allowed to read, the same
 * way {@link CatalogAdminHandler} extends {@code CatalogReadOnlyHandler}. Holding one of
 * these is enough to read as well as write.
 *
 * <h2>An implementation must register as this interface only</h2>
 *
 * Extending {@link DcatReadOnlyService} is safe; <em>providing</em> it from the same
 * component is not. The RDF projection behind SPARQL reads through
 * {@link DcatReadOnlyService}, and a portal configured with {@code sparqlSupported}
 * requires that projection before it will write — so a component publishing both ends of
 * {@code store → read → graph → admin} closes a cycle and nothing in the chain ever
 * activates. It fails as an absent service, not as a reported cycle, so it is read off the
 * references rather than the log.
 * <p>
 * Hence {@code service = { DcatAdminService.class }} on the implementation, and a separate
 * component for {@link DcatReadOnlyService}. Leaving the {@code service} element off would
 * also be safe — DS defaults it to the directly implemented interfaces, which is this one
 * alone — but it is pinned so the rule is visible where it has to hold.
 */
public interface DcatAdminService extends DcatReadOnlyService {
	
	CatalogAdminHandler getCatalogAdminHandler();
	
	DataServiceAdminHandler getDataServiceAdminHandler();
	
	DatasetAdminHandler getDatasetAdminHandler();
	
	DistributionAdminHandler getDistributionAdminHandler();
	
	DatasetSeriesAdminHandler getDataSeriesAdminHandler();

}
