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
package org.eclipse.fennec.dcat.atlas.odata;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "DCAT.Atlas OData repository",
		description = "In-memory snapshot of the store that backs the OData endpoints")
public @interface ODataConfig {

	/**
	 * Turning the repository off also stops the snapshot being built, so a deployment
	 * that does not want OData pays neither the memory nor the startup cost. The servlet
	 * root itself is configured separately, under the emf.odata PIDs.
	 */
	@AttributeDefinition(name = "Enabled", description = "Whether to build the snapshot and answer OData queries at "
			+ "all. Off unless a deployment asks for it: the snapshot holds the whole store in memory and the "
			+ "endpoints are a second way in, so neither should arrive by omission.")
	boolean enabled() default false;

	/**
	 * How often to rebuild the snapshot even though nothing announced a change. The pass
	 * skips while the store revision is unchanged, so the interval bounds how long the
	 * snapshot can be silently stale after a change that produced no event — it is not a
	 * poll of the store.
	 */
	@AttributeDefinition(name = "Reconcile interval (s)",
			description = "Interval for the store/snapshot reconciliation pass; 0 disables it")
	long reconcileIntervalSeconds() default 300L;
}
