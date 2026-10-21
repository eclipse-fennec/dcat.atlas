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
 * What validation is configured to do, and what it will actually do.
 *
 * @since 1.0
 */
public class ValidationDTO extends DTO {

	/** Where the SHACL shapes are read from; empty or {@code null} when none are configured. */
	public String shapesDirectory;

	/** How many shape files were parsed. Zero with {@link #enforceOnWrite} is the sharp failure. */
	public int shapeFiles;

	/** Where the controlled-vocabulary data is read from; {@code null} when there is none. */
	public String vocabularyDirectory;

	/** How many vocabulary files were parsed. Zero with CV shapes loaded reports false violations. */
	public int vocabularyFiles;

	/** What the configuration asks for. */
	public boolean enforceOnWrite;

	/**
	 * What enforcement will actually do — {@code enforceOnWrite} and at least one shape.
	 * <p>
	 * The pair exists because the two disagree silently: with enforcement on and no shapes,
	 * every write is validated against an empty shapes graph, every write conforms, and the
	 * portal reports that it is enforcing.
	 */
	public boolean effectivelyEnforcing;
}
