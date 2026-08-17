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

import java.util.List;
import java.util.Optional;

import dcat.Distribution;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
public interface DistributionReadOnlyService {
	
	//**FR-10 (Distribution composition):** Distributions are created/deleted in the context of 
	//their Dataset; a Distribution without a Dataset is not allowed. 
	
	Optional<Distribution> getDistributionForDataset(String datasetId, String distributionId);

	List<Distribution> listDistributionsForDataset(String datasetId);

	/**
	 * Strong ETag validator for a distribution, or empty if absent (F-16).
	 * <p>
	 * The owning dataset is required because a Distribution is contained in its
	 * Dataset and has no stored representation of its own, so its version <em>is</em>
	 * the dataset's: changing a distribution changes the dataset. Identifying one by
	 * bare id would mean searching every dataset for it on every conditional request.
	 */
	Optional<String> etag(String datasetId, String distributionId);

}
