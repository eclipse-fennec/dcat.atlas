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
package vcard.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import rdf.Resource;

import vcard.Address;
import vcard.OrganizationType;
import vcard.VcardPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Organization Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link vcard.impl.OrganizationTypeImpl#getFn <em>Fn</em>}</li>
 *   <li>{@link vcard.impl.OrganizationTypeImpl#getOrganizationName <em>Organization Name</em>}</li>
 *   <li>{@link vcard.impl.OrganizationTypeImpl#getHasAddress <em>Has Address</em>}</li>
 *   <li>{@link vcard.impl.OrganizationTypeImpl#getHasEmail <em>Has Email</em>}</li>
 *   <li>{@link vcard.impl.OrganizationTypeImpl#getHasURL <em>Has URL</em>}</li>
 *   <li>{@link vcard.impl.OrganizationTypeImpl#getHasTelephone <em>Has Telephone</em>}</li>
 *   <li>{@link vcard.impl.OrganizationTypeImpl#getAbout <em>About</em>}</li>
 *   <li>{@link vcard.impl.OrganizationTypeImpl#getNodeID <em>Node ID</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OrganizationTypeImpl extends MinimalEObjectImpl.Container implements OrganizationType {
	/**
	 * The default value of the '{@link #getFn() <em>Fn</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFn()
	 * @generated
	 * @ordered
	 */
	protected static final String FN_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFn() <em>Fn</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFn()
	 * @generated
	 * @ordered
	 */
	protected String fn = FN_EDEFAULT;

	/**
	 * The default value of the '{@link #getOrganizationName() <em>Organization Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOrganizationName()
	 * @generated
	 * @ordered
	 */
	protected static final String ORGANIZATION_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOrganizationName() <em>Organization Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOrganizationName()
	 * @generated
	 * @ordered
	 */
	protected String organizationName = ORGANIZATION_NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getHasAddress() <em>Has Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHasAddress()
	 * @generated
	 * @ordered
	 */
	protected Address hasAddress;

	/**
	 * The cached value of the '{@link #getHasEmail() <em>Has Email</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHasEmail()
	 * @generated
	 * @ordered
	 */
	protected EList<Resource> hasEmail;

	/**
	 * The cached value of the '{@link #getHasURL() <em>Has URL</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHasURL()
	 * @generated
	 * @ordered
	 */
	protected EList<Resource> hasURL;

	/**
	 * The cached value of the '{@link #getHasTelephone() <em>Has Telephone</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHasTelephone()
	 * @generated
	 * @ordered
	 */
	protected EList<String> hasTelephone;

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
	 * The default value of the '{@link #getNodeID() <em>Node ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNodeID()
	 * @generated
	 * @ordered
	 */
	protected static final String NODE_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNodeID() <em>Node ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNodeID()
	 * @generated
	 * @ordered
	 */
	protected String nodeID = NODE_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OrganizationTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return VcardPackage.Literals.ORGANIZATION_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFn() {
		return fn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFn(String newFn) {
		String oldFn = fn;
		fn = newFn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION_TYPE__FN, oldFn, fn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getOrganizationName() {
		return organizationName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOrganizationName(String newOrganizationName) {
		String oldOrganizationName = organizationName;
		organizationName = newOrganizationName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION_TYPE__ORGANIZATION_NAME, oldOrganizationName, organizationName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Address getHasAddress() {
		return hasAddress;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHasAddress(Address newHasAddress, NotificationChain msgs) {
		Address oldHasAddress = hasAddress;
		hasAddress = newHasAddress;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS, oldHasAddress, newHasAddress);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHasAddress(Address newHasAddress) {
		if (newHasAddress != hasAddress) {
			NotificationChain msgs = null;
			if (hasAddress != null)
				msgs = ((InternalEObject)hasAddress).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS, null, msgs);
			if (newHasAddress != null)
				msgs = ((InternalEObject)newHasAddress).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS, null, msgs);
			msgs = basicSetHasAddress(newHasAddress, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS, newHasAddress, newHasAddress));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Resource> getHasEmail() {
		if (hasEmail == null) {
			hasEmail = new EObjectContainmentEList<Resource>(Resource.class, this, VcardPackage.ORGANIZATION_TYPE__HAS_EMAIL);
		}
		return hasEmail;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Resource> getHasURL() {
		if (hasURL == null) {
			hasURL = new EObjectContainmentEList<Resource>(Resource.class, this, VcardPackage.ORGANIZATION_TYPE__HAS_URL);
		}
		return hasURL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getHasTelephone() {
		if (hasTelephone == null) {
			hasTelephone = new EDataTypeUniqueEList<String>(String.class, this, VcardPackage.ORGANIZATION_TYPE__HAS_TELEPHONE);
		}
		return hasTelephone;
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
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION_TYPE__ABOUT, oldAbout, about));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getNodeID() {
		return nodeID;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNodeID(String newNodeID) {
		String oldNodeID = nodeID;
		nodeID = newNodeID;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION_TYPE__NODE_ID, oldNodeID, nodeID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS:
				return basicSetHasAddress(null, msgs);
			case VcardPackage.ORGANIZATION_TYPE__HAS_EMAIL:
				return ((InternalEList<?>)getHasEmail()).basicRemove(otherEnd, msgs);
			case VcardPackage.ORGANIZATION_TYPE__HAS_URL:
				return ((InternalEList<?>)getHasURL()).basicRemove(otherEnd, msgs);
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
			case VcardPackage.ORGANIZATION_TYPE__FN:
				return getFn();
			case VcardPackage.ORGANIZATION_TYPE__ORGANIZATION_NAME:
				return getOrganizationName();
			case VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS:
				return getHasAddress();
			case VcardPackage.ORGANIZATION_TYPE__HAS_EMAIL:
				return getHasEmail();
			case VcardPackage.ORGANIZATION_TYPE__HAS_URL:
				return getHasURL();
			case VcardPackage.ORGANIZATION_TYPE__HAS_TELEPHONE:
				return getHasTelephone();
			case VcardPackage.ORGANIZATION_TYPE__ABOUT:
				return getAbout();
			case VcardPackage.ORGANIZATION_TYPE__NODE_ID:
				return getNodeID();
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
			case VcardPackage.ORGANIZATION_TYPE__FN:
				setFn((String)newValue);
				return;
			case VcardPackage.ORGANIZATION_TYPE__ORGANIZATION_NAME:
				setOrganizationName((String)newValue);
				return;
			case VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS:
				setHasAddress((Address)newValue);
				return;
			case VcardPackage.ORGANIZATION_TYPE__HAS_EMAIL:
				getHasEmail().clear();
				getHasEmail().addAll((Collection<? extends Resource>)newValue);
				return;
			case VcardPackage.ORGANIZATION_TYPE__HAS_URL:
				getHasURL().clear();
				getHasURL().addAll((Collection<? extends Resource>)newValue);
				return;
			case VcardPackage.ORGANIZATION_TYPE__HAS_TELEPHONE:
				getHasTelephone().clear();
				getHasTelephone().addAll((Collection<? extends String>)newValue);
				return;
			case VcardPackage.ORGANIZATION_TYPE__ABOUT:
				setAbout((String)newValue);
				return;
			case VcardPackage.ORGANIZATION_TYPE__NODE_ID:
				setNodeID((String)newValue);
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
			case VcardPackage.ORGANIZATION_TYPE__FN:
				setFn(FN_EDEFAULT);
				return;
			case VcardPackage.ORGANIZATION_TYPE__ORGANIZATION_NAME:
				setOrganizationName(ORGANIZATION_NAME_EDEFAULT);
				return;
			case VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS:
				setHasAddress((Address)null);
				return;
			case VcardPackage.ORGANIZATION_TYPE__HAS_EMAIL:
				getHasEmail().clear();
				return;
			case VcardPackage.ORGANIZATION_TYPE__HAS_URL:
				getHasURL().clear();
				return;
			case VcardPackage.ORGANIZATION_TYPE__HAS_TELEPHONE:
				getHasTelephone().clear();
				return;
			case VcardPackage.ORGANIZATION_TYPE__ABOUT:
				setAbout(ABOUT_EDEFAULT);
				return;
			case VcardPackage.ORGANIZATION_TYPE__NODE_ID:
				setNodeID(NODE_ID_EDEFAULT);
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
			case VcardPackage.ORGANIZATION_TYPE__FN:
				return FN_EDEFAULT == null ? fn != null : !FN_EDEFAULT.equals(fn);
			case VcardPackage.ORGANIZATION_TYPE__ORGANIZATION_NAME:
				return ORGANIZATION_NAME_EDEFAULT == null ? organizationName != null : !ORGANIZATION_NAME_EDEFAULT.equals(organizationName);
			case VcardPackage.ORGANIZATION_TYPE__HAS_ADDRESS:
				return hasAddress != null;
			case VcardPackage.ORGANIZATION_TYPE__HAS_EMAIL:
				return hasEmail != null && !hasEmail.isEmpty();
			case VcardPackage.ORGANIZATION_TYPE__HAS_URL:
				return hasURL != null && !hasURL.isEmpty();
			case VcardPackage.ORGANIZATION_TYPE__HAS_TELEPHONE:
				return hasTelephone != null && !hasTelephone.isEmpty();
			case VcardPackage.ORGANIZATION_TYPE__ABOUT:
				return ABOUT_EDEFAULT == null ? about != null : !ABOUT_EDEFAULT.equals(about);
			case VcardPackage.ORGANIZATION_TYPE__NODE_ID:
				return NODE_ID_EDEFAULT == null ? nodeID != null : !NODE_ID_EDEFAULT.equals(nodeID);
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
		result.append(" (fn: ");
		result.append(fn);
		result.append(", organizationName: ");
		result.append(organizationName);
		result.append(", hasTelephone: ");
		result.append(hasTelephone);
		result.append(", about: ");
		result.append(about);
		result.append(", nodeID: ");
		result.append(nodeID);
		result.append(')');
		return result.toString();
	}

} //OrganizationTypeImpl
