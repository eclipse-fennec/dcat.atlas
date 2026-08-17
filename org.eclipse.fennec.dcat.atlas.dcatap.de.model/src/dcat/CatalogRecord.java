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
package dcat;

import org.eclipse.emf.common.util.EList;

import org.osgi.annotation.versioning.ProviderType;

import rdf.DateOrDateTimeLiteral;
import rdf.IdentifiedResource;
import rdf.PlainLiteral;

import terms.Standard;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Catalog Record</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *         dcat:CatalogRecord represents a metadata item in the catalog, primarily concerning the registration information,
 *         such as who added the item and when.
 * 
 *         https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog_Record
 *       
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link dcat.CatalogRecord#getTitle <em>Title</em>}</li>
 *   <li>{@link dcat.CatalogRecord#getDescription <em>Description</em>}</li>
 *   <li>{@link dcat.CatalogRecord#getIssued <em>Issued</em>}</li>
 *   <li>{@link dcat.CatalogRecord#getModified <em>Modified</em>}</li>
 *   <li>{@link dcat.CatalogRecord#getConformsTo <em>Conforms To</em>}</li>
 *   <li>{@link dcat.CatalogRecord#getLanguage <em>Language</em>}</li>
 *   <li>{@link dcat.CatalogRecord#getPrimaryTopic <em>Primary Topic</em>}</li>
 * </ul>
 *
 * @see dcat.DcatPackage#getCatalogRecord()
 * @model extendedMetaData="name='CatalogRecord' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface CatalogRecord extends IdentifiedResource {
	/**
	 * Returns the value of the '<em><b>Title</b></em>' containment reference list.
	 * The list contents are of type {@link rdf.PlainLiteral}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Title</em>' containment reference list.
	 * @see dcat.DcatPackage#getCatalogRecord_Title()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='title' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<PlainLiteral> getTitle();

	/**
	 * Returns the value of the '<em><b>Description</b></em>' containment reference list.
	 * The list contents are of type {@link rdf.PlainLiteral}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' containment reference list.
	 * @see dcat.DcatPackage#getCatalogRecord_Description()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='description' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<PlainLiteral> getDescription();

	/**
	 * Returns the value of the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Issued</em>' containment reference.
	 * @see #setIssued(DateOrDateTimeLiteral)
	 * @see dcat.DcatPackage#getCatalogRecord_Issued()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='issued' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	DateOrDateTimeLiteral getIssued();

	/**
	 * Sets the value of the '{@link dcat.CatalogRecord#getIssued <em>Issued</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Issued</em>' containment reference.
	 * @see #getIssued()
	 * @generated
	 */
	void setIssued(DateOrDateTimeLiteral value);

	/**
	 * Returns the value of the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Modified</em>' containment reference.
	 * @see #setModified(DateOrDateTimeLiteral)
	 * @see dcat.DcatPackage#getCatalogRecord_Modified()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='modified' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	DateOrDateTimeLiteral getModified();

	/**
	 * Sets the value of the '{@link dcat.CatalogRecord#getModified <em>Modified</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Modified</em>' containment reference.
	 * @see #getModified()
	 * @generated
	 */
	void setModified(DateOrDateTimeLiteral value);

	/**
	 * Returns the value of the '<em><b>Conforms To</b></em>' containment reference list.
	 * The list contents are of type {@link terms.Standard}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Conforms To</em>' containment reference list.
	 * @see dcat.DcatPackage#getCatalogRecord_ConformsTo()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='conformsTo' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<Standard> getConformsTo();

	/**
	 * Returns the value of the '<em><b>Language</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Language</em>' attribute list.
	 * @see dcat.DcatPackage#getCatalogRecord_Language()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='language' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<String> getLanguage();

	/**
	 * Returns the value of the '<em><b>Primary Topic</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Primary Topic</em>' reference.
	 * @see #setPrimaryTopic(DcatResource)
	 * @see dcat.DcatPackage#getCatalogRecord_PrimaryTopic()
	 * @model required="true"
	 *        extendedMetaData="kind='element' name='primaryTopic' namespace='http://xmlns.com/foaf/0.1/'"
	 * @generated
	 */
	DcatResource getPrimaryTopic();

	/**
	 * Sets the value of the '{@link dcat.CatalogRecord#getPrimaryTopic <em>Primary Topic</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Primary Topic</em>' reference.
	 * @see #getPrimaryTopic()
	 * @generated
	 */
	void setPrimaryTopic(DcatResource value);

} // CatalogRecord
