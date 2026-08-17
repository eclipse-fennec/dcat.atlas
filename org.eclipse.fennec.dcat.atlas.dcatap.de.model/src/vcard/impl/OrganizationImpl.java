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

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import rdf.impl.IdentifiedResourceImpl;

import vcard.Address;
import vcard.Organization;
import vcard.VcardPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Organization</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link vcard.impl.OrganizationImpl#getFn <em>Fn</em>}</li>
 *   <li>{@link vcard.impl.OrganizationImpl#getOrganizationName <em>Organization Name</em>}</li>
 *   <li>{@link vcard.impl.OrganizationImpl#getHasAddress <em>Has Address</em>}</li>
 *   <li>{@link vcard.impl.OrganizationImpl#getHasTelephone <em>Has Telephone</em>}</li>
 *   <li>{@link vcard.impl.OrganizationImpl#getNodeID <em>Node ID</em>}</li>
 *   <li>{@link vcard.impl.OrganizationImpl#getHasEmail <em>Has Email</em>}</li>
 *   <li>{@link vcard.impl.OrganizationImpl#getHasURL <em>Has URL</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OrganizationImpl extends IdentifiedResourceImpl implements Organization {
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
	 * The cached value of the '{@link #getHasTelephone() <em>Has Telephone</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHasTelephone()
	 * @generated
	 * @ordered
	 */
	protected EList<String> hasTelephone;

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
	 * The cached value of the '{@link #getHasEmail() <em>Has Email</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHasEmail()
	 * @generated
	 * @ordered
	 */
	protected EList<String> hasEmail;

	/**
	 * The cached value of the '{@link #getHasURL() <em>Has URL</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHasURL()
	 * @generated
	 * @ordered
	 */
	protected EList<String> hasURL;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OrganizationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return VcardPackage.Literals.ORGANIZATION;
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
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION__FN, oldFn, fn));
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
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION__ORGANIZATION_NAME, oldOrganizationName, organizationName));
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
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION__HAS_ADDRESS, oldHasAddress, newHasAddress);
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
				msgs = ((InternalEObject)hasAddress).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - VcardPackage.ORGANIZATION__HAS_ADDRESS, null, msgs);
			if (newHasAddress != null)
				msgs = ((InternalEObject)newHasAddress).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - VcardPackage.ORGANIZATION__HAS_ADDRESS, null, msgs);
			msgs = basicSetHasAddress(newHasAddress, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION__HAS_ADDRESS, newHasAddress, newHasAddress));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getHasTelephone() {
		if (hasTelephone == null) {
			hasTelephone = new EDataTypeUniqueEList<String>(String.class, this, VcardPackage.ORGANIZATION__HAS_TELEPHONE);
		}
		return hasTelephone;
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
			eNotify(new ENotificationImpl(this, Notification.SET, VcardPackage.ORGANIZATION__NODE_ID, oldNodeID, nodeID));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getHasEmail() {
		if (hasEmail == null) {
			hasEmail = new EDataTypeUniqueEList<String>(String.class, this, VcardPackage.ORGANIZATION__HAS_EMAIL);
		}
		return hasEmail;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getHasURL() {
		if (hasURL == null) {
			hasURL = new EDataTypeUniqueEList<String>(String.class, this, VcardPackage.ORGANIZATION__HAS_URL);
		}
		return hasURL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case VcardPackage.ORGANIZATION__HAS_ADDRESS:
				return basicSetHasAddress(null, msgs);
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
			case VcardPackage.ORGANIZATION__FN:
				return getFn();
			case VcardPackage.ORGANIZATION__ORGANIZATION_NAME:
				return getOrganizationName();
			case VcardPackage.ORGANIZATION__HAS_ADDRESS:
				return getHasAddress();
			case VcardPackage.ORGANIZATION__HAS_TELEPHONE:
				return getHasTelephone();
			case VcardPackage.ORGANIZATION__NODE_ID:
				return getNodeID();
			case VcardPackage.ORGANIZATION__HAS_EMAIL:
				return getHasEmail();
			case VcardPackage.ORGANIZATION__HAS_URL:
				return getHasURL();
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
			case VcardPackage.ORGANIZATION__FN:
				setFn((String)newValue);
				return;
			case VcardPackage.ORGANIZATION__ORGANIZATION_NAME:
				setOrganizationName((String)newValue);
				return;
			case VcardPackage.ORGANIZATION__HAS_ADDRESS:
				setHasAddress((Address)newValue);
				return;
			case VcardPackage.ORGANIZATION__HAS_TELEPHONE:
				getHasTelephone().clear();
				getHasTelephone().addAll((Collection<? extends String>)newValue);
				return;
			case VcardPackage.ORGANIZATION__NODE_ID:
				setNodeID((String)newValue);
				return;
			case VcardPackage.ORGANIZATION__HAS_EMAIL:
				getHasEmail().clear();
				getHasEmail().addAll((Collection<? extends String>)newValue);
				return;
			case VcardPackage.ORGANIZATION__HAS_URL:
				getHasURL().clear();
				getHasURL().addAll((Collection<? extends String>)newValue);
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
			case VcardPackage.ORGANIZATION__FN:
				setFn(FN_EDEFAULT);
				return;
			case VcardPackage.ORGANIZATION__ORGANIZATION_NAME:
				setOrganizationName(ORGANIZATION_NAME_EDEFAULT);
				return;
			case VcardPackage.ORGANIZATION__HAS_ADDRESS:
				setHasAddress((Address)null);
				return;
			case VcardPackage.ORGANIZATION__HAS_TELEPHONE:
				getHasTelephone().clear();
				return;
			case VcardPackage.ORGANIZATION__NODE_ID:
				setNodeID(NODE_ID_EDEFAULT);
				return;
			case VcardPackage.ORGANIZATION__HAS_EMAIL:
				getHasEmail().clear();
				return;
			case VcardPackage.ORGANIZATION__HAS_URL:
				getHasURL().clear();
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
			case VcardPackage.ORGANIZATION__FN:
				return FN_EDEFAULT == null ? fn != null : !FN_EDEFAULT.equals(fn);
			case VcardPackage.ORGANIZATION__ORGANIZATION_NAME:
				return ORGANIZATION_NAME_EDEFAULT == null ? organizationName != null : !ORGANIZATION_NAME_EDEFAULT.equals(organizationName);
			case VcardPackage.ORGANIZATION__HAS_ADDRESS:
				return hasAddress != null;
			case VcardPackage.ORGANIZATION__HAS_TELEPHONE:
				return hasTelephone != null && !hasTelephone.isEmpty();
			case VcardPackage.ORGANIZATION__NODE_ID:
				return NODE_ID_EDEFAULT == null ? nodeID != null : !NODE_ID_EDEFAULT.equals(nodeID);
			case VcardPackage.ORGANIZATION__HAS_EMAIL:
				return hasEmail != null && !hasEmail.isEmpty();
			case VcardPackage.ORGANIZATION__HAS_URL:
				return hasURL != null && !hasURL.isEmpty();
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
		result.append(", nodeID: ");
		result.append(nodeID);
		result.append(", hasEmail: ");
		result.append(hasEmail);
		result.append(", hasURL: ");
		result.append(hasURL);
		result.append(')');
		return result.toString();
	}

} //OrganizationImpl
