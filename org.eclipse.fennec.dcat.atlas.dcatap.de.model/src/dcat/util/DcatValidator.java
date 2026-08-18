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
package dcat.util;

import dcat.*;

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
 * @see dcat.DcatPackage
 * @generated
 */
public class DcatValidator extends EObjectValidator {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final DcatValidator INSTANCE = new DcatValidator();

	/**
	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
	 * @generated
	 */
	public static final String DIAGNOSTIC_SOURCE = "dcat";

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
	public DcatValidator() {
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
	  return DcatPackage.eINSTANCE;
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
			case DcatPackage.CATALOG:
				return validateCatalog((Catalog)value, diagnostics, context);
			case DcatPackage.DATASET:
				return validateDataset((Dataset)value, diagnostics, context);
			case DcatPackage.DISTRIBUTION:
				return validateDistribution((Distribution)value, diagnostics, context);
			case DcatPackage.RELATIONSHIP:
				return validateRelationship((Relationship)value, diagnostics, context);
			case DcatPackage.CATALOG_RECORD:
				return validateCatalogRecord((CatalogRecord)value, diagnostics, context);
			case DcatPackage.DATA_SERVICE:
				return validateDataService((DataService)value, diagnostics, context);
			case DcatPackage.DCAT_RESOURCE:
				return validateDcatResource((DcatResource)value, diagnostics, context);
			case DcatPackage.DATASET_SERIES:
				return validateDatasetSeries((DatasetSeries)value, diagnostics, context);
			default:
				return true;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCatalog(Catalog catalog, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(catalog, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasIdentity(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ThemeIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_TypeIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_AccessRightsIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasPolicyIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_QualifiedAttributionIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_RelationIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_IsReferencedByIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_LanguageIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ContributorIDIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ApplicableLegislationIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_PoliticalGeocodingLevelURIIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_HasDescription(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_AccrualPeriodicityIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_WasGeneratedByIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_HasVersionIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateCatalog_ThemeTaxonomyIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateCatalog_HasPartIsIri(catalog, diagnostics, context);
		if (result || diagnostics != null) result &= validateCatalog_HomepageIsIri(catalog, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the ThemeTaxonomyIsIri constraint of '<em>Catalog</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String CATALOG__THEME_TAXONOMY_IS_IRI__EEXPRESSION = "self.themeTaxonomy->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the ThemeTaxonomyIsIri constraint of '<em>Catalog</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCatalog_ThemeTaxonomyIsIri(Catalog catalog, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.CATALOG,
				 catalog,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "ThemeTaxonomyIsIri",
				 CATALOG__THEME_TAXONOMY_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the HasPartIsIri constraint of '<em>Catalog</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String CATALOG__HAS_PART_IS_IRI__EEXPRESSION = "self.hasPart->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the HasPartIsIri constraint of '<em>Catalog</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCatalog_HasPartIsIri(Catalog catalog, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.CATALOG,
				 catalog,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasPartIsIri",
				 CATALOG__HAS_PART_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the HomepageIsIri constraint of '<em>Catalog</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String CATALOG__HOMEPAGE_IS_IRI__EEXPRESSION = "self.homepage = null or self.homepage.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the HomepageIsIri constraint of '<em>Catalog</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCatalog_HomepageIsIri(Catalog catalog, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.CATALOG,
				 catalog,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HomepageIsIri",
				 CATALOG__HOMEPAGE_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataset(Dataset dataset, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(dataset, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasIdentity(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ThemeIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_TypeIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_AccessRightsIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasPolicyIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_QualifiedAttributionIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_RelationIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_IsReferencedByIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_LanguageIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ContributorIDIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ApplicableLegislationIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_PoliticalGeocodingLevelURIIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_HasDescription(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_AccrualPeriodicityIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_WasGeneratedByIsIri(dataset, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_HasVersionIsIri(dataset, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the HasDescription constraint of '<em>Dataset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DATASET__HAS_DESCRIPTION__EEXPRESSION = "self.description->notEmpty()";

	/**
	 * Validates the HasDescription constraint of '<em>Dataset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataset_HasDescription(Dataset dataset, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DATASET,
				 dataset,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasDescription",
				 DATASET__HAS_DESCRIPTION__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the AccrualPeriodicityIsIri constraint of '<em>Dataset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DATASET__ACCRUAL_PERIODICITY_IS_IRI__EEXPRESSION = "self.accrualPeriodicity = null or self.accrualPeriodicity.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the AccrualPeriodicityIsIri constraint of '<em>Dataset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataset_AccrualPeriodicityIsIri(Dataset dataset, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DATASET,
				 dataset,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "AccrualPeriodicityIsIri",
				 DATASET__ACCRUAL_PERIODICITY_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the WasGeneratedByIsIri constraint of '<em>Dataset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DATASET__WAS_GENERATED_BY_IS_IRI__EEXPRESSION = "self.wasGeneratedBy->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the WasGeneratedByIsIri constraint of '<em>Dataset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataset_WasGeneratedByIsIri(Dataset dataset, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DATASET,
				 dataset,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "WasGeneratedByIsIri",
				 DATASET__WAS_GENERATED_BY_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the HasVersionIsIri constraint of '<em>Dataset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DATASET__HAS_VERSION_IS_IRI__EEXPRESSION = "self.hasVersion->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the HasVersionIsIri constraint of '<em>Dataset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataset_HasVersionIsIri(Dataset dataset, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DATASET,
				 dataset,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasVersionIsIri",
				 DATASET__HAS_VERSION_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(distribution, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_HasIdentity(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_AccessURLIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_DownloadURLIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_FormatIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_MediaTypeIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_PackageFormatIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_AccessRightsIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_HasPolicyIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_AvailabilityIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_StatusIsIri(distribution, diagnostics, context);
		if (result || diagnostics != null) result &= validateDistribution_ApplicableLegislationIsIri(distribution, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the HasIdentity constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__HAS_IDENTITY__EEXPRESSION = "self.about <> null and self.about.size() > 0";

	/**
	 * Validates the HasIdentity constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_HasIdentity(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasIdentity",
				 DISTRIBUTION__HAS_IDENTITY__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the AccessURLIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__ACCESS_URL_IS_IRI__EEXPRESSION = "self.accessURL->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the AccessURLIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_AccessURLIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "AccessURLIsIri",
				 DISTRIBUTION__ACCESS_URL_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the DownloadURLIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__DOWNLOAD_URL_IS_IRI__EEXPRESSION = "self.downloadURL->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the DownloadURLIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_DownloadURLIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "DownloadURLIsIri",
				 DISTRIBUTION__DOWNLOAD_URL_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the FormatIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__FORMAT_IS_IRI__EEXPRESSION = "self.format = null or self.format.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the FormatIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_FormatIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "FormatIsIri",
				 DISTRIBUTION__FORMAT_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the MediaTypeIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__MEDIA_TYPE_IS_IRI__EEXPRESSION = "self.mediaType = null or self.mediaType.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the MediaTypeIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_MediaTypeIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "MediaTypeIsIri",
				 DISTRIBUTION__MEDIA_TYPE_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the PackageFormatIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__PACKAGE_FORMAT_IS_IRI__EEXPRESSION = "self.packageFormat = null or self.packageFormat.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the PackageFormatIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_PackageFormatIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "PackageFormatIsIri",
				 DISTRIBUTION__PACKAGE_FORMAT_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the AccessRightsIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__ACCESS_RIGHTS_IS_IRI__EEXPRESSION = "self.accessRights = null or self.accessRights.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the AccessRightsIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_AccessRightsIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "AccessRightsIsIri",
				 DISTRIBUTION__ACCESS_RIGHTS_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the HasPolicyIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__HAS_POLICY_IS_IRI__EEXPRESSION = "self.hasPolicy = null or self.hasPolicy.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the HasPolicyIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_HasPolicyIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasPolicyIsIri",
				 DISTRIBUTION__HAS_POLICY_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the AvailabilityIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__AVAILABILITY_IS_IRI__EEXPRESSION = "self.availability = null or self.availability.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the AvailabilityIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_AvailabilityIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "AvailabilityIsIri",
				 DISTRIBUTION__AVAILABILITY_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the StatusIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__STATUS_IS_IRI__EEXPRESSION = "self.status = null or self.status.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the StatusIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_StatusIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "StatusIsIri",
				 DISTRIBUTION__STATUS_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the ApplicableLegislationIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DISTRIBUTION__APPLICABLE_LEGISLATION_IS_IRI__EEXPRESSION = "self.applicableLegislation->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the ApplicableLegislationIsIri constraint of '<em>Distribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDistribution_ApplicableLegislationIsIri(Distribution distribution, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DISTRIBUTION,
				 distribution,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "ApplicableLegislationIsIri",
				 DISTRIBUTION__APPLICABLE_LEGISLATION_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateRelationship(Relationship relationship, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(relationship, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(relationship, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(relationship, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(relationship, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(relationship, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(relationship, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(relationship, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(relationship, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(relationship, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(relationship, diagnostics, context);
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCatalogRecord(CatalogRecord catalogRecord, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(catalogRecord, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(catalogRecord, diagnostics, context);
		if (result || diagnostics != null) result &= validateCatalogRecord_LanguageIsIri(catalogRecord, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the LanguageIsIri constraint of '<em>Catalog Record</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String CATALOG_RECORD__LANGUAGE_IS_IRI__EEXPRESSION = "self.language->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the LanguageIsIri constraint of '<em>Catalog Record</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateCatalogRecord_LanguageIsIri(CatalogRecord catalogRecord, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.CATALOG_RECORD,
				 catalogRecord,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "LanguageIsIri",
				 CATALOG_RECORD__LANGUAGE_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataService(DataService dataService, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(dataService, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasIdentity(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ThemeIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_TypeIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_AccessRightsIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasPolicyIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_QualifiedAttributionIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_RelationIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_IsReferencedByIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_LanguageIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ContributorIDIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ApplicableLegislationIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_PoliticalGeocodingLevelURIIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataService_EndpointURLIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataService_EndpointDescriptionIsIri(dataService, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataService_FormatIsIri(dataService, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the EndpointURLIsIri constraint of '<em>Data Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DATA_SERVICE__ENDPOINT_URL_IS_IRI__EEXPRESSION = "self.endpointURL->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the EndpointURLIsIri constraint of '<em>Data Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataService_EndpointURLIsIri(DataService dataService, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DATA_SERVICE,
				 dataService,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "EndpointURLIsIri",
				 DATA_SERVICE__ENDPOINT_URL_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the EndpointDescriptionIsIri constraint of '<em>Data Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DATA_SERVICE__ENDPOINT_DESCRIPTION_IS_IRI__EEXPRESSION = "self.endpointDescription->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the EndpointDescriptionIsIri constraint of '<em>Data Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataService_EndpointDescriptionIsIri(DataService dataService, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DATA_SERVICE,
				 dataService,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "EndpointDescriptionIsIri",
				 DATA_SERVICE__ENDPOINT_DESCRIPTION_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the FormatIsIri constraint of '<em>Data Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DATA_SERVICE__FORMAT_IS_IRI__EEXPRESSION = "self.format->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the FormatIsIri constraint of '<em>Data Service</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDataService_FormatIsIri(DataService dataService, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DATA_SERVICE,
				 dataService,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "FormatIsIri",
				 DATA_SERVICE__FORMAT_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(dcatResource, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasIdentity(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ThemeIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_TypeIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_AccessRightsIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasPolicyIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_QualifiedAttributionIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_RelationIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_IsReferencedByIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_LanguageIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ContributorIDIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ApplicableLegislationIsIri(dcatResource, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_PoliticalGeocodingLevelURIIsIri(dcatResource, diagnostics, context);
		return result;
	}

	/**
	 * The cached validation expression for the HasIdentity constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__HAS_IDENTITY__EEXPRESSION = "self.about <> null and self.about.size() > 0";

	/**
	 * Validates the HasIdentity constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_HasIdentity(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasIdentity",
				 DCAT_RESOURCE__HAS_IDENTITY__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the ThemeIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__THEME_IS_IRI__EEXPRESSION = "self.theme->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the ThemeIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_ThemeIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "ThemeIsIri",
				 DCAT_RESOURCE__THEME_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the TypeIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__TYPE_IS_IRI__EEXPRESSION = "self.type->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the TypeIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_TypeIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "TypeIsIri",
				 DCAT_RESOURCE__TYPE_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the AccessRightsIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__ACCESS_RIGHTS_IS_IRI__EEXPRESSION = "self.accessRights = null or self.accessRights.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the AccessRightsIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_AccessRightsIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "AccessRightsIsIri",
				 DCAT_RESOURCE__ACCESS_RIGHTS_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the HasPolicyIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__HAS_POLICY_IS_IRI__EEXPRESSION = "self.hasPolicy = null or self.hasPolicy.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*')";

	/**
	 * Validates the HasPolicyIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_HasPolicyIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "HasPolicyIsIri",
				 DCAT_RESOURCE__HAS_POLICY_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the QualifiedAttributionIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__QUALIFIED_ATTRIBUTION_IS_IRI__EEXPRESSION = "self.qualifiedAttribution->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the QualifiedAttributionIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_QualifiedAttributionIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "QualifiedAttributionIsIri",
				 DCAT_RESOURCE__QUALIFIED_ATTRIBUTION_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the RelationIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__RELATION_IS_IRI__EEXPRESSION = "self.relation->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the RelationIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_RelationIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "RelationIsIri",
				 DCAT_RESOURCE__RELATION_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the IsReferencedByIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__IS_REFERENCED_BY_IS_IRI__EEXPRESSION = "self.isReferencedBy->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the IsReferencedByIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_IsReferencedByIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "IsReferencedByIsIri",
				 DCAT_RESOURCE__IS_REFERENCED_BY_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the LanguageIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__LANGUAGE_IS_IRI__EEXPRESSION = "self.language->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the LanguageIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_LanguageIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "LanguageIsIri",
				 DCAT_RESOURCE__LANGUAGE_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the ContributorIDIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__CONTRIBUTOR_ID_IS_IRI__EEXPRESSION = "self.contributorID->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the ContributorIDIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_ContributorIDIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "ContributorIDIsIri",
				 DCAT_RESOURCE__CONTRIBUTOR_ID_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the ApplicableLegislationIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__APPLICABLE_LEGISLATION_IS_IRI__EEXPRESSION = "self.applicableLegislation->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the ApplicableLegislationIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_ApplicableLegislationIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "ApplicableLegislationIsIri",
				 DCAT_RESOURCE__APPLICABLE_LEGISLATION_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * The cached validation expression for the PoliticalGeocodingLevelURIIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static final String DCAT_RESOURCE__POLITICAL_GEOCODING_LEVEL_URI_IS_IRI__EEXPRESSION = "self.politicalGeocodingLevelURI->forAll(v | v.matches('[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*'))";

	/**
	 * Validates the PoliticalGeocodingLevelURIIsIri constraint of '<em>Resource</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDcatResource_PoliticalGeocodingLevelURIIsIri(DcatResource dcatResource, DiagnosticChain diagnostics, Map<Object, Object> context) {
		return
			validate
				(DcatPackage.Literals.DCAT_RESOURCE,
				 dcatResource,
				 diagnostics,
				 context,
				 "http://www.eclipse.org/fennec/m2x/ocl/1.0",
				 "PoliticalGeocodingLevelURIIsIri",
				 DCAT_RESOURCE__POLITICAL_GEOCODING_LEVEL_URI_IS_IRI__EEXPRESSION,
				 Diagnostic.ERROR,
				 DIAGNOSTIC_SOURCE,
				 0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean validateDatasetSeries(DatasetSeries datasetSeries, DiagnosticChain diagnostics, Map<Object, Object> context) {
		if (!validate_NoCircularContainment(datasetSeries, diagnostics, context)) return false;
		boolean result = validate_EveryMultiplicityConforms(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryDataValueConforms(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryProxyResolves(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validate_UniqueID(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryKeyUnique(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= rdfValidator.validateIdentifiedResource_AboutIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasIdentity(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ThemeIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_TypeIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_AccessRightsIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_HasPolicyIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_QualifiedAttributionIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_RelationIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_IsReferencedByIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_LanguageIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ContributorIDIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_ApplicableLegislationIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDcatResource_PoliticalGeocodingLevelURIIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_HasDescription(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_AccrualPeriodicityIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_WasGeneratedByIsIri(datasetSeries, diagnostics, context);
		if (result || diagnostics != null) result &= validateDataset_HasVersionIsIri(datasetSeries, diagnostics, context);
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

} //DcatValidator
