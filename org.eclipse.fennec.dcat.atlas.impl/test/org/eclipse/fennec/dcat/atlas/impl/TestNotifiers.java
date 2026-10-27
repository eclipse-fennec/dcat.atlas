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
import org.eclipse.fennec.dcat.atlas.impl.runtime.RecordingEventAdmin;

/**
 * The collaborator DS would inject, for a test that builds an admin service by hand.
 *
 * <p>
 * The reference is mandatory, so a service DS built always has one and the write paths call
 * it without asking. A service a test built with one of the package-visible constructors has
 * nothing injected, so a write would fail on the announcement rather than on what the test is
 * about — hence {@link #announcing}, for the tests that need the service to work but have no
 * interest in what it announced.
 * </p>
 *
 * <p>
 * A test that <em>is</em> about the announcements builds its own {@link RecordingEventAdmin}
 * and keeps it, so it can read back what was posted.
 * </p>
 */
final class TestNotifiers {

	/** A notifier posting into a recorder nobody reads. */
	static DcatChangeNotifier discarding() {
		return new RecordingEventAdmin().notifier();
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
