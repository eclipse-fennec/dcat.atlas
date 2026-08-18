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
package vcard.util;

import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.EObjectValidator;

import rdf.util.RdfValidator;

import vcard.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see vcard.VcardPackage
 * @generated
 */
public class VcardValidator extends EObjectValidator {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final VcardValidator INSTANCE = new VcardValidator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "vcard";

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
	public VcardValidator() {
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
	  return VcardPackage.eINSTANCE;
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
			case VcardPackage.ADDRESS:
				return validateAddress((Address)value, diagnostics, context);
			case VcardPackage.ORGANIZATION:
				return validateOrganization((Organization)value, diagnostics, context);
			default:
				return true;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateAddress(Address address, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(address, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(address, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(address, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(address, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(address, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(address, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(address, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(address, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(address, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(address, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateOrganization(Organization organization, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(organization, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(organization, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validateOrganization_HasEmailIsMailto(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validateOrganization_HasTelephoneIsTel(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validateOrganization_HasURLIsIri(organization, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the HasEmailIsMailto constraint of '<em>Organization</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String ORGANIZATION__HAS_EMAIL_IS_MAILTO__EEXPRESSION = "self.hasEmail->forAll(v | v.matches('mailto:[^\\\\s@]+@[^\\\\s@]+'))";

	/**
	 * Validates the HasEmailIsMailto constraint of '<em>Organization</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateOrganization_HasEmailIsMailto(Organization organization, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(VcardPackage.Literals.ORGANIZATION,
				 organization,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasEmailIsMailto",
				 ORGANIZATION__HAS_EMAIL_IS_MAILTO__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the HasTelephoneIsTel constraint of '<em>Organization</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String ORGANIZATION__HAS_TELEPHONE_IS_TEL__EEXPRESSION = "self.hasTelephone->forAll(v | v.matches('tel:\\\\S+'))";

	/**
	 * Validates the HasTelephoneIsTel constraint of '<em>Organization</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateOrganization_HasTelephoneIsTel(Organization organization, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(VcardPackage.Literals.ORGANIZATION,
				 organization,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasTelephoneIsTel",
				 ORGANIZATION__HAS_TELEPHONE_IS_TEL__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the HasURLIsIri constraint of '<em>Organization</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String ORGANIZATION__HAS_URL_IS_IRI__EEXPRESSION = "self.hasURL->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the HasURLIsIri constraint of '<em>Organization</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateOrganization_HasURLIsIri(Organization organization, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(VcardPackage.Literals.ORGANIZATION,
				 organization,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasURLIsIri",
				 ORGANIZATION__HAS_URL_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
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

} //VcardValidator
