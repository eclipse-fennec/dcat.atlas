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
package org.eclipse.fennec.dcat.atlas.client.impl;

import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClientFactory;

import aQute.bnd.annotation.spi.ServiceProvider;

/**
 * Makes {@link DcatAtlasClient#builder()} work.
 * <p>
 * {@link ServiceProvider} is what emits the {@code META-INF/services} entry and the
 * {@code osgi.serviceloader} capability, so the file does not have to be maintained by
 * hand and the bundle also works under an OSGi ServiceLoader mediator.
 */
@ServiceProvider(DcatAtlasClientFactory.class)
public class DefaultDcatAtlasClientFactory implements DcatAtlasClientFactory {

	@Override
	public DcatAtlasClient.Builder builder() {
		return new DcatAtlasClientBuilderImpl();
	}
}
