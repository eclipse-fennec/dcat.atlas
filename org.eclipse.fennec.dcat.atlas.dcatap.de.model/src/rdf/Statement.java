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
package rdf;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Statement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link rdf.Statement#getStatement <em>Statement</em>}</li>
 * </ul>
 *
 * @see rdf.RdfPackage#getStatement()
 * @model extendedMetaData="name='Statement' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface Statement extends EObject {
	/**
	 * Returns the value of the '<em><b>Statement</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Statement</em>' containment reference.
	 * @see #setStatement(StatementType)
	 * @see rdf.RdfPackage#getStatement_Statement()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='Statement' namespace='##targetNamespace'"
	 * @generated
	 */
	StatementType getStatement();

	/**
	 * Sets the value of the '{@link rdf.Statement#getStatement <em>Statement</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Statement</em>' containment reference.
	 * @see #getStatement()
	 * @generated
	 */
	void setStatement(StatementType value);

} // Statement
