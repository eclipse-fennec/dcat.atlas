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

import skos.Concept;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *         dcat:DataService represents a data service. A data service is a collection of operations accessible through an
 *         interface (API) that provide access to one or more datasets or data processing functions.
 *       
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link dcat.DataService#getEndpointDescription <em>Endpoint Description</em>}</li>
 *   <li>{@link dcat.DataService#getEndpointURL <em>Endpoint URL</em>}</li>
 *   <li>{@link dcat.DataService#getServesDataset <em>Serves Dataset</em>}</li>
 *   <li>{@link dcat.DataService#getFormat <em>Format</em>}</li>
 * </ul>
 *
 * @see dcat.DcatPackage#getDataService()
 * @model extendedMetaData="name='DataService' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface DataService extends DcatResource {
	/**
	 * Returns the value of the '<em><b>Endpoint Description</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Endpoint Description</em>' attribute list.
	 * @see dcat.DcatPackage#getDataService_EndpointDescription()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='endpointDescription' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getEndpointDescription();

	/**
	 * Returns the value of the '<em><b>Endpoint URL</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Endpoint URL</em>' attribute list.
	 * @see dcat.DcatPackage#getDataService_EndpointURL()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
	 *        extendedMetaData="kind='element' name='endpointURL' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getEndpointURL();

	/**
	 * Returns the value of the '<em><b>Serves Dataset</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Serves Dataset</em>' containment reference.
	 * @see #setServesDataset(Dataset)
	 * @see dcat.DcatPackage#getDataService_ServesDataset()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='servesDataset' namespace='##targetNamespace'"
	 * @generated
	 */
	Dataset getServesDataset();

	/**
	 * Sets the value of the '{@link dcat.DataService#getServesDataset <em>Serves Dataset</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Serves Dataset</em>' containment reference.
	 * @see #getServesDataset()
	 * @generated
	 */
	void setServesDataset(Dataset value);

	/**
	 * Returns the value of the '<em><b>Format</b></em>' containment reference list.
	 * The list contents are of type {@link skos.Concept}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * dcterms:format — the format(s) in which the data service delivers data. New in DCAT-AP.de 3.0.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Format</em>' containment reference list.
	 * @see dcat.DcatPackage#getDataService_Format()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='format' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<Concept> getFormat();

} // DataService
