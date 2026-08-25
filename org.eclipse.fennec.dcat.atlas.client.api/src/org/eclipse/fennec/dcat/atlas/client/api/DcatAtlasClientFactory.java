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
package org.eclipse.fennec.dcat.atlas.client.api;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Bootstrap seam that lets {@link DcatAtlasClient#builder()} find an implementation
 * without this API bundle depending on one.
 * <p>
 * The plain-Java implementation registers exactly one provider through
 * {@link java.util.ServiceLoader} ({@code META-INF/services}), which
 * {@link DcatAtlasClient#builder()} loads — the same arrangement
 * {@code jakarta.ws.rs.client.ClientBuilder.newBuilder()} uses to locate its own
 * implementation, and the same one {@code model.atlas}'s client uses.
 */
@ConsumerType
public interface DcatAtlasClientFactory {

	/**
	 * @return a fresh client builder
	 */
	DcatAtlasClient.Builder builder();
}
