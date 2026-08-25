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

import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.integrity.ResourceInUseException;
import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.validation.ModelConstraintException;
import org.eclipse.fennec.dcat.atlas.api.validation.ShaclViolationException;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;

/**
 * CRUD + upsert per DCAT-AP 3 entity (FR-1..FR-8).
 *
 * <h2>What the implementation actually does</h2>
 *
 * There is <b>no database and no transaction</b>. This javadoc claimed "transactional
 * against Jena TDB2" from the original design; the store has been a directory of XMI
 * files — one per resource, a Distribution inside its Dataset's file (FR-10) — since the
 * 2026-08-17 storage rework, and Jena appears only as the disposable in-memory projection
 * behind the SPARQL endpoint. A git-backed store is Phase 2 of the persistence plan.
 * <p>
 * A write is therefore a file write, and is atomic only to the extent one file is. An
 * operation that touches two resources — linking a member that has to be stored first —
 * is not rolled back if the second write fails.
 * <p>
 * Validation does happen before anything is stored, in two layers, both at the persistence
 * boundary so they hold for every caller of this interface and not only for requests
 * arriving over REST:
 * <ul>
 * <li>the model's own constraints — the ecore's declared multiplicities and the OCL
 * invariants annotated on it — refused as {@link ModelConstraintException}. These ship
 * inside the model and are always available, gated only by {@code validateOnWrite};</li>
 * <li>the DCAT-AP.de SHACL shapes (FR-4), refused as {@link ShaclViolationException}, when
 * an operator has configured shapes and switched enforcement on. With no shapes configured
 * this checks nothing, because an empty shapes set conforms to everything.</li>
 * </ul>
 */
public interface CatalogAdminService extends CatalogReadOnlyService{

    Catalog upsertCatalog(Catalog catalog);        
    
    /**
     * Deletes the catalog under {@code id}.
     *
     * <h4>What {@code cascade} decides</h4>
     *
     * A catalog may be referenced by other resources. With {@code cascade} {@code false}
     * the delete is refused as {@link ResourceInUseException} while any referrer remains;
     * with {@code true} every referrer is unlinked first and the whole thing — the unlinks
     * and the delete — is one commit, so no reader ever sees a state where the catalog is
     * gone and something still points at it.
     *
     * <h4>Optimistic locking covers the target only</h4>
     *
     * Callers over HTTP may supply {@code If-Match}, and it is evaluated against the
     * <em>target's</em> ETag. A cascade also rewrites the referrers, whose ETags the caller
     * never saw and so cannot have checked: the F-16 guarantee therefore covers the resource
     * being deleted and not the resources being unlinked. That is deliberate — requiring an
     * ETag per referrer would mean enumerating them first, which is the work the cascade
     * exists to avoid — but it is a real narrowing, and direct OSGi callers see no HTTP
     * preconditions at all, which is why it is written here too.
     *
     * @param id      the catalog to delete
     * @param cascade unlink every referrer first, instead of refusing
     * @return the logical IRIs of the resources that were unlinked, empty when nothing
     *         referenced this one; a caller reports them so a client can invalidate the
     *         ETags this call moved
     * @throws ResourceInUseException if something still references it and {@code cascade}
     *                                is {@code false}
     */
    List<String> deleteCatalog(String id, boolean cascade);
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
