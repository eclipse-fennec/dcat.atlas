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
package org.eclipse.fennec.dcat.atlas.api.runtime;

import java.util.List;

import org.osgi.dto.DTO;

/**
 * What this portal is and what it holds, at one moment.
 *
 * <p>
 * A DTO and not the model, for the reason the OSGi DTOs exist: a reader gets a snapshot it
 * cannot reach through, and does not have to carry EMF to read a number. Everything here is a
 * copy of state that went on living the moment it was built.
 * </p>
 *
 * <p>
 * {@link #changeCount} is the number that also travels as the service's
 * {@code service.changecount} property, and it is the version a consumer compares: there is
 * no revision and no commit id here, because the storage layer's own is not public and would
 * not survive a change of backend. It counts changes rather than describing them — "has
 * anything happened" is a comparison, and "what" is another snapshot.
 * </p>
 *
 * @since 1.0
 */
public class DcatAtlasRuntimeDTO extends DTO {

	/** How often this snapshot has changed since the portal started. */
	public long changeCount;

	/** When this snapshot was built, in epoch milliseconds. */
	public long takenAt;

	/** What this portal calls things, and what it tells the world to call them. */
	public IdentityDTO identity;

	/**
	 * Where this portal is reachable.
	 *
	 * <p>
	 * What a deployment configured, when it configured anything. Otherwise what the Jakarta
	 * RS whiteboard says it is bound to — and since the HTTP service binds every interface
	 * by default, that is <em>every address of the host</em>: on a developer machine, each
	 * Docker bridge gateway and the LAN address, and typically not {@code localhost}. True,
	 * but operational rather than public.
	 * </p>
	 *
	 * <p>
	 * <strong>The address clients should use is {@link IdentityDTO#publicBase}</strong>, not
	 * this. A deployment that wants the two to agree sets {@code dcat.atlas.endpoint}; one
	 * behind a proxy has to, because the framework cannot see the address it is reached at.
	 * </p>
	 */
	public List<String> endpoints;

	/** Where what it holds is kept, described without naming a backend. */
	public StoreDTO store;

	/** What validation is configured to do, and what it will actually do. */
	public ValidationDTO validation;

	/** The SPARQL projection. */
	public GraphDTO graph;

	/** One per collection: how much is in it, and what it is called. */
	public List<CollectionDTO> collections;

	/** Every catalog, with what it holds. */
	public List<CatalogSnapshotDTO> catalogs;

	/** Everything configured that is not live, with the reason. Empty is the good case. */
	public List<FailureDTO> failures;
}
