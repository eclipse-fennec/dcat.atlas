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
package terms.util;

import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.EObjectValidator;

import rdf.util.RdfValidator;

import terms.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see terms.TermsPackage
 * @generated
 */
public class TermsValidator extends EObjectValidator {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final TermsValidator INSTANCE = new TermsValidator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "terms";

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
	protected RdfValidator rdfValidator;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TermsValidator() {
		super();
		rdfValidator = RdfValidator.INSTANCE;
	}

	/**
	 * Returns the package of this validator switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EPackage getEPackage() {
	  return TermsPackage.eINSTANCE;
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
			case TermsPackage.LICENSE_DOCUMENT:
				return validateLicenseDocument((LicenseDocument)value, diagnostics, context);
			case TermsPackage.LOCATION:
				return validateLocation((Location)value, diagnostics, context);
			case TermsPackage.PERIOD_OF_TIME:
				return validatePeriodOfTime((PeriodOfTime)value, diagnostics, context);
			case TermsPackage.PROVENANCE_STATEMENT:
				return validateProvenanceStatement((ProvenanceStatement)value, diagnostics, context);
			case TermsPackage.RIGHTS_STATEMENT:
				return validateRightsStatement((RightsStatement)value, diagnostics, context);
			case TermsPackage.STANDARD:
				return validateStandard((Standard)value, diagnostics, context);
			default:
				return true;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateLicenseDocument(LicenseDocument licenseDocument, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(licenseDocument, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(licenseDocument, diagnostics, context);
		if (result || diagnostics != null) result &= validateLicenseDocument_TypeIsIri(licenseDocument, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the TypeIsIri constraint of '<em>License Document</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String LICENSE_DOCUMENT__TYPE_IS_IRI__EEXPRESSION = "self.type->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the TypeIsIri constraint of '<em>License Document</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateLicenseDocument_TypeIsIri(LicenseDocument licenseDocument, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(TermsPackage.Literals.LICENSE_DOCUMENT,
				 licenseDocument,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "TypeIsIri",
				 LICENSE_DOCUMENT__TYPE_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateLocation(Location location, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(location, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(location, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(location, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(location, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(location, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(location, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(location, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(location, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(location, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(location, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validatePeriodOfTime(PeriodOfTime periodOfTime, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(periodOfTime, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(periodOfTime, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(periodOfTime, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(periodOfTime, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(periodOfTime, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(periodOfTime, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(periodOfTime, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(periodOfTime, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(periodOfTime, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(periodOfTime, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateProvenanceStatement(ProvenanceStatement provenanceStatement, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(provenanceStatement, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(provenanceStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(provenanceStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(provenanceStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(provenanceStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(provenanceStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(provenanceStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(provenanceStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(provenanceStatement, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(provenanceStatement, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateRightsStatement(RightsStatement rightsStatement, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(rightsStatement, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(rightsStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(rightsStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(rightsStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(rightsStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(rightsStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(rightsStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(rightsStatement, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(rightsStatement, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(rightsStatement, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateStandard(Standard standard, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(standard, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(standard, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(standard, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(standard, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(standard, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(standard, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(standard, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(standard, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(standard, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(standard, diagnostics, context);
		return result;
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

} //TermsValidator
