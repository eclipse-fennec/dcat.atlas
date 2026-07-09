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
package org.eclipse.fennec.dcat.atlas.api;

import java.util.List;
import java.util.Optional;

import dcat.DataService;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
public interface DataServiceReadOnlyService {
	
	Optional<DataService> getDataService(String id);

    List<DataService> listDataServices();

    /** Strong ETag validator for the stored data service {@code id}, or empty if absent (F-16). */
    Optional<String> etag(String id);

}
