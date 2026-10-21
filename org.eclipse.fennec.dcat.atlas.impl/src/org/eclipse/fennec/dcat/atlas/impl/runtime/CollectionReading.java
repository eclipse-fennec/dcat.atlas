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

import java.util.List;

/**
 * One collection's ids, from the store's listing.
 *
 * <p>
 * Ids and not entities: a listing costs nothing per resource, and loading them would make the
 * snapshot proportional to the size of the portal — which the telemetry consumer calls once
 * per gauge per export interval.
 * </p>
 */
record CollectionReading(String name, List<String> ids, boolean validateOnWrite) {
}
