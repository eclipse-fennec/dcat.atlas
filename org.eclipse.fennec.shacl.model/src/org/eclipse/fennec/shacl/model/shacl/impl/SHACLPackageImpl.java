/**
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
package org.eclipse.fennec.shacl.model.shacl.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import org.eclipse.fennec.shacl.model.shacl.AbstractResult;
import org.eclipse.fennec.shacl.model.shacl.SHACLFactory;
import org.eclipse.fennec.shacl.model.shacl.SHACLPackage;
import org.eclipse.fennec.shacl.model.shacl.ValidationReport;
import org.eclipse.fennec.shacl.model.shacl.ValidationResult;

import rdf.RdfPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SHACLPackageImpl extends EPackageImpl implements SHACLPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass validationReportEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass abstractResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass validationResultEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private SHACLPackageImpl() {
		super(eNS_URI, SHACLFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link SHACLPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static SHACLPackage init() {
		if (isInited) return (SHACLPackage)EPackage.Registry.INSTANCE.getEPackage(SHACLPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredSHACLPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		SHACLPackageImpl theSHACLPackage = registeredSHACLPackage instanceof SHACLPackageImpl ? (SHACLPackageImpl)registeredSHACLPackage : new SHACLPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		RdfPackage.eINSTANCE.eClass();
		XMLTypePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theSHACLPackage.createPackageContents();

		// Initialize created meta-data
		theSHACLPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theSHACLPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(SHACLPackage.eNS_URI, theSHACLPackage);
		return theSHACLPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getValidationReport() {
		return validationReportEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getValidationReport_Conforms() {
		return (EAttribute)validationReportEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getValidationReport_Result() {
		return (EReference)validationReportEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getValidationReport_ShapesGraphWellFormed() {
		return (EAttribute)validationReportEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAbstractResult() {
		return abstractResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_FocusNode() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_ResultPath() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_ResultMessage() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_ResultSeverity() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_Value() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_SourceShape() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_SourceConstraintComponent() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_SourceConstraint() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAbstractResult_Detail() {
		return (EReference)abstractResultEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getValidationResult() {
		return validationResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SHACLFactory getSHACLFactory() {
		return (SHACLFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		validationReportEClass = createEClass(VALIDATION_REPORT);
		createEAttribute(validationReportEClass, VALIDATION_REPORT__CONFORMS);
		createEReference(validationReportEClass, VALIDATION_REPORT__RESULT);
		createEAttribute(validationReportEClass, VALIDATION_REPORT__SHAPES_GRAPH_WELL_FORMED);

		abstractResultEClass = createEClass(ABSTRACT_RESULT);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__FOCUS_NODE);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__RESULT_PATH);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__RESULT_MESSAGE);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__RESULT_SEVERITY);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__VALUE);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__SOURCE_SHAPE);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__SOURCE_CONSTRAINT_COMPONENT);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__SOURCE_CONSTRAINT);
		createEReference(abstractResultEClass, ABSTRACT_RESULT__DETAIL);

		validationResultEClass = createEClass(VALIDATION_RESULT);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		XMLTypePackage theXMLTypePackage = (XMLTypePackage)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);
		RdfPackage theRdfPackage = (RdfPackage)EPackage.Registry.INSTANCE.getEPackage(RdfPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		validationResultEClass.getESuperTypes().add(this.getAbstractResult());

		// Initialize classes, features, and operations; add parameters
		initEClass(validationReportEClass, ValidationReport.class, "ValidationReport", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getValidationReport_Conforms(), theXMLTypePackage.getBoolean(), "conforms", null, 0, 1, ValidationReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getValidationReport_Result(), this.getValidationResult(), null, "result", null, 0, -1, ValidationReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValidationReport_ShapesGraphWellFormed(), theXMLTypePackage.getBoolean(), "shapesGraphWellFormed", null, 0, 1, ValidationReport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(abstractResultEClass, AbstractResult.class, "AbstractResult", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getAbstractResult_FocusNode(), theRdfPackage.getResource(), null, "focusNode", null, 0, 1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAbstractResult_ResultPath(), theRdfPackage.getResource(), null, "resultPath", null, 0, 1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAbstractResult_ResultMessage(), theRdfPackage.getPlainLiteral(), null, "resultMessage", null, 0, -1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAbstractResult_ResultSeverity(), theRdfPackage.getResource(), null, "resultSeverity", null, 0, 1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAbstractResult_Value(), theRdfPackage.getResource(), null, "value", null, 0, 1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAbstractResult_SourceShape(), theRdfPackage.getResource(), null, "sourceShape", null, 0, 1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAbstractResult_SourceConstraintComponent(), theRdfPackage.getResource(), null, "sourceConstraintComponent", null, 0, 1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAbstractResult_SourceConstraint(), theRdfPackage.getResource(), null, "sourceConstraint", null, 0, 1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAbstractResult_Detail(), this.getAbstractResult(), null, "detail", null, 0, -1, AbstractResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(validationResultEClass, ValidationResult.class, "ValidationResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// Version
		createVersionAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>Version</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createVersionAnnotations() {
		String source = "Version";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "value", "1.0"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "documentation", "Results subset of the W3C Shapes Constraint Language (SHACL) vocabulary\n(http://www.w3.org/ns/shacl#). Only the SHACL *validation report* vocabulary is\nmodelled here (sh:ValidationReport / sh:ValidationResult and the result properties),\nnot the constraint vocabulary (sh:Shape, sh:NodeShape, constraint components, ...),\nsince this bundle exists to represent the report returned by validation (FR-4/FR-5,\nFR-19), not to author shapes. URI-valued result properties reuse rdf:Resource and\nlang-tagged messages reuse rdf:PlainLiteral from the DCAT-AP model bundle so the\nreport serialises as spec-correct RDF (rdf:resource references, xml:lang literals).",
			   "complianceLevel", "17.0",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.shacl.model",
			   "resource", "XML",
			   "copyrightText", "Copyright (c) 2026 Contributors to the Eclipse Foundation.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n  Data In Motion Consulting - initial implementation"
		   });
		addAnnotation
		  (validationReportEClass,
		   source,
		   new String[] {
			   "documentation", "The class of SHACL validation reports."
		   });
		addAnnotation
		  (getValidationReport_Conforms(),
		   source,
		   new String[] {
			   "documentation", "True if the validation did not produce any validation results, and false otherwise."
		   });
		addAnnotation
		  (getValidationReport_Result(),
		   source,
		   new String[] {
			   "documentation", "The validation results contained in a validation report."
		   });
		addAnnotation
		  (getValidationReport_ShapesGraphWellFormed(),
		   source,
		   new String[] {
			   "documentation", "If true then the validation engine was certain that the shapes graph has passed all SHACL syntax requirements during the validation process."
		   });
		addAnnotation
		  (abstractResultEClass,
		   source,
		   new String[] {
			   "documentation", "The base class of validation results, typically not instantiated directly."
		   });
		addAnnotation
		  (getAbstractResult_FocusNode(),
		   source,
		   new String[] {
			   "documentation", "The focus node that was validated when the result was produced."
		   });
		addAnnotation
		  (getAbstractResult_ResultPath(),
		   source,
		   new String[] {
			   "documentation", "The path of a validation result, based on the path of the validated property shape. Only simple predicate paths are represented as a plain IRI; complex SHACL path expressions are not expanded."
		   });
		addAnnotation
		  (getAbstractResult_ResultMessage(),
		   source,
		   new String[] {
			   "documentation", "Human-readable messages explaining the cause of the result (may be language-tagged)."
		   });
		addAnnotation
		  (getAbstractResult_ResultSeverity(),
		   source,
		   new String[] {
			   "documentation", "The severity of the result: sh:Violation (MUSS, blocks a write), sh:Warning or sh:Info (SOLL/recommendation). Held as the severity IRI via rdf:resource."
		   });
		addAnnotation
		  (getAbstractResult_Value(),
		   source,
		   new String[] {
			   "documentation", "An RDF node that has caused the result. Only IRI values are represented (via rdf:resource); literal offending values are not captured here."
		   });
		addAnnotation
		  (getAbstractResult_SourceShape(),
		   source,
		   new String[] {
			   "documentation", "The shape that was validated when the result was produced (as an IRI reference into the shapes graph)."
		   });
		addAnnotation
		  (getAbstractResult_SourceConstraintComponent(),
		   source,
		   new String[] {
			   "documentation", "The constraint component that is the source of the result."
		   });
		addAnnotation
		  (getAbstractResult_SourceConstraint(),
		   source,
		   new String[] {
			   "documentation", "The constraint that was validated when the result was produced."
		   });
		addAnnotation
		  (getAbstractResult_Detail(),
		   source,
		   new String[] {
			   "documentation", "Links a result with other results that provide more details, e.g. violations against nested shapes."
		   });
		addAnnotation
		  (validationResultEClass,
		   source,
		   new String[] {
			   "documentation", "The class of validation results."
		   });
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";
		addAnnotation
		  (validationReportEClass,
		   source,
		   new String[] {
			   "name", "ValidationReport",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getValidationReport_Conforms(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "conforms",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getValidationReport_Result(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "result",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getValidationReport_ShapesGraphWellFormed(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "shapesGraphWellFormed",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (abstractResultEClass,
		   source,
		   new String[] {
			   "name", "AbstractResult",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getAbstractResult_FocusNode(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "focusNode",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAbstractResult_ResultPath(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "resultPath",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAbstractResult_ResultMessage(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "resultMessage",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAbstractResult_ResultSeverity(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "resultSeverity",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAbstractResult_Value(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "value",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAbstractResult_SourceShape(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "sourceShape",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAbstractResult_SourceConstraintComponent(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "sourceConstraintComponent",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAbstractResult_SourceConstraint(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "sourceConstraint",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getAbstractResult_Detail(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "detail",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (validationResultEClass,
		   source,
		   new String[] {
			   "name", "ValidationResult",
			   "kind", "elementOnly"
		   });
	}

} //SHACLPackageImpl
