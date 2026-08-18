/**
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
package spdx.impl;

import adms.AdmsPackage;

import adms.impl.AdmsPackageImpl;

import dcat.DcatPackage;

import dcat.impl.DcatPackageImpl;

import foaf.FoafPackage;

import foaf.impl.FoafPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EValidator;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import rdf.RdfPackage;

import rdf.impl.RdfPackageImpl;

import spdx.Checksum;
import spdx.SpdxFactory;
import spdx.SpdxPackage;

import spdx.util.SpdxValidator;

import terms.TermsPackage;

import terms.impl.TermsPackageImpl;

import vcard.VcardPackage;

import vcard.impl.VcardPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SpdxPackageImpl extends EPackageImpl implements SpdxPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass checksumEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see spdx.SpdxPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private SpdxPackageImpl() {
		super(eNS_URI, SpdxFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link SpdxPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static SpdxPackage init() {
		if (isInited) return (SpdxPackage)EPackage.Registry.INSTANCE.getEPackage(SpdxPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredSpdxPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		SpdxPackageImpl theSpdxPackage = registeredSpdxPackage instanceof SpdxPackageImpl ? (SpdxPackageImpl)registeredSpdxPackage : new SpdxPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		XMLTypePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DcatPackage.eNS_URI);
		DcatPackageImpl theDcatPackage = (DcatPackageImpl)(registeredPackage instanceof DcatPackageImpl ? registeredPackage : DcatPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(FoafPackage.eNS_URI);
		FoafPackageImpl theFoafPackage = (FoafPackageImpl)(registeredPackage instanceof FoafPackageImpl ? registeredPackage : FoafPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(RdfPackage.eNS_URI);
		RdfPackageImpl theRdfPackage = (RdfPackageImpl)(registeredPackage instanceof RdfPackageImpl ? registeredPackage : RdfPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(TermsPackage.eNS_URI);
		TermsPackageImpl theTermsPackage = (TermsPackageImpl)(registeredPackage instanceof TermsPackageImpl ? registeredPackage : TermsPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(VcardPackage.eNS_URI);
		VcardPackageImpl theVcardPackage = (VcardPackageImpl)(registeredPackage instanceof VcardPackageImpl ? registeredPackage : VcardPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(AdmsPackage.eNS_URI);
		AdmsPackageImpl theAdmsPackage = (AdmsPackageImpl)(registeredPackage instanceof AdmsPackageImpl ? registeredPackage : AdmsPackage.eINSTANCE);

		// Create package meta-data objects
		theSpdxPackage.createPackageContents();
		theDcatPackage.createPackageContents();
		theFoafPackage.createPackageContents();
		theRdfPackage.createPackageContents();
		theTermsPackage.createPackageContents();
		theVcardPackage.createPackageContents();
		theAdmsPackage.createPackageContents();

		// Initialize created meta-data
		theSpdxPackage.initializePackageContents();
		theDcatPackage.initializePackageContents();
		theFoafPackage.initializePackageContents();
		theRdfPackage.initializePackageContents();
		theTermsPackage.initializePackageContents();
		theVcardPackage.initializePackageContents();
		theAdmsPackage.initializePackageContents();

		// Register package validator
		EValidator.Registry.INSTANCE.put
			(theSpdxPackage,
			 new EValidator.Descriptor() {
				 public EValidator getEValidator() {
					 return SpdxValidator.INSTANCE;
				 }
			 });

		// Mark meta-data to indicate it can't be changed
		theSpdxPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(SpdxPackage.eNS_URI, theSpdxPackage);
		return theSpdxPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getChecksum() {
		return checksumEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getChecksum_ChecksumValue() {
		return (EAttribute)checksumEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getChecksum_NodeID() {
		return (EAttribute)checksumEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getChecksum_Algorithm() {
		return (EAttribute)checksumEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SpdxFactory getSpdxFactory() {
		return (SpdxFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		checksumEClass = createEClass(CHECKSUM);
		createEAttribute(checksumEClass, CHECKSUM__CHECKSUM_VALUE);
		createEAttribute(checksumEClass, CHECKSUM__NODE_ID);
		createEAttribute(checksumEClass, CHECKSUM__ALGORITHM);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		RdfPackage theRdfPackage = (RdfPackage)EPackage.Registry.INSTANCE.getEPackage(RdfPackage.eNS_URI);
		XMLTypePackage theXMLTypePackage = (XMLTypePackage)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		checksumEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());

		// Initialize classes, features, and operations; add parameters
		initEClass(checksumEClass, Checksum.class, "Checksum", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getChecksum_ChecksumValue(), theXMLTypePackage.getHexBinary(), "checksumValue", null, 1, 1, Checksum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getChecksum_NodeID(), theXMLTypePackage.getNCName(), "nodeID", null, 0, 1, Checksum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getChecksum_Algorithm(), theXMLTypePackage.getAnyURI(), "algorithm", null, 1, 1, Checksum.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/Ecore
		createEcoreAnnotations();
		// http://www.eclipse.org/fennec/m2x/ocl/1.0
		create_1Annotations();
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/Ecore</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createEcoreAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/Ecore";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "validationDelegates", "http://www.eclipse.org/fennec/m2x/ocl/1.0"
		   });
		addAnnotation
		  (checksumEClass,
		   source,
		   new String[] {
			   "constraints", "AlgorithmIsIri"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/fennec/m2x/ocl/1.0</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void create_1Annotations() {
		String source = "http://www.eclipse.org/fennec/m2x/ocl/1.0";
		addAnnotation
		  (checksumEClass,
		   source,
		   new String[] {
			   "AlgorithmIsIri", "self.algorithm = null or self.algorithm.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')"
		   });
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";
		addAnnotation
		  (checksumEClass,
		   source,
		   new String[] {
			   "name", "Checksum",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getChecksum_ChecksumValue(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "checksumValue",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getChecksum_NodeID(),
		   source,
		   new String[] {
			   "kind", "attribute",
			   "name", "nodeID",
			   "namespace", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		   });
		addAnnotation
		  (getChecksum_Algorithm(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "algorithm",
			   "namespace", "##targetNamespace"
		   });
	}

} //SpdxPackageImpl
