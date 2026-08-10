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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Period Of Time</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link terms.PeriodOfTime#getPeriodOfTime <em>Period Of Time</em>}</li>
 * </ul>
 *
 * @see terms.TermsPackage#getPeriodOfTime()
 * @model extendedMetaData="name='PeriodOfTime' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface PeriodOfTime extends EObject {
	/**
	 * Returns the value of the '<em><b>Period Of Time</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Period Of Time</em>' containment reference.
	 * @see #setPeriodOfTime(PeriodOfTimeType)
	 * @see terms.TermsPackage#getPeriodOfTime_PeriodOfTime()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='PeriodOfTime' namespace='##targetNamespace'"
	 * @generated
	 */
	PeriodOfTimeType getPeriodOfTime();

	/**
	 * Sets the value of the '{@link terms.PeriodOfTime#getPeriodOfTime <em>Period Of Time</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Period Of Time</em>' containment reference.
	 * @see #getPeriodOfTime()
	 * @generated
	 */
	void setPeriodOfTime(PeriodOfTimeType value);

} // PeriodOfTime
