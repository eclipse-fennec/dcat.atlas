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

import java.util.Collection;
import java.util.Optional;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Polls the service registry for a service appearing or going away.
 * <p>
 * Every test here works by writing a ConfigAdmin configuration and then waiting for the
 * component it drives, and ConfigAdmin dispatches asynchronously — so "the service is not
 * there" and "the service is not there <em>yet</em>" need telling apart, and only a bounded
 * wait does that. A plain {@code @InjectService} cannot: the configurations are created by
 * the test body, not before it.
 */
public final class AwaitService {

	private AwaitService() {
	}

	/** How long to wait by default. Generous: an activation here opens an HTTP client. */
	public static final long TIMEOUT_MS = 10_000;

	/**
	 * Waits for one service matching {@code filter} and returns it.
	 *
	 * @param filter an LDAP filter over the service properties, or {@code null} for any
	 * @return the service, or empty if none appeared in time
	 */
	public static <T> Optional<T> appears(BundleContext ctx, Class<T> type, String filter)
			throws InterruptedException, InvalidSyntaxException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			Collection<ServiceReference<T>> refs = ctx.getServiceReferences(type, filter);
			if (!refs.isEmpty()) {
				return Optional.ofNullable(ctx.getService(refs.iterator().next()));
			}
			Thread.sleep(50);
		}
		return Optional.empty();
	}

	/**
	 * Waits until no service matches {@code filter}.
	 *
	 * @return {@code true} if the registry was clear within the timeout
	 */
	public static <T> boolean disappears(BundleContext ctx, Class<T> type, String filter)
			throws InterruptedException, InvalidSyntaxException {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			if (ctx.getServiceReferences(type, filter).isEmpty()) {
				return true;
			}
			Thread.sleep(50);
		}
		return false;
	}

	/** How many services currently match. */
	public static <T> int count(BundleContext ctx, Class<T> type, String filter) throws InvalidSyntaxException {
		return ctx.getServiceReferences(type, filter).size();
	}

	/**
	 * Waits until no <em>usable</em> service matches, i.e. one that can actually be obtained.
	 *
	 * <h2>Why a reference is not enough</h2>
	 *
	 * The client component is a <b>delayed</b> component: DS registers its service as soon as
	 * a configuration satisfies it and only builds the instance when somebody first asks for
	 * it. So a component whose activation refuses — the readiness gate throwing — still leaves
	 * a {@code ServiceReference} in the registry, and the refusal is visible only as
	 * {@code getService()} answering {@code null}. Checking for an absent reference would
	 * therefore test the wrong thing, and pass for the wrong reason.
	 *
	 * @return {@code true} if nothing matching {@code filter} could be obtained within
	 *         {@code millis}
	 */
	public static <T> boolean staysUnusable(BundleContext ctx, Class<T> type, String filter, long millis)
			throws InterruptedException, InvalidSyntaxException {
		long deadline = System.currentTimeMillis() + millis;
		while (System.currentTimeMillis() < deadline) {
			for (ServiceReference<T> reference : ctx.getServiceReferences(type, filter)) {
				if (ctx.getService(reference) != null) {
					return false;
				}
			}
			Thread.sleep(50);
		}
		return true;
	}

}
