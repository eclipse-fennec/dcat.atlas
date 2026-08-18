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
 *       VCARD XML Schema
 *       http://www.w3.org/2006/vcard/ns#
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
 * 
 *       FOAF XML Schema (relevant parts for DCAT version 2)
 *       http://xmlns.com/foaf/0.1/
 *       Modified 2019-10-03
 *     
 * 
 *       ISA Location XML Schema
 *       http://www.w3.org/ns/locn#
 *       Updated 2019-10-03
 *     
 * <!-- end-model-doc -->
 * @see vcard.VcardFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore validationDelegates='http://www.eclipse.org/fennec/m2x/ocl/1.0'"
 * @generated
 */
@ProviderType
@EPackage(uri = VcardPackage.eNS_URI, fingerprint = "fp1:1186332e59d254dea3df1e8d5ca7fac41c99aa1a274c2ac94fdabe93163981aa", genModel = "/model/dcatap.genmodel", genModelSourceLocations = {"model/dcatap.genmodel","org.eclipse.fennec.dcat.atlas.dcatap.de.model/model/dcatap.genmodel"}, ecore = "/model/vcard.ecore", ecoreSourceLocations = "/model/vcard.ecore")
public interface VcardPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "vcard";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.w3.org/2006/vcard/ns#";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "vcard";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	VcardPackage eINSTANCE = vcard.impl.VcardPackageImpl.init();

	/**
	 * The meta object id for the '{@link vcard.impl.AddressImpl <em>Address</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see vcard.impl.AddressImpl
	 * @see vcard.impl.VcardPackageImpl#getAddress()
	 * @generated
	 */
	int ADDRESS = 0;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Street Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS__STREET_ADDRESS = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Locality</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS__LOCALITY = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Postal Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS__POSTAL_CODE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Country Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS__COUNTRY_NAME = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ADDRESS_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link vcard.impl.OrganizationImpl <em>Organization</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see vcard.impl.OrganizationImpl
	 * @see vcard.impl.VcardPackageImpl#getOrganization()
	 * @generated
	 */
	int ORGANIZATION = 1;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Fn</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__FN = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Organization Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__ORGANIZATION_NAME = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Has Address</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__HAS_ADDRESS = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Has Telephone</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__HAS_TELEPHONE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Has Email</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__HAS_EMAIL = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Has URL</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION__HAS_URL = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>Organization</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 7;

	/**
	 * The number of operations of the '<em>Organization</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORGANIZATION_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link vcard.Address <em>Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Address</em>'.
	 * @see vcard.Address
	 * @generated
	 */
	EClass getAddress();

	/**
	 * Returns the meta object for the attribute '{@link vcard.Address#getStreetAddress <em>Street Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Street Address</em>'.
	 * @see vcard.Address#getStreetAddress()
	 * @see #getAddress()
	 * @generated
	 */
	EAttribute getAddress_StreetAddress();

	/**
	 * Returns the meta object for the attribute '{@link vcard.Address#getLocality <em>Locality</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Locality</em>'.
	 * @see vcard.Address#getLocality()
	 * @see #getAddress()
	 * @generated
	 */
	EAttribute getAddress_Locality();

	/**
	 * Returns the meta object for the attribute '{@link vcard.Address#getPostalCode <em>Postal Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Postal Code</em>'.
	 * @see vcard.Address#getPostalCode()
	 * @see #getAddress()
	 * @generated
	 */
	EAttribute getAddress_PostalCode();

	/**
	 * Returns the meta object for the attribute '{@link vcard.Address#getCountryName <em>Country Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Country Name</em>'.
	 * @see vcard.Address#getCountryName()
	 * @see #getAddress()
	 * @generated
	 */
	EAttribute getAddress_CountryName();

	/**
	 * Returns the meta object for the attribute '{@link vcard.Address#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see vcard.Address#getNodeID()
	 * @see #getAddress()
	 * @generated
	 */
	EAttribute getAddress_NodeID();

	/**
	 * Returns the meta object for class '{@link vcard.Organization <em>Organization</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Organization</em>'.
	 * @see vcard.Organization
	 * @generated
	 */
	EClass getOrganization();

	/**
	 * Returns the meta object for the attribute '{@link vcard.Organization#getFn <em>Fn</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fn</em>'.
	 * @see vcard.Organization#getFn()
	 * @see #getOrganization()
	 * @generated
	 */
	EAttribute getOrganization_Fn();

	/**
	 * Returns the meta object for the attribute '{@link vcard.Organization#getOrganizationName <em>Organization Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Organization Name</em>'.
	 * @see vcard.Organization#getOrganizationName()
	 * @see #getOrganization()
	 * @generated
	 */
	EAttribute getOrganization_OrganizationName();

	/**
	 * Returns the meta object for the containment reference '{@link vcard.Organization#getHasAddress <em>Has Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Has Address</em>'.
	 * @see vcard.Organization#getHasAddress()
	 * @see #getOrganization()
	 * @generated
	 */
	EReference getOrganization_HasAddress();

	/**
	 * Returns the meta object for the attribute list '{@link vcard.Organization#getHasTelephone <em>Has Telephone</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Has Telephone</em>'.
	 * @see vcard.Organization#getHasTelephone()
	 * @see #getOrganization()
	 * @generated
	 */
	EAttribute getOrganization_HasTelephone();

	/**
	 * Returns the meta object for the attribute '{@link vcard.Organization#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see vcard.Organization#getNodeID()
	 * @see #getOrganization()
	 * @generated
	 */
	EAttribute getOrganization_NodeID();

	/**
	 * Returns the meta object for the attribute list '{@link vcard.Organization#getHasEmail <em>Has Email</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Has Email</em>'.
	 * @see vcard.Organization#getHasEmail()
	 * @see #getOrganization()
	 * @generated
	 */
	EAttribute getOrganization_HasEmail();

	/**
	 * Returns the meta object for the attribute list '{@link vcard.Organization#getHasURL <em>Has URL</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Has URL</em>'.
	 * @see vcard.Organization#getHasURL()
	 * @see #getOrganization()
	 * @generated
	 */
	EAttribute getOrganization_HasURL();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	VcardFactory getVcardFactory();

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
		 * The meta object literal for the '{@link vcard.impl.AddressImpl <em>Address</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see vcard.impl.AddressImpl
		 * @see vcard.impl.VcardPackageImpl#getAddress()
		 * @generated
		 */
		EClass ADDRESS = eINSTANCE.getAddress();

		/**
		 * The meta object literal for the '<em><b>Street Address</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ADDRESS__STREET_ADDRESS = eINSTANCE.getAddress_StreetAddress();

		/**
		 * The meta object literal for the '<em><b>Locality</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ADDRESS__LOCALITY = eINSTANCE.getAddress_Locality();

		/**
		 * The meta object literal for the '<em><b>Postal Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ADDRESS__POSTAL_CODE = eINSTANCE.getAddress_PostalCode();

		/**
		 * The meta object literal for the '<em><b>Country Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ADDRESS__COUNTRY_NAME = eINSTANCE.getAddress_CountryName();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ADDRESS__NODE_ID = eINSTANCE.getAddress_NodeID();

		/**
		 * The meta object literal for the '{@link vcard.impl.OrganizationImpl <em>Organization</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see vcard.impl.OrganizationImpl
		 * @see vcard.impl.VcardPackageImpl#getOrganization()
		 * @generated
		 */
		EClass ORGANIZATION = eINSTANCE.getOrganization();

		/**
		 * The meta object literal for the '<em><b>Fn</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORGANIZATION__FN = eINSTANCE.getOrganization_Fn();

		/**
		 * The meta object literal for the '<em><b>Organization Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORGANIZATION__ORGANIZATION_NAME = eINSTANCE.getOrganization_OrganizationName();

		/**
		 * The meta object literal for the '<em><b>Has Address</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ORGANIZATION__HAS_ADDRESS = eINSTANCE.getOrganization_HasAddress();

		/**
		 * The meta object literal for the '<em><b>Has Telephone</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORGANIZATION__HAS_TELEPHONE = eINSTANCE.getOrganization_HasTelephone();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORGANIZATION__NODE_ID = eINSTANCE.getOrganization_NodeID();

		/**
		 * The meta object literal for the '<em><b>Has Email</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORGANIZATION__HAS_EMAIL = eINSTANCE.getOrganization_HasEmail();

		/**
		 * The meta object literal for the '<em><b>Has URL</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORGANIZATION__HAS_URL = eINSTANCE.getOrganization_HasURL();

	}

} //VcardPackage
