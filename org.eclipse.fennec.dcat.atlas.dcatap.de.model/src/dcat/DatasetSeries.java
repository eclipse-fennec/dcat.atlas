/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package dcat;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Dataset Series</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *         dcat:DatasetSeries represents a collection of datasets that are published
 *         separately, but share some common characteristics that group them
 *         (e.g. a time series or a set of regional datasets).
 *         New in DCAT-AP.de 3.0. Datasets reference their series via dcat:inSeries.
 *         title, description, publisher, contactPoint, issued and modified are
 *         inherited from DcatResource.
 *       
 * <!-- end-model-doc -->
 *
 *
 * @see dcat.DcatPackage#getDatasetSeries()
 * @model extendedMetaData="name='DatasetSeries' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface DatasetSeries extends Dataset {
} // DatasetSeries
