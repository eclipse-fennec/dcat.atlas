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
package org.eclipse.fennec.dcat.atlas.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Shared configuration for the file-based DCAT-AP stores. Every entity service
 * {@code @Designate}s this same OCD.
 * <p>
 * A single <em>root</em> rather than a directory per service, because the stores
 * are no longer independent: a Catalog's {@code dcat:dataset} link is an EMF
 * cross-resource reference that has to resolve into the dataset store, so one
 * {@code URIConverter} URI map has to cover every collection at once. The
 * per-collection subdirectory names are therefore fixed rather than configurable
 * — see {@link org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout}.
 * <p>
 * Persistence is a directory of XMI files, one per resource. Distributions have
 * no directory of their own: {@code dcat:distribution} is containment, so a
 * Distribution is stored inside its Dataset's file (FR-10).
 */
@ObjectClassDefinition(name = "DCAT-AP Atlas file store", description = "File-system persistence for the DCAT-AP entity stores")
public @interface StoreConfig {

	/**
	 * Root directory holding one subdirectory per collection, each with one
	 * {@code <id>.xmi} file per resource. Subdirectories are created on activation
	 * if they do not exist.
	 */
	@AttributeDefinition(name = "Store root", description = "Root directory holding the per-collection store subdirectories")
	String root() default "data";

	/**
	 * Whether a write is checked against the model's own constraints — the ecore
	 * multiplicities and the OCL invariants annotated on it — and refused if it
	 * violates them.
	 * <p>
	 * Defaults to {@code false} and is switched on in the shipped runtime configurations,
	 * the same arrangement {@code ShapesConfig.enforceOnWrite} uses. Off by default so
	 * that importing a corpus written before a constraint existed does not need the
	 * constraint removed, and so the store's own tests can exercise storage mechanics with
	 * deliberately minimal entities. A deployment serving a portal should leave it on:
	 * unlike SHACL, these constraints ship inside the model and need no operator setup, so
	 * this is the only always-available check.
	 */
	@AttributeDefinition(name = "Validate on write", description = "Refuse a write whose entity violates the model's OCL constraints or declared multiplicities")
	boolean validateOnWrite() default false;
}
