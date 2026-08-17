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


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

import rdf.RdfPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * 
 *       SPDX XML Schema (relevant parts for DCAT-AP.de 3.0)
 *       http://spdx.org/rdf/terms#
 *       spdx:Checksum (DCAT-AP.de 3.0 §4.12) is referenced from dcat:Distribution via spdx:checksum.
 *     
 * <!-- end-model-doc -->
 * @see spdx.SpdxFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = SpdxPackage.eNS_URI, fingerprint = "fp1:e88f2780921fc274ea5115ed22d221684c50b3a19116bb0163c66c9f0a8af3c1", genModel = "/model/dcatap.genmodel", genModelSourceLocations = {"model/dcatap.genmodel","org.eclipse.fennec.dcat.atlas.dcatap.de.model/model/dcatap.genmodel"}, ecore = "/model/spdx.ecore", ecoreSourceLocations = "/model/spdx.ecore")
public interface SpdxPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "spdx";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://spdx.org/rdf/terms#";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "spdx";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SpdxPackage eINSTANCE = spdx.impl.SpdxPackageImpl.init();

	/**
	 * The meta object id for the '{@link spdx.impl.ChecksumImpl <em>Checksum</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see spdx.impl.ChecksumImpl
	 * @see spdx.impl.SpdxPackageImpl#getChecksum()
	 * @generated
	 */
	int CHECKSUM = 0;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHECKSUM__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Checksum Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHECKSUM__CHECKSUM_VALUE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHECKSUM__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Algorithm</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHECKSUM__ALGORITHM = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Checksum</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHECKSUM_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Checksum</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CHECKSUM_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link spdx.Checksum <em>Checksum</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Checksum</em>'.
	 * @see spdx.Checksum
	 * @generated
	 */
	EClass getChecksum();

	/**
	 * Returns the meta object for the attribute '{@link spdx.Checksum#getChecksumValue <em>Checksum Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Checksum Value</em>'.
	 * @see spdx.Checksum#getChecksumValue()
	 * @see #getChecksum()
	 * @generated
	 */
	EAttribute getChecksum_ChecksumValue();

	/**
	 * Returns the meta object for the attribute '{@link spdx.Checksum#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see spdx.Checksum#getNodeID()
	 * @see #getChecksum()
	 * @generated
	 */
	EAttribute getChecksum_NodeID();

	/**
	 * Returns the meta object for the attribute '{@link spdx.Checksum#getAlgorithm <em>Algorithm</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Algorithm</em>'.
	 * @see spdx.Checksum#getAlgorithm()
	 * @see #getChecksum()
	 * @generated
	 */
	EAttribute getChecksum_Algorithm();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	SpdxFactory getSpdxFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link spdx.impl.ChecksumImpl <em>Checksum</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see spdx.impl.ChecksumImpl
		 * @see spdx.impl.SpdxPackageImpl#getChecksum()
		 * @generated
		 */
		EClass CHECKSUM = eINSTANCE.getChecksum();

		/**
		 * The meta object literal for the '<em><b>Checksum Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CHECKSUM__CHECKSUM_VALUE = eINSTANCE.getChecksum_ChecksumValue();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CHECKSUM__NODE_ID = eINSTANCE.getChecksum_NodeID();

		/**
		 * The meta object literal for the '<em><b>Algorithm</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CHECKSUM__ALGORITHM = eINSTANCE.getChecksum_Algorithm();

	}

} //SpdxPackage
