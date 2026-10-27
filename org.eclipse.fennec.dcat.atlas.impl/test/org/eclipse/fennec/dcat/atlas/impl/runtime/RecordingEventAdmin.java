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

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * The EventAdmin a plain test has instead of a framework one, keeping what was posted.
 *
 * <p>
 * It builds the real {@link DcatChangeNotifier} rather than standing in for it, so a test
 * sees the topic and the properties an admin service would actually announce. It lives in
 * the notifier's own package because the field DS injects is package-visible.
 * </p>
 */
public class RecordingEventAdmin implements EventAdmin {

	private final List<Event> posted = new ArrayList<>();

	/** A notifier posting into this recorder, wired as DS would wire one. */
	public DcatChangeNotifier notifier() {
		DcatChangeNotifier notifier = new DcatChangeNotifier();
		notifier.eventAdmin = this;
		return notifier;
	}

	@Override
	public void postEvent(Event event) {
		posted.add(event);
	}

	@Override
	public void sendEvent(Event event) {
		posted.add(event);
	}

	/** Everything announced so far, oldest first. */
	public List<Event> posted() {
		return List.copyOf(posted);
	}

	/** The topics announced so far, which is what a test usually wants to compare. */
	public List<String> topics() {
		return posted.stream().map(Event::getTopic).toList();
	}
}
