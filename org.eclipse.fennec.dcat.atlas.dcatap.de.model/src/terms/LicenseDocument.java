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

import org.eclipse.emf.common.util.EList;

import org.osgi.annotation.versioning.ProviderType;

import rdf.IdentifiedResource;
import rdf.PlainLiteral;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>License Document</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link terms.LicenseDocument#getType <em>Type</em>}</li>
 *   <li>{@link terms.LicenseDocument#getTitle <em>Title</em>}</li>
 *   <li>{@link terms.LicenseDocument#getDescription <em>Description</em>}</li>
 *   <li>{@link terms.LicenseDocument#getIdentifier <em>Identifier</em>}</li>
 *   <li>{@link terms.LicenseDocument#getNodeID <em>Node ID</em>}</li>
 * </ul>
 *
 * @see terms.TermsPackage#getLicenseDocument()
 * @model annotation="http://www.eclipse.org/emf/2002/Ecore constraints='TypeIsIri'"
 *        annotation="http://www.eclipse.org/fennec/m2x/ocl/1.0 TypeIsIri='self.type-&gt;forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))'"
 *        extendedMetaData="name='LicenseDocument' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface LicenseDocument extends IdentifiedResource {
	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute list.
	 * @see terms.TermsPackage#getLicenseDocument_Type()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='type' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getType();

	/**
	 * Returns the value of the '<em><b>Title</b></em>' containment reference list.
	 * The list contents are of type {@link rdf.PlainLiteral}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Title</em>' containment reference list.
	 * @see terms.TermsPackage#getLicenseDocument_Title()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='title' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<PlainLiteral> getTitle();

	/**
	 * Returns the value of the '<em><b>Description</b></em>' containment reference list.
	 * The list contents are of type {@link rdf.PlainLiteral}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' containment reference list.
	 * @see terms.TermsPackage#getLicenseDocument_Description()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='description' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<PlainLiteral> getDescription();

	/**
	 * Returns the value of the '<em><b>Identifier</b></em>' containment reference list.
	 * The list contents are of type {@link rdf.PlainLiteral}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Identifier</em>' containment reference list.
	 * @see terms.TermsPackage#getLicenseDocument_Identifier()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='identifier' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<PlainLiteral> getIdentifier();

	/**
	 * Returns the value of the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node ID</em>' attribute.
	 * @see #setNodeID(String)
	 * @see terms.TermsPackage#getLicenseDocument_NodeID()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NCName"
	 *        extendedMetaData="kind='attribute' name='nodeID' namespace='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
	 * @generated
	 */
	String getNodeID();

	/**
	 * Sets the value of the '{@link terms.LicenseDocument#getNodeID <em>Node ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Node ID</em>' attribute.
	 * @see #getNodeID()
	 * @generated
	 */
	void setNodeID(String value);

} // LicenseDocument
