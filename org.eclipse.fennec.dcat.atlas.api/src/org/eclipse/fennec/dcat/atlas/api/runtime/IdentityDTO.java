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

import org.osgi.dto.DTO;

/**
 * The two bases every identity in this portal is built from.
 *
 * @since 1.0
 */
public class IdentityDTO extends DTO {

	/** What resources are called in the store, independent of where the portal is reachable. */
	public String logicalBase;

	/**
	 * What the portal tells the world to call them. A wrong one is one of the six silent
	 * failures: the portal serves {@code 200} and advertises IRIs that resolve to somebody else.
	 */
	public String publicBase;
}
