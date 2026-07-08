package org.eclipse.fennec.dcat.atlas.api;

import dcat.Catalog;

/**
 * CRUD + upsert per DCAT-AP 3 entity (FR-1..FR-8).
 * Implementation: transactional against Jena TDB2, SHACL-validated before commit.
 */
public interface CatalogAdminService extends CatalogReadOnlyService{

    Catalog upsertCatalog(Catalog catalog);        
    
    void deleteCatalog(String id, boolean cascade);        

}
