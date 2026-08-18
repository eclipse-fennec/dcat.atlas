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
package vcard;

import org.eclipse.emf.common.util.EList;

import org.osgi.annotation.versioning.ProviderType;

import rdf.IdentifiedResource;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Organization</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link vcard.Organization#getFn <em>Fn</em>}</li>
 *   <li>{@link vcard.Organization#getOrganizationName <em>Organization Name</em>}</li>
 *   <li>{@link vcard.Organization#getHasAddress <em>Has Address</em>}</li>
 *   <li>{@link vcard.Organization#getHasTelephone <em>Has Telephone</em>}</li>
 *   <li>{@link vcard.Organization#getNodeID <em>Node ID</em>}</li>
 *   <li>{@link vcard.Organization#getHasEmail <em>Has Email</em>}</li>
 *   <li>{@link vcard.Organization#getHasURL <em>Has URL</em>}</li>
 * </ul>
 *
 * @see vcard.VcardPackage#getOrganization()
 * @model annotation="http://www.eclipse.org/emf/2002/Ecore constraints='HasEmailIsMailto HasTelephoneIsTel HasURLIsIri'"
 *        annotation="http://www.eclipse.org/fennec/m2x/ocl/1.0 HasEmailIsMailto='self.hasEmail-&gt;forAll(v | v.matches(\'mailto:[^\\\\s@]+@[^\\\\s@]+\'))' HasTelephoneIsTel='self.hasTelephone-&gt;forAll(v | v.matches(\'tel:\\\\S+\'))' HasURLIsIri='self.hasURL-&gt;forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))'"
 *        extendedMetaData="name='Organization' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface Organization extends IdentifiedResource {
	/**
	 * Returns the value of the '<em><b>Fn</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Fn</em>' attribute.
	 * @see #setFn(String)
	 * @see vcard.VcardPackage#getOrganization_Fn()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='fn' namespace='##targetNamespace'"
	 * @generated
	 */
	String getFn();

	/**
	 * Sets the value of the '{@link vcard.Organization#getFn <em>Fn</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fn</em>' attribute.
	 * @see #getFn()
	 * @generated
	 */
	void setFn(String value);

	/**
	 * Returns the value of the '<em><b>Organization Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Organization Name</em>' attribute.
	 * @see #setOrganizationName(String)
	 * @see vcard.VcardPackage#getOrganization_OrganizationName()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='organization-name' namespace='##targetNamespace'"
	 * @generated
	 */
	String getOrganizationName();

	/**
	 * Sets the value of the '{@link vcard.Organization#getOrganizationName <em>Organization Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Organization Name</em>' attribute.
	 * @see #getOrganizationName()
	 * @generated
	 */
	void setOrganizationName(String value);

	/**
	 * Returns the value of the '<em><b>Has Address</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Address</em>' containment reference.
	 * @see #setHasAddress(Address)
	 * @see vcard.VcardPackage#getOrganization_HasAddress()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='hasAddress' namespace='##targetNamespace'"
	 * @generated
	 */
	Address getHasAddress();

	/**
	 * Sets the value of the '{@link vcard.Organization#getHasAddress <em>Has Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Address</em>' containment reference.
	 * @see #getHasAddress()
	 * @generated
	 */
	void setHasAddress(Address value);

	/**
	 * Returns the value of the '<em><b>Has Telephone</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Telephone</em>' attribute list.
	 * @see vcard.VcardPackage#getOrganization_HasTelephone()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='hasTelephone' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getHasTelephone();

	/**
	 * Returns the value of the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node ID</em>' attribute.
	 * @see #setNodeID(String)
	 * @see vcard.VcardPackage#getOrganization_NodeID()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NCName"
	 *        extendedMetaData="kind='attribute' name='nodeID' namespace='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
	 * @generated
	 */
	String getNodeID();

	/**
	 * Sets the value of the '{@link vcard.Organization#getNodeID <em>Node ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Node ID</em>' attribute.
	 * @see #getNodeID()
	 * @generated
	 */
	void setNodeID(String value);

	/**
	 * Returns the value of the '<em><b>Has Email</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Email</em>' attribute list.
	 * @see vcard.VcardPackage#getOrganization_HasEmail()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='hasEmail' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getHasEmail();

	/**
	 * Returns the value of the '<em><b>Has URL</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has URL</em>' attribute list.
	 * @see vcard.VcardPackage#getOrganization_HasURL()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='hasURL' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getHasURL();

} // Organization
