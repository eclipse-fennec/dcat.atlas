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
package rdf.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import rdf.IdentifiedResource;
import rdf.RdfPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Identified Resource</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link rdf.impl.IdentifiedResourceImpl#getAbout <em>About</em>}</li>
 *   <li>{@link rdf.impl.IdentifiedResourceImpl#getManagedBy <em>Managed By</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class IdentifiedResourceImpl extends MinimalEObjectImpl.Container implements IdentifiedResource {
	/**
	 * The default value of the '{@link #getAbout() <em>About</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAbout()
	 * @generated
	 * @ordered
	 */
	protected static final String ABOUT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAbout() <em>About</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAbout()
	 * @generated
	 * @ordered
	 */
	protected String about = ABOUT_EDEFAULT;

	/**
	 * The default value of the '{@link #getManagedBy() <em>Managed By</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getManagedBy()
	 * @generated
	 * @ordered
	 */
	protected static final String MANAGED_BY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getManagedBy() <em>Managed By</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getManagedBy()
	 * @generated
	 * @ordered
	 */
	protected String managedBy = MANAGED_BY_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IdentifiedResourceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RdfPackage.Literals.IDENTIFIED_RESOURCE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getAbout() {
		return about;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAbout(String newAbout) {
		String oldAbout = about;
		about = newAbout;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RdfPackage.IDENTIFIED_RESOURCE__ABOUT, oldAbout, about));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getManagedBy() {
		return managedBy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setManagedBy(String newManagedBy) {
		String oldManagedBy = managedBy;
		managedBy = newManagedBy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RdfPackage.IDENTIFIED_RESOURCE__MANAGED_BY, oldManagedBy, managedBy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RdfPackage.IDENTIFIED_RESOURCE__ABOUT:
				return getAbout();
			case RdfPackage.IDENTIFIED_RESOURCE__MANAGED_BY:
				return getManagedBy();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case RdfPackage.IDENTIFIED_RESOURCE__ABOUT:
				setAbout((String)newValue);
				return;
			case RdfPackage.IDENTIFIED_RESOURCE__MANAGED_BY:
				setManagedBy((String)newValue);
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
			case RdfPackage.IDENTIFIED_RESOURCE__ABOUT:
				setAbout(ABOUT_EDEFAULT);
				return;
			case RdfPackage.IDENTIFIED_RESOURCE__MANAGED_BY:
				setManagedBy(MANAGED_BY_EDEFAULT);
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
			case RdfPackage.IDENTIFIED_RESOURCE__ABOUT:
				return ABOUT_EDEFAULT == null ? about != null : !ABOUT_EDEFAULT.equals(about);
			case RdfPackage.IDENTIFIED_RESOURCE__MANAGED_BY:
				return MANAGED_BY_EDEFAULT == null ? managedBy != null : !MANAGED_BY_EDEFAULT.equals(managedBy);
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
		result.append(" (about: ");
		result.append(about);
		result.append(", managedBy: ");
		result.append(managedBy);
		result.append(')');
		return result.toString();
	}

} //IdentifiedResourceImpl
