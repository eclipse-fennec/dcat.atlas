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
package org.eclipse.fennec.dcat.atlas.client.osgi.tests.helper;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jakartars.runtime.JakartarsServiceRuntime;
import org.osgi.service.jakartars.runtime.dto.ApplicationDTO;
import org.osgi.service.jakartars.runtime.dto.ResourceDTO;
import org.osgi.service.jakartars.runtime.dto.RuntimeDTO;

/**
 * Waits until the portal hosted in this framework is actually able to answer, before a test
 * points a client at it.
 * <p>
 * The osgitech/Jersey whiteboard composes every registered resource into one Jersey
 * application and <b>reloads the whole application whenever the set of resources changes</b>.
 * While a reload is in flight, requests to endpoints that are otherwise registered return
 * {@code 404}.
 * <p>
 * That matters more here than it does for a suite that speaks HTTP directly, because this
 * one goes through the client: a 404 during a reload is mapped to
 * {@code Optional.empty()} by a read and to a {@code NotFoundException} by a link, and
 * neither is distinguishable from the real thing. So the wait is for a <em>stable</em>
 * whiteboard — every resource present, and the change counter quiet for a while — not
 * merely for the resources to appear.
 */
public final class PortalReady {

	private PortalReady() {
	}

	/** The names ({@code @JakartarsName}) of every read + admin resource the portal registers. */
	public static final Set<String> ALL_RESOURCES = Set.of( //
			"CatalogReadOnlyResource", "CatalogAdminResource", //
			"DatasetReadOnlyResource", "DatasetAdminResource", //
			"DataServiceReadOnlyResource", "DataServiceAdminResource", //
			"DatasetSeriesReadOnlyResource", "DatasetSeriesAdminResource", //
			"DistributionReadOnlyResource", "DistributionAdminResource", //
			"SparqlResource", "SparqlAdminResource");

	/**
	 * Blocks until every resource in {@link #ALL_RESOURCES} is registered and the whiteboard
	 * has been quiescent for {@code quietMillis}, or the timeout elapses.
	 *
	 * @return {@code true} if a stable state was reached within the timeout
	 */
	public static boolean awaitStable(BundleContext ctx, long timeoutMillis, long quietMillis)
			throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		Object lastKey = null;
		long stableSince = System.currentTimeMillis();

		while (System.currentTimeMillis() < deadline) {
			ServiceReference<JakartarsServiceRuntime> ref = ctx.getServiceReference(JakartarsServiceRuntime.class);
			Set<String> present = Set.of();
			Object changeCount = null;
			if (ref != null) {
				changeCount = ref.getProperty("service.changecount");
				JakartarsServiceRuntime runtime = ctx.getService(ref);
				try {
					if (runtime != null) {
						present = resourceNames(runtime.getRuntimeDTO());
					}
				} finally {
					ctx.ungetService(ref);
				}
			}

			// Prefer the whiteboard's own change counter; fall back to the set of registered
			// resource names, whose changes are what trigger the reloads.
			Object key = changeCount != null ? changeCount : present;
			long now = System.currentTimeMillis();
			if (present.containsAll(ALL_RESOURCES) && key.equals(lastKey)) {
				if (now - stableSince >= quietMillis) {
					return true;
				}
			} else {
				lastKey = key;
				stableSince = now;
			}
			Thread.sleep(100);
		}
		return false;
	}

	/**
	 * What the whiteboard currently reports. For diagnostics: a test that times out waiting
	 * should be able to say what <em>did</em> register.
	 */
	public static Set<String> registeredResources(BundleContext ctx) {
		ServiceReference<JakartarsServiceRuntime> ref = ctx.getServiceReference(JakartarsServiceRuntime.class);
		if (ref == null) {
			return Set.of();
		}
		JakartarsServiceRuntime runtime = ctx.getService(ref);
		try {
			return runtime == null ? Set.of() : resourceNames(runtime.getRuntimeDTO());
		} finally {
			ctx.ungetService(ref);
		}
	}

	private static Set<String> resourceNames(RuntimeDTO dto) {
		Set<String> names = new HashSet<>();
		if (dto != null) {
			collect(dto.defaultApplication, names);
			if (dto.applicationDTOs != null) {
				for (ApplicationDTO app : dto.applicationDTOs) {
					collect(app, names);
				}
			}
		}
		return names;
	}

	private static void collect(ApplicationDTO app, Set<String> names) {
		if (app != null && app.resourceDTOs != null) {
			for (ResourceDTO resource : app.resourceDTOs) {
				names.add(resource.name);
			}
		}
	}
}
