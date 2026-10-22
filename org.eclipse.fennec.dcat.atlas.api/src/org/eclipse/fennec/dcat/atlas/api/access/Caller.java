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
package org.eclipse.fennec.dcat.atlas.api.access;

/**
 * Who a write is from: the generating system the gateway authenticated it as, or
 * {@link #ANONYMOUS} for everybody else.
 * <p>
 * The portal authenticates nobody itself — the policy enforcement point is upstream
 * (APISix/Keycloak), which puts the authenticated client into a request header the REST
 * adapter turns into a {@code Caller}. The gateway must therefore also strip that header
 * from what the client sent, or ownership can simply be claimed.
 * <p>
 * A human admin working through the UI, and a direct in-process caller, both arrive as
 * {@link #ANONYMOUS}, which may write anything no system has claimed and nothing that one
 * has.
 *
 * @param system the authenticated generating system, or {@code null} when there is none;
 *               blank is normalised to {@code null} so an empty header cannot become an
 *               identity that only matches itself
 */
public record Caller(String system) {

	/** A caller the gateway identified as no particular system: a human, or a direct call. */
	public static final Caller ANONYMOUS = new Caller(null);

	public Caller {
		system = system == null || system.isBlank() ? null : system.strip();
	}

	/** The caller a header value names, or {@link #ANONYMOUS} when it names nobody. */
	public static Caller of(String system) {
		Caller caller = new Caller(system);
		return caller.system() == null ? ANONYMOUS : caller;
	}

	/** Whether this caller is a generating system rather than a human or a direct call. */
	public boolean isSystem() {
		return system != null;
	}

	/**
	 * Whether this caller may write a resource managed by {@code managedBy} — the whole
	 * access decision, which the store asks on every write, delete and collateral rewrite.
	 * <p>
	 * Unmanaged resources are open to everyone; a managed one is writable only by the
	 * system managing it. Reads are not restricted at all: this protects a generated
	 * resource from being changed, not from being seen.
	 *
	 * @param managedBy the stored {@code managedBy}, or {@code null}/blank when no system
	 *                  manages the resource
	 */
	public boolean mayWrite(String managedBy) {
		return managedBy == null || managedBy.isBlank() || managedBy.equals(system);
	}

	/**
	 * Refuses the write unless {@link #mayWrite} allows it.
	 *
	 * @param resource the resource's identity, for the refusal to name
	 * @throws SystemManagedException if this caller is not the managing system
	 */
	public void requireMayWrite(String managedBy, String resource) {
		if (!mayWrite(managedBy)) {
			throw new SystemManagedException("Cannot change %s: it is managed by %s%s".formatted(resource, managedBy,
					isSystem() ? " and not by " + system : ""), managedBy, resource);
		}
	}

	/**
	 * The identity to stamp on a resource nobody manages yet: itself when it is a system,
	 * nothing when it is not.
	 * <p>
	 * A replace claims an unmanaged resource as much as a create does. Whatever a system
	 * last wrote in full is its content, and claiming on write is also what marks the
	 * resources stored before management existed, with no migration step. The cost is that
	 * a system write to a resource a person created takes it out of their hands, which
	 * needs the system to address their id deliberately.
	 */
	public String stamp() {
		return system;
	}
}
