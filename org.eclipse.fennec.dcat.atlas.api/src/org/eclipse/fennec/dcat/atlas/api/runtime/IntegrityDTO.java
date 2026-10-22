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
package org.eclipse.fennec.dcat.atlas.api.runtime;

import java.util.List;

import org.osgi.dto.DTO;

/**
 * What a full scan of the stored content found.
 *
 * @since 1.0
 */
public class IntegrityDTO extends DTO {

	/** When the scan ran, in epoch milliseconds. */
	public long takenAt;

	/** How long it took, in milliseconds — the number that says why it is not in the snapshot. */
	public long durationMillis;

	/** How many resources were read. */
	public int resourcesScanned;

	/** What is wrong, with {@link FailureReasons} from the content half of the list. */
	public List<FailureDTO> failures;
}
