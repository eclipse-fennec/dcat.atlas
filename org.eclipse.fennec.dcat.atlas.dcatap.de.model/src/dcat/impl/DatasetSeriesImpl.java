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

import dcat.DatasetSeries;
import dcat.DcatPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import skos.Concept;

import terms.Location;
import terms.PeriodOfTime;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Dataset Series</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link dcat.impl.DatasetSeriesImpl#getAccrualPeriodicity <em>Accrual Periodicity</em>}</li>
 *   <li>{@link dcat.impl.DatasetSeriesImpl#getSpatial <em>Spatial</em>}</li>
 *   <li>{@link dcat.impl.DatasetSeriesImpl#getTemporal <em>Temporal</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DatasetSeriesImpl extends DcatResourceImpl implements DatasetSeries {
	/**
	 * The cached value of the '{@link #getAccrualPeriodicity() <em>Accrual Periodicity</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAccrualPeriodicity()
	 * @generated
	 * @ordered
	 */
	protected Concept accrualPeriodicity;

	/**
	 * The cached value of the '{@link #getSpatial() <em>Spatial</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSpatial()
	 * @generated
	 * @ordered
	 */
	protected EList<Location> spatial;

	/**
	 * The cached value of the '{@link #getTemporal() <em>Temporal</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTemporal()
	 * @generated
	 * @ordered
	 */
	protected EList<PeriodOfTime> temporal;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DatasetSeriesImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DcatPackage.Literals.DATASET_SERIES;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Concept getAccrualPeriodicity() {
		return accrualPeriodicity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAccrualPeriodicity(Concept newAccrualPeriodicity, NotificationChain msgs) {
		Concept oldAccrualPeriodicity = accrualPeriodicity;
		accrualPeriodicity = newAccrualPeriodicity;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY, oldAccrualPeriodicity, newAccrualPeriodicity);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAccrualPeriodicity(Concept newAccrualPeriodicity) {
		if (newAccrualPeriodicity != accrualPeriodicity) {
			NotificationChain msgs = null;
			if (accrualPeriodicity != null)
				msgs = ((InternalEObject)accrualPeriodicity).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY, null, msgs);
			if (newAccrualPeriodicity != null)
				msgs = ((InternalEObject)newAccrualPeriodicity).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY, null, msgs);
			msgs = basicSetAccrualPeriodicity(newAccrualPeriodicity, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY, newAccrualPeriodicity, newAccrualPeriodicity));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Location> getSpatial() {
		if (spatial == null) {
			spatial = new EObjectContainmentEList<Location>(Location.class, this, DcatPackage.DATASET_SERIES__SPATIAL);
		}
		return spatial;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<PeriodOfTime> getTemporal() {
		if (temporal == null) {
			temporal = new EObjectContainmentEList<PeriodOfTime>(PeriodOfTime.class, this, DcatPackage.DATASET_SERIES__TEMPORAL);
		}
		return temporal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY:
				return basicSetAccrualPeriodicity(null, msgs);
			case DcatPackage.DATASET_SERIES__SPATIAL:
				return ((InternalEList<?>)getSpatial()).basicRemove(otherEnd, msgs);
			case DcatPackage.DATASET_SERIES__TEMPORAL:
				return ((InternalEList<?>)getTemporal()).basicRemove(otherEnd, msgs);
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
			case DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY:
				return getAccrualPeriodicity();
			case DcatPackage.DATASET_SERIES__SPATIAL:
				return getSpatial();
			case DcatPackage.DATASET_SERIES__TEMPORAL:
				return getTemporal();
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
			case DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY:
				setAccrualPeriodicity((Concept)newValue);
				return;
			case DcatPackage.DATASET_SERIES__SPATIAL:
				getSpatial().clear();
				getSpatial().addAll((Collection<? extends Location>)newValue);
				return;
			case DcatPackage.DATASET_SERIES__TEMPORAL:
				getTemporal().clear();
				getTemporal().addAll((Collection<? extends PeriodOfTime>)newValue);
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
			case DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY:
				setAccrualPeriodicity((Concept)null);
				return;
			case DcatPackage.DATASET_SERIES__SPATIAL:
				getSpatial().clear();
				return;
			case DcatPackage.DATASET_SERIES__TEMPORAL:
				getTemporal().clear();
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
			case DcatPackage.DATASET_SERIES__ACCRUAL_PERIODICITY:
				return accrualPeriodicity != null;
			case DcatPackage.DATASET_SERIES__SPATIAL:
				return spatial != null && !spatial.isEmpty();
			case DcatPackage.DATASET_SERIES__TEMPORAL:
				return temporal != null && !temporal.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //DatasetSeriesImpl
