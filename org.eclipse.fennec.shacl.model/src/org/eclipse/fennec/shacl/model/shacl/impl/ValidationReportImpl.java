/*
 */
package org.eclipse.fennec.shacl.model.shacl.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.shacl.model.shacl.SHACLPackage;
import org.eclipse.fennec.shacl.model.shacl.ValidationReport;
import org.eclipse.fennec.shacl.model.shacl.ValidationResult;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Validation Report</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.ValidationReportImpl#isConforms <em>Conforms</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.ValidationReportImpl#getResult <em>Result</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.ValidationReportImpl#isShapesGraphWellFormed <em>Shapes Graph Well Formed</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ValidationReportImpl extends MinimalEObjectImpl.Container implements ValidationReport {
	/**
	 * The default value of the '{@link #isConforms() <em>Conforms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isConforms()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CONFORMS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isConforms() <em>Conforms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isConforms()
	 * @generated
	 * @ordered
	 */
	protected boolean conforms = CONFORMS_EDEFAULT;

	/**
	 * The cached value of the '{@link #getResult() <em>Result</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResult()
	 * @generated
	 * @ordered
	 */
	protected EList<ValidationResult> result;

	/**
	 * The default value of the '{@link #isShapesGraphWellFormed() <em>Shapes Graph Well Formed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isShapesGraphWellFormed()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SHAPES_GRAPH_WELL_FORMED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isShapesGraphWellFormed() <em>Shapes Graph Well Formed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isShapesGraphWellFormed()
	 * @generated
	 * @ordered
	 */
	protected boolean shapesGraphWellFormed = SHAPES_GRAPH_WELL_FORMED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ValidationReportImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SHACLPackage.Literals.VALIDATION_REPORT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isConforms() {
		return conforms;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setConforms(boolean newConforms) {
		boolean oldConforms = conforms;
		conforms = newConforms;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.VALIDATION_REPORT__CONFORMS, oldConforms, conforms));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ValidationResult> getResult() {
		if (result == null) {
			result = new EObjectContainmentEList<ValidationResult>(ValidationResult.class, this, SHACLPackage.VALIDATION_REPORT__RESULT);
		}
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isShapesGraphWellFormed() {
		return shapesGraphWellFormed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setShapesGraphWellFormed(boolean newShapesGraphWellFormed) {
		boolean oldShapesGraphWellFormed = shapesGraphWellFormed;
		shapesGraphWellFormed = newShapesGraphWellFormed;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.VALIDATION_REPORT__SHAPES_GRAPH_WELL_FORMED, oldShapesGraphWellFormed, shapesGraphWellFormed));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SHACLPackage.VALIDATION_REPORT__RESULT:
				return ((InternalEList<?>)getResult()).basicRemove(otherEnd, msgs);
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
			case SHACLPackage.VALIDATION_REPORT__CONFORMS:
				return isConforms();
			case SHACLPackage.VALIDATION_REPORT__RESULT:
				return getResult();
			case SHACLPackage.VALIDATION_REPORT__SHAPES_GRAPH_WELL_FORMED:
				return isShapesGraphWellFormed();
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
			case SHACLPackage.VALIDATION_REPORT__CONFORMS:
				setConforms((Boolean)newValue);
				return;
			case SHACLPackage.VALIDATION_REPORT__RESULT:
				getResult().clear();
				getResult().addAll((Collection<? extends ValidationResult>)newValue);
				return;
			case SHACLPackage.VALIDATION_REPORT__SHAPES_GRAPH_WELL_FORMED:
				setShapesGraphWellFormed((Boolean)newValue);
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
			case SHACLPackage.VALIDATION_REPORT__CONFORMS:
				setConforms(CONFORMS_EDEFAULT);
				return;
			case SHACLPackage.VALIDATION_REPORT__RESULT:
				getResult().clear();
				return;
			case SHACLPackage.VALIDATION_REPORT__SHAPES_GRAPH_WELL_FORMED:
				setShapesGraphWellFormed(SHAPES_GRAPH_WELL_FORMED_EDEFAULT);
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
			case SHACLPackage.VALIDATION_REPORT__CONFORMS:
				return conforms != CONFORMS_EDEFAULT;
			case SHACLPackage.VALIDATION_REPORT__RESULT:
				return result != null && !result.isEmpty();
			case SHACLPackage.VALIDATION_REPORT__SHAPES_GRAPH_WELL_FORMED:
				return shapesGraphWellFormed != SHAPES_GRAPH_WELL_FORMED_EDEFAULT;
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
		result.append(" (conforms: ");
		result.append(conforms);
		result.append(", shapesGraphWellFormed: ");
		result.append(shapesGraphWellFormed);
		result.append(')');
		return result.toString();
	}

} //ValidationReportImpl
