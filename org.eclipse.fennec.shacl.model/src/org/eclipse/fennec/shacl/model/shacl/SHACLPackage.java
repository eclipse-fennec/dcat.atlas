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


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Results subset of the W3C Shapes Constraint Language (SHACL) vocabulary
 * (http://www.w3.org/ns/shacl#). Only the SHACL *validation report* vocabulary is
 * modelled here (sh:ValidationReport / sh:ValidationResult and the result properties),
 * not the constraint vocabulary (sh:Shape, sh:NodeShape, constraint components, ...),
 * since this bundle exists to represent the report returned by validation (FR-4/FR-5,
 * FR-19), not to author shapes. URI-valued result properties reuse rdf:Resource and
 * lang-tagged messages reuse rdf:PlainLiteral from the DCAT-AP model bundle so the
 * report serialises as spec-correct RDF (rdf:resource references, xml:lang literals).
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.shacl.model.shacl.SHACLFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.shacl.model' resource='XML' copyrightText='Copyright (c) 2026 Contributors to the Eclipse Foundation.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n  Data In Motion Consulting - initial implementation'"
 * @generated
 */
@ProviderType
@EPackage(uri = SHACLPackage.eNS_URI, genModel = "/model/shacl.genmodel", genModelSourceLocations = {"model/shacl.genmodel","org.eclipse.fennec.shacl.model/model/shacl.genmodel"}, ecore = "/model/shacl.ecore", ecoreSourceLocations = "/model/shacl.ecore")
public interface SHACLPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "shacl";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.w3.org/ns/shacl#";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "shacl";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SHACLPackage eINSTANCE = org.eclipse.fennec.shacl.model.shacl.impl.SHACLPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.shacl.model.shacl.impl.ValidationReportImpl <em>Validation Report</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.shacl.model.shacl.impl.ValidationReportImpl
	 * @see org.eclipse.fennec.shacl.model.shacl.impl.SHACLPackageImpl#getValidationReport()
	 * @generated
	 */
	int VALIDATION_REPORT = 0;

	/**
	 * The feature id for the '<em><b>Conforms</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REPORT__CONFORMS = 0;

	/**
	 * The feature id for the '<em><b>Result</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REPORT__RESULT = 1;

	/**
	 * The feature id for the '<em><b>Shapes Graph Well Formed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REPORT__SHAPES_GRAPH_WELL_FORMED = 2;

	/**
	 * The number of structural features of the '<em>Validation Report</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REPORT_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Validation Report</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REPORT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl <em>Abstract Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl
	 * @see org.eclipse.fennec.shacl.model.shacl.impl.SHACLPackageImpl#getAbstractResult()
	 * @generated
	 */
	int ABSTRACT_RESULT = 1;

	/**
	 * The feature id for the '<em><b>Focus Node</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__FOCUS_NODE = 0;

	/**
	 * The feature id for the '<em><b>Result Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__RESULT_PATH = 1;

	/**
	 * The feature id for the '<em><b>Result Message</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__RESULT_MESSAGE = 2;

	/**
	 * The feature id for the '<em><b>Result Severity</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__RESULT_SEVERITY = 3;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__VALUE = 4;

	/**
	 * The feature id for the '<em><b>Source Shape</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__SOURCE_SHAPE = 5;

	/**
	 * The feature id for the '<em><b>Source Constraint Component</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT = 6;

	/**
	 * The feature id for the '<em><b>Source Constraint</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__SOURCE_CONSTRAINT = 7;

	/**
	 * The feature id for the '<em><b>Detail</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT__DETAIL = 8;

	/**
	 * The number of structural features of the '<em>Abstract Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT_FEATURE_COUNT = 9;

	/**
	 * The number of operations of the '<em>Abstract Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ABSTRACT_RESULT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.shacl.model.shacl.impl.ValidationResultImpl <em>Validation Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.shacl.model.shacl.impl.ValidationResultImpl
	 * @see org.eclipse.fennec.shacl.model.shacl.impl.SHACLPackageImpl#getValidationResult()
	 * @generated
	 */
	int VALIDATION_RESULT = 2;

	/**
	 * The feature id for the '<em><b>Focus Node</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__FOCUS_NODE = ABSTRACT_RESULT__FOCUS_NODE;

	/**
	 * The feature id for the '<em><b>Result Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__RESULT_PATH = ABSTRACT_RESULT__RESULT_PATH;

	/**
	 * The feature id for the '<em><b>Result Message</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__RESULT_MESSAGE = ABSTRACT_RESULT__RESULT_MESSAGE;

	/**
	 * The feature id for the '<em><b>Result Severity</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__RESULT_SEVERITY = ABSTRACT_RESULT__RESULT_SEVERITY;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__VALUE = ABSTRACT_RESULT__VALUE;

	/**
	 * The feature id for the '<em><b>Source Shape</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__SOURCE_SHAPE = ABSTRACT_RESULT__SOURCE_SHAPE;

	/**
	 * The feature id for the '<em><b>Source Constraint Component</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__SOURCE_CONSTRAINT_COMPONENT = ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT;

	/**
	 * The feature id for the '<em><b>Source Constraint</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__SOURCE_CONSTRAINT = ABSTRACT_RESULT__SOURCE_CONSTRAINT;

	/**
	 * The feature id for the '<em><b>Detail</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__DETAIL = ABSTRACT_RESULT__DETAIL;

	/**
	 * The number of structural features of the '<em>Validation Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT_FEATURE_COUNT = ABSTRACT_RESULT_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Validation Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT_OPERATION_COUNT = ABSTRACT_RESULT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport <em>Validation Report</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Validation Report</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.ValidationReport
	 * @generated
	 */
	EClass getValidationReport();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport#isConforms <em>Conforms</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Conforms</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.ValidationReport#isConforms()
	 * @see #getValidationReport()
	 * @generated
	 */
	EAttribute getValidationReport_Conforms();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport#getResult <em>Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Result</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.ValidationReport#getResult()
	 * @see #getValidationReport()
	 * @generated
	 */
	EReference getValidationReport_Result();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.shacl.model.shacl.ValidationReport#isShapesGraphWellFormed <em>Shapes Graph Well Formed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Shapes Graph Well Formed</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.ValidationReport#isShapesGraphWellFormed()
	 * @see #getValidationReport()
	 * @generated
	 */
	EAttribute getValidationReport_ShapesGraphWellFormed();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult <em>Abstract Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Abstract Result</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult
	 * @generated
	 */
	EClass getAbstractResult();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getFocusNode <em>Focus Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Focus Node</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getFocusNode()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_FocusNode();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultPath <em>Result Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Result Path</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultPath()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_ResultPath();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultMessage <em>Result Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Result Message</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultMessage()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_ResultMessage();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultSeverity <em>Result Severity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Result Severity</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getResultSeverity()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_ResultSeverity();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getValue()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_Value();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceShape <em>Source Shape</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Source Shape</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceShape()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_SourceShape();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceConstraintComponent <em>Source Constraint Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Source Constraint Component</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceConstraintComponent()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_SourceConstraintComponent();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceConstraint <em>Source Constraint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Source Constraint</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getSourceConstraint()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_SourceConstraint();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.shacl.model.shacl.AbstractResult#getDetail <em>Detail</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Detail</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.AbstractResult#getDetail()
	 * @see #getAbstractResult()
	 * @generated
	 */
	EReference getAbstractResult_Detail();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.shacl.model.shacl.ValidationResult <em>Validation Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Validation Result</em>'.
	 * @see org.eclipse.fennec.shacl.model.shacl.ValidationResult
	 * @generated
	 */
	EClass getValidationResult();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	SHACLFactory getSHACLFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.shacl.model.shacl.impl.ValidationReportImpl <em>Validation Report</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.shacl.model.shacl.impl.ValidationReportImpl
		 * @see org.eclipse.fennec.shacl.model.shacl.impl.SHACLPackageImpl#getValidationReport()
		 * @generated
		 */
		EClass VALIDATION_REPORT = eINSTANCE.getValidationReport();

		/**
		 * The meta object literal for the '<em><b>Conforms</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALIDATION_REPORT__CONFORMS = eINSTANCE.getValidationReport_Conforms();

		/**
		 * The meta object literal for the '<em><b>Result</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VALIDATION_REPORT__RESULT = eINSTANCE.getValidationReport_Result();

		/**
		 * The meta object literal for the '<em><b>Shapes Graph Well Formed</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALIDATION_REPORT__SHAPES_GRAPH_WELL_FORMED = eINSTANCE.getValidationReport_ShapesGraphWellFormed();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl <em>Abstract Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.shacl.model.shacl.impl.AbstractResultImpl
		 * @see org.eclipse.fennec.shacl.model.shacl.impl.SHACLPackageImpl#getAbstractResult()
		 * @generated
		 */
		EClass ABSTRACT_RESULT = eINSTANCE.getAbstractResult();

		/**
		 * The meta object literal for the '<em><b>Focus Node</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__FOCUS_NODE = eINSTANCE.getAbstractResult_FocusNode();

		/**
		 * The meta object literal for the '<em><b>Result Path</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__RESULT_PATH = eINSTANCE.getAbstractResult_ResultPath();

		/**
		 * The meta object literal for the '<em><b>Result Message</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__RESULT_MESSAGE = eINSTANCE.getAbstractResult_ResultMessage();

		/**
		 * The meta object literal for the '<em><b>Result Severity</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__RESULT_SEVERITY = eINSTANCE.getAbstractResult_ResultSeverity();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__VALUE = eINSTANCE.getAbstractResult_Value();

		/**
		 * The meta object literal for the '<em><b>Source Shape</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__SOURCE_SHAPE = eINSTANCE.getAbstractResult_SourceShape();

		/**
		 * The meta object literal for the '<em><b>Source Constraint Component</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT = eINSTANCE.getAbstractResult_SourceConstraintComponent();

		/**
		 * The meta object literal for the '<em><b>Source Constraint</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__SOURCE_CONSTRAINT = eINSTANCE.getAbstractResult_SourceConstraint();

		/**
		 * The meta object literal for the '<em><b>Detail</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ABSTRACT_RESULT__DETAIL = eINSTANCE.getAbstractResult_Detail();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.shacl.model.shacl.impl.ValidationResultImpl <em>Validation Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.shacl.model.shacl.impl.ValidationResultImpl
		 * @see org.eclipse.fennec.shacl.model.shacl.impl.SHACLPackageImpl#getValidationResult()
		 * @generated
		 */
		EClass VALIDATION_RESULT = eINSTANCE.getValidationResult();

	}

} //SHACLPackage
