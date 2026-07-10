/*
 */
package org.eclipse.fennec.shacl.model.shacl;

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.shacl.model.shacl.SHACLPackage
 * @generated
 */
@ProviderType
public interface SHACLFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	SHACLFactory eINSTANCE = org.eclipse.fennec.shacl.model.shacl.impl.SHACLFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Validation Report</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Validation Report</em>'.
	 * @generated
	 */
	ValidationReport createValidationReport();

	/**
	 * Returns a new object of class '<em>Validation Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Validation Result</em>'.
	 * @generated
	 */
	ValidationResult createValidationResult();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	SHACLPackage getSHACLPackage();

} //SHACLFactory
