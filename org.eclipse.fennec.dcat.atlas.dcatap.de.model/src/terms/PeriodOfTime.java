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
package terms;

import org.osgi.annotation.versioning.ProviderType;

import rdf.DateOrDateTimeLiteral;
import rdf.IdentifiedResource;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Period Of Time</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link terms.PeriodOfTime#getStartDate <em>Start Date</em>}</li>
 *   <li>{@link terms.PeriodOfTime#getEndDate <em>End Date</em>}</li>
 *   <li>{@link terms.PeriodOfTime#getNodeID <em>Node ID</em>}</li>
 * </ul>
 *
 * @see terms.TermsPackage#getPeriodOfTime()
 * @model extendedMetaData="name='PeriodOfTime' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface PeriodOfTime extends IdentifiedResource {
	/**
	 * Returns the value of the '<em><b>Start Date</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start Date</em>' containment reference.
	 * @see #setStartDate(DateOrDateTimeLiteral)
	 * @see terms.TermsPackage#getPeriodOfTime_StartDate()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='startDate' namespace='http://www.w3.org/ns/dcat#'"
	 * @generated
	 */
	DateOrDateTimeLiteral getStartDate();

	/**
	 * Sets the value of the '{@link terms.PeriodOfTime#getStartDate <em>Start Date</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start Date</em>' containment reference.
	 * @see #getStartDate()
	 * @generated
	 */
	void setStartDate(DateOrDateTimeLiteral value);

	/**
	 * Returns the value of the '<em><b>End Date</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End Date</em>' containment reference.
	 * @see #setEndDate(DateOrDateTimeLiteral)
	 * @see terms.TermsPackage#getPeriodOfTime_EndDate()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='endDate' namespace='http://www.w3.org/ns/dcat#'"
	 * @generated
	 */
	DateOrDateTimeLiteral getEndDate();

	/**
	 * Sets the value of the '{@link terms.PeriodOfTime#getEndDate <em>End Date</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End Date</em>' containment reference.
	 * @see #getEndDate()
	 * @generated
	 */
	void setEndDate(DateOrDateTimeLiteral value);

	/**
	 * Returns the value of the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node ID</em>' attribute.
	 * @see #setNodeID(String)
	 * @see terms.TermsPackage#getPeriodOfTime_NodeID()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NCName"
	 *        extendedMetaData="kind='attribute' name='nodeID' namespace='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
	 * @generated
	 */
	String getNodeID();

	/**
	 * Sets the value of the '{@link terms.PeriodOfTime#getNodeID <em>Node ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Node ID</em>' attribute.
	 * @see #getNodeID()
	 * @generated
	 */
	void setNodeID(String value);

} // PeriodOfTime
