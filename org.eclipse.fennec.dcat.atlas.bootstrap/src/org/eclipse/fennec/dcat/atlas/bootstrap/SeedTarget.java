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
package org.eclipse.fennec.dcat.atlas.bootstrap;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;

/**
 * Where one collection's seeds go: what it already holds, and how to store one.
 *
 * <p>
 * The two halves of seeding a collection, and nothing else. Each of the four admin
 * services offers them under a different name and a different type, and the seeding rule
 * is the same for all four — so it is written once here rather than four times against
 * four interfaces.
 * </p>
 */
interface SeedTarget {

	/** Whether the portal already holds {@code id} in this collection. */
	boolean holds(String id);

	/** Stores {@code resource}, which is this collection's kind of resource. */
	void write(EObject resource);

	static SeedTarget of(Predicate<String> holds, Consumer<EObject> write) {
		return new SeedTarget() {

			@Override
			public boolean holds(String id) {
				return holds.test(id);
			}

			@Override
			public void write(EObject resource) {
				write.accept(resource);
			}
		};
	}
}
