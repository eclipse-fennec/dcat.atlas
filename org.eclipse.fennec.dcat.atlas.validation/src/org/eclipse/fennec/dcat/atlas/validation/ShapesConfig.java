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

	@AttributeDefinition(required = false, description = "Directory containing the controlled-vocabulary reference data "
			+ "(the DCAT-AP.de owl:imports targets: EU authority tables, the GovData license register, etc.) as local "
			+ "RDF files (*.ttl/*.rdf/*.xml/*.nt/*.jsonld). They are merged into the graph being validated so the "
			+ "controlled-vocabulary shapes' skos:inScheme/sh:class checks resolve (F-22). Empty = no vocabulary data "
			+ "loaded, so include the controlled-vocabularies shape file only together with this directory, otherwise "
			+ "every vocabulary-constrained value reports a (false) violation.")
	String vocabularyDirectory() default "";

	@AttributeDefinition(required = false, description = "When true, the admin write endpoints reject a non-conformant "
			+ "entity with 422 before persisting it (FR-4). When false, writes are never blocked and validation is "
			+ "available only as the explicit dry-run (FR-5). Has no effect when no shapes are configured.")
	boolean enforceOnWrite() default false;
}
