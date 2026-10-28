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
package org.eclipse.fennec.dcat.atlas.api.runtime;

/**
 * What an admin service announces when it has changed the portal, and under which names.
 *
 * <p>
 * Every topic sits under {@link #BASE_TOPIC}, so a handler that wants all of them subscribes
 * to {@code org/eclipse/fennec/dcat/atlas/*} once. The topics say what happened rather than
 * what has to be done about it: a consumer that only needs to know that something moved —
 * the runtime inventory is one — can ignore the distinction entirely.
 * </p>
 *
 * @author ilenia
 * @since Oct 26, 2026
 */
public final class DcatChangeNotifierTopics {

	/** The prefix every topic below shares; subscribe to {@code BASE_TOPIC + "*"} for all of them. */
	public static final String BASE_TOPIC = "org/eclipse/fennec/dcat/atlas/";

	/** A resource was stored, whether it existed before or not. */
	public static final String CHANGE_TOPIC = BASE_TOPIC + "CHANGED";

	/** A resource was removed from its collection. */
	public static final String DELETE_TOPIC = BASE_TOPIC + "DELETED";

	/** A resource became a member of a catalog, a data service or a dataset series. */
	public static final String LINK_TOPIC = BASE_TOPIC + "LINKED";

	/** A membership was withdrawn; the resource itself is still stored. */
	public static final String UNLINK_TOPIC = BASE_TOPIC + "UNLINKED";

	/** Event property: the store id of the resource the event is about. */
	public static final String ENTITY_ID = "entity.id";

	/**
	 * Event property: which collection it is in, one of {@code DcatIds.COLLECTIONS}.
	 * <p>
	 * An id alone does not identify a resource — the collections have separate id spaces —
	 * so a consumer that wants to act on one resource rather than re-read everything needs
	 * both.
	 */
	public static final String ENTITY_COLLECTION = "entity.collection";

	private DcatChangeNotifierTopics() {
		// constants only
	}
}
