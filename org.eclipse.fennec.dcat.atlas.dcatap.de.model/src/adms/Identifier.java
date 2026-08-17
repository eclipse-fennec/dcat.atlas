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
package adms;

import org.osgi.annotation.versioning.ProviderType;

import rdf.IdentifiedResource;
import rdf.TypedLiteral;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Identifier</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link adms.Identifier#getNotation <em>Notation</em>}</li>
 * </ul>
 *
 * @see adms.AdmsPackage#getIdentifier()
 * @model extendedMetaData="name='Identifier' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface Identifier extends IdentifiedResource {
	/**
	 * Returns the value of the '<em><b>Notation</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Notation</em>' containment reference.
	 * @see #setNotation(TypedLiteral)
	 * @see adms.AdmsPackage#getIdentifier_Notation()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='notation' namespace='http://www.w3.org/2004/02/skos/core#'"
	 * @generated
	 */
	TypedLiteral getNotation();

	/**
	 * Sets the value of the '{@link adms.Identifier#getNotation <em>Notation</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Notation</em>' containment reference.
	 * @see #getNotation()
	 * @generated
	 */
	void setNotation(TypedLiteral value);

} // Identifier
