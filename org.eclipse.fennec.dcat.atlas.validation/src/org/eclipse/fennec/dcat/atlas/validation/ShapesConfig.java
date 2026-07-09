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
package org.eclipse.fennec.dcat.atlas.validation;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configuration for {@link DcatValidationServiceImpl}: the filesystem directory
 * holding the DCAT-AP.de SHACL shape {@code *.ttl} files. The shapes are kept
 * <em>outside</em> the application (they carry their own upstream license), so the
 * operator points this at wherever they installed them. When unset, validation is
 * a no-op (everything conforms) — see the impl.
 */
@ObjectClassDefinition(name = "DCAT.Atlas SHACL Validation", description = "SHACL validation of DCAT-AP.de entities (FR-4/FR-5).")
public @interface ShapesConfig {

	@AttributeDefinition(required = false, description = "Directory containing the DCAT-AP.de SHACL shape .ttl files. "
			+ "All *.ttl in it are loaded into one shapes graph. Empty = validation disabled.")
	String shapesDirectory() default "";
}
