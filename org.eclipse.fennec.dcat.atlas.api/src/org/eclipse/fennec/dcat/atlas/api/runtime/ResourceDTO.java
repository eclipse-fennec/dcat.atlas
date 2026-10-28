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

import org.osgi.dto.DTO;

/**
 * One stored resource, named both ways.
 * <p>
 * Both names are here because neither alone is enough: the id is what the collection is keyed
 * by and what a store operation takes, the IRI is what a caller can dereference. Deriving one
 * from the other needs the public base, which a consumer of this snapshot has no business
 * reassembling.
 * <p>
 * No title: a title has to be read out of the resource itself, and a snapshot that read every
 * dataset to name it would cost more than everything it reports. Catalogues are the exception
 * and have their own type.
 *
 * @since 1.0
 */
public class ResourceDTO extends DTO {

	/** The id it is stored under. */
	public String id;

	/** The IRI it is served as — the public one, which is what a caller can dereference. */
	public String iri;
}
