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
package org.eclipse.fennec.dcat.atlas.impl.runtime;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * The EventAdmin a plain test has instead of a framework one: it drops everything.
 *
 * <p>
 * <strong>Not a stand-in for the real one, and nothing is asserted against it.</strong>
 * Felix's EventAdmin cannot run outside a framework — it is built from a
 * {@code BundleContext} and finds its handlers by tracking services — so a plain JUnit test
 * has no way to use it. What an announcement actually does is therefore tested where the
 * real one is running, in {@code rest.tests}: {@code ChangeAnnouncementIntegrationTest} for
 * the topics and their properties, {@code RuntimeServiceIntegrationTest} for what a consumer
 * makes of them.
 * </p>
 *
 * <p>
 * This exists only because {@code DcatChangeNotifier} is a mandatory reference: without it a
 * hand-built admin service fails on the announcement rather than on what the test is about.
 * It lives in the notifier's own package because the field DS injects is package-visible.
 * </p>
 */
public final class DiscardingEventAdmin implements EventAdmin {

	/** A notifier announcing into this, wired as DS would wire one. */
	public static DcatChangeNotifier notifier() {
		DcatChangeNotifier notifier = new DcatChangeNotifier();
		notifier.eventAdmin = new DiscardingEventAdmin();
		return notifier;
	}

	@Override
	public void postEvent(Event event) {
		// Deliberately nothing: see the class comment.
	}

	@Override
	public void sendEvent(Event event) {
		// Deliberately nothing: see the class comment.
	}
}
