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
package terms;


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
 *       DCterms XML Schema (relevant parts for DCAT version 2)
 *       XML Schema for http://purl.org/dc/terms/ namespace
 *       updated 2019-10-03
 *       By PW
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
 * @see terms.TermsFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore validationDelegates='http://www.eclipse.org/fennec/m2x/ocl/1.0'"
 * @generated
 */
@ProviderType
@EPackage(uri = TermsPackage.eNS_URI, fingerprint = "fp1:961a36d28ee4d224a3c72c89b323e45b2699bb76c7736c9237818311378172a8", genModel = "/model/dcatap.genmodel", genModelSourceLocations = {"model/dcatap.genmodel","org.eclipse.fennec.dcat.atlas.dcatap.de.model/model/dcatap.genmodel"}, ecore = "/model/terms.ecore", ecoreSourceLocations = "/model/terms.ecore")
public interface TermsPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "terms";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://purl.org/dc/terms/";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "terms";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	TermsPackage eINSTANCE = terms.impl.TermsPackageImpl.init();

	/**
	 * The meta object id for the '{@link terms.impl.LicenseDocumentImpl <em>License Document</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see terms.impl.LicenseDocumentImpl
	 * @see terms.impl.TermsPackageImpl#getLicenseDocument()
	 * @generated
	 */
	int LICENSE_DOCUMENT = 0;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LICENSE_DOCUMENT__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LICENSE_DOCUMENT__TYPE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LICENSE_DOCUMENT__TITLE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LICENSE_DOCUMENT__DESCRIPTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LICENSE_DOCUMENT__IDENTIFIER = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LICENSE_DOCUMENT__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>License Document</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LICENSE_DOCUMENT_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>License Document</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LICENSE_DOCUMENT_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link terms.impl.LocationImpl <em>Location</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see terms.impl.LocationImpl
	 * @see terms.impl.TermsPackageImpl#getLocation()
	 * @generated
	 */
	int LOCATION = 1;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Geometry</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION__GEOMETRY = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Pref Label</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION__PREF_LABEL = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Location</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Location</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link terms.impl.PeriodOfTimeImpl <em>Period Of Time</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see terms.impl.PeriodOfTimeImpl
	 * @see terms.impl.TermsPackageImpl#getPeriodOfTime()
	 * @generated
	 */
	int PERIOD_OF_TIME = 2;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERIOD_OF_TIME__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Start Date</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERIOD_OF_TIME__START_DATE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>End Date</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERIOD_OF_TIME__END_DATE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERIOD_OF_TIME__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Period Of Time</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERIOD_OF_TIME_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Period Of Time</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERIOD_OF_TIME_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link terms.impl.ProvenanceStatementImpl <em>Provenance Statement</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see terms.impl.ProvenanceStatementImpl
	 * @see terms.impl.TermsPackageImpl#getProvenanceStatement()
	 * @generated
	 */
	int PROVENANCE_STATEMENT = 3;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVENANCE_STATEMENT__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVENANCE_STATEMENT__DESCRIPTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVENANCE_STATEMENT__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Provenance Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVENANCE_STATEMENT_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Provenance Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROVENANCE_STATEMENT_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link terms.impl.RightsStatementImpl <em>Rights Statement</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see terms.impl.RightsStatementImpl
	 * @see terms.impl.TermsPackageImpl#getRightsStatement()
	 * @generated
	 */
	int RIGHTS_STATEMENT = 4;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RIGHTS_STATEMENT__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RIGHTS_STATEMENT__TITLE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RIGHTS_STATEMENT__DESCRIPTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RIGHTS_STATEMENT__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Rights Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RIGHTS_STATEMENT_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Rights Statement</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RIGHTS_STATEMENT_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link terms.impl.StandardImpl <em>Standard</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see terms.impl.StandardImpl
	 * @see terms.impl.TermsPackageImpl#getStandard()
	 * @generated
	 */
	int STANDARD = 5;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STANDARD__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STANDARD__TITLE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STANDARD__DESCRIPTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STANDARD__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Standard</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STANDARD_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Standard</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STANDARD_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link terms.LicenseDocument <em>License Document</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>License Document</em>'.
	 * @see terms.LicenseDocument
	 * @generated
	 */
	EClass getLicenseDocument();

	/**
	 * Returns the meta object for the attribute list '{@link terms.LicenseDocument#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Type</em>'.
	 * @see terms.LicenseDocument#getType()
	 * @see #getLicenseDocument()
	 * @generated
	 */
	EAttribute getLicenseDocument_Type();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.LicenseDocument#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Title</em>'.
	 * @see terms.LicenseDocument#getTitle()
	 * @see #getLicenseDocument()
	 * @generated
	 */
	EReference getLicenseDocument_Title();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.LicenseDocument#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Description</em>'.
	 * @see terms.LicenseDocument#getDescription()
	 * @see #getLicenseDocument()
	 * @generated
	 */
	EReference getLicenseDocument_Description();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.LicenseDocument#getIdentifier <em>Identifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Identifier</em>'.
	 * @see terms.LicenseDocument#getIdentifier()
	 * @see #getLicenseDocument()
	 * @generated
	 */
	EReference getLicenseDocument_Identifier();

	/**
	 * Returns the meta object for the attribute '{@link terms.LicenseDocument#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see terms.LicenseDocument#getNodeID()
	 * @see #getLicenseDocument()
	 * @generated
	 */
	EAttribute getLicenseDocument_NodeID();

	/**
	 * Returns the meta object for class '{@link terms.Location <em>Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Location</em>'.
	 * @see terms.Location
	 * @generated
	 */
	EClass getLocation();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.Location#getGeometry <em>Geometry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Geometry</em>'.
	 * @see terms.Location#getGeometry()
	 * @see #getLocation()
	 * @generated
	 */
	EReference getLocation_Geometry();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.Location#getPrefLabel <em>Pref Label</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Pref Label</em>'.
	 * @see terms.Location#getPrefLabel()
	 * @see #getLocation()
	 * @generated
	 */
	EReference getLocation_PrefLabel();

	/**
	 * Returns the meta object for the attribute '{@link terms.Location#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see terms.Location#getNodeID()
	 * @see #getLocation()
	 * @generated
	 */
	EAttribute getLocation_NodeID();

	/**
	 * Returns the meta object for class '{@link terms.PeriodOfTime <em>Period Of Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Period Of Time</em>'.
	 * @see terms.PeriodOfTime
	 * @generated
	 */
	EClass getPeriodOfTime();

	/**
	 * Returns the meta object for the containment reference '{@link terms.PeriodOfTime#getStartDate <em>Start Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Start Date</em>'.
	 * @see terms.PeriodOfTime#getStartDate()
	 * @see #getPeriodOfTime()
	 * @generated
	 */
	EReference getPeriodOfTime_StartDate();

	/**
	 * Returns the meta object for the containment reference '{@link terms.PeriodOfTime#getEndDate <em>End Date</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>End Date</em>'.
	 * @see terms.PeriodOfTime#getEndDate()
	 * @see #getPeriodOfTime()
	 * @generated
	 */
	EReference getPeriodOfTime_EndDate();

	/**
	 * Returns the meta object for the attribute '{@link terms.PeriodOfTime#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see terms.PeriodOfTime#getNodeID()
	 * @see #getPeriodOfTime()
	 * @generated
	 */
	EAttribute getPeriodOfTime_NodeID();

	/**
	 * Returns the meta object for class '{@link terms.ProvenanceStatement <em>Provenance Statement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Provenance Statement</em>'.
	 * @see terms.ProvenanceStatement
	 * @generated
	 */
	EClass getProvenanceStatement();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.ProvenanceStatement#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Description</em>'.
	 * @see terms.ProvenanceStatement#getDescription()
	 * @see #getProvenanceStatement()
	 * @generated
	 */
	EReference getProvenanceStatement_Description();

	/**
	 * Returns the meta object for the attribute '{@link terms.ProvenanceStatement#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see terms.ProvenanceStatement#getNodeID()
	 * @see #getProvenanceStatement()
	 * @generated
	 */
	EAttribute getProvenanceStatement_NodeID();

	/**
	 * Returns the meta object for class '{@link terms.RightsStatement <em>Rights Statement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Rights Statement</em>'.
	 * @see terms.RightsStatement
	 * @generated
	 */
	EClass getRightsStatement();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.RightsStatement#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Title</em>'.
	 * @see terms.RightsStatement#getTitle()
	 * @see #getRightsStatement()
	 * @generated
	 */
	EReference getRightsStatement_Title();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.RightsStatement#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Description</em>'.
	 * @see terms.RightsStatement#getDescription()
	 * @see #getRightsStatement()
	 * @generated
	 */
	EReference getRightsStatement_Description();

	/**
	 * Returns the meta object for the attribute '{@link terms.RightsStatement#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see terms.RightsStatement#getNodeID()
	 * @see #getRightsStatement()
	 * @generated
	 */
	EAttribute getRightsStatement_NodeID();

	/**
	 * Returns the meta object for class '{@link terms.Standard <em>Standard</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Standard</em>'.
	 * @see terms.Standard
	 * @generated
	 */
	EClass getStandard();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.Standard#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Title</em>'.
	 * @see terms.Standard#getTitle()
	 * @see #getStandard()
	 * @generated
	 */
	EReference getStandard_Title();

	/**
	 * Returns the meta object for the containment reference list '{@link terms.Standard#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Description</em>'.
	 * @see terms.Standard#getDescription()
	 * @see #getStandard()
	 * @generated
	 */
	EReference getStandard_Description();

	/**
	 * Returns the meta object for the attribute '{@link terms.Standard#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see terms.Standard#getNodeID()
	 * @see #getStandard()
	 * @generated
	 */
	EAttribute getStandard_NodeID();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	TermsFactory getTermsFactory();

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
		 * The meta object literal for the '{@link terms.impl.LicenseDocumentImpl <em>License Document</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see terms.impl.LicenseDocumentImpl
		 * @see terms.impl.TermsPackageImpl#getLicenseDocument()
		 * @generated
		 */
		EClass LICENSE_DOCUMENT = eINSTANCE.getLicenseDocument();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LICENSE_DOCUMENT__TYPE = eINSTANCE.getLicenseDocument_Type();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LICENSE_DOCUMENT__TITLE = eINSTANCE.getLicenseDocument_Title();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LICENSE_DOCUMENT__DESCRIPTION = eINSTANCE.getLicenseDocument_Description();

		/**
		 * The meta object literal for the '<em><b>Identifier</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LICENSE_DOCUMENT__IDENTIFIER = eINSTANCE.getLicenseDocument_Identifier();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LICENSE_DOCUMENT__NODE_ID = eINSTANCE.getLicenseDocument_NodeID();

		/**
		 * The meta object literal for the '{@link terms.impl.LocationImpl <em>Location</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see terms.impl.LocationImpl
		 * @see terms.impl.TermsPackageImpl#getLocation()
		 * @generated
		 */
		EClass LOCATION = eINSTANCE.getLocation();

		/**
		 * The meta object literal for the '<em><b>Geometry</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LOCATION__GEOMETRY = eINSTANCE.getLocation_Geometry();

		/**
		 * The meta object literal for the '<em><b>Pref Label</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LOCATION__PREF_LABEL = eINSTANCE.getLocation_PrefLabel();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LOCATION__NODE_ID = eINSTANCE.getLocation_NodeID();

		/**
		 * The meta object literal for the '{@link terms.impl.PeriodOfTimeImpl <em>Period Of Time</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see terms.impl.PeriodOfTimeImpl
		 * @see terms.impl.TermsPackageImpl#getPeriodOfTime()
		 * @generated
		 */
		EClass PERIOD_OF_TIME = eINSTANCE.getPeriodOfTime();

		/**
		 * The meta object literal for the '<em><b>Start Date</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PERIOD_OF_TIME__START_DATE = eINSTANCE.getPeriodOfTime_StartDate();

		/**
		 * The meta object literal for the '<em><b>End Date</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PERIOD_OF_TIME__END_DATE = eINSTANCE.getPeriodOfTime_EndDate();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PERIOD_OF_TIME__NODE_ID = eINSTANCE.getPeriodOfTime_NodeID();

		/**
		 * The meta object literal for the '{@link terms.impl.ProvenanceStatementImpl <em>Provenance Statement</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see terms.impl.ProvenanceStatementImpl
		 * @see terms.impl.TermsPackageImpl#getProvenanceStatement()
		 * @generated
		 */
		EClass PROVENANCE_STATEMENT = eINSTANCE.getProvenanceStatement();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PROVENANCE_STATEMENT__DESCRIPTION = eINSTANCE.getProvenanceStatement_Description();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROVENANCE_STATEMENT__NODE_ID = eINSTANCE.getProvenanceStatement_NodeID();

		/**
		 * The meta object literal for the '{@link terms.impl.RightsStatementImpl <em>Rights Statement</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see terms.impl.RightsStatementImpl
		 * @see terms.impl.TermsPackageImpl#getRightsStatement()
		 * @generated
		 */
		EClass RIGHTS_STATEMENT = eINSTANCE.getRightsStatement();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RIGHTS_STATEMENT__TITLE = eINSTANCE.getRightsStatement_Title();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RIGHTS_STATEMENT__DESCRIPTION = eINSTANCE.getRightsStatement_Description();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RIGHTS_STATEMENT__NODE_ID = eINSTANCE.getRightsStatement_NodeID();

		/**
		 * The meta object literal for the '{@link terms.impl.StandardImpl <em>Standard</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see terms.impl.StandardImpl
		 * @see terms.impl.TermsPackageImpl#getStandard()
		 * @generated
		 */
		EClass STANDARD = eINSTANCE.getStandard();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STANDARD__TITLE = eINSTANCE.getStandard_Title();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference STANDARD__DESCRIPTION = eINSTANCE.getStandard_Description();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STANDARD__NODE_ID = eINSTANCE.getStandard_NodeID();

	}

} //TermsPackage
