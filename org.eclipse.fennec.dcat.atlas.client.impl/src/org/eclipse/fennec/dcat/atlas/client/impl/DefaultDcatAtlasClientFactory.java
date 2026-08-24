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
import org.osgi.service.component.annotations.Component;

/**
 * Makes the client reachable both ways: as an OSGi service, and as
 * {@link DcatAtlasClient#builder()} on a plain classpath.
 *
 * <h2>Two mechanisms, deliberately</h2>
 *
 * A DS component for OSGi, so the front-end can take this from the service registry, and the
 * hand-written {@code META-INF/services} descriptor beside this package for plain Java, where
 * {@code builder()} finds it through {@code ServiceLoader}. The annotation is inert off a
 * framework and the descriptor is ignored on one, so neither gets in the other's way.
 *
 * <h2>Why not bnd's {@code @ServiceProvider}</h2>
 *
 * It looks like the tidier answer — one annotation emitting the descriptor <em>and</em> the
 * {@code osgi.serviceloader} capability — and it was what this class used first. Two problems.
 * It needs {@code biz.aQute.bnd.annotation} on the buildpath, which
 * {@code cnf/ext/central.mvn} pulls in at {@code ${bndversion}} — the version of whichever
 * bnd is driving — so the bundle resolves under a gradle build pinned to one version and
 * fails to compile in an IDE running another. And the capability it emits only becomes a
 * usable service through a ServiceLoader mediator, which made SPI-Fly load-bearing for
 * reaching our own factory. A DS component needs neither: SCR registers it, and bnd derives
 * the {@code osgi.service} capability from the component itself.
 */
@Component(service = DcatAtlasClientFactory.class)
public class DefaultDcatAtlasClientFactory implements DcatAtlasClientFactory {

	@Override
	public DcatAtlasClient.Builder builder() {
		return new DcatAtlasClientBuilderImpl();
	}
}
