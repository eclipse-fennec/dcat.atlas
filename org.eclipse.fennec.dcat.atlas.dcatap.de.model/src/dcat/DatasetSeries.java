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

import org.eclipse.emf.common.util.EList;

import org.osgi.annotation.versioning.ProviderType;

import skos.Concept;

import terms.Location;
import terms.PeriodOfTime;

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
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link dcat.DatasetSeries#getAccrualPeriodicity <em>Accrual Periodicity</em>}</li>
 *   <li>{@link dcat.DatasetSeries#getSpatial <em>Spatial</em>}</li>
 *   <li>{@link dcat.DatasetSeries#getTemporal <em>Temporal</em>}</li>
 * </ul>
 *
 * @see dcat.DcatPackage#getDatasetSeries()
 * @model extendedMetaData="name='DatasetSeries' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface DatasetSeries extends DcatResource {
	/**
	 * Returns the value of the '<em><b>Accrual Periodicity</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Accrual Periodicity</em>' containment reference.
	 * @see #setAccrualPeriodicity(Concept)
	 * @see dcat.DcatPackage#getDatasetSeries_AccrualPeriodicity()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='accrualPeriodicity' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	Concept getAccrualPeriodicity();

	/**
	 * Sets the value of the '{@link dcat.DatasetSeries#getAccrualPeriodicity <em>Accrual Periodicity</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Accrual Periodicity</em>' containment reference.
	 * @see #getAccrualPeriodicity()
	 * @generated
	 */
	void setAccrualPeriodicity(Concept value);

	/**
	 * Returns the value of the '<em><b>Spatial</b></em>' containment reference list.
	 * The list contents are of type {@link terms.Location}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Spatial</em>' containment reference list.
	 * @see dcat.DcatPackage#getDatasetSeries_Spatial()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='spatial' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<Location> getSpatial();

	/**
	 * Returns the value of the '<em><b>Temporal</b></em>' containment reference list.
	 * The list contents are of type {@link terms.PeriodOfTime}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Temporal</em>' containment reference list.
	 * @see dcat.DcatPackage#getDatasetSeries_Temporal()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='temporal' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<PeriodOfTime> getTemporal();

} // DatasetSeries
