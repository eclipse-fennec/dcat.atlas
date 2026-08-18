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
package vcard.impl;

import adms.AdmsPackage;

import adms.impl.AdmsPackageImpl;

import dcat.DcatPackage;

import dcat.impl.DcatPackageImpl;

import foaf.FoafPackage;

import foaf.impl.FoafPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EValidator;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import rdf.RdfPackage;

import rdf.impl.RdfPackageImpl;

import spdx.SpdxPackage;

import spdx.impl.SpdxPackageImpl;

import terms.TermsPackage;

import terms.impl.TermsPackageImpl;

import vcard.Address;
import vcard.Organization;
import vcard.VcardFactory;
import vcard.VcardPackage;

import vcard.util.VcardValidator;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class VcardPackageImpl extends EPackageImpl implements VcardPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass addressEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass organizationEClass = null;

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
	 * @see vcard.VcardPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private VcardPackageImpl() {
		super(eNS_URI, VcardFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link VcardPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static VcardPackage init() {
		if (isInited) return (VcardPackage)EPackage.Registry.INSTANCE.getEPackage(VcardPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredVcardPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		VcardPackageImpl theVcardPackage = registeredVcardPackage instanceof VcardPackageImpl ? (VcardPackageImpl)registeredVcardPackage : new VcardPackageImpl();

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
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(SpdxPackage.eNS_URI);
		SpdxPackageImpl theSpdxPackage = (SpdxPackageImpl)(registeredPackage instanceof SpdxPackageImpl ? registeredPackage : SpdxPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(TermsPackage.eNS_URI);
		TermsPackageImpl theTermsPackage = (TermsPackageImpl)(registeredPackage instanceof TermsPackageImpl ? registeredPackage : TermsPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(AdmsPackage.eNS_URI);
		AdmsPackageImpl theAdmsPackage = (AdmsPackageImpl)(registeredPackage instanceof AdmsPackageImpl ? registeredPackage : AdmsPackage.eINSTANCE);

		// Create package meta-data objects
		theVcardPackage.createPackageContents();
		theDcatPackage.createPackageContents();
		theFoafPackage.createPackageContents();
		theRdfPackage.createPackageContents();
		theSpdxPackage.createPackageContents();
		theTermsPackage.createPackageContents();
		theAdmsPackage.createPackageContents();

		// Initialize created meta-data
		theVcardPackage.initializePackageContents();
		theDcatPackage.initializePackageContents();
		theFoafPackage.initializePackageContents();
		theRdfPackage.initializePackageContents();
		theSpdxPackage.initializePackageContents();
		theTermsPackage.initializePackageContents();
		theAdmsPackage.initializePackageContents();

		// Register package validator
		EValidator.Registry.INSTANCE.put
			(theVcardPackage,
			 new EValidator.Descriptor() {
				 public EValidator getEValidator() {
					 return VcardValidator.INSTANCE;
				 }
			 });

		// Mark meta-data to indicate it can't be changed
		theVcardPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(VcardPackage.eNS_URI, theVcardPackage);
		return theVcardPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getAddress() {
		return addressEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAddress_StreetAddress() {
		return (EAttribute)addressEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAddress_Locality() {
		return (EAttribute)addressEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAddress_PostalCode() {
		return (EAttribute)addressEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAddress_CountryName() {
		return (EAttribute)addressEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAddress_NodeID() {
		return (EAttribute)addressEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getOrganization() {
		return organizationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrganization_Fn() {
		return (EAttribute)organizationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrganization_OrganizationName() {
		return (EAttribute)organizationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getOrganization_HasAddress() {
		return (EReference)organizationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrganization_HasTelephone() {
		return (EAttribute)organizationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrganization_NodeID() {
		return (EAttribute)organizationEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrganization_HasEmail() {
		return (EAttribute)organizationEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getOrganization_HasURL() {
		return (EAttribute)organizationEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public VcardFactory getVcardFactory() {
		return (VcardFactory)getEFactoryInstance();
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
		addressEClass = createEClass(ADDRESS);
		createEAttribute(addressEClass, ADDRESS__STREET_ADDRESS);
		createEAttribute(addressEClass, ADDRESS__LOCALITY);
		createEAttribute(addressEClass, ADDRESS__POSTAL_CODE);
		createEAttribute(addressEClass, ADDRESS__COUNTRY_NAME);
		createEAttribute(addressEClass, ADDRESS__NODE_ID);

		organizationEClass = createEClass(ORGANIZATION);
		createEAttribute(organizationEClass, ORGANIZATION__FN);
		createEAttribute(organizationEClass, ORGANIZATION__ORGANIZATION_NAME);
		createEReference(organizationEClass, ORGANIZATION__HAS_ADDRESS);
		createEAttribute(organizationEClass, ORGANIZATION__HAS_TELEPHONE);
		createEAttribute(organizationEClass, ORGANIZATION__NODE_ID);
		createEAttribute(organizationEClass, ORGANIZATION__HAS_EMAIL);
		createEAttribute(organizationEClass, ORGANIZATION__HAS_URL);
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
		addressEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());
		organizationEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());

		// Initialize classes, features, and operations; add parameters
		initEClass(addressEClass, Address.class, "Address", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAddress_StreetAddress(), theXMLTypePackage.getString(), "streetAddress", null, 1, 1, Address.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAddress_Locality(), theXMLTypePackage.getString(), "locality", null, 1, 1, Address.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAddress_PostalCode(), theXMLTypePackage.getString(), "postalCode", null, 1, 1, Address.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAddress_CountryName(), theXMLTypePackage.getString(), "countryName", null, 0, 1, Address.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAddress_NodeID(), theXMLTypePackage.getNCName(), "nodeID", null, 0, 1, Address.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(organizationEClass, Organization.class, "Organization", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOrganization_Fn(), theXMLTypePackage.getString(), "fn", null, 0, 1, Organization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOrganization_OrganizationName(), theXMLTypePackage.getString(), "organizationName", null, 0, 1, Organization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOrganization_HasAddress(), this.getAddress(), null, "hasAddress", null, 0, 1, Organization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOrganization_HasTelephone(), theXMLTypePackage.getAnyURI(), "hasTelephone", null, 0, -1, Organization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOrganization_NodeID(), theXMLTypePackage.getNCName(), "nodeID", null, 0, 1, Organization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOrganization_HasEmail(), theXMLTypePackage.getAnyURI(), "hasEmail", null, 0, -1, Organization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOrganization_HasURL(), theXMLTypePackage.getAnyURI(), "hasURL", null, 0, -1, Organization.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/Ecore
		createEcoreAnnotations();
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
		// http://www.eclipse.org/fennec/m2x/ocl/1.0
		create_1Annotations();
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
		  (organizationEClass,
		   source,
		   new String[] {
			   "constraints", "HasEmailIsMailto HasTelephoneIsTel HasURLIsIri"
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
		  (addressEClass,
		   source,
		   new String[] {
			   "name", "Address",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getAddress_StreetAddress(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "street-address",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAddress_Locality(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "locality",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAddress_PostalCode(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "postal-code",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAddress_CountryName(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "country-name",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAddress_NodeID(),
		   source,
		   new String[] {
			   "kind", "attribute",
			   "name", "nodeID",
			   "namespace", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		   });
		addAnnotation
		  (organizationEClass,
		   source,
		   new String[] {
			   "name", "Organization",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getOrganization_Fn(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "fn",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getOrganization_OrganizationName(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "organization-name",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getOrganization_HasAddress(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hasAddress",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getOrganization_HasTelephone(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hasTelephone",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getOrganization_NodeID(),
		   source,
		   new String[] {
			   "kind", "attribute",
			   "name", "nodeID",
			   "namespace", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		   });
		addAnnotation
		  (getOrganization_HasEmail(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hasEmail",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getOrganization_HasURL(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hasURL",
			   "namespace", "##targetNamespace"
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
		  (organizationEClass,
		   source,
		   new String[] {
			   "HasEmailIsMailto", "self.hasEmail->forAll(v | v.matches(\'mailto:[^\\\\s@]+@[^\\\\s@]+\'))",
			   "HasTelephoneIsTel", "self.hasTelephone->forAll(v | v.matches(\'tel:\\\\S+\'))",
			   "HasURLIsIri", "self.hasURL->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))"
		   });
	}

} //VcardPackageImpl
