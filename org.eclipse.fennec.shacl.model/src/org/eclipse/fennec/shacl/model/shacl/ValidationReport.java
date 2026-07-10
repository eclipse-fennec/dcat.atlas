/*
 */
package org.eclipse.fennec.shacl.model.shacl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Validation Report</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The class of SHACL validation reports.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport#isConforms <em>Conforms</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport#getResult <em>Result</em>}</li>
 *   <li>{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport#isShapesGraphWellFormed <em>Shapes Graph Well Formed</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getValidationReport()
 * @model extendedMetaData="name='ValidationReport' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface ValidationReport extends EObject {
	/**
	 * Returns the value of the '<em><b>Conforms</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * True if the validation did not produce any validation results, and false otherwise.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Conforms</em>' attribute.
	 * @see #setConforms(boolean)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getValidationReport_Conforms()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Boolean"
	 *        extendedMetaData="kind='element' name='conforms' namespace='##targetNamespace'"
	 * @generated
	 */
	boolean isConforms();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport#isConforms <em>Conforms</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Conforms</em>' attribute.
	 * @see #isConforms()
	 * @generated
	 */
	void setConforms(boolean value);

	/**
	 * Returns the value of the '<em><b>Result</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.shacl.model.shacl.ValidationResult}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The validation results contained in a validation report.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Result</em>' containment reference list.
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getValidationReport_Result()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='result' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<ValidationResult> getResult();

	/**
	 * Returns the value of the '<em><b>Shapes Graph Well Formed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * If true then the validation engine was certain that the shapes graph has passed all SHACL syntax requirements during the validation process.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Shapes Graph Well Formed</em>' attribute.
	 * @see #setShapesGraphWellFormed(boolean)
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#getValidationReport_ShapesGraphWellFormed()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Boolean"
	 *        extendedMetaData="kind='element' name='shapesGraphWellFormed' namespace='##targetNamespace'"
	 * @generated
	 */
	boolean isShapesGraphWellFormed();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport#isShapesGraphWellFormed <em>Shapes Graph Well Formed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shapes Graph Well Formed</em>' attribute.
	 * @see #isShapesGraphWellFormed()
	 * @generated
	 */
	void setShapesGraphWellFormed(boolean value);

} // ValidationReport
