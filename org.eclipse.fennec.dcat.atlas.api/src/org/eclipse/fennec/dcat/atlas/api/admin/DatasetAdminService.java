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
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyService;

import dcat.Dataset;

/**
 * 
 * @author ilenia
 * @since Jul 7, 2026
 */
public interface DatasetAdminService extends DatasetReadOnlyService {
	
    Dataset upsertDataset(Dataset dataset);
    
    /**
     * Deletes the dataset under {@code id}.
     *
     * <h4>What {@code cascade} decides</h4>
     *
     * A dataset may be referenced by other resources. With {@code cascade} {@code false}
     * the delete is refused as {@link ResourceInUseException} while any referrer remains;
     * with {@code true} every referrer is unlinked first and the whole thing — the unlinks
     * and the delete — is one commit, so no reader ever sees a state where the dataset is
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
     * @param id      the dataset to delete
     * @param cascade unlink every referrer first, instead of refusing
     * @return the logical IRIs of the resources that were unlinked, empty when nothing
     *         referenced this one; a caller reports them so a client can invalidate the
     *         ETags this call moved
     * @throws ResourceInUseException if something still references it and {@code cascade}
     *                                is {@code false}
     */
    List<String> deleteDataset(String id, boolean cascade);
}
