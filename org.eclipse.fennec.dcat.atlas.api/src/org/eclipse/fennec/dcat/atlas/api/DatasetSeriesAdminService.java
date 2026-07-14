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

import dcat.Dataset;
import dcat.DatasetSeries;

/**
 * 
 * @author ilenia
 * @since Jul 7, 2026
 */
public interface DatasetSeriesAdminService extends DatasetSeriesReadOnlyService{
	
    DatasetSeries upsertDatasetSeries(DatasetSeries series);
    
    void deleteDatasetSeries(String id, boolean cascade);
    
    //**FR-11 (Series membership):** Datasets can be assigned to / removed from a DatasetSeries 
    DatasetSeries addDatasetToDatasetSeries(String datasetSeriesId, Dataset dataset);
    
    void deleteDatasetFromDatasetSeries(String datasetSeriesId, String datasetId);

}
