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

import java.nio.file.Path;

import org.eclipse.emf.ecore.EObject;

/**
 * One seed file, understood: what it holds and where that belongs.
 *
 * @param file       the file it was read from, so a refusal can name it
 * @param collection the collection it is stored in, one of {@code DcatIds.COLLECTIONS}
 * @param id         the id within that collection, taken from its {@code about}
 * @param resource   what the file holds
 */
record Seed(Path file, String collection, String id, EObject resource) {
}
