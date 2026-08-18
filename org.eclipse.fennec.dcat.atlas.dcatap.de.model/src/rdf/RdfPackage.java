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
package rdf;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

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
 * <!-- end-model-doc -->
 * @see rdf.RdfFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore validationDelegates='http://www.eclipse.org/fennec/m2x/ocl/1.0'"
 * @generated
 */
@ProviderType
@EPackage(uri = RdfPackage.eNS_URI, fingerprint = "fp1:f4782aac339582b8a6f5082d003dfa157e770ebda211da037c7bd170eaf29118", genModel = "/model/dcatap.genmodel", genModelSourceLocations = {"model/dcatap.genmodel","org.eclipse.fennec.dcat.atlas.dcatap.de.model/model/dcatap.genmodel"}, ecore = "/model/rdf.ecore", ecoreSourceLocations = "/model/rdf.ecore")
public interface RdfPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "rdf";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "rdf";

	/**
	 * The package content type ID.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eCONTENT_TYPE = "dcat.rdf";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RdfPackage eINSTANCE = rdf.impl.RdfPackageImpl.init();

	/**
	 * The meta object id for the '{@link rdf.impl.DateOrDateTimeLiteralImpl <em>Date Or Date Time Literal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see rdf.impl.DateOrDateTimeLiteralImpl
	 * @see rdf.impl.RdfPackageImpl#getDateOrDateTimeLiteral()
	 * @generated
	 */
	int DATE_OR_DATE_TIME_LITERAL = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATE_OR_DATE_TIME_LITERAL__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Datatype</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATE_OR_DATE_TIME_LITERAL__DATATYPE = 1;

	/**
	 * The number of structural features of the '<em>Date Or Date Time Literal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATE_OR_DATE_TIME_LITERAL_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Date Or Date Time Literal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATE_OR_DATE_TIME_LITERAL_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link rdf.impl.PlainLiteralImpl <em>Plain Literal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see rdf.impl.PlainLiteralImpl
	 * @see rdf.impl.RdfPackageImpl#getPlainLiteral()
	 * @generated
	 */
	int PLAIN_LITERAL = 1;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAIN_LITERAL__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Lang</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAIN_LITERAL__LANG = 1;

	/**
	 * The number of structural features of the '<em>Plain Literal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAIN_LITERAL_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Plain Literal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAIN_LITERAL_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link rdf.impl.TypedLiteralImpl <em>Typed Literal</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see rdf.impl.TypedLiteralImpl
	 * @see rdf.impl.RdfPackageImpl#getTypedLiteral()
	 * @generated
	 */
	int TYPED_LITERAL = 2;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPED_LITERAL__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Datatype</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPED_LITERAL__DATATYPE = 1;

	/**
	 * The number of structural features of the '<em>Typed Literal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPED_LITERAL_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Typed Literal</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TYPED_LITERAL_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link rdf.impl.IdentifiedResourceImpl <em>Identified Resource</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see rdf.impl.IdentifiedResourceImpl
	 * @see rdf.impl.RdfPackageImpl#getIdentifiedResource()
	 * @generated
	 */
	int IDENTIFIED_RESOURCE = 3;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIED_RESOURCE__ABOUT = 0;

	/**
	 * The number of structural features of the '<em>Identified Resource</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIED_RESOURCE_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Identified Resource</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTIFIED_RESOURCE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link rdf.Datatype <em>Datatype</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see rdf.Datatype
	 * @see rdf.impl.RdfPackageImpl#getDatatype()
	 * @generated
	 */
	int DATATYPE = 4;

	/**
	 * The meta object id for the '<em>Date Or Date Time</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see javax.xml.datatype.XMLGregorianCalendar
	 * @see rdf.impl.RdfPackageImpl#getDateOrDateTime()
	 * @generated
	 */
	int DATE_OR_DATE_TIME = 5;


	/**
	 * Returns the meta object for class '{@link rdf.DateOrDateTimeLiteral <em>Date Or Date Time Literal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Date Or Date Time Literal</em>'.
	 * @see rdf.DateOrDateTimeLiteral
	 * @generated
	 */
	EClass getDateOrDateTimeLiteral();

	/**
	 * Returns the meta object for the attribute '{@link rdf.DateOrDateTimeLiteral#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see rdf.DateOrDateTimeLiteral#getValue()
	 * @see #getDateOrDateTimeLiteral()
	 * @generated
	 */
	EAttribute getDateOrDateTimeLiteral_Value();

	/**
	 * Returns the meta object for the attribute '{@link rdf.DateOrDateTimeLiteral#getDatatype <em>Datatype</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Datatype</em>'.
	 * @see rdf.DateOrDateTimeLiteral#getDatatype()
	 * @see #getDateOrDateTimeLiteral()
	 * @generated
	 */
	EAttribute getDateOrDateTimeLiteral_Datatype();

	/**
	 * Returns the meta object for class '{@link rdf.PlainLiteral <em>Plain Literal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Plain Literal</em>'.
	 * @see rdf.PlainLiteral
	 * @generated
	 */
	EClass getPlainLiteral();

	/**
	 * Returns the meta object for the attribute '{@link rdf.PlainLiteral#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see rdf.PlainLiteral#getValue()
	 * @see #getPlainLiteral()
	 * @generated
	 */
	EAttribute getPlainLiteral_Value();

	/**
	 * Returns the meta object for the attribute '{@link rdf.PlainLiteral#getLang <em>Lang</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Lang</em>'.
	 * @see rdf.PlainLiteral#getLang()
	 * @see #getPlainLiteral()
	 * @generated
	 */
	EAttribute getPlainLiteral_Lang();

	/**
	 * Returns the meta object for class '{@link rdf.TypedLiteral <em>Typed Literal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Typed Literal</em>'.
	 * @see rdf.TypedLiteral
	 * @generated
	 */
	EClass getTypedLiteral();

	/**
	 * Returns the meta object for the attribute '{@link rdf.TypedLiteral#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see rdf.TypedLiteral#getValue()
	 * @see #getTypedLiteral()
	 * @generated
	 */
	EAttribute getTypedLiteral_Value();

	/**
	 * Returns the meta object for the attribute '{@link rdf.TypedLiteral#getDatatype <em>Datatype</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Datatype</em>'.
	 * @see rdf.TypedLiteral#getDatatype()
	 * @see #getTypedLiteral()
	 * @generated
	 */
	EAttribute getTypedLiteral_Datatype();

	/**
	 * Returns the meta object for class '{@link rdf.IdentifiedResource <em>Identified Resource</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Identified Resource</em>'.
	 * @see rdf.IdentifiedResource
	 * @generated
	 */
	EClass getIdentifiedResource();

	/**
	 * Returns the meta object for the attribute '{@link rdf.IdentifiedResource#getAbout <em>About</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>About</em>'.
	 * @see rdf.IdentifiedResource#getAbout()
	 * @see #getIdentifiedResource()
	 * @generated
	 */
	EAttribute getIdentifiedResource_About();

	/**
	 * Returns the meta object for enum '{@link rdf.Datatype <em>Datatype</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Datatype</em>'.
	 * @see rdf.Datatype
	 * @generated
	 */
	EEnum getDatatype();

	/**
	 * Returns the meta object for data type '{@link javax.xml.datatype.XMLGregorianCalendar <em>Date Or Date Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Date Or Date Time</em>'.
	 * @see javax.xml.datatype.XMLGregorianCalendar
	 * @model instanceClass="javax.xml.datatype.XMLGregorianCalendar"
	 *        extendedMetaData="name='dateOrDateTime' memberTypes='http://www.eclipse.org/emf/2003/XMLType#date http://www.eclipse.org/emf/2003/XMLType#dateTime http://www.eclipse.org/emf/2003/XMLType#gYear http://www.eclipse.org/emf/2003/XMLType#gYearMonth'"
	 * @generated
	 */
	EDataType getDateOrDateTime();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	RdfFactory getRdfFactory();

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
		 * The meta object literal for the '{@link rdf.impl.DateOrDateTimeLiteralImpl <em>Date Or Date Time Literal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see rdf.impl.DateOrDateTimeLiteralImpl
		 * @see rdf.impl.RdfPackageImpl#getDateOrDateTimeLiteral()
		 * @generated
		 */
		EClass DATE_OR_DATE_TIME_LITERAL = eINSTANCE.getDateOrDateTimeLiteral();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATE_OR_DATE_TIME_LITERAL__VALUE = eINSTANCE.getDateOrDateTimeLiteral_Value();

		/**
		 * The meta object literal for the '<em><b>Datatype</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATE_OR_DATE_TIME_LITERAL__DATATYPE = eINSTANCE.getDateOrDateTimeLiteral_Datatype();

		/**
		 * The meta object literal for the '{@link rdf.impl.PlainLiteralImpl <em>Plain Literal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see rdf.impl.PlainLiteralImpl
		 * @see rdf.impl.RdfPackageImpl#getPlainLiteral()
		 * @generated
		 */
		EClass PLAIN_LITERAL = eINSTANCE.getPlainLiteral();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAIN_LITERAL__VALUE = eINSTANCE.getPlainLiteral_Value();

		/**
		 * The meta object literal for the '<em><b>Lang</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAIN_LITERAL__LANG = eINSTANCE.getPlainLiteral_Lang();

		/**
		 * The meta object literal for the '{@link rdf.impl.TypedLiteralImpl <em>Typed Literal</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see rdf.impl.TypedLiteralImpl
		 * @see rdf.impl.RdfPackageImpl#getTypedLiteral()
		 * @generated
		 */
		EClass TYPED_LITERAL = eINSTANCE.getTypedLiteral();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPED_LITERAL__VALUE = eINSTANCE.getTypedLiteral_Value();

		/**
		 * The meta object literal for the '<em><b>Datatype</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TYPED_LITERAL__DATATYPE = eINSTANCE.getTypedLiteral_Datatype();

		/**
		 * The meta object literal for the '{@link rdf.impl.IdentifiedResourceImpl <em>Identified Resource</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see rdf.impl.IdentifiedResourceImpl
		 * @see rdf.impl.RdfPackageImpl#getIdentifiedResource()
		 * @generated
		 */
		EClass IDENTIFIED_RESOURCE = eINSTANCE.getIdentifiedResource();

		/**
		 * The meta object literal for the '<em><b>About</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IDENTIFIED_RESOURCE__ABOUT = eINSTANCE.getIdentifiedResource_About();

		/**
		 * The meta object literal for the '{@link rdf.Datatype <em>Datatype</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see rdf.Datatype
		 * @see rdf.impl.RdfPackageImpl#getDatatype()
		 * @generated
		 */
		EEnum DATATYPE = eINSTANCE.getDatatype();

		/**
		 * The meta object literal for the '<em>Date Or Date Time</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see javax.xml.datatype.XMLGregorianCalendar
		 * @see rdf.impl.RdfPackageImpl#getDateOrDateTime()
		 * @generated
		 */
		EDataType DATE_OR_DATE_TIME = eINSTANCE.getDateOrDateTime();

	}

} //RdfPackage
