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

import dcat.Distribution;

/**
 * 
 * @author ilenia
 * @since Jul 7, 2026
 */
public interface DistributionAdminService extends DistributionReadOnlyService {

    // Distribution is a root-level resource (DCATAPRoot#distribution) keyed by its own
    // rdf:about, like the other entities; the dataset link is a URI reference handled
    // as a relationship (FR-10), not part of the upsert.
    Distribution upsertDistribution(Distribution distribution);

    void deleteDistribution(String id, boolean cascade);

}
