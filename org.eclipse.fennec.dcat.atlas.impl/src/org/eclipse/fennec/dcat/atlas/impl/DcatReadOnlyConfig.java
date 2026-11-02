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
 * Configuration for a read-only portal.
 * <p>
 * It has one attribute, and that is the point: a portal that only publishes has nothing to
 * decide per entity type.
 */
@ObjectClassDefinition(name = "DCAT-AP Atlas read-only services", description = "The read side of the portal, and the store it reads from")
public @interface DcatReadOnlyConfig {

	/**
	 * Which store the handlers read from; see
	 * {@code DcatAdminConfig.storeService_target}. The default matches nothing, so a
	 * deployment has to say.
	 */
	@AttributeDefinition(name = "Store target filter", description = "The target filter selecting which DcatStoreService the handlers read from")
	String storeService_target() default "(scope=no-inject)";
}
