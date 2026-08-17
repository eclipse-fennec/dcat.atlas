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

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Catalog</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *         dcat:Catalog represents a catalog, which is a dataset in which
 *         each individual item is a metadata record
 *         describing some resource; the scope of dcat:Catalog
 *         is collections of metadata about datasets or data services.
 *       
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link dcat.Catalog#getCatalog <em>Catalog</em>}</li>
 *   <li>{@link dcat.Catalog#getRecord <em>Record</em>}</li>
 *   <li>{@link dcat.Catalog#getDataset <em>Dataset</em>}</li>
 *   <li>{@link dcat.Catalog#getService <em>Service</em>}</li>
 *   <li>{@link dcat.Catalog#getThemeTaxonomy <em>Theme Taxonomy</em>}</li>
 *   <li>{@link dcat.Catalog#getHasPart <em>Has Part</em>}</li>
 *   <li>{@link dcat.Catalog#getHomepage <em>Homepage</em>}</li>
 * </ul>
 *
 * @see dcat.DcatPackage#getCatalog()
 * @model extendedMetaData="name='Catalog' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface Catalog extends Dataset {
	/**
	 * Returns the value of the '<em><b>Catalog</b></em>' reference list.
	 * The list contents are of type {@link dcat.Catalog}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Catalog</em>' reference list.
	 * @see dcat.DcatPackage#getCatalog_Catalog()
	 * @model extendedMetaData="kind='element' name='catalog' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<Catalog> getCatalog();

	/**
	 * Returns the value of the '<em><b>Record</b></em>' containment reference list.
	 * The list contents are of type {@link dcat.CatalogRecord}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Record</em>' containment reference list.
	 * @see dcat.DcatPackage#getCatalog_Record()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='record' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<CatalogRecord> getRecord();

	/**
	 * Returns the value of the '<em><b>Dataset</b></em>' reference list.
	 * The list contents are of type {@link dcat.Dataset}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dataset</em>' reference list.
	 * @see dcat.DcatPackage#getCatalog_Dataset()
	 * @model extendedMetaData="kind='element' name='dataset' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<Dataset> getDataset();

	/**
	 * Returns the value of the '<em><b>Service</b></em>' reference list.
	 * The list contents are of type {@link dcat.DataService}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Service</em>' reference list.
	 * @see dcat.DcatPackage#getCatalog_Service()
	 * @model extendedMetaData="kind='element' name='service' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<DataService> getService();

	/**
	 * Returns the value of the '<em><b>Theme Taxonomy</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Theme Taxonomy</em>' attribute list.
	 * @see dcat.DcatPackage#getCatalog_ThemeTaxonomy()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='themeTaxonomy' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getThemeTaxonomy();

	/**
	 * Returns the value of the '<em><b>Has Part</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Part</em>' attribute list.
	 * @see dcat.DcatPackage#getCatalog_HasPart()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='hasPart' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<String> getHasPart();

	/**
	 * Returns the value of the '<em><b>Homepage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Homepage</em>' attribute.
	 * @see #setHomepage(String)
	 * @see dcat.DcatPackage#getCatalog_Homepage()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='homepage' namespace='http://xmlns.com/foaf/0.1/'"
	 * @generated
	 */
	String getHomepage();

	/**
	 * Sets the value of the '{@link dcat.Catalog#getHomepage <em>Homepage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Homepage</em>' attribute.
	 * @see #getHomepage()
	 * @generated
	 */
	void setHomepage(String value);

} // Catalog
