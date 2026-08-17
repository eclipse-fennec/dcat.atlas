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

import dcat.DataService;
import dcat.Dataset;

/**
 * 
 * @author ilenia
 * @since Jul 7, 2026
 */
public interface DataServiceAdminService extends DataServiceReadOnlyService{
	
    DataService upsertDataService(DataService service);

    void deleteDataService(String id, boolean cascade);


    //Relationships and Composition Methods
    //**dcat:servesDataset:** the Dataset(s) this DataService serves can be assigned to /
    //removed from it **without** re-sending the DataService in full. The reference is
    //declared on the DataService, so the DataService is what is edited and what these
    //methods return. This is not the inverse of dcat:accessService: that one says which
    //service gives access to a Distribution, this one which datasets a service serves.
    DataService addDatasetToDataService(String dataServiceId, Dataset dataset);

    /**
     *If a Dataset was already created, a client can use this method to simply link
     *the existing Dataset to the existing DataService. Must fail if either the DataService
     *or the Dataset does not exist
     */
    DataService linkDatasetToDataService(String dataServiceId, String datasetId);

    void deleteDatasetFromDataService(String dataServiceId, String datasetId);

}
