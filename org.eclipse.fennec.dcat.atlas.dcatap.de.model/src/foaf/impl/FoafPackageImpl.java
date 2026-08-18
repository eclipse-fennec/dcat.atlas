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
package foaf.impl;

import adms.AdmsPackage;

import adms.impl.AdmsPackageImpl;

import dcat.DcatPackage;

import dcat.impl.DcatPackageImpl;

import foaf.Agent;
import foaf.Document;
import foaf.FoafFactory;
import foaf.FoafPackage;
import foaf.Organization;
import foaf.Person;

import foaf.util.FoafValidator;

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

import vcard.VcardPackage;

import vcard.impl.VcardPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class FoafPackageImpl extends EPackageImpl implements FoafPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass agentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass documentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass organizationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass personEClass = null;

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
	 * @see foaf.FoafPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private FoafPackageImpl() {
		super(eNS_URI, FoafFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link FoafPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static FoafPackage init() {
		if (isInited) return (FoafPackage)EPackage.Registry.INSTANCE.getEPackage(FoafPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredFoafPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		FoafPackageImpl theFoafPackage = registeredFoafPackage instanceof FoafPackageImpl ? (FoafPackageImpl)registeredFoafPackage : new FoafPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		XMLTypePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DcatPackage.eNS_URI);
		DcatPackageImpl theDcatPackage = (DcatPackageImpl)(registeredPackage instanceof DcatPackageImpl ? registeredPackage : DcatPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(RdfPackage.eNS_URI);
		RdfPackageImpl theRdfPackage = (RdfPackageImpl)(registeredPackage instanceof RdfPackageImpl ? registeredPackage : RdfPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(SpdxPackage.eNS_URI);
		SpdxPackageImpl theSpdxPackage = (SpdxPackageImpl)(registeredPackage instanceof SpdxPackageImpl ? registeredPackage : SpdxPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(TermsPackage.eNS_URI);
		TermsPackageImpl theTermsPackage = (TermsPackageImpl)(registeredPackage instanceof TermsPackageImpl ? registeredPackage : TermsPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(VcardPackage.eNS_URI);
		VcardPackageImpl theVcardPackage = (VcardPackageImpl)(registeredPackage instanceof VcardPackageImpl ? registeredPackage : VcardPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(AdmsPackage.eNS_URI);
		AdmsPackageImpl theAdmsPackage = (AdmsPackageImpl)(registeredPackage instanceof AdmsPackageImpl ? registeredPackage : AdmsPackage.eINSTANCE);

		// Create package meta-data objects
		theFoafPackage.createPackageContents();
		theDcatPackage.createPackageContents();
		theRdfPackage.createPackageContents();
		theSpdxPackage.createPackageContents();
		theTermsPackage.createPackageContents();
		theVcardPackage.createPackageContents();
		theAdmsPackage.createPackageContents();

		// Initialize created meta-data
		theFoafPackage.initializePackageContents();
		theDcatPackage.initializePackageContents();
		theRdfPackage.initializePackageContents();
		theSpdxPackage.initializePackageContents();
		theTermsPackage.initializePackageContents();
		theVcardPackage.initializePackageContents();
		theAdmsPackage.initializePackageContents();

		// Register package validator
		EValidator.Registry.INSTANCE.put
			(theFoafPackage,
			 new EValidator.Descriptor() {
				 public EValidator getEValidator() {
					 return FoafValidator.INSTANCE;
				 }
			 });

		// Mark meta-data to indicate it can't be changed
		theFoafPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(FoafPackage.eNS_URI, theFoafPackage);
		return theFoafPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getAgent() {
		return agentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getAgent_Name() {
		return (EReference)agentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAgent_Type() {
		return (EAttribute)agentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAgent_NodeID() {
		return (EAttribute)agentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAgent_Phone() {
		return (EAttribute)agentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getAgent_Mbox() {
		return (EAttribute)agentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDocument() {
		return documentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDocument_Name() {
		return (EReference)documentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDocument_NodeID() {
		return (EAttribute)documentEClass.getEStructuralFeatures().get(1);
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
	public EClass getPerson() {
		return personEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FoafFactory getFoafFactory() {
		return (FoafFactory)getEFactoryInstance();
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
		agentEClass = createEClass(AGENT);
		createEReference(agentEClass, AGENT__NAME);
		createEAttribute(agentEClass, AGENT__TYPE);
		createEAttribute(agentEClass, AGENT__NODE_ID);
		createEAttribute(agentEClass, AGENT__PHONE);
		createEAttribute(agentEClass, AGENT__MBOX);

		documentEClass = createEClass(DOCUMENT);
		createEReference(documentEClass, DOCUMENT__NAME);
		createEAttribute(documentEClass, DOCUMENT__NODE_ID);

		organizationEClass = createEClass(ORGANIZATION);

		personEClass = createEClass(PERSON);
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
		agentEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());
		documentEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());
		organizationEClass.getESuperTypes().add(this.getAgent());
		personEClass.getESuperTypes().add(this.getAgent());

		// Initialize classes, features, and operations; add parameters
		initEClass(agentEClass, Agent.class, "Agent", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getAgent_Name(), theRdfPackage.getPlainLiteral(), null, "name", null, 1, -1, Agent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAgent_Type(), theXMLTypePackage.getAnyURI(), "type", null, 0, 1, Agent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAgent_NodeID(), theXMLTypePackage.getNCName(), "nodeID", null, 0, 1, Agent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAgent_Phone(), theXMLTypePackage.getAnyURI(), "phone", null, 0, 1, Agent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAgent_Mbox(), theXMLTypePackage.getAnyURI(), "mbox", null, 0, 1, Agent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(documentEClass, Document.class, "Document", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDocument_Name(), theRdfPackage.getPlainLiteral(), null, "name", null, 0, -1, Document.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDocument_NodeID(), theXMLTypePackage.getNCName(), "nodeID", null, 0, 1, Document.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(organizationEClass, Organization.class, "Organization", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(personEClass, Person.class, "Person", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

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
		  (agentEClass,
		   source,
		   new String[] {
			   "constraints", "TypeIsIri MboxIsMailto PhoneIsTel"
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
		  (agentEClass,
		   source,
		   new String[] {
			   "TypeIsIri", "self.type = null or self.type.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "MboxIsMailto", "self.mbox = null or self.mbox.matches(\'mailto:[^\\\\s@]+@[^\\\\s@]+\')",
			   "PhoneIsTel", "self.phone = null or self.phone.matches(\'tel:\\\\S+\')"
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
		  (agentEClass,
		   source,
		   new String[] {
			   "name", "Agent",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getAgent_Name(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "name",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAgent_Type(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "type",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getAgent_NodeID(),
		   source,
		   new String[] {
			   "kind", "attribute",
			   "name", "nodeID",
			   "namespace", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		   });
		addAnnotation
		  (getAgent_Phone(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "phone",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAgent_Mbox(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "mbox",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (documentEClass,
		   source,
		   new String[] {
			   "name", "Document",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getDocument_Name(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "name",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDocument_NodeID(),
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
			   "name", "Organisation",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (personEClass,
		   source,
		   new String[] {
			   "name", "Person",
			   "kind", "elementOnly"
		   });
	}

} //FoafPackageImpl
