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

/**
 * 
 * @author ilenia
 * @since Jul 7, 2026
 */
/**
 * Relationship/membership maintenance without re-uploading the target resource (FR-9/10/11).
 */
public interface CatalogRelationService {
	
	void linkDatasetToCatalog(String catalogId, String datasetId);
    void unlinkDatasetFromCatalog(String catalogId, String datasetId);
    void linkServiceToCatalog(String catalogId, String serviceId);
    void unlinkServiceFromCatalog(String catalogId, String serviceId);
    void linkSubCatalog(String catalogId, String subCatalogId);
    void unlinkSubCatalog(String catalogId, String subCatalogId);
    void addSeriesMember(String seriesId, String datasetId);     // from AP1
    void removeSeriesMember(String seriesId, String datasetId);
    void setAccessService(String distributionId, String serviceId);

}
