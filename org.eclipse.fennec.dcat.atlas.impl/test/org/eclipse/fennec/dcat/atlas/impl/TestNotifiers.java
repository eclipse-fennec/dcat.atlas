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
 * The reference is mandatory, so a service DS built always has one and the write paths call
 * it without asking. A service a test built with one of the package-visible constructors has
 * nothing injected, so a write would fail on the announcement rather than on what the test is
 * about — hence {@link #announcing}, which gives it one that announces into nothing.
 * </p>
 *
 * <p>
 * No test here asserts on an announcement. There is no framework in a plain JUnit run and so
 * no EventAdmin to assert against; what the announcements do is tested against the real Felix
 * one in {@code rest.tests} — see {@link DiscardingEventAdmin}.
 * </p>
 *
 * <p>
 * Two classes rather than one because the two fields being wired are package-visible in
 * different packages: {@code changeNotifier} is set from here, {@code eventAdmin} only from
 * the notifier's own package.
 * </p>
 */
final class TestNotifiers {

	/** A notifier whose announcements go nowhere. */
	static DcatChangeNotifier discarding() {
		return DiscardingEventAdmin.notifier();
	}

	static CatalogAdminServiceImpl announcing(CatalogAdminServiceImpl service) {
		service.changeNotifier = discarding();
		return service;
	}

	static DatasetAdminServiceImpl announcing(DatasetAdminServiceImpl service) {
		service.changeNotifier = discarding();
		return service;
	}

	static DataServiceAdminServiceImpl announcing(DataServiceAdminServiceImpl service) {
		service.changeNotifier = discarding();
		return service;
	}

	static DatasetSeriesAdminServiceImpl announcing(DatasetSeriesAdminServiceImpl service) {
		service.changeNotifier = discarding();
		return service;
	}

	static DistributionAdminServiceImpl announcing(DistributionAdminServiceImpl service) {
		service.changeNotifier = discarding();
		return service;
	}

	private TestNotifiers() {
		// helpers only
	}
}
