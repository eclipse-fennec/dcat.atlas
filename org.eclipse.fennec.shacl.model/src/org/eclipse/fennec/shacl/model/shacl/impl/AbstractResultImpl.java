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

import org.eclipse.fennec.shacl.model.shacl.AbstractResult;
import org.eclipse.fennec.shacl.model.shacl.SHACLPackage;

import rdf.PlainLiteral;
import rdf.Resource;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Abstract Result</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getFocusNode <em>Focus Node</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getResultPath <em>Result Path</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getResultMessage <em>Result Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getResultSeverity <em>Result Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getValue <em>Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getSourceShape <em>Source Shape</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getSourceConstraintComponent <em>Source Constraint Component</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getSourceConstraint <em>Source Constraint</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl#getDetail <em>Detail</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class AbstractResultImpl extends MinimalEObjectImpl.Container implements AbstractResult {
	/**
	 * The cached value of the '{@link #getFocusNode() <em>Focus Node</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFocusNode()
	 * @generated
	 * @ordered
	 */
	protected Resource focusNode;

	/**
	 * The cached value of the '{@link #getResultPath() <em>Result Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResultPath()
	 * @generated
	 * @ordered
	 */
	protected Resource resultPath;

	/**
	 * The cached value of the '{@link #getResultMessage() <em>Result Message</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResultMessage()
	 * @generated
	 * @ordered
	 */
	protected EList<PlainLiteral> resultMessage;

	/**
	 * The cached value of the '{@link #getResultSeverity() <em>Result Severity</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResultSeverity()
	 * @generated
	 * @ordered
	 */
	protected Resource resultSeverity;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected Resource value;

	/**
	 * The cached value of the '{@link #getSourceShape() <em>Source Shape</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceShape()
	 * @generated
	 * @ordered
	 */
	protected Resource sourceShape;

	/**
	 * The cached value of the '{@link #getSourceConstraintComponent() <em>Source Constraint Component</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceConstraintComponent()
	 * @generated
	 * @ordered
	 */
	protected Resource sourceConstraintComponent;

	/**
	 * The cached value of the '{@link #getSourceConstraint() <em>Source Constraint</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceConstraint()
	 * @generated
	 * @ordered
	 */
	protected Resource sourceConstraint;

	/**
	 * The cached value of the '{@link #getDetail() <em>Detail</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDetail()
	 * @generated
	 * @ordered
	 */
	protected EList<AbstractResult> detail;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AbstractResultImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SHACLPackage.Literals.ABSTRACT_RESULT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource getFocusNode() {
		return focusNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFocusNode(Resource newFocusNode, NotificationChain msgs) {
		Resource oldFocusNode = focusNode;
		focusNode = newFocusNode;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE, oldFocusNode, newFocusNode);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFocusNode(Resource newFocusNode) {
		if (newFocusNode != focusNode) {
			NotificationChain msgs = null;
			if (focusNode != null)
				msgs = ((InternalEObject)focusNode).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE, null, msgs);
			if (newFocusNode != null)
				msgs = ((InternalEObject)newFocusNode).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE, null, msgs);
			msgs = basicSetFocusNode(newFocusNode, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE, newFocusNode, newFocusNode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource getResultPath() {
		return resultPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetResultPath(Resource newResultPath, NotificationChain msgs) {
		Resource oldResultPath = resultPath;
		resultPath = newResultPath;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__RESULT_PATH, oldResultPath, newResultPath);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResultPath(Resource newResultPath) {
		if (newResultPath != resultPath) {
			NotificationChain msgs = null;
			if (resultPath != null)
				msgs = ((InternalEObject)resultPath).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__RESULT_PATH, null, msgs);
			if (newResultPath != null)
				msgs = ((InternalEObject)newResultPath).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__RESULT_PATH, null, msgs);
			msgs = basicSetResultPath(newResultPath, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__RESULT_PATH, newResultPath, newResultPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<PlainLiteral> getResultMessage() {
		if (resultMessage == null) {
			resultMessage = new EObjectContainmentEList<PlainLiteral>(PlainLiteral.class, this, SHACLPackage.ABSTRACT_RESULT__RESULT_MESSAGE);
		}
		return resultMessage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource getResultSeverity() {
		return resultSeverity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetResultSeverity(Resource newResultSeverity, NotificationChain msgs) {
		Resource oldResultSeverity = resultSeverity;
		resultSeverity = newResultSeverity;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY, oldResultSeverity, newResultSeverity);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResultSeverity(Resource newResultSeverity) {
		if (newResultSeverity != resultSeverity) {
			NotificationChain msgs = null;
			if (resultSeverity != null)
				msgs = ((InternalEObject)resultSeverity).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY, null, msgs);
			if (newResultSeverity != null)
				msgs = ((InternalEObject)newResultSeverity).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY, null, msgs);
			msgs = basicSetResultSeverity(newResultSeverity, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY, newResultSeverity, newResultSeverity));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetValue(Resource newValue, NotificationChain msgs) {
		Resource oldValue = value;
		value = newValue;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__VALUE, oldValue, newValue);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setValue(Resource newValue) {
		if (newValue != value) {
			NotificationChain msgs = null;
			if (value != null)
				msgs = ((InternalEObject)value).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__VALUE, null, msgs);
			if (newValue != null)
				msgs = ((InternalEObject)newValue).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__VALUE, null, msgs);
			msgs = basicSetValue(newValue, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__VALUE, newValue, newValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource getSourceShape() {
		return sourceShape;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSourceShape(Resource newSourceShape, NotificationChain msgs) {
		Resource oldSourceShape = sourceShape;
		sourceShape = newSourceShape;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE, oldSourceShape, newSourceShape);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSourceShape(Resource newSourceShape) {
		if (newSourceShape != sourceShape) {
			NotificationChain msgs = null;
			if (sourceShape != null)
				msgs = ((InternalEObject)sourceShape).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE, null, msgs);
			if (newSourceShape != null)
				msgs = ((InternalEObject)newSourceShape).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE, null, msgs);
			msgs = basicSetSourceShape(newSourceShape, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE, newSourceShape, newSourceShape));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource getSourceConstraintComponent() {
		return sourceConstraintComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSourceConstraintComponent(Resource newSourceConstraintComponent, NotificationChain msgs) {
		Resource oldSourceConstraintComponent = sourceConstraintComponent;
		sourceConstraintComponent = newSourceConstraintComponent;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT, oldSourceConstraintComponent, newSourceConstraintComponent);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSourceConstraintComponent(Resource newSourceConstraintComponent) {
		if (newSourceConstraintComponent != sourceConstraintComponent) {
			NotificationChain msgs = null;
			if (sourceConstraintComponent != null)
				msgs = ((InternalEObject)sourceConstraintComponent).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT, null, msgs);
			if (newSourceConstraintComponent != null)
				msgs = ((InternalEObject)newSourceConstraintComponent).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT, null, msgs);
			msgs = basicSetSourceConstraintComponent(newSourceConstraintComponent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT, newSourceConstraintComponent, newSourceConstraintComponent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource getSourceConstraint() {
		return sourceConstraint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSourceConstraint(Resource newSourceConstraint, NotificationChain msgs) {
		Resource oldSourceConstraint = sourceConstraint;
		sourceConstraint = newSourceConstraint;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT, oldSourceConstraint, newSourceConstraint);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSourceConstraint(Resource newSourceConstraint) {
		if (newSourceConstraint != sourceConstraint) {
			NotificationChain msgs = null;
			if (sourceConstraint != null)
				msgs = ((InternalEObject)sourceConstraint).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT, null, msgs);
			if (newSourceConstraint != null)
				msgs = ((InternalEObject)newSourceConstraint).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT, null, msgs);
			msgs = basicSetSourceConstraint(newSourceConstraint, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT, newSourceConstraint, newSourceConstraint));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<AbstractResult> getDetail() {
		if (detail == null) {
			detail = new EObjectContainmentEList<AbstractResult>(AbstractResult.class, this, SHACLPackage.ABSTRACT_RESULT__DETAIL);
		}
		return detail;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE:
				return basicSetFocusNode(null, msgs);
			case SHACLPackage.ABSTRACT_RESULT__RESULT_PATH:
				return basicSetResultPath(null, msgs);
			case SHACLPackage.ABSTRACT_RESULT__RESULT_MESSAGE:
				return ((InternalEList<?>)getResultMessage()).basicRemove(otherEnd, msgs);
			case SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY:
				return basicSetResultSeverity(null, msgs);
			case SHACLPackage.ABSTRACT_RESULT__VALUE:
				return basicSetValue(null, msgs);
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE:
				return basicSetSourceShape(null, msgs);
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT:
				return basicSetSourceConstraintComponent(null, msgs);
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT:
				return basicSetSourceConstraint(null, msgs);
			case SHACLPackage.ABSTRACT_RESULT__DETAIL:
				return ((InternalEList<?>)getDetail()).basicRemove(otherEnd, msgs);
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
			case SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE:
				return getFocusNode();
			case SHACLPackage.ABSTRACT_RESULT__RESULT_PATH:
				return getResultPath();
			case SHACLPackage.ABSTRACT_RESULT__RESULT_MESSAGE:
				return getResultMessage();
			case SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY:
				return getResultSeverity();
			case SHACLPackage.ABSTRACT_RESULT__VALUE:
				return getValue();
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE:
				return getSourceShape();
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT:
				return getSourceConstraintComponent();
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT:
				return getSourceConstraint();
			case SHACLPackage.ABSTRACT_RESULT__DETAIL:
				return getDetail();
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
			case SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE:
				setFocusNode((Resource)newValue);
				return;
			case SHACLPackage.ABSTRACT_RESULT__RESULT_PATH:
				setResultPath((Resource)newValue);
				return;
			case SHACLPackage.ABSTRACT_RESULT__RESULT_MESSAGE:
				getResultMessage().clear();
				getResultMessage().addAll((Collection<? extends PlainLiteral>)newValue);
				return;
			case SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY:
				setResultSeverity((Resource)newValue);
				return;
			case SHACLPackage.ABSTRACT_RESULT__VALUE:
				setValue((Resource)newValue);
				return;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE:
				setSourceShape((Resource)newValue);
				return;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT:
				setSourceConstraintComponent((Resource)newValue);
				return;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT:
				setSourceConstraint((Resource)newValue);
				return;
			case SHACLPackage.ABSTRACT_RESULT__DETAIL:
				getDetail().clear();
				getDetail().addAll((Collection<? extends AbstractResult>)newValue);
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
			case SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE:
				setFocusNode((Resource)null);
				return;
			case SHACLPackage.ABSTRACT_RESULT__RESULT_PATH:
				setResultPath((Resource)null);
				return;
			case SHACLPackage.ABSTRACT_RESULT__RESULT_MESSAGE:
				getResultMessage().clear();
				return;
			case SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY:
				setResultSeverity((Resource)null);
				return;
			case SHACLPackage.ABSTRACT_RESULT__VALUE:
				setValue((Resource)null);
				return;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE:
				setSourceShape((Resource)null);
				return;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT:
				setSourceConstraintComponent((Resource)null);
				return;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT:
				setSourceConstraint((Resource)null);
				return;
			case SHACLPackage.ABSTRACT_RESULT__DETAIL:
				getDetail().clear();
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
			case SHACLPackage.ABSTRACT_RESULT__FOCUS_NODE:
				return focusNode != null;
			case SHACLPackage.ABSTRACT_RESULT__RESULT_PATH:
				return resultPath != null;
			case SHACLPackage.ABSTRACT_RESULT__RESULT_MESSAGE:
				return resultMessage != null && !resultMessage.isEmpty();
			case SHACLPackage.ABSTRACT_RESULT__RESULT_SEVERITY:
				return resultSeverity != null;
			case SHACLPackage.ABSTRACT_RESULT__VALUE:
				return value != null;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_SHAPE:
				return sourceShape != null;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT:
				return sourceConstraintComponent != null;
			case SHACLPackage.ABSTRACT_RESULT__SOURCE_CONSTRAINT:
				return sourceConstraint != null;
			case SHACLPackage.ABSTRACT_RESULT__DETAIL:
				return detail != null && !detail.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //AbstractResultImpl
