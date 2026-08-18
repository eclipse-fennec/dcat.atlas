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
package foaf;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

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
 *       FOAF XML Schema (relevant parts for DCAT version 2)
 *       http://xmlns.com/foaf/0.1/
 *       Modified 2019-10-03
 *     
 * 
 *       RDF XML Schema (relevant parts for DCAT version 2)
 *       XML Schema for http://www.w3.org/1999/02/22-rdf-syntax-ns# namespace
 *       Modified 2019-10-03
 *     
 * 
 *       See http://www.w3.org/XML/1998/namespace.html and
 *       http://www.w3.org/TR/REC-xml for information about this namespace.
 * 
 *       This schema document describes the XML namespace, in a form
 *       suitable for import by other schema documents.
 * 
 *       Note that local names in this namespace are intended to be defined
 *       only by the World Wide Web Consortium or its subgroups. The
 *       following names are currently defined in this namespace and should
 *       not be used with conflicting semantics by any Working Group,
 *       specification, or document instance:
 * 
 *       base (as an attribute name): denotes an attribute whose value
 *       provides a URI to be used as the base for interpreting any
 *       relative URIs in the scope of the element on which it
 *       appears; its value is inherited. This name is reserved
 *       by virtue of its definition in the XML Base specification.
 * 
 *       lang (as an attribute name): denotes an attribute whose value
 *       is a language code for the natural language of the content of
 *       any element; its value is inherited. This name is reserved
 *       by virtue of its definition in the XML specification.
 * 
 *       space (as an attribute name): denotes an attribute whose
 *       value is a keyword indicating what whitespace processing
 *       discipline is intended for the content of the element; its
 *       value is inherited. This name is reserved by virtue of its
 *       definition in the XML specification.
 * 
 *       Father (in any context at all): denotes Jon Bosak, the chair of
 *       the original XML Working Group. This name is reserved by
 *       the following decision of the W3C XML Plenary and
 *       XML Coordination groups:
 * 
 *       In appreciation for his vision, leadership and dedication
 *       the W3C XML Plenary on this 10th day of February, 2000
 *       reserves for Jon Bosak in perpetuity the XML name
 *       xml:Father
 *     
 * This schema defines attributes and an attribute group
 *       suitable for use by
 *       schemas wishing to allow xml:base, xml:lang or xml:space attributes
 *       on elements they define.
 * 
 *       To enable this, such a schema must import this schema
 *       for the XML namespace, e.g. as follows:
 *       <schema . . .>
 *       . . .
 *       <import namespace="http://www.w3.org/XML/1998/namespace"
 *       schemaLocation="http://www.w3.org/2001/03/xml.xsd"/>
 * 
 *       Subsequently, qualified reference to any of the attributes
 *       or the group defined below will have the desired effect, e.g.
 * 
 *       <type . . .>
 *       . . .
 *       <attributeGroup ref="xml:specialAttrs"/>
 * 
 *       will define a type which will schema-validate an instance
 *       element with any of those attributes
 *     
 * In keeping with the XML Schema WG's standard versioning
 *       policy, this schema document will persist at
 *       http://www.w3.org/2001/03/xml.xsd.
 *       At the date of issue it can also be found at
 *       http://www.w3.org/2001/xml.xsd.
 *       The schema document at that URI may however change in the future,
 *       in order to remain compatible with the latest version of XML Schema
 *       itself. In other words, if the XML Schema namespace changes, the version
 *       of this document at
 *       http://www.w3.org/2001/xml.xsd will change
 *       accordingly; the version at
 *       http://www.w3.org/2001/03/xml.xsd will not change.
 *     
 * 
 *       DCAT version 2 XML Schema
 *       XML Schema for http://www.w3.org/ns/dcat# namespace
 * 
 *       Description: This is an XML Schema for the DCAT version 2 specification.
 *       The schema is based on the one used by GeoNetwork for storing GeoNetwork data internally.
 *       The schema adheres to a 'normalized' RDF/XML syntax that can be processed with good old XML Technology (XPath,
 *       XSLT, XQuery).
 *       The schema is not intended to be used outside GeoNetwork.
 * 
 *       Created 2019-10-03 / Author PW
 *     
 * 
 *       DCterms XML Schema (relevant parts for DCAT version 2)
 *       XML Schema for http://purl.org/dc/terms/ namespace
 *       updated 2019-10-03
 *       By PW
 *     
 * 
 *       SKOS XML Schema
 *       http://www.w3.org/2004/02/skos/core#
 *     
 * <!-- end-model-doc -->
 * @see foaf.FoafFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore validationDelegates='http://www.eclipse.org/fennec/m2x/ocl/1.0'"
 * @generated
 */
@ProviderType
@EPackage(uri = FoafPackage.eNS_URI, fingerprint = "fp1:e4344045c4cf80c0a635d64a447d5e88b2577dbe50dd9204151bd24a927e0f50", genModel = "/model/dcatap.genmodel", genModelSourceLocations = {"model/dcatap.genmodel","org.eclipse.fennec.dcat.atlas.dcatap.de.model/model/dcatap.genmodel"}, ecore = "/model/foaf.ecore", ecoreSourceLocations = "/model/foaf.ecore")
public interface FoafPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "foaf";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://xmlns.com/foaf/0.1/";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "foaf";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	FoafPackage eINSTANCE = foaf.impl.FoafPackageImpl.init();

	/**
	 * The meta object id for the '{@link foaf.impl.AgentImpl <em>Agent</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see foaf.impl.AgentImpl
	 * @see foaf.impl.FoafPackageImpl#getAgent()
	 * @generated
	 */
	int AGENT = 0;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGENT__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGENT__NAME = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGENT__TYPE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGENT__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Phone</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGENT__PHONE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Mbox</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGENT__MBOX = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Agent</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGENT_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>Agent</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGENT_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link foaf.impl.DocumentImpl <em>Document</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see foaf.impl.DocumentImpl
	 * @see foaf.impl.FoafPackageImpl#getDocument()
	 * @generated
	 */
	int DOCUMENT = 1;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT__NAME = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Document</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Document</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOCUMENT_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link foaf.impl.OrganizationImpl <em>Organization</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see foaf.impl.OrganizationImpl
	 * @see foaf.impl.FoafPackageImpl#getOrganization()
	 * @generated
	 */
	int ORGANIZATION = 2;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__ABOUT = AGENT__ABOUT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__NAME = AGENT__NAME;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__TYPE = AGENT__TYPE;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__NODE_ID = AGENT__NODE_ID;

	/**
	 * The feature id for the '<em><b>Phone</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__PHONE = AGENT__PHONE;

	/**
	 * The feature id for the '<em><b>Mbox</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__MBOX = AGENT__MBOX;

	/**
	 * The number of structural features of the '<em>Organization</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION_FEATURE_COUNT = AGENT_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Organization</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION_OPERATION_COUNT = AGENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link foaf.impl.PersonImpl <em>Person</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see foaf.impl.PersonImpl
	 * @see foaf.impl.FoafPackageImpl#getPerson()
	 * @generated
	 */
	int PERSON = 3;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__ABOUT = AGENT__ABOUT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__NAME = AGENT__NAME;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__TYPE = AGENT__TYPE;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__NODE_ID = AGENT__NODE_ID;

	/**
	 * The feature id for the '<em><b>Phone</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__PHONE = AGENT__PHONE;

	/**
	 * The feature id for the '<em><b>Mbox</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON__MBOX = AGENT__MBOX;

	/**
	 * The number of structural features of the '<em>Person</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON_FEATURE_COUNT = AGENT_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Person</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSON_OPERATION_COUNT = AGENT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link foaf.Agent <em>Agent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Agent</em>'.
	 * @see foaf.Agent
	 * @generated
	 */
	EClass getAgent();

	/**
	 * Returns the meta object for the containment reference list '{@link foaf.Agent#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Name</em>'.
	 * @see foaf.Agent#getName()
	 * @see #getAgent()
	 * @generated
	 */
	EReference getAgent_Name();

	/**
	 * Returns the meta object for the attribute '{@link foaf.Agent#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see foaf.Agent#getType()
	 * @see #getAgent()
	 * @generated
	 */
	EAttribute getAgent_Type();

	/**
	 * Returns the meta object for the attribute '{@link foaf.Agent#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see foaf.Agent#getNodeID()
	 * @see #getAgent()
	 * @generated
	 */
	EAttribute getAgent_NodeID();

	/**
	 * Returns the meta object for the attribute '{@link foaf.Agent#getPhone <em>Phone</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Phone</em>'.
	 * @see foaf.Agent#getPhone()
	 * @see #getAgent()
	 * @generated
	 */
	EAttribute getAgent_Phone();

	/**
	 * Returns the meta object for the attribute '{@link foaf.Agent#getMbox <em>Mbox</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mbox</em>'.
	 * @see foaf.Agent#getMbox()
	 * @see #getAgent()
	 * @generated
	 */
	EAttribute getAgent_Mbox();

	/**
	 * Returns the meta object for class '{@link foaf.Document <em>Document</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Document</em>'.
	 * @see foaf.Document
	 * @generated
	 */
	EClass getDocument();

	/**
	 * Returns the meta object for the containment reference list '{@link foaf.Document#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Name</em>'.
	 * @see foaf.Document#getName()
	 * @see #getDocument()
	 * @generated
	 */
	EReference getDocument_Name();

	/**
	 * Returns the meta object for the attribute '{@link foaf.Document#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see foaf.Document#getNodeID()
	 * @see #getDocument()
	 * @generated
	 */
	EAttribute getDocument_NodeID();

	/**
	 * Returns the meta object for class '{@link foaf.Organization <em>Organization</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Organization</em>'.
	 * @see foaf.Organization
	 * @generated
	 */
	EClass getOrganization();

	/**
	 * Returns the meta object for class '{@link foaf.Person <em>Person</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Person</em>'.
	 * @see foaf.Person
	 * @generated
	 */
	EClass getPerson();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	FoafFactory getFoafFactory();

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
		 * The meta object literal for the '{@link foaf.impl.AgentImpl <em>Agent</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see foaf.impl.AgentImpl
		 * @see foaf.impl.FoafPackageImpl#getAgent()
		 * @generated
		 */
		EClass AGENT = eINSTANCE.getAgent();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference AGENT__NAME = eINSTANCE.getAgent_Name();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGENT__TYPE = eINSTANCE.getAgent_Type();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGENT__NODE_ID = eINSTANCE.getAgent_NodeID();

		/**
		 * The meta object literal for the '<em><b>Phone</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGENT__PHONE = eINSTANCE.getAgent_Phone();

		/**
		 * The meta object literal for the '<em><b>Mbox</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGENT__MBOX = eINSTANCE.getAgent_Mbox();

		/**
		 * The meta object literal for the '{@link foaf.impl.DocumentImpl <em>Document</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see foaf.impl.DocumentImpl
		 * @see foaf.impl.FoafPackageImpl#getDocument()
		 * @generated
		 */
		EClass DOCUMENT = eINSTANCE.getDocument();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DOCUMENT__NAME = eINSTANCE.getDocument_Name();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DOCUMENT__NODE_ID = eINSTANCE.getDocument_NodeID();

		/**
		 * The meta object literal for the '{@link foaf.impl.OrganizationImpl <em>Organization</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see foaf.impl.OrganizationImpl
		 * @see foaf.impl.FoafPackageImpl#getOrganization()
		 * @generated
		 */
		EClass ORGANIZATION = eINSTANCE.getOrganization();

		/**
		 * The meta object literal for the '{@link foaf.impl.PersonImpl <em>Person</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see foaf.impl.PersonImpl
		 * @see foaf.impl.FoafPackageImpl#getPerson()
		 * @generated
		 */
		EClass PERSON = eINSTANCE.getPerson();

	}

} //FoafPackage
