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
/**
 * Public API of the DCAT.Atlas client: the {@code DcatAtlasClient} contract, the
 * {@code ClientConfiguration} value type, the {@code JakartaRsClientProvider} seam
 * and the typed exception hierarchy.
 * <p>
 * Deliberately free of OSGi runtime dependencies — no Declarative Services, no
 * framework, no ConfigAdmin — so the client is usable as a plain library. The only
 * OSGi artefacts here are the compile-time versioning annotations.
 */
@org.osgi.annotation.versioning.Version("1.0.0")
package org.eclipse.fennec.dcat.atlas.client.api;
