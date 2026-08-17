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
    
    /**
     *If a Dataset was already created, a client can use this method to simply link
     *the existing Dataset to the existing Catalog. Must fail if either the Catalog or
     *the Dataset does not exist
     */
    Catalog linkDatasetToCatalog(String catalogId, String datasetId);
    
    void deleteDatasetFromCatalog(String catalogId, String datasetId);
    
    Catalog addDataServiceToCatalog(String catalogId, DataService dataService);
    
    /**
     *If a DataService was already created, a client can use this method to simply link
     *the existing DataService to the existing Catalog. Must fail if either the Catalog or
     *the DataService does not exist
     */
    Catalog linkDataServiceToCatalog(String catalogId, String dataServiceId);
    
    void deleteDataServiceFromCatalog(String catalogId, String dataServiceId);
    
    Catalog addSubCatalogToCatalog(String catalogId, Catalog subCatalog);
    
    /**
     *If a Catalog was already created, a client can use this method to simply link
     *the existing Catalog to the another existing Catalog, as sub catalog. 
     *Must fail if either the parent Catalog or the sub catalog does not exist.
     */
    Catalog linkSubCatalogToCatalog(String catalogId, String subCatalogId);
    
    void deleteSubCatalogFromCatalog(String catalogId, String subCatalogId);
}
