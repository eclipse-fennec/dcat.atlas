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
 * Configuration for the write side of the portal.
 * <p>
 * It is one block because the handlers behind {@code DcatAdminService} are plain objects
 * rather than components: there is nothing to configure per entity type, and nothing that
 * could disagree about which store it writes to.
 */
@ObjectClassDefinition(name = "DCAT-AP Atlas admin services", description = "The write side of the portal, and the store it writes to")
public @interface DcatAdminConfig {

	/**
	 * Which store the handlers write to.
	 * <p>
	 * This is the whole point of binding the handlers here rather than registering ten
	 * components: with more than one {@code DcatStoreService} in the system, the store is
	 * selected once, so the handlers cannot end up writing to different ones. The default
	 * matches nothing, so a deployment has to say.
	 */
	@AttributeDefinition(name = "Store target filter", description = "The target filter selecting which DcatStoreService the handlers write to")
	String storeService_target() default "(scope=no-inject)";

	/**
	 * Whether this deployment serves SPARQL, and so maintains the RDF projection a write
	 * has to invalidate.
	 * <p>
	 * A deployment decision, not a runtime one — the sparql bundle is either installed or
	 * it is not — which is why the handlers hold the graph service rather than looking it
	 * up per write.
	 * <p>
	 * It is also what makes the {@code DcatGraphService} reference required, the same way
	 * {@code validateOnWrite} makes the validation service required on the store:
	 * {@code reproject} reads an absent graph service as "no projection", so a portal left
	 * on with nothing bound would answer queries from a projection no write ever updated.
	 * A portal that cannot keep that promise does not start.
	 * <p>
	 * Switching it on means also raising the reference's minimum cardinality
	 * ({@code graphService.cardinality.minimum:int=1}), so DS waits for the projection
	 * instead of activating without it; the check in the component is what catches an
	 * operator who set this and forgot that line.
	 */
	@AttributeDefinition(name = "SPARQL supported", description = "This deployment serves SPARQL, so a DcatGraphService has to be bound")
	boolean sparqlSupported() default false;
}
