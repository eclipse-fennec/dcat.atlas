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
package org.eclipse.fennec.dcat.atlas.api;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;

/**
 * CRUD + upsert per DCAT-AP 3 entity (FR-1..FR-8).
 * Implementation: transactional against Jena TDB2, SHACL-validated before commit.
 */
public interface CatalogAdminService extends CatalogReadOnlyService{

    Catalog upsertCatalog(Catalog catalog);        
    
    void deleteCatalog(String id, boolean cascade);       
    
    
    //Relationships and Composition Methods
    //**FR-9 (Catalog membership):** Dataset, DataService and sub-catalog can be 
    //assigned to / removed from a catalog **without** re-sending the target resource in full.
    Catalog addDatasetToCatalog(String catalogId, Dataset dataset);
    
    void deleteDatasetFromCatalog(String catalogId, String datasetId);
    
    Catalog addDataServiceToCatalog(String catalogId, DataService dataService);
    
    void deleteDataServiceFromCatalog(String catalogId, String dataServiceId);
    
    Catalog addSubCatalogToCatalog(String catalogId, Catalog subCatalog);
    
    void deleteSubCatalogFromCatalog(String catalogId, String subCatalogId);
}
