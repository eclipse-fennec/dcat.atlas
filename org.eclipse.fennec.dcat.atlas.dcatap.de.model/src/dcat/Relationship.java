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

import rdf.IdentifiedResource;
import rdf.PlainLiteral;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Relationship</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link dcat.Relationship#getRelation <em>Relation</em>}</li>
 *   <li>{@link dcat.Relationship#getHadRole <em>Had Role</em>}</li>
 *   <li>{@link dcat.Relationship#getDescription <em>Description</em>}</li>
 *   <li>{@link dcat.Relationship#getNodeID <em>Node ID</em>}</li>
 * </ul>
 *
 * @see dcat.DcatPackage#getRelationship()
 * @model extendedMetaData="name='Relationship' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface Relationship extends IdentifiedResource {
	/**
	 * Returns the value of the '<em><b>Relation</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Relation</em>' attribute list.
	 * @see dcat.DcatPackage#getRelationship_Relation()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI" required="true"
	 *        extendedMetaData="kind='element' name='relation' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<String> getRelation();

	/**
	 * Returns the value of the '<em><b>Had Role</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Had Role</em>' attribute list.
	 * @see dcat.DcatPackage#getRelationship_HadRole()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI" required="true"
	 *        extendedMetaData="kind='element' name='hadRole' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getHadRole();

	/**
	 * Returns the value of the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' containment reference.
	 * @see #setDescription(PlainLiteral)
	 * @see dcat.DcatPackage#getRelationship_Description()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='description' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	PlainLiteral getDescription();

	/**
	 * Sets the value of the '{@link dcat.Relationship#getDescription <em>Description</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' containment reference.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(PlainLiteral value);

	/**
	 * Returns the value of the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node ID</em>' attribute.
	 * @see #setNodeID(String)
	 * @see dcat.DcatPackage#getRelationship_NodeID()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NCName"
	 *        extendedMetaData="kind='attribute' name='nodeID' namespace='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
	 * @generated
	 */
	String getNodeID();

	/**
	 * Sets the value of the '{@link dcat.Relationship#getNodeID <em>Node ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Node ID</em>' attribute.
	 * @see #getNodeID()
	 * @generated
	 */
	void setNodeID(String value);

} // Relationship
