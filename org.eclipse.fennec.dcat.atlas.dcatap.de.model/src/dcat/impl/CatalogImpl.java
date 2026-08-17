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

import dcat.Catalog;
import dcat.CatalogRecord;
import dcat.DataService;
import dcat.Dataset;
import dcat.DcatPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Catalog</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link dcat.impl.CatalogImpl#getCatalog <em>Catalog</em>}</li>
 *   <li>{@link dcat.impl.CatalogImpl#getRecord <em>Record</em>}</li>
 *   <li>{@link dcat.impl.CatalogImpl#getDataset <em>Dataset</em>}</li>
 *   <li>{@link dcat.impl.CatalogImpl#getService <em>Service</em>}</li>
 *   <li>{@link dcat.impl.CatalogImpl#getThemeTaxonomy <em>Theme Taxonomy</em>}</li>
 *   <li>{@link dcat.impl.CatalogImpl#getHasPart <em>Has Part</em>}</li>
 *   <li>{@link dcat.impl.CatalogImpl#getHomepage <em>Homepage</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CatalogImpl extends DatasetImpl implements Catalog {
	/**
	 * The cached value of the '{@link #getCatalog() <em>Catalog</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCatalog()
	 * @generated
	 * @ordered
	 */
	protected EList<Catalog> catalog;

	/**
	 * The cached value of the '{@link #getRecord() <em>Record</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRecord()
	 * @generated
	 * @ordered
	 */
	protected EList<CatalogRecord> record;

	/**
	 * The cached value of the '{@link #getDataset() <em>Dataset</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataset()
	 * @generated
	 * @ordered
	 */
	protected EList<Dataset> dataset;

	/**
	 * The cached value of the '{@link #getService() <em>Service</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getService()
	 * @generated
	 * @ordered
	 */
	protected EList<DataService> service;

	/**
	 * The cached value of the '{@link #getThemeTaxonomy() <em>Theme Taxonomy</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThemeTaxonomy()
	 * @generated
	 * @ordered
	 */
	protected EList<String> themeTaxonomy;

	/**
	 * The cached value of the '{@link #getHasPart() <em>Has Part</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHasPart()
	 * @generated
	 * @ordered
	 */
	protected EList<String> hasPart;

	/**
	 * The default value of the '{@link #getHomepage() <em>Homepage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHomepage()
	 * @generated
	 * @ordered
	 */
	protected static final String HOMEPAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getHomepage() <em>Homepage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHomepage()
	 * @generated
	 * @ordered
	 */
	protected String homepage = HOMEPAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CatalogImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DcatPackage.Literals.CATALOG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Catalog> getCatalog() {
		if (catalog == null) {
			catalog = new EObjectResolvingEList<Catalog>(Catalog.class, this, DcatPackage.CATALOG__CATALOG);
		}
		return catalog;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<CatalogRecord> getRecord() {
		if (record == null) {
			record = new EObjectContainmentEList<CatalogRecord>(CatalogRecord.class, this, DcatPackage.CATALOG__RECORD);
		}
		return record;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Dataset> getDataset() {
		if (dataset == null) {
			dataset = new EObjectResolvingEList<Dataset>(Dataset.class, this, DcatPackage.CATALOG__DATASET);
		}
		return dataset;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<DataService> getService() {
		if (service == null) {
			service = new EObjectResolvingEList<DataService>(DataService.class, this, DcatPackage.CATALOG__SERVICE);
		}
		return service;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getThemeTaxonomy() {
		if (themeTaxonomy == null) {
			themeTaxonomy = new EDataTypeUniqueEList<String>(String.class, this, DcatPackage.CATALOG__THEME_TAXONOMY);
		}
		return themeTaxonomy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getHasPart() {
		if (hasPart == null) {
			hasPart = new EDataTypeUniqueEList<String>(String.class, this, DcatPackage.CATALOG__HAS_PART);
		}
		return hasPart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getHomepage() {
		return homepage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHomepage(String newHomepage) {
		String oldHomepage = homepage;
		homepage = newHomepage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DcatPackage.CATALOG__HOMEPAGE, oldHomepage, homepage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DcatPackage.CATALOG__RECORD:
				return ((InternalEList<?>)getRecord()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DcatPackage.CATALOG__CATALOG:
				return getCatalog();
			case DcatPackage.CATALOG__RECORD:
				return getRecord();
			case DcatPackage.CATALOG__DATASET:
				return getDataset();
			case DcatPackage.CATALOG__SERVICE:
				return getService();
			case DcatPackage.CATALOG__THEME_TAXONOMY:
				return getThemeTaxonomy();
			case DcatPackage.CATALOG__HAS_PART:
				return getHasPart();
			case DcatPackage.CATALOG__HOMEPAGE:
				return getHomepage();
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
			case DcatPackage.CATALOG__CATALOG:
				getCatalog().clear();
				getCatalog().addAll((Collection<? extends Catalog>)newValue);
				return;
			case DcatPackage.CATALOG__RECORD:
				getRecord().clear();
				getRecord().addAll((Collection<? extends CatalogRecord>)newValue);
				return;
			case DcatPackage.CATALOG__DATASET:
				getDataset().clear();
				getDataset().addAll((Collection<? extends Dataset>)newValue);
				return;
			case DcatPackage.CATALOG__SERVICE:
				getService().clear();
				getService().addAll((Collection<? extends DataService>)newValue);
				return;
			case DcatPackage.CATALOG__THEME_TAXONOMY:
				getThemeTaxonomy().clear();
				getThemeTaxonomy().addAll((Collection<? extends String>)newValue);
				return;
			case DcatPackage.CATALOG__HAS_PART:
				getHasPart().clear();
				getHasPart().addAll((Collection<? extends String>)newValue);
				return;
			case DcatPackage.CATALOG__HOMEPAGE:
				setHomepage((String)newValue);
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
			case DcatPackage.CATALOG__CATALOG:
				getCatalog().clear();
				return;
			case DcatPackage.CATALOG__RECORD:
				getRecord().clear();
				return;
			case DcatPackage.CATALOG__DATASET:
				getDataset().clear();
				return;
			case DcatPackage.CATALOG__SERVICE:
				getService().clear();
				return;
			case DcatPackage.CATALOG__THEME_TAXONOMY:
				getThemeTaxonomy().clear();
				return;
			case DcatPackage.CATALOG__HAS_PART:
				getHasPart().clear();
				return;
			case DcatPackage.CATALOG__HOMEPAGE:
				setHomepage(HOMEPAGE_EDEFAULT);
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
			case DcatPackage.CATALOG__CATALOG:
				return catalog != null && !catalog.isEmpty();
			case DcatPackage.CATALOG__RECORD:
				return record != null && !record.isEmpty();
			case DcatPackage.CATALOG__DATASET:
				return dataset != null && !dataset.isEmpty();
			case DcatPackage.CATALOG__SERVICE:
				return service != null && !service.isEmpty();
			case DcatPackage.CATALOG__THEME_TAXONOMY:
				return themeTaxonomy != null && !themeTaxonomy.isEmpty();
			case DcatPackage.CATALOG__HAS_PART:
				return hasPart != null && !hasPart.isEmpty();
			case DcatPackage.CATALOG__HOMEPAGE:
				return HOMEPAGE_EDEFAULT == null ? homepage != null : !HOMEPAGE_EDEFAULT.equals(homepage);
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
		result.append(" (themeTaxonomy: ");
		result.append(themeTaxonomy);
		result.append(", hasPart: ");
		result.append(hasPart);
		result.append(", homepage: ");
		result.append(homepage);
		result.append(')');
		return result.toString();
	}

} //CatalogImpl
