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
import dcat.Distribution;

/**
 *
 * @author ilenia
 * @since Jul 7, 2026
 */
public interface DistributionAdminService extends DistributionReadOnlyService {

	//**FR-10 (Distribution composition):** Distributions are created/deleted in the context of
	//their Dataset; a Distribution without a Dataset is not allowed.
    Distribution upsertDistributionToDataset(String datasetId, Distribution distribution);

    void deleteDistributionFromDataset(String datasetId, String distributionId);

    /**
     * FR-10: records {@code dcat:accessService} on the distribution, referencing an
     * already catalogued DataService — the service that gives access to this
     * distribution (DCAT-AP.de 3.0 §4.6.24, "Ausliefernder Datenservice").
     * <p>
     * The link is stored as a {@code dcat:accessService rdf:resource} pointer to the
     * service's {@code rdf:about}, so the service stays a single, independently
     * maintained catalog entity rather than being copied into the distribution.
     * <p>
     * Idempotent: re-adding a service that is already referenced leaves the
     * distribution — and therefore its ETag — untouched.
     *
     * @param datasetId      the owning dataset
     * @param distributionId the distribution to link from
     * @param dataService    the service to reference; must carry an {@code rdf:about}
     * @return the distribution as stored
     */
    Distribution addAccessServiceToDistribution(String datasetId, String distributionId, DataService dataService);

    /**
     * Removes the {@code dcat:accessService} reference to {@code dataServiceId} from the
     * distribution. Idempotent: a no-op when no such reference is present. The referenced
     * DataService itself is never deleted — it belongs to the catalog, not to the
     * distribution.
     */
    void deleteAccessServiceFromDistribution(String datasetId, String distributionId, String dataServiceId);

}
