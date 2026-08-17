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
package dcat.impl;

import dcat.DataService;
import dcat.Dataset;
import dcat.DcatPackage;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.util.EDataTypeEList;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link dcat.impl.DataServiceImpl#getEndpointDescription <em>Endpoint Description</em>}</li>
 *   <li>{@link dcat.impl.DataServiceImpl#getEndpointURL <em>Endpoint URL</em>}</li>
 *   <li>{@link dcat.impl.DataServiceImpl#getServesDataset <em>Serves Dataset</em>}</li>
 *   <li>{@link dcat.impl.DataServiceImpl#getFormat <em>Format</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataServiceImpl extends DcatResourceImpl implements DataService {
	/**
	 * The cached value of the '{@link #getEndpointDescription() <em>Endpoint Description</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndpointDescription()
	 * @generated
	 * @ordered
	 */
	protected EList<String> endpointDescription;

	/**
	 * The cached value of the '{@link #getEndpointURL() <em>Endpoint URL</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndpointURL()
	 * @generated
	 * @ordered
	 */
	protected EList<String> endpointURL;

	/**
	 * The cached value of the '{@link #getServesDataset() <em>Serves Dataset</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getServesDataset()
	 * @generated
	 * @ordered
	 */
	protected EList<Dataset> servesDataset;

	/**
	 * The cached value of the '{@link #getFormat() <em>Format</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormat()
	 * @generated
	 * @ordered
	 */
	protected EList<String> format;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DataServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DcatPackage.Literals.DATA_SERVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getEndpointDescription() {
		if (endpointDescription == null) {
			endpointDescription = new EDataTypeEList<String>(String.class, this, DcatPackage.DATA_SERVICE__ENDPOINT_DESCRIPTION);
		}
		return endpointDescription;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getEndpointURL() {
		if (endpointURL == null) {
			endpointURL = new EDataTypeEList<String>(String.class, this, DcatPackage.DATA_SERVICE__ENDPOINT_URL);
		}
		return endpointURL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Dataset> getServesDataset() {
		if (servesDataset == null) {
			servesDataset = new EObjectResolvingEList<Dataset>(Dataset.class, this, DcatPackage.DATA_SERVICE__SERVES_DATASET);
		}
		return servesDataset;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getFormat() {
		if (format == null) {
			format = new EDataTypeUniqueEList<String>(String.class, this, DcatPackage.DATA_SERVICE__FORMAT);
		}
		return format;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DcatPackage.DATA_SERVICE__ENDPOINT_DESCRIPTION:
				return getEndpointDescription();
			case DcatPackage.DATA_SERVICE__ENDPOINT_URL:
				return getEndpointURL();
			case DcatPackage.DATA_SERVICE__SERVES_DATASET:
				return getServesDataset();
			case DcatPackage.DATA_SERVICE__FORMAT:
				return getFormat();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DcatPackage.DATA_SERVICE__ENDPOINT_DESCRIPTION:
				getEndpointDescription().clear();
				getEndpointDescription().addAll((Collection<? extends String>)newValue);
				return;
			case DcatPackage.DATA_SERVICE__ENDPOINT_URL:
				getEndpointURL().clear();
				getEndpointURL().addAll((Collection<? extends String>)newValue);
				return;
			case DcatPackage.DATA_SERVICE__SERVES_DATASET:
				getServesDataset().clear();
				getServesDataset().addAll((Collection<? extends Dataset>)newValue);
				return;
			case DcatPackage.DATA_SERVICE__FORMAT:
				getFormat().clear();
				getFormat().addAll((Collection<? extends String>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case DcatPackage.DATA_SERVICE__ENDPOINT_DESCRIPTION:
				getEndpointDescription().clear();
				return;
			case DcatPackage.DATA_SERVICE__ENDPOINT_URL:
				getEndpointURL().clear();
				return;
			case DcatPackage.DATA_SERVICE__SERVES_DATASET:
				getServesDataset().clear();
				return;
			case DcatPackage.DATA_SERVICE__FORMAT:
				getFormat().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case DcatPackage.DATA_SERVICE__ENDPOINT_DESCRIPTION:
				return endpointDescription != null && !endpointDescription.isEmpty();
			case DcatPackage.DATA_SERVICE__ENDPOINT_URL:
				return endpointURL != null && !endpointURL.isEmpty();
			case DcatPackage.DATA_SERVICE__SERVES_DATASET:
				return servesDataset != null && !servesDataset.isEmpty();
			case DcatPackage.DATA_SERVICE__FORMAT:
				return format != null && !format.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (endpointDescription: ");
		result.append(endpointDescription);
		result.append(", endpointURL: ");
		result.append(endpointURL);
		result.append(", format: ");
		result.append(format);
		result.append(')');
		return result.toString();
	}

} //DataServiceImpl
