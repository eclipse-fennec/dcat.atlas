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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.dcat.atlas.rest.tests.helper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jakartars.runtime.JakartarsServiceRuntime;
import org.osgi.service.jakartars.runtime.dto.ApplicationDTO;
import org.osgi.service.jakartars.runtime.dto.ResourceDTO;
import org.osgi.service.jakartars.runtime.dto.RuntimeDTO;

/**
 * Waits until the Jakarta REST whiteboard has reached a <em>stable</em> state
 * before a test issues its first request.
 * <p>
 * The osgitech/Jersey whiteboard composes every registered resource into a
 * single Jersey application and <strong>reloads that whole application whenever
 * the set of resources changes</strong>. While a reload is in flight, requests
 * to endpoints that are otherwise registered transiently return {@code 404}.
 * <p>
 * All integration tests here share one framework, so if some resources register
 * late (e.g. because they depend on other services and activate in a staggered
 * order) a reload can land in the middle of an unrelated test, producing
 * intermittent, cross-cutting 404s. {@link ResourceAware} only reports that a
 * resource's DTO is <em>present</em> — it does not wait for the reloads to stop.
 * <p>
 * This helper instead polls the runtime until (a) all expected resources are
 * present, (b) the {@link PublicIris} service every collection resource holds as
 * a mandatory reference is registered, and (c) neither the whiteboard's change
 * indicator, its set of resource names, nor that service's identity has moved for
 * a short window, i.e. no further reloads are pending.
 * <p>
 * Watching the whiteboard alone is not enough. It reports its own view, and its
 * change counter does not move when a service one of its resources depends on is
 * <em>replaced</em> — yet that replacement unregisters and re-registers every
 * resource holding it, which is another reload. {@code PublicIris} is the one such
 * service common to all of them, and since it requires its configuration it can
 * arrive, or be replaced, later than the resources first appear.
 */
public final class RestReady {

	private RestReady() {
	}

	/** The names ({@code @JakartarsName}) of every read + admin resource in the suite. */
	public static final Set<String> ALL_RESOURCES = Set.of( //
			"CatalogReadOnlyResource", "CatalogAdminResource", //
			"DatasetReadOnlyResource", "DatasetAdminResource", //
			"DataServiceReadOnlyResource", "DataServiceAdminResource", //
			"DatasetSeriesReadOnlyResource", "DatasetSeriesAdminResource", //
			"DistributionReadOnlyResource", "DistributionAdminResource", //
			"SparqlResource", "SparqlAdminResource");

	/**
	 * Blocks until all {@code required} resources are registered and the whiteboard
	 * has been quiescent for {@code quietMillis}, or the timeout elapses.
	 *
	 * @return {@code true} if a stable state was reached within the timeout
	 */
	public static boolean awaitStable(BundleContext ctx, Set<String> required, long timeoutMillis, long quietMillis)
			throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		List<Object> lastKey = null;
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

			// All three, rather than the change counter alone: a resource can be
			// unregistered and re-registered between two polls without the counter
			// differing from the one we recorded, and a replaced PublicIris does not
			// touch the counter at all. Arrays.asList because any of them can be null.
			Long irisId = serviceId(ctx, PublicIris.class);
			List<Object> key = Arrays.asList(changeCount, present, irisId);
			long now = System.currentTimeMillis();
			if (irisId != null && present.containsAll(required) && key.equals(lastKey)) {
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
	 * The {@code service.id} of the registered {@code type}, or {@code null} when
	 * none is. The id rather than a boolean, so that a service replaced between two
	 * polls is seen as a change rather than as continuous presence.
	 */
	private static Long serviceId(BundleContext ctx, Class<?> type) {
		ServiceReference<?> ref = ctx.getServiceReference(type);
		return ref == null ? null : (Long) ref.getProperty(Constants.SERVICE_ID);
	}

	/**
	 * The resource names the whiteboard currently reports. For diagnostics: a test
	 * that times out waiting should be able to say what <em>did</em> register.
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
