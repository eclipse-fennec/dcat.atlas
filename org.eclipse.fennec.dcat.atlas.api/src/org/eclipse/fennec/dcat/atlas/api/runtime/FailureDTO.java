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
 * One configured thing that is not live, or one piece of content that is wrong.
 *
 * @since 1.0
 */
public class FailureDTO extends DTO {

	/** Why, as one of the constants in {@link FailureReasons}. */
	public String reason;

	/** What it is about — a component name, a collection, a resource IRI, a directory. */
	public String subject;

	/** What a reader needs to know that the reason alone does not say. */
	public String message;
}
