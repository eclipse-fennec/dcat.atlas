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

/**
 * One thing wrong with one stored resource, found by the explicit scan.
 *
 * @param collection where it lives
 * @param id         which resource
 * @param reason     one of the content reasons in {@code FailureReasons}
 * @param message    what a reader needs beyond the reason
 */
public record IntegrityFinding(String collection, String id, String reason, String message) {
}
