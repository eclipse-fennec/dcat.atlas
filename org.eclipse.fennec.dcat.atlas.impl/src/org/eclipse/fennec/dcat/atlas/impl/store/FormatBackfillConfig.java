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
package org.eclipse.fennec.dcat.atlas.impl.store;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/** Configuration of {@link FormatBackfill}; the store coordinates mirror {@link StoreConfig}. */
@ObjectClassDefinition(name = "DCAT.Atlas format backfill",
		description = "Completes dct:format from dcat:mediaType on the distributions already in the store, once per start.")
public @interface FormatBackfillConfig {

	/**
	 * Which store to complete. There is no base path here any more: where the collections
	 * sit inside the store is the store's own business, and naming it twice was a second
	 * place for the two to disagree.
	 */
	@AttributeDefinition(name = "Store target filter", description = "The target filter selecting which DcatStoreService to complete")
	String storeService_target() default "(scope=no-inject)";

	@AttributeDefinition(name = "Enabled", description = "false skips the pass. New writes are completed regardless; this only concerns what is already stored.")
	boolean enabled() default true;
}
