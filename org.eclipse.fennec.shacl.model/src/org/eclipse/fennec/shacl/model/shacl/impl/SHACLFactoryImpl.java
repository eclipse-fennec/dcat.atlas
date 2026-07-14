/**
 */
package org.eclipse.fennec.shacl.model.shacl.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.shacl.model.shacl.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SHACLFactoryImpl extends EFactoryImpl implements SHACLFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static SHACLFactory init() {
		try {
			SHACLFactory theSHACLFactory = (SHACLFactory)EPackage.Registry.INSTANCE.getEFactory(SHACLPackage.eNS_URI);
			if (theSHACLFactory != null) {
				return theSHACLFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new SHACLFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SHACLFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case SHACLPackage.VALIDATION_REPORT: return createValidationReport();
			case SHACLPackage.VALIDATION_RESULT: return createValidationResult();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ValidationReport createValidationReport() {
		ValidationReportImpl validationReport = new ValidationReportImpl();
		return validationReport;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ValidationResult createValidationResult() {
		ValidationResultImpl validationResult = new ValidationResultImpl();
		return validationResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SHACLPackage getSHACLPackage() {
		return (SHACLPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static SHACLPackage getPackage() {
		return SHACLPackage.eINSTANCE;
	}

} //SHACLFactoryImpl
