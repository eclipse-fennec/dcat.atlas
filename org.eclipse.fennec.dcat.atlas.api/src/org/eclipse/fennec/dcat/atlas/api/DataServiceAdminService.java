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

import dcat.DataService;

/**
 * 
 * @author ilenia
 * @since Jul 7, 2026
 */
public interface DataServiceAdminService extends DataServiceReadOnlyService{
	
    DataService upsertDataService(DataService service);
    
    void deleteDataService(String id, boolean cascade);

}
