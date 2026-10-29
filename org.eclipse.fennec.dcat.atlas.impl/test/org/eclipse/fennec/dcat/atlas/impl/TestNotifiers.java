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

import org.eclipse.fennec.dcat.atlas.impl.runtime.DcatChangeNotifier;
import org.eclipse.fennec.dcat.atlas.impl.runtime.DiscardingEventAdmin;

/**
 * The collaborator DS would inject, for a test that builds an admin service by hand.
 *
 * <p>
 * The reference is mandatory and arrives through the constructor, so every admin service a
 * test builds is handed one of these. It announces into nothing: no test here asserts on an
 * announcement, because a plain JUnit run has no framework and so no EventAdmin to assert
 * against. What the announcements do is tested against the real Felix one in
 * {@code rest.tests} — see {@link DiscardingEventAdmin}.
 * </p>
 *
 * <p>
 * Two classes rather than one because {@code eventAdmin} is package-visible in the notifier's
 * own package and can only be set from there.
 * </p>
 */
public final class TestNotifiers {

	/** A notifier whose announcements go nowhere. */
	public static DcatChangeNotifier discarding() {
		return DiscardingEventAdmin.notifier();
	}

	private TestNotifiers() {
		// helpers only
	}
}
