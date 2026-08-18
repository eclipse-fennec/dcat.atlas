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
package spdx.util;

import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.EObjectValidator;

import rdf.util.RdfValidator;

import spdx.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see spdx.SpdxPackage
 * @generated
 */
public class SpdxValidator extends EObjectValidator {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final SpdxValidator INSTANCE = new SpdxValidator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "spdx";

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
	public SpdxValidator() {
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
	  return SpdxPackage.eINSTANCE;
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
			case SpdxPackage.CHECKSUM:
				return validateChecksum((Checksum)value, diagnostics, context);
			default:
				return true;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateChecksum(Checksum checksum, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(checksum, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(checksum, diagnostics, context);
		if (result || diagnostics != null) result &= validateChecksum_AlgorithmIsIri(checksum, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the AlgorithmIsIri constraint of '<em>Checksum</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String CHECKSUM__ALGORITHM_IS_IRI__EEXPRESSION = "self.algorithm = null or self.algorithm.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the AlgorithmIsIri constraint of '<em>Checksum</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateChecksum_AlgorithmIsIri(Checksum checksum, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(SpdxPackage.Literals.CHECKSUM,
				 checksum,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "AlgorithmIsIri",
				 CHECKSUM__ALGORITHM_IS_IRI__EEXPRESSION,
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

} //SpdxValidator
