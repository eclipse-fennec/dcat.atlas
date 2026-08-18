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
package spdx;

import org.osgi.annotation.versioning.ProviderType;

import rdf.IdentifiedResource;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Checksum</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *         spdx:Checksum — a value that allows the content of a file to be
 *         verified (DCAT-AP.de 3.0 §4.12). Referenced from dcat:Distribution
 *         via spdx:checksum. Carries the algorithm used and the resulting value.
 *       
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link spdx.Checksum#getChecksumValue <em>Checksum Value</em>}</li>
 *   <li>{@link spdx.Checksum#getNodeID <em>Node ID</em>}</li>
 *   <li>{@link spdx.Checksum#getAlgorithm <em>Algorithm</em>}</li>
 * </ul>
 *
 * @see spdx.SpdxPackage#getChecksum()
 * @model annotation="http://www.eclipse.org/emf/2002/Ecore constraints='AlgorithmIsIri'"
 *        annotation="http://www.eclipse.org/fennec/m2x/ocl/1.0 AlgorithmIsIri='self.algorithm = null or self.algorithm.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')'"
 *        extendedMetaData="name='Checksum' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface Checksum extends IdentifiedResource {
	/**
	 * Returns the value of the '<em><b>Checksum Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Checksum Value</em>' attribute.
	 * @see #setChecksumValue(byte[])
	 * @see spdx.SpdxPackage#getChecksum_ChecksumValue()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.HexBinary" required="true"
	 *        extendedMetaData="kind='element' name='checksumValue' namespace='##targetNamespace'"
	 * @generated
	 */
	byte[] getChecksumValue();

	/**
	 * Sets the value of the '{@link spdx.Checksum#getChecksumValue <em>Checksum Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Checksum Value</em>' attribute.
	 * @see #getChecksumValue()
	 * @generated
	 */
	void setChecksumValue(byte[] value);

	/**
	 * Returns the value of the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node ID</em>' attribute.
	 * @see #setNodeID(String)
	 * @see spdx.SpdxPackage#getChecksum_NodeID()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NCName"
	 *        extendedMetaData="kind='attribute' name='nodeID' namespace='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
	 * @generated
	 */
	String getNodeID();

	/**
	 * Sets the value of the '{@link spdx.Checksum#getNodeID <em>Node ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Node ID</em>' attribute.
	 * @see #getNodeID()
	 * @generated
	 */
	void setNodeID(String value);

	/**
	 * Returns the value of the '<em><b>Algorithm</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Algorithm</em>' attribute.
	 * @see #setAlgorithm(String)
	 * @see spdx.SpdxPackage#getChecksum_Algorithm()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI" required="true"
	 *        extendedMetaData="kind='element' name='algorithm' namespace='##targetNamespace'"
	 * @generated
	 */
	String getAlgorithm();

	/**
	 * Sets the value of the '{@link spdx.Checksum#getAlgorithm <em>Algorithm</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Algorithm</em>' attribute.
	 * @see #getAlgorithm()
	 * @generated
	 */
	void setAlgorithm(String value);

} // Checksum
