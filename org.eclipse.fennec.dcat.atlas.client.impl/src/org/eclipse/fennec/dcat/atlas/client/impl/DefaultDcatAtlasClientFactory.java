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
 * The {@link java.util.ServiceLoader}-discovered factory behind {@link DcatAtlasClient#builder()}.
 *
 * <h2>One annotation, both worlds</h2>
 *
 * {@code @ServiceProvider} makes bnd generate the {@code META-INF/services} descriptor into the
 * bundle jar <em>and</em> the matching {@code osgi.serviceloader} capability. Off a framework the
 * descriptor is what {@code ServiceLoader} finds; on one, SPI-Fly reads the capability and
 * registers this class as a {@link DcatAtlasClientFactory} service, which is how the OSGi
 * front-end's mandatory {@code @Reference} is satisfied. Same arrangement as
 * {@code model.atlas}'s {@code DefaultModelAtlasClientFactory}.
 *
 * <h2>Two consequences worth knowing</h2>
 *
 * The generated descriptor reaches the <em>jar</em> only, never the classes directory, so a
 * plain JUnit test cannot reach this class through {@code builder()} — {@code DcatAtlasClientTest}
 * constructs the factory directly for that reason. And the annotation adds a hard
 * {@code osgi.extender=osgi.serviceloader.registrar} requirement, so every bndrun containing
 * this bundle needs SPI-Fly to resolve; {@code client.osgi.tests} already has it.
 */
@ServiceProvider(DcatAtlasClientFactory.class)
public class DefaultDcatAtlasClientFactory implements DcatAtlasClientFactory {

	@Override
	public DcatAtlasClient.Builder builder() {
		return new DcatAtlasClientBuilderImpl();
	}
}
