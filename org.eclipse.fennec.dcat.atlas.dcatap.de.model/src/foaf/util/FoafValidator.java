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
package foaf.util;

import foaf.*;

import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.EObjectValidator;

import rdf.util.RdfValidator;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see foaf.FoafPackage
 * @generated
 */
public class FoafValidator extends EObjectValidator {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final FoafValidator INSTANCE = new FoafValidator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "foaf";

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
	public FoafValidator() {
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
	  return FoafPackage.eINSTANCE;
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
			case FoafPackage.AGENT:
				return validateAgent((Agent)value, diagnostics, context);
			case FoafPackage.DOCUMENT:
				return validateDocument((Document)value, diagnostics, context);
			case FoafPackage.ORGANIZATION:
				return validateOrganization((Organization)value, diagnostics, context);
			case FoafPackage.PERSON:
				return validatePerson((Person)value, diagnostics, context);
			default:
				return true;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateAgent(Agent agent, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(agent, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(agent, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validateAgent_TypeIsIri(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validateAgent_MboxIsMailto(agent, diagnostics, context);
		if (result || diagnostics != null) result &= validateAgent_PhoneIsTel(agent, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the TypeIsIri constraint of '<em>Agent</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String AGENT__TYPE_IS_IRI__EEXPRESSION = "self.type = null or self.type.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the TypeIsIri constraint of '<em>Agent</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateAgent_TypeIsIri(Agent agent, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(FoafPackage.Literals.AGENT,
				 agent,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "TypeIsIri",
				 AGENT__TYPE_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the MboxIsMailto constraint of '<em>Agent</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String AGENT__MBOX_IS_MAILTO__EEXPRESSION = "self.mbox = null or self.mbox.matches('mailto:[^\\\\s@]+@[^\\\\s@]+')";

	/**
	 * Validates the MboxIsMailto constraint of '<em>Agent</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateAgent_MboxIsMailto(Agent agent, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(FoafPackage.Literals.AGENT,
				 agent,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "MboxIsMailto",
				 AGENT__MBOX_IS_MAILTO__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the PhoneIsTel constraint of '<em>Agent</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String AGENT__PHONE_IS_TEL__EEXPRESSION = "self.phone = null or self.phone.matches('tel:\\\\S+')";

	/**
	 * Validates the PhoneIsTel constraint of '<em>Agent</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateAgent_PhoneIsTel(Agent agent, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(FoafPackage.Literals.AGENT,
				 agent,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "PhoneIsTel",
				 AGENT__PHONE_IS_TEL__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDocument(Document document, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(document, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(document, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(document, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(document, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(document, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(document, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(document, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(document, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(document, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(document, diagnostics, context);
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
		if (result || diagnostics != null) result &= validateAgent_TypeIsIri(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validateAgent_MboxIsMailto(organization, diagnostics, context);
		if (result || diagnostics != null) result &= validateAgent_PhoneIsTel(organization, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validatePerson(Person person, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(person, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(person, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(person, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(person, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(person, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(person, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(person, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(person, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(person, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(person, diagnostics, context);
		if (result || diagnostics != null) result &= validateAgent_TypeIsIri(person, diagnostics, context);
		if (result || diagnostics != null) result &= validateAgent_MboxIsMailto(person, diagnostics, context);
		if (result || diagnostics != null) result &= validateAgent_PhoneIsTel(person, diagnostics, context);
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

} //FoafValidator
