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
package rdf.util;

import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.EObjectValidator;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import org.eclipse.emf.ecore.xml.type.util.XMLTypeValidator;

import rdf.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see rdf.RdfPackage
 * @generated
 */
public class RdfValidator extends EObjectValidator {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final RdfValidator INSTANCE = new RdfValidator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "rdf";

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;

	/**
	 * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;

	/**
	 * The cached base package validator.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected XMLTypeValidator xmlTypeValidator;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RdfValidator() {
		super();
		xmlTypeValidator = XMLTypeValidator.INSTANCE;
	}

	/**
	 * Returns the package of this validator switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EPackage getEPackage() {
	  return RdfPackage.eINSTANCE;
	}

	/**
	 * Calls <code>validateXXX</code> for the corresponding classifier of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected boolean validate(int classifierID, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
		switch (classifierID) {
			case RdfPackage.DATE_OR_DATE_TIME_LITERAL:
				return validateDateOrDateTimeLiteral((DateOrDateTimeLiteral)value, diagnostics, context);
			case RdfPackage.PLAIN_LITERAL:
				return validatePlainLiteral((PlainLiteral)value, diagnostics, context);
			case RdfPackage.TYPED_LITERAL:
				return validateTypedLiteral((TypedLiteral)value, diagnostics, context);
			case RdfPackage.IDENTIFIED_RESOURCE:
				return validateIdentifiedResource((IdentifiedResource)value, diagnostics, context);
			case RdfPackage.DATATYPE:
				return validateDatatype((Datatype)value, diagnostics, context);
			case RdfPackage.DATE_OR_DATE_TIME:
				return validateDateOrDateTime((XMLGregorianCalendar)value, diagnostics, context);
			default:
				return true;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDateOrDateTimeLiteral(DateOrDateTimeLiteral dateOrDateTimeLiteral, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(dateOrDateTimeLiteral, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validatePlainLiteral(PlainLiteral plainLiteral, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return validate_EveryDefaultConstraint(plainLiteral, diagnostics, context);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateTypedLiteral(TypedLiteral typedLiteral, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(typedLiteral, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(typedLiteral, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(typedLiteral, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(typedLiteral, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(typedLiteral, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(typedLiteral, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(typedLiteral, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(typedLiteral, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(typedLiteral, diagnostics, context);
		if (result || diagnostics != null) result &= validateTypedLiteral_DatatypeIsIri(typedLiteral, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the DatatypeIsIri constraint of '<em>Typed Literal</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String TYPED_LITERAL__DATATYPE_IS_IRI__EEXPRESSION = "self.datatype = null or self.datatype.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the DatatypeIsIri constraint of '<em>Typed Literal</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateTypedLiteral_DatatypeIsIri(TypedLiteral typedLiteral, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(RdfPackage.Literals.TYPED_LITERAL,
				 typedLiteral,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "DatatypeIsIri",
				 TYPED_LITERAL__DATATYPE_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateIdentifiedResource(IdentifiedResource identifiedResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(identifiedResource, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(identifiedResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(identifiedResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(identifiedResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(identifiedResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(identifiedResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(identifiedResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(identifiedResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(identifiedResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateIdentifiedResource_AboutIsIri(identifiedResource, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the AboutIsIri constraint of '<em>Identified Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String IDENTIFIED_RESOURCE__ABOUT_IS_IRI__EEXPRESSION = "self.about = null or self.about.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the AboutIsIri constraint of '<em>Identified Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateIdentifiedResource_AboutIsIri(IdentifiedResource identifiedResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(RdfPackage.Literals.IDENTIFIED_RESOURCE,
				 identifiedResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "AboutIsIri",
				 IDENTIFIED_RESOURCE__ABOUT_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDatatype(Datatype datatype, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDateOrDateTime(XMLGregorianCalendar dateOrDateTime, DiagnosticChain diagnostics, Map<Object, Object> context) {
		boolean result = validateDateOrDateTime_MemberTypes(dateOrDateTime, diagnostics, context);
		return result;
	}

	/**
	 * Validates the MemberTypes constraint of '<em>Date Or Date Time</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDateOrDateTime_MemberTypes(XMLGregorianCalendar dateOrDateTime, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (diagnostics != null) {
			BasicDiagnostic tempDiagnostics = new BasicDiagnostic();
			if (XMLTypePackage.Literals.DATE.isInstance(dateOrDateTime)) {
				if (xmlTypeValidator.validateDate(dateOrDateTime, tempDiagnostics, context)) return true;
			}
			if (XMLTypePackage.Literals.DATE_TIME.isInstance(dateOrDateTime)) {
				if (xmlTypeValidator.validateDateTime(dateOrDateTime, tempDiagnostics, context)) return true;
			}
			if (XMLTypePackage.Literals.GYEAR.isInstance(dateOrDateTime)) {
				if (xmlTypeValidator.validateGYear(dateOrDateTime, tempDiagnostics, context)) return true;
			}
			if (XMLTypePackage.Literals.GYEAR_MONTH.isInstance(dateOrDateTime)) {
				if (xmlTypeValidator.validateGYearMonth(dateOrDateTime, tempDiagnostics, context)) return true;
			}
			for (Diagnostic diagnostic : tempDiagnostics.getChildren()) {
				diagnostics.add(diagnostic);
			}
		}
		else {
			if (XMLTypePackage.Literals.DATE.isInstance(dateOrDateTime)) {
				if (xmlTypeValidator.validateDate(dateOrDateTime, null, context)) return true;
			}
			if (XMLTypePackage.Literals.DATE_TIME.isInstance(dateOrDateTime)) {
				if (xmlTypeValidator.validateDateTime(dateOrDateTime, null, context)) return true;
			}
			if (XMLTypePackage.Literals.GYEAR.isInstance(dateOrDateTime)) {
				if (xmlTypeValidator.validateGYear(dateOrDateTime, null, context)) return true;
			}
			if (XMLTypePackage.Literals.GYEAR_MONTH.isInstance(dateOrDateTime)) {
				if (xmlTypeValidator.validateGYearMonth(dateOrDateTime, null, context)) return true;
			}
		}
		return false;
	}

	/**
	 * Returns the resource locator that will be used to fetch messages for this validator's diagnostics.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		// TODO
		// Specialize this to return a resource locator for messages specific to this validator.
		// Ensure that you remove @generated or mark it @generated NOT
		return super.getResourceLocator();
	}

} //RdfValidator
