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

import java.util.Map;

import org.eclipse.fennec.dcat.atlas.api.runtime.DcatChangeNotifierTopics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * How an admin service says it changed the portal.
 *
 * <p>
 * It posts rather than sends, and that is the point of it being here at all: the announcement
 * happens on the thread that has just finished a write, and a consumer reading the store back
 * from that thread would re-enter the operation that is still completing. Posting hands the
 * event to EventAdmin's own delivery thread, so the write returns and the consumer reads a
 * store nobody is in the middle of.
 * </p>
 *
 * <p>
 * Every announcement names the resource both ways — {@code entity.collection} and
 * {@code entity.id} — because the collections have separate id spaces, so an id on its own
 * does not say what changed.
 * </p>
 *
 * @author ilenia
 * @since Oct 26, 2026
 */
@Component(name = "DcatChangeNotifier", immediate = true, service = DcatChangeNotifier.class)
public class DcatChangeNotifier {

	@Reference
	EventAdmin eventAdmin;

	/** A resource was stored, whether it existed before or not. */
	public void entityChanged(String collection, String entityId) {
		announce(DcatChangeNotifierTopics.CHANGE_TOPIC, collection, entityId);
	}

	/** A resource was removed from its collection. */
	public void entityDeleted(String collection, String entityId) {
		announce(DcatChangeNotifierTopics.DELETE_TOPIC, collection, entityId);
	}

	/** A resource became a member of a catalog, a data service or a dataset series. */
	public void entityLinked(String collection, String entityId) {
		announce(DcatChangeNotifierTopics.LINK_TOPIC, collection, entityId);
	}

	/** A membership was withdrawn; the resource itself is still stored. */
	public void entityUnlinked(String collection, String entityId) {
		announce(DcatChangeNotifierTopics.UNLINK_TOPIC, collection, entityId);
	}

	private void announce(String topic, String collection, String entityId) {
		eventAdmin.postEvent(new Event(topic, Map.of(DcatChangeNotifierTopics.ENTITY_COLLECTION, collection,
				DcatChangeNotifierTopics.ENTITY_ID, entityId)));
	}
}
