/*
 */
package spdx;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

import rdf.Resource;

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
 *   <li>{@link spdx.Checksum#getAlgorithm <em>Algorithm</em>}</li>
 *   <li>{@link spdx.Checksum#getChecksumValue <em>Checksum Value</em>}</li>
 *   <li>{@link spdx.Checksum#getAbout <em>About</em>}</li>
 *   <li>{@link spdx.Checksum#getNodeID <em>Node ID</em>}</li>
 * </ul>
 *
 * @see spdx.SpdxPackage#getChecksum()
 * @model extendedMetaData="name='Checksum' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface Checksum extends EObject {
	/**
	 * Returns the value of the '<em><b>Algorithm</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Algorithm</em>' containment reference.
	 * @see #setAlgorithm(Resource)
	 * @see spdx.SpdxPackage#getChecksum_Algorithm()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='algorithm' namespace='##targetNamespace'"
	 * @generated
	 */
	Resource getAlgorithm();

	/**
	 * Sets the value of the '{@link spdx.Checksum#getAlgorithm <em>Algorithm</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Algorithm</em>' containment reference.
	 * @see #getAlgorithm()
	 * @generated
	 */
	void setAlgorithm(Resource value);

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
	 * Returns the value of the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>About</em>' attribute.
	 * @see #setAbout(String)
	 * @see spdx.SpdxPackage#getChecksum_About()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='attribute' name='about' namespace='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
	 * @generated
	 */
	String getAbout();

	/**
	 * Sets the value of the '{@link spdx.Checksum#getAbout <em>About</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>About</em>' attribute.
	 * @see #getAbout()
	 * @generated
	 */
	void setAbout(String value);

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

} // Checksum
