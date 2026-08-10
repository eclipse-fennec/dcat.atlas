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
package org.eclipse.fennec.shacl.model.shacl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

import rdf.PlainLiteral;
import rdf.Resource;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Abstract Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The base class of validation results, typically not instantiated directly.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getFocusNode <em>Focus Node</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultPath <em>Result Path</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultMessage <em>Result Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultSeverity <em>Result Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getValue <em>Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceShape <em>Source Shape</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceConstraintComponent <em>Source Constraint Component</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceConstraint <em>Source Constraint</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getDetail <em>Detail</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult()
 * @model abstract="true"
 *        extendedMetaData="name='AbstractResult' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface AbstractResult extends EObject {
	/**
	 * Returns the value of the '<em><b>Focus Node</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The focus node that was validated when the result was produced.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Focus Node</em>' containment reference.
	 * @see #setFocusNode(Resource)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_FocusNode()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='focusNode' namespace='##targetNamespace'"
	 * @generated
	 */
	Resource getFocusNode();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getFocusNode <em>Focus Node</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Focus Node</em>' containment reference.
	 * @see #getFocusNode()
	 * @generated
	 */
	void setFocusNode(Resource value);

	/**
	 * Returns the value of the '<em><b>Result Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The path of a validation result, based on the path of the validated property shape. Only simple predicate paths are represented as a plain IRI; complex SHACL path expressions are not expanded.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Result Path</em>' containment reference.
	 * @see #setResultPath(Resource)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_ResultPath()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='resultPath' namespace='##targetNamespace'"
	 * @generated
	 */
	Resource getResultPath();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultPath <em>Result Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Result Path</em>' containment reference.
	 * @see #getResultPath()
	 * @generated
	 */
	void setResultPath(Resource value);

	/**
	 * Returns the value of the '<em><b>Result Message</b></em>' containment reference list.
	 * The list contents are of type {@link rdf.PlainLiteral}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Human-readable messages explaining the cause of the result (may be language-tagged).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Result Message</em>' containment reference list.
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_ResultMessage()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='resultMessage' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<PlainLiteral> getResultMessage();

	/**
	 * Returns the value of the '<em><b>Result Severity</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The severity of the result: sh:Violation (MUSS, blocks a write), sh:Warning or sh:Info (SOLL/recommendation). Held as the severity IRI via rdf:resource.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Result Severity</em>' containment reference.
	 * @see #setResultSeverity(Resource)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_ResultSeverity()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='resultSeverity' namespace='##targetNamespace'"
	 * @generated
	 */
	Resource getResultSeverity();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultSeverity <em>Result Severity</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Result Severity</em>' containment reference.
	 * @see #getResultSeverity()
	 * @generated
	 */
	void setResultSeverity(Resource value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An RDF node that has caused the result. Only IRI values are represented (via rdf:resource); literal offending values are not captured here.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Value</em>' containment reference.
	 * @see #setValue(Resource)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_Value()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='value' namespace='##targetNamespace'"
	 * @generated
	 */
	Resource getValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getValue <em>Value</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' containment reference.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(Resource value);

	/**
	 * Returns the value of the '<em><b>Source Shape</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The shape that was validated when the result was produced (as an IRI reference into the shapes graph).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Shape</em>' containment reference.
	 * @see #setSourceShape(Resource)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_SourceShape()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='sourceShape' namespace='##targetNamespace'"
	 * @generated
	 */
	Resource getSourceShape();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceShape <em>Source Shape</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Shape</em>' containment reference.
	 * @see #getSourceShape()
	 * @generated
	 */
	void setSourceShape(Resource value);

	/**
	 * Returns the value of the '<em><b>Source Constraint Component</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The constraint component that is the source of the result.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Constraint Component</em>' containment reference.
	 * @see #setSourceConstraintComponent(Resource)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_SourceConstraintComponent()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='sourceConstraintComponent' namespace='##targetNamespace'"
	 * @generated
	 */
	Resource getSourceConstraintComponent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceConstraintComponent <em>Source Constraint Component</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Constraint Component</em>' containment reference.
	 * @see #getSourceConstraintComponent()
	 * @generated
	 */
	void setSourceConstraintComponent(Resource value);

	/**
	 * Returns the value of the '<em><b>Source Constraint</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The constraint that was validated when the result was produced.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Constraint</em>' containment reference.
	 * @see #setSourceConstraint(Resource)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_SourceConstraint()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='sourceConstraint' namespace='##targetNamespace'"
	 * @generated
	 */
	Resource getSourceConstraint();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceConstraint <em>Source Constraint</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Constraint</em>' containment reference.
	 * @see #getSourceConstraint()
	 * @generated
	 */
	void setSourceConstraint(Resource value);

	/**
	 * Returns the value of the '<em><b>Detail</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.shacl.model.shacl.AbstractResult}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Links a result with other results that provide more details, e.g. violations against nested shapes.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Detail</em>' containment reference list.
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getAbstractResult_Detail()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='detail' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<AbstractResult> getDetail();

} // AbstractResult
