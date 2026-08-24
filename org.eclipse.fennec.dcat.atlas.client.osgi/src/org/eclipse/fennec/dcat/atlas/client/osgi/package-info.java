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
 * The OSGi front-end: a ConfigAdmin factory component publishing one configured portal as a
 * synchronous {@code DcatAtlasClient} and an asynchronous {@code AsyncDcatAtlasClient}.
 * <p>
 * Only {@code AsyncDcatAtlasClient} is API here — everything else is the component and its
 * configuration, which consumers reach through the service registry rather than by type.
 */
@org.osgi.annotation.versioning.Version("1.0.0")
package org.eclipse.fennec.dcat.atlas.client.osgi;
