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
package dcat;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

import rdf.RdfPackage;

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
 * DCAT Application Profile for data portals in Europe — DCAT-AP.de Version 3.0
 * <!-- end-model-doc -->
 * @see dcat.DcatFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore validationDelegates='http://www.eclipse.org/fennec/m2x/ocl/1.0'"
 * @generated
 */
@ProviderType
@EPackage(uri = DcatPackage.eNS_URI, fingerprint = "fp1:0412bd1f74f77b773b1163720704461185874cd712ccb44b94982e9d02629789", genModel = "/model/dcatap.genmodel", genModelSourceLocations = {"model/dcatap.genmodel","org.eclipse.fennec.dcat.atlas.dcatap.de.model/model/dcatap.genmodel"}, ecore = "/model/dcatap.ecore", ecoreSourceLocations = "/model/dcatap.ecore")
public interface DcatPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "dcat";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.w3.org/ns/dcat#";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "dcat";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DcatPackage eINSTANCE = dcat.impl.DcatPackageImpl.init();

	/**
	 * The meta object id for the '{@link dcat.impl.DcatResourceImpl <em>Resource</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see dcat.impl.DcatResourceImpl
	 * @see dcat.impl.DcatPackageImpl#getDcatResource()
	 * @generated
	 */
	int DCAT_RESOURCE = 6;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__IDENTIFIER = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__TITLE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__DESCRIPTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Theme</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__THEME = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Keyword</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__KEYWORD = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__TYPE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Contact Point</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__CONTACT_POINT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Creator</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__CREATOR = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Publisher</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__PUBLISHER = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__ISSUED = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__MODIFIED = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Landing Page</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__LANDING_PAGE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Access Rights</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__ACCESS_RIGHTS = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Conforms To</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__CONFORMS_TO = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>License</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__LICENSE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Rights</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__RIGHTS = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Has Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__HAS_POLICY = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Qualified Attribution</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__QUALIFIED_ATTRIBUTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Qualified Relation</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__QUALIFIED_RELATION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 18;

	/**
	 * The feature id for the '<em><b>Relation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__RELATION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 19;

	/**
	 * The feature id for the '<em><b>Is Referenced By</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__IS_REFERENCED_BY = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 20;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__LANGUAGE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 21;

	/**
	 * The feature id for the '<em><b>Contributor ID</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__CONTRIBUTOR_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 22;

	/**
	 * The feature id for the '<em><b>Applicable Legislation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__APPLICABLE_LEGISLATION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 23;

	/**
	 * The feature id for the '<em><b>Originator</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__ORIGINATOR = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 24;

	/**
	 * The feature id for the '<em><b>Custodian</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__CUSTODIAN = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 25;

	/**
	 * The feature id for the '<em><b>Political Geocoding Level URI</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__POLITICAL_GEOCODING_LEVEL_URI = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 26;

	/**
	 * The feature id for the '<em><b>Adms Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__ADMS_IDENTIFIER = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 27;

	/**
	 * The feature id for the '<em><b>Provenance</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE__PROVENANCE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 28;

	/**
	 * The number of structural features of the '<em>Resource</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 29;

	/**
	 * The number of operations of the '<em>Resource</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DCAT_RESOURCE_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link dcat.impl.DatasetImpl <em>Dataset</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see dcat.impl.DatasetImpl
	 * @see dcat.impl.DcatPackageImpl#getDataset()
	 * @generated
	 */
	int DATASET = 1;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__ABOUT = DCAT_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__IDENTIFIER = DCAT_RESOURCE__IDENTIFIER;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__TITLE = DCAT_RESOURCE__TITLE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__DESCRIPTION = DCAT_RESOURCE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Theme</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__THEME = DCAT_RESOURCE__THEME;

	/**
	 * The feature id for the '<em><b>Keyword</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__KEYWORD = DCAT_RESOURCE__KEYWORD;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__TYPE = DCAT_RESOURCE__TYPE;

	/**
	 * The feature id for the '<em><b>Contact Point</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__CONTACT_POINT = DCAT_RESOURCE__CONTACT_POINT;

	/**
	 * The feature id for the '<em><b>Creator</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__CREATOR = DCAT_RESOURCE__CREATOR;

	/**
	 * The feature id for the '<em><b>Publisher</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__PUBLISHER = DCAT_RESOURCE__PUBLISHER;

	/**
	 * The feature id for the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__ISSUED = DCAT_RESOURCE__ISSUED;

	/**
	 * The feature id for the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__MODIFIED = DCAT_RESOURCE__MODIFIED;

	/**
	 * The feature id for the '<em><b>Landing Page</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__LANDING_PAGE = DCAT_RESOURCE__LANDING_PAGE;

	/**
	 * The feature id for the '<em><b>Access Rights</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__ACCESS_RIGHTS = DCAT_RESOURCE__ACCESS_RIGHTS;

	/**
	 * The feature id for the '<em><b>Conforms To</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__CONFORMS_TO = DCAT_RESOURCE__CONFORMS_TO;

	/**
	 * The feature id for the '<em><b>License</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__LICENSE = DCAT_RESOURCE__LICENSE;

	/**
	 * The feature id for the '<em><b>Rights</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__RIGHTS = DCAT_RESOURCE__RIGHTS;

	/**
	 * The feature id for the '<em><b>Has Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__HAS_POLICY = DCAT_RESOURCE__HAS_POLICY;

	/**
	 * The feature id for the '<em><b>Qualified Attribution</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__QUALIFIED_ATTRIBUTION = DCAT_RESOURCE__QUALIFIED_ATTRIBUTION;

	/**
	 * The feature id for the '<em><b>Qualified Relation</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__QUALIFIED_RELATION = DCAT_RESOURCE__QUALIFIED_RELATION;

	/**
	 * The feature id for the '<em><b>Relation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__RELATION = DCAT_RESOURCE__RELATION;

	/**
	 * The feature id for the '<em><b>Is Referenced By</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__IS_REFERENCED_BY = DCAT_RESOURCE__IS_REFERENCED_BY;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__LANGUAGE = DCAT_RESOURCE__LANGUAGE;

	/**
	 * The feature id for the '<em><b>Contributor ID</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__CONTRIBUTOR_ID = DCAT_RESOURCE__CONTRIBUTOR_ID;

	/**
	 * The feature id for the '<em><b>Applicable Legislation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__APPLICABLE_LEGISLATION = DCAT_RESOURCE__APPLICABLE_LEGISLATION;

	/**
	 * The feature id for the '<em><b>Originator</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__ORIGINATOR = DCAT_RESOURCE__ORIGINATOR;

	/**
	 * The feature id for the '<em><b>Custodian</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__CUSTODIAN = DCAT_RESOURCE__CUSTODIAN;

	/**
	 * The feature id for the '<em><b>Political Geocoding Level URI</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__POLITICAL_GEOCODING_LEVEL_URI = DCAT_RESOURCE__POLITICAL_GEOCODING_LEVEL_URI;

	/**
	 * The feature id for the '<em><b>Adms Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__ADMS_IDENTIFIER = DCAT_RESOURCE__ADMS_IDENTIFIER;

	/**
	 * The feature id for the '<em><b>Provenance</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__PROVENANCE = DCAT_RESOURCE__PROVENANCE;

	/**
	 * The feature id for the '<em><b>Distribution</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__DISTRIBUTION = DCAT_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Spatial Resolution In Meters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__SPATIAL_RESOLUTION_IN_METERS = DCAT_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Temporal Resolution</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__TEMPORAL_RESOLUTION = DCAT_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Accrual Periodicity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__ACCRUAL_PERIODICITY = DCAT_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Spatial</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__SPATIAL = DCAT_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Temporal</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__TEMPORAL = DCAT_RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Was Generated By</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__WAS_GENERATED_BY = DCAT_RESOURCE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>In Series</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__IN_SERIES = DCAT_RESOURCE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__VERSION = DCAT_RESOURCE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Has Version</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET__HAS_VERSION = DCAT_RESOURCE_FEATURE_COUNT + 9;

	/**
	 * The number of structural features of the '<em>Dataset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_FEATURE_COUNT = DCAT_RESOURCE_FEATURE_COUNT + 10;

	/**
	 * The number of operations of the '<em>Dataset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_OPERATION_COUNT = DCAT_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link dcat.impl.CatalogImpl <em>Catalog</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see dcat.impl.CatalogImpl
	 * @see dcat.impl.DcatPackageImpl#getCatalog()
	 * @generated
	 */
	int CATALOG = 0;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__ABOUT = DATASET__ABOUT;

	/**
	 * The feature id for the '<em><b>Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__IDENTIFIER = DATASET__IDENTIFIER;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__TITLE = DATASET__TITLE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__DESCRIPTION = DATASET__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Theme</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__THEME = DATASET__THEME;

	/**
	 * The feature id for the '<em><b>Keyword</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__KEYWORD = DATASET__KEYWORD;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__TYPE = DATASET__TYPE;

	/**
	 * The feature id for the '<em><b>Contact Point</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__CONTACT_POINT = DATASET__CONTACT_POINT;

	/**
	 * The feature id for the '<em><b>Creator</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__CREATOR = DATASET__CREATOR;

	/**
	 * The feature id for the '<em><b>Publisher</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__PUBLISHER = DATASET__PUBLISHER;

	/**
	 * The feature id for the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__ISSUED = DATASET__ISSUED;

	/**
	 * The feature id for the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__MODIFIED = DATASET__MODIFIED;

	/**
	 * The feature id for the '<em><b>Landing Page</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__LANDING_PAGE = DATASET__LANDING_PAGE;

	/**
	 * The feature id for the '<em><b>Access Rights</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__ACCESS_RIGHTS = DATASET__ACCESS_RIGHTS;

	/**
	 * The feature id for the '<em><b>Conforms To</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__CONFORMS_TO = DATASET__CONFORMS_TO;

	/**
	 * The feature id for the '<em><b>License</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__LICENSE = DATASET__LICENSE;

	/**
	 * The feature id for the '<em><b>Rights</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__RIGHTS = DATASET__RIGHTS;

	/**
	 * The feature id for the '<em><b>Has Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__HAS_POLICY = DATASET__HAS_POLICY;

	/**
	 * The feature id for the '<em><b>Qualified Attribution</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__QUALIFIED_ATTRIBUTION = DATASET__QUALIFIED_ATTRIBUTION;

	/**
	 * The feature id for the '<em><b>Qualified Relation</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__QUALIFIED_RELATION = DATASET__QUALIFIED_RELATION;

	/**
	 * The feature id for the '<em><b>Relation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__RELATION = DATASET__RELATION;

	/**
	 * The feature id for the '<em><b>Is Referenced By</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__IS_REFERENCED_BY = DATASET__IS_REFERENCED_BY;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__LANGUAGE = DATASET__LANGUAGE;

	/**
	 * The feature id for the '<em><b>Contributor ID</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__CONTRIBUTOR_ID = DATASET__CONTRIBUTOR_ID;

	/**
	 * The feature id for the '<em><b>Applicable Legislation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__APPLICABLE_LEGISLATION = DATASET__APPLICABLE_LEGISLATION;

	/**
	 * The feature id for the '<em><b>Originator</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__ORIGINATOR = DATASET__ORIGINATOR;

	/**
	 * The feature id for the '<em><b>Custodian</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__CUSTODIAN = DATASET__CUSTODIAN;

	/**
	 * The feature id for the '<em><b>Political Geocoding Level URI</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__POLITICAL_GEOCODING_LEVEL_URI = DATASET__POLITICAL_GEOCODING_LEVEL_URI;

	/**
	 * The feature id for the '<em><b>Adms Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__ADMS_IDENTIFIER = DATASET__ADMS_IDENTIFIER;

	/**
	 * The feature id for the '<em><b>Provenance</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__PROVENANCE = DATASET__PROVENANCE;

	/**
	 * The feature id for the '<em><b>Distribution</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__DISTRIBUTION = DATASET__DISTRIBUTION;

	/**
	 * The feature id for the '<em><b>Spatial Resolution In Meters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__SPATIAL_RESOLUTION_IN_METERS = DATASET__SPATIAL_RESOLUTION_IN_METERS;

	/**
	 * The feature id for the '<em><b>Temporal Resolution</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__TEMPORAL_RESOLUTION = DATASET__TEMPORAL_RESOLUTION;

	/**
	 * The feature id for the '<em><b>Accrual Periodicity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__ACCRUAL_PERIODICITY = DATASET__ACCRUAL_PERIODICITY;

	/**
	 * The feature id for the '<em><b>Spatial</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__SPATIAL = DATASET__SPATIAL;

	/**
	 * The feature id for the '<em><b>Temporal</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__TEMPORAL = DATASET__TEMPORAL;

	/**
	 * The feature id for the '<em><b>Was Generated By</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__WAS_GENERATED_BY = DATASET__WAS_GENERATED_BY;

	/**
	 * The feature id for the '<em><b>In Series</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__IN_SERIES = DATASET__IN_SERIES;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__VERSION = DATASET__VERSION;

	/**
	 * The feature id for the '<em><b>Has Version</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__HAS_VERSION = DATASET__HAS_VERSION;

	/**
	 * The feature id for the '<em><b>Catalog</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__CATALOG = DATASET_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Record</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__RECORD = DATASET_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Dataset</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__DATASET = DATASET_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Service</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__SERVICE = DATASET_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Theme Taxonomy</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__THEME_TAXONOMY = DATASET_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Has Part</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__HAS_PART = DATASET_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Homepage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG__HOMEPAGE = DATASET_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>Catalog</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_FEATURE_COUNT = DATASET_FEATURE_COUNT + 7;

	/**
	 * The number of operations of the '<em>Catalog</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_OPERATION_COUNT = DATASET_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link dcat.impl.DistributionImpl <em>Distribution</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see dcat.impl.DistributionImpl
	 * @see dcat.impl.DcatPackageImpl#getDistribution()
	 * @generated
	 */
	int DISTRIBUTION = 2;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__TITLE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__DESCRIPTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Access Service</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__ACCESS_SERVICE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__FORMAT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Media Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__MEDIA_TYPE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Package Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__PACKAGE_FORMAT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Byte Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__BYTE_SIZE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Compress Format</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__COMPRESS_FORMAT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Spatial Resolution In Meters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__SPATIAL_RESOLUTION_IN_METERS = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Temporal Resolution</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__TEMPORAL_RESOLUTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Access Rights</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__ACCESS_RIGHTS = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>License</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__LICENSE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Conforms To</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__CONFORMS_TO = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Rights</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__RIGHTS = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Has Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__HAS_POLICY = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__ISSUED = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__MODIFIED = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>License Attribution By Text</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__LICENSE_ATTRIBUTION_BY_TEXT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 18;

	/**
	 * The feature id for the '<em><b>Availability</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__AVAILABILITY = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 19;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__STATUS = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 20;

	/**
	 * The feature id for the '<em><b>Checksum</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__CHECKSUM = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 21;

	/**
	 * The feature id for the '<em><b>Applicable Legislation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__APPLICABLE_LEGISLATION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 22;

	/**
	 * The feature id for the '<em><b>Download URL</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__DOWNLOAD_URL = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 23;

	/**
	 * The feature id for the '<em><b>Access URL</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION__ACCESS_URL = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 24;

	/**
	 * The number of structural features of the '<em>Distribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 25;

	/**
	 * The number of operations of the '<em>Distribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DISTRIBUTION_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link dcat.impl.RelationshipImpl <em>Relationship</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see dcat.impl.RelationshipImpl
	 * @see dcat.impl.DcatPackageImpl#getRelationship()
	 * @generated
	 */
	int RELATIONSHIP = 3;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONSHIP__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Had Role</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONSHIP__HAD_ROLE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONSHIP__DESCRIPTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONSHIP__NODE_ID = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Relationship</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONSHIP_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Relationship</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RELATIONSHIP_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link dcat.impl.CatalogRecordImpl <em>Catalog Record</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see dcat.impl.CatalogRecordImpl
	 * @see dcat.impl.DcatPackageImpl#getCatalogRecord()
	 * @generated
	 */
	int CATALOG_RECORD = 4;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD__ABOUT = RdfPackage.IDENTIFIED_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD__TITLE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD__DESCRIPTION = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD__ISSUED = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD__MODIFIED = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Conforms To</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD__CONFORMS_TO = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD__LANGUAGE = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Primary Topic</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD__PRIMARY_TOPIC = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>Catalog Record</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD_FEATURE_COUNT = RdfPackage.IDENTIFIED_RESOURCE_FEATURE_COUNT + 7;

	/**
	 * The number of operations of the '<em>Catalog Record</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CATALOG_RECORD_OPERATION_COUNT = RdfPackage.IDENTIFIED_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link dcat.impl.DataServiceImpl <em>Data Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see dcat.impl.DataServiceImpl
	 * @see dcat.impl.DcatPackageImpl#getDataService()
	 * @generated
	 */
	int DATA_SERVICE = 5;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__ABOUT = DCAT_RESOURCE__ABOUT;

	/**
	 * The feature id for the '<em><b>Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__IDENTIFIER = DCAT_RESOURCE__IDENTIFIER;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__TITLE = DCAT_RESOURCE__TITLE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__DESCRIPTION = DCAT_RESOURCE__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Theme</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__THEME = DCAT_RESOURCE__THEME;

	/**
	 * The feature id for the '<em><b>Keyword</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__KEYWORD = DCAT_RESOURCE__KEYWORD;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__TYPE = DCAT_RESOURCE__TYPE;

	/**
	 * The feature id for the '<em><b>Contact Point</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__CONTACT_POINT = DCAT_RESOURCE__CONTACT_POINT;

	/**
	 * The feature id for the '<em><b>Creator</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__CREATOR = DCAT_RESOURCE__CREATOR;

	/**
	 * The feature id for the '<em><b>Publisher</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__PUBLISHER = DCAT_RESOURCE__PUBLISHER;

	/**
	 * The feature id for the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__ISSUED = DCAT_RESOURCE__ISSUED;

	/**
	 * The feature id for the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__MODIFIED = DCAT_RESOURCE__MODIFIED;

	/**
	 * The feature id for the '<em><b>Landing Page</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__LANDING_PAGE = DCAT_RESOURCE__LANDING_PAGE;

	/**
	 * The feature id for the '<em><b>Access Rights</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__ACCESS_RIGHTS = DCAT_RESOURCE__ACCESS_RIGHTS;

	/**
	 * The feature id for the '<em><b>Conforms To</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__CONFORMS_TO = DCAT_RESOURCE__CONFORMS_TO;

	/**
	 * The feature id for the '<em><b>License</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__LICENSE = DCAT_RESOURCE__LICENSE;

	/**
	 * The feature id for the '<em><b>Rights</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__RIGHTS = DCAT_RESOURCE__RIGHTS;

	/**
	 * The feature id for the '<em><b>Has Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__HAS_POLICY = DCAT_RESOURCE__HAS_POLICY;

	/**
	 * The feature id for the '<em><b>Qualified Attribution</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__QUALIFIED_ATTRIBUTION = DCAT_RESOURCE__QUALIFIED_ATTRIBUTION;

	/**
	 * The feature id for the '<em><b>Qualified Relation</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__QUALIFIED_RELATION = DCAT_RESOURCE__QUALIFIED_RELATION;

	/**
	 * The feature id for the '<em><b>Relation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__RELATION = DCAT_RESOURCE__RELATION;

	/**
	 * The feature id for the '<em><b>Is Referenced By</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__IS_REFERENCED_BY = DCAT_RESOURCE__IS_REFERENCED_BY;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__LANGUAGE = DCAT_RESOURCE__LANGUAGE;

	/**
	 * The feature id for the '<em><b>Contributor ID</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__CONTRIBUTOR_ID = DCAT_RESOURCE__CONTRIBUTOR_ID;

	/**
	 * The feature id for the '<em><b>Applicable Legislation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__APPLICABLE_LEGISLATION = DCAT_RESOURCE__APPLICABLE_LEGISLATION;

	/**
	 * The feature id for the '<em><b>Originator</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__ORIGINATOR = DCAT_RESOURCE__ORIGINATOR;

	/**
	 * The feature id for the '<em><b>Custodian</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__CUSTODIAN = DCAT_RESOURCE__CUSTODIAN;

	/**
	 * The feature id for the '<em><b>Political Geocoding Level URI</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__POLITICAL_GEOCODING_LEVEL_URI = DCAT_RESOURCE__POLITICAL_GEOCODING_LEVEL_URI;

	/**
	 * The feature id for the '<em><b>Adms Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__ADMS_IDENTIFIER = DCAT_RESOURCE__ADMS_IDENTIFIER;

	/**
	 * The feature id for the '<em><b>Provenance</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__PROVENANCE = DCAT_RESOURCE__PROVENANCE;

	/**
	 * The feature id for the '<em><b>Endpoint Description</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__ENDPOINT_DESCRIPTION = DCAT_RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Endpoint URL</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__ENDPOINT_URL = DCAT_RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Serves Dataset</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__SERVES_DATASET = DCAT_RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Format</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE__FORMAT = DCAT_RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Data Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE_FEATURE_COUNT = DCAT_RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Data Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SERVICE_OPERATION_COUNT = DCAT_RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link dcat.impl.DatasetSeriesImpl <em>Dataset Series</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see dcat.impl.DatasetSeriesImpl
	 * @see dcat.impl.DcatPackageImpl#getDatasetSeries()
	 * @generated
	 */
	int DATASET_SERIES = 7;

	/**
	 * The feature id for the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__ABOUT = DATASET__ABOUT;

	/**
	 * The feature id for the '<em><b>Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__IDENTIFIER = DATASET__IDENTIFIER;

	/**
	 * The feature id for the '<em><b>Title</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__TITLE = DATASET__TITLE;

	/**
	 * The feature id for the '<em><b>Description</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__DESCRIPTION = DATASET__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Theme</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__THEME = DATASET__THEME;

	/**
	 * The feature id for the '<em><b>Keyword</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__KEYWORD = DATASET__KEYWORD;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__TYPE = DATASET__TYPE;

	/**
	 * The feature id for the '<em><b>Contact Point</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__CONTACT_POINT = DATASET__CONTACT_POINT;

	/**
	 * The feature id for the '<em><b>Creator</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__CREATOR = DATASET__CREATOR;

	/**
	 * The feature id for the '<em><b>Publisher</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__PUBLISHER = DATASET__PUBLISHER;

	/**
	 * The feature id for the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__ISSUED = DATASET__ISSUED;

	/**
	 * The feature id for the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__MODIFIED = DATASET__MODIFIED;

	/**
	 * The feature id for the '<em><b>Landing Page</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__LANDING_PAGE = DATASET__LANDING_PAGE;

	/**
	 * The feature id for the '<em><b>Access Rights</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__ACCESS_RIGHTS = DATASET__ACCESS_RIGHTS;

	/**
	 * The feature id for the '<em><b>Conforms To</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__CONFORMS_TO = DATASET__CONFORMS_TO;

	/**
	 * The feature id for the '<em><b>License</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__LICENSE = DATASET__LICENSE;

	/**
	 * The feature id for the '<em><b>Rights</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__RIGHTS = DATASET__RIGHTS;

	/**
	 * The feature id for the '<em><b>Has Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__HAS_POLICY = DATASET__HAS_POLICY;

	/**
	 * The feature id for the '<em><b>Qualified Attribution</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__QUALIFIED_ATTRIBUTION = DATASET__QUALIFIED_ATTRIBUTION;

	/**
	 * The feature id for the '<em><b>Qualified Relation</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__QUALIFIED_RELATION = DATASET__QUALIFIED_RELATION;

	/**
	 * The feature id for the '<em><b>Relation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__RELATION = DATASET__RELATION;

	/**
	 * The feature id for the '<em><b>Is Referenced By</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__IS_REFERENCED_BY = DATASET__IS_REFERENCED_BY;

	/**
	 * The feature id for the '<em><b>Language</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__LANGUAGE = DATASET__LANGUAGE;

	/**
	 * The feature id for the '<em><b>Contributor ID</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__CONTRIBUTOR_ID = DATASET__CONTRIBUTOR_ID;

	/**
	 * The feature id for the '<em><b>Applicable Legislation</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__APPLICABLE_LEGISLATION = DATASET__APPLICABLE_LEGISLATION;

	/**
	 * The feature id for the '<em><b>Originator</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__ORIGINATOR = DATASET__ORIGINATOR;

	/**
	 * The feature id for the '<em><b>Custodian</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__CUSTODIAN = DATASET__CUSTODIAN;

	/**
	 * The feature id for the '<em><b>Political Geocoding Level URI</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__POLITICAL_GEOCODING_LEVEL_URI = DATASET__POLITICAL_GEOCODING_LEVEL_URI;

	/**
	 * The feature id for the '<em><b>Adms Identifier</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__ADMS_IDENTIFIER = DATASET__ADMS_IDENTIFIER;

	/**
	 * The feature id for the '<em><b>Provenance</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__PROVENANCE = DATASET__PROVENANCE;

	/**
	 * The feature id for the '<em><b>Distribution</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__DISTRIBUTION = DATASET__DISTRIBUTION;

	/**
	 * The feature id for the '<em><b>Spatial Resolution In Meters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__SPATIAL_RESOLUTION_IN_METERS = DATASET__SPATIAL_RESOLUTION_IN_METERS;

	/**
	 * The feature id for the '<em><b>Temporal Resolution</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__TEMPORAL_RESOLUTION = DATASET__TEMPORAL_RESOLUTION;

	/**
	 * The feature id for the '<em><b>Accrual Periodicity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__ACCRUAL_PERIODICITY = DATASET__ACCRUAL_PERIODICITY;

	/**
	 * The feature id for the '<em><b>Spatial</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__SPATIAL = DATASET__SPATIAL;

	/**
	 * The feature id for the '<em><b>Temporal</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__TEMPORAL = DATASET__TEMPORAL;

	/**
	 * The feature id for the '<em><b>Was Generated By</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__WAS_GENERATED_BY = DATASET__WAS_GENERATED_BY;

	/**
	 * The feature id for the '<em><b>In Series</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__IN_SERIES = DATASET__IN_SERIES;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__VERSION = DATASET__VERSION;

	/**
	 * The feature id for the '<em><b>Has Version</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES__HAS_VERSION = DATASET__HAS_VERSION;

	/**
	 * The number of structural features of the '<em>Dataset Series</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES_FEATURE_COUNT = DATASET_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Dataset Series</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATASET_SERIES_OPERATION_COUNT = DATASET_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link dcat.Catalog <em>Catalog</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Catalog</em>'.
	 * @see dcat.Catalog
	 * @generated
	 */
	EClass getCatalog();

	/**
	 * Returns the meta object for the reference list '{@link dcat.Catalog#getCatalog <em>Catalog</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Catalog</em>'.
	 * @see dcat.Catalog#getCatalog()
	 * @see #getCatalog()
	 * @generated
	 */
	EReference getCatalog_Catalog();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.Catalog#getRecord <em>Record</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Record</em>'.
	 * @see dcat.Catalog#getRecord()
	 * @see #getCatalog()
	 * @generated
	 */
	EReference getCatalog_Record();

	/**
	 * Returns the meta object for the reference list '{@link dcat.Catalog#getDataset <em>Dataset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Dataset</em>'.
	 * @see dcat.Catalog#getDataset()
	 * @see #getCatalog()
	 * @generated
	 */
	EReference getCatalog_Dataset();

	/**
	 * Returns the meta object for the reference list '{@link dcat.Catalog#getService <em>Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Service</em>'.
	 * @see dcat.Catalog#getService()
	 * @see #getCatalog()
	 * @generated
	 */
	EReference getCatalog_Service();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.Catalog#getThemeTaxonomy <em>Theme Taxonomy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Theme Taxonomy</em>'.
	 * @see dcat.Catalog#getThemeTaxonomy()
	 * @see #getCatalog()
	 * @generated
	 */
	EAttribute getCatalog_ThemeTaxonomy();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.Catalog#getHasPart <em>Has Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Has Part</em>'.
	 * @see dcat.Catalog#getHasPart()
	 * @see #getCatalog()
	 * @generated
	 */
	EAttribute getCatalog_HasPart();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Catalog#getHomepage <em>Homepage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Homepage</em>'.
	 * @see dcat.Catalog#getHomepage()
	 * @see #getCatalog()
	 * @generated
	 */
	EAttribute getCatalog_Homepage();

	/**
	 * Returns the meta object for class '{@link dcat.Dataset <em>Dataset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Dataset</em>'.
	 * @see dcat.Dataset
	 * @generated
	 */
	EClass getDataset();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.Dataset#getDistribution <em>Distribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Distribution</em>'.
	 * @see dcat.Dataset#getDistribution()
	 * @see #getDataset()
	 * @generated
	 */
	EReference getDataset_Distribution();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Dataset#getSpatialResolutionInMeters <em>Spatial Resolution In Meters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Spatial Resolution In Meters</em>'.
	 * @see dcat.Dataset#getSpatialResolutionInMeters()
	 * @see #getDataset()
	 * @generated
	 */
	EAttribute getDataset_SpatialResolutionInMeters();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Dataset#getTemporalResolution <em>Temporal Resolution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Temporal Resolution</em>'.
	 * @see dcat.Dataset#getTemporalResolution()
	 * @see #getDataset()
	 * @generated
	 */
	EAttribute getDataset_TemporalResolution();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Dataset#getAccrualPeriodicity <em>Accrual Periodicity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Accrual Periodicity</em>'.
	 * @see dcat.Dataset#getAccrualPeriodicity()
	 * @see #getDataset()
	 * @generated
	 */
	EAttribute getDataset_AccrualPeriodicity();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.Dataset#getSpatial <em>Spatial</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Spatial</em>'.
	 * @see dcat.Dataset#getSpatial()
	 * @see #getDataset()
	 * @generated
	 */
	EReference getDataset_Spatial();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.Dataset#getTemporal <em>Temporal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Temporal</em>'.
	 * @see dcat.Dataset#getTemporal()
	 * @see #getDataset()
	 * @generated
	 */
	EReference getDataset_Temporal();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.Dataset#getWasGeneratedBy <em>Was Generated By</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Was Generated By</em>'.
	 * @see dcat.Dataset#getWasGeneratedBy()
	 * @see #getDataset()
	 * @generated
	 */
	EAttribute getDataset_WasGeneratedBy();

	/**
	 * Returns the meta object for the reference list '{@link dcat.Dataset#getInSeries <em>In Series</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>In Series</em>'.
	 * @see dcat.Dataset#getInSeries()
	 * @see #getDataset()
	 * @generated
	 */
	EReference getDataset_InSeries();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Dataset#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see dcat.Dataset#getVersion()
	 * @see #getDataset()
	 * @generated
	 */
	EAttribute getDataset_Version();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.Dataset#getHasVersion <em>Has Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Has Version</em>'.
	 * @see dcat.Dataset#getHasVersion()
	 * @see #getDataset()
	 * @generated
	 */
	EAttribute getDataset_HasVersion();

	/**
	 * Returns the meta object for class '{@link dcat.Distribution <em>Distribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Distribution</em>'.
	 * @see dcat.Distribution
	 * @generated
	 */
	EClass getDistribution();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Title</em>'.
	 * @see dcat.Distribution#getTitle()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_Title();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Description</em>'.
	 * @see dcat.Distribution#getDescription()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_Description();

	/**
	 * Returns the meta object for the reference list '{@link dcat.Distribution#getAccessService <em>Access Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Access Service</em>'.
	 * @see dcat.Distribution#getAccessService()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_AccessService();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getFormat <em>Format</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Format</em>'.
	 * @see dcat.Distribution#getFormat()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_Format();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getMediaType <em>Media Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Media Type</em>'.
	 * @see dcat.Distribution#getMediaType()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_MediaType();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getPackageFormat <em>Package Format</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Package Format</em>'.
	 * @see dcat.Distribution#getPackageFormat()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_PackageFormat();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getByteSize <em>Byte Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Byte Size</em>'.
	 * @see dcat.Distribution#getByteSize()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_ByteSize();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getCompressFormat <em>Compress Format</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Compress Format</em>'.
	 * @see dcat.Distribution#getCompressFormat()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_CompressFormat();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getSpatialResolutionInMeters <em>Spatial Resolution In Meters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Spatial Resolution In Meters</em>'.
	 * @see dcat.Distribution#getSpatialResolutionInMeters()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_SpatialResolutionInMeters();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getTemporalResolution <em>Temporal Resolution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Temporal Resolution</em>'.
	 * @see dcat.Distribution#getTemporalResolution()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_TemporalResolution();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getAccessRights <em>Access Rights</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Access Rights</em>'.
	 * @see dcat.Distribution#getAccessRights()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_AccessRights();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getLicense <em>License</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>License</em>'.
	 * @see dcat.Distribution#getLicense()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_License();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.Distribution#getConformsTo <em>Conforms To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Conforms To</em>'.
	 * @see dcat.Distribution#getConformsTo()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_ConformsTo();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getRights <em>Rights</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Rights</em>'.
	 * @see dcat.Distribution#getRights()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_Rights();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getHasPolicy <em>Has Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Has Policy</em>'.
	 * @see dcat.Distribution#getHasPolicy()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_HasPolicy();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getIssued <em>Issued</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Issued</em>'.
	 * @see dcat.Distribution#getIssued()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_Issued();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getModified <em>Modified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Modified</em>'.
	 * @see dcat.Distribution#getModified()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_Modified();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see dcat.Distribution#getNodeID()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_NodeID();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getLicenseAttributionByText <em>License Attribution By Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>License Attribution By Text</em>'.
	 * @see dcat.Distribution#getLicenseAttributionByText()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_LicenseAttributionByText();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getAvailability <em>Availability</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Availability</em>'.
	 * @see dcat.Distribution#getAvailability()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_Availability();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Distribution#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see dcat.Distribution#getStatus()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_Status();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Distribution#getChecksum <em>Checksum</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Checksum</em>'.
	 * @see dcat.Distribution#getChecksum()
	 * @see #getDistribution()
	 * @generated
	 */
	EReference getDistribution_Checksum();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.Distribution#getApplicableLegislation <em>Applicable Legislation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Applicable Legislation</em>'.
	 * @see dcat.Distribution#getApplicableLegislation()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_ApplicableLegislation();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.Distribution#getDownloadURL <em>Download URL</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Download URL</em>'.
	 * @see dcat.Distribution#getDownloadURL()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_DownloadURL();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.Distribution#getAccessURL <em>Access URL</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Access URL</em>'.
	 * @see dcat.Distribution#getAccessURL()
	 * @see #getDistribution()
	 * @generated
	 */
	EAttribute getDistribution_AccessURL();

	/**
	 * Returns the meta object for class '{@link dcat.Relationship <em>Relationship</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Relationship</em>'.
	 * @see dcat.Relationship
	 * @generated
	 */
	EClass getRelationship();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Relationship#getHadRole <em>Had Role</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Had Role</em>'.
	 * @see dcat.Relationship#getHadRole()
	 * @see #getRelationship()
	 * @generated
	 */
	EReference getRelationship_HadRole();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.Relationship#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Description</em>'.
	 * @see dcat.Relationship#getDescription()
	 * @see #getRelationship()
	 * @generated
	 */
	EReference getRelationship_Description();

	/**
	 * Returns the meta object for the attribute '{@link dcat.Relationship#getNodeID <em>Node ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ID</em>'.
	 * @see dcat.Relationship#getNodeID()
	 * @see #getRelationship()
	 * @generated
	 */
	EAttribute getRelationship_NodeID();

	/**
	 * Returns the meta object for class '{@link dcat.CatalogRecord <em>Catalog Record</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Catalog Record</em>'.
	 * @see dcat.CatalogRecord
	 * @generated
	 */
	EClass getCatalogRecord();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.CatalogRecord#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Title</em>'.
	 * @see dcat.CatalogRecord#getTitle()
	 * @see #getCatalogRecord()
	 * @generated
	 */
	EReference getCatalogRecord_Title();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.CatalogRecord#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Description</em>'.
	 * @see dcat.CatalogRecord#getDescription()
	 * @see #getCatalogRecord()
	 * @generated
	 */
	EReference getCatalogRecord_Description();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.CatalogRecord#getIssued <em>Issued</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Issued</em>'.
	 * @see dcat.CatalogRecord#getIssued()
	 * @see #getCatalogRecord()
	 * @generated
	 */
	EReference getCatalogRecord_Issued();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.CatalogRecord#getModified <em>Modified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Modified</em>'.
	 * @see dcat.CatalogRecord#getModified()
	 * @see #getCatalogRecord()
	 * @generated
	 */
	EReference getCatalogRecord_Modified();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.CatalogRecord#getConformsTo <em>Conforms To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Conforms To</em>'.
	 * @see dcat.CatalogRecord#getConformsTo()
	 * @see #getCatalogRecord()
	 * @generated
	 */
	EReference getCatalogRecord_ConformsTo();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.CatalogRecord#getLanguage <em>Language</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Language</em>'.
	 * @see dcat.CatalogRecord#getLanguage()
	 * @see #getCatalogRecord()
	 * @generated
	 */
	EAttribute getCatalogRecord_Language();

	/**
	 * Returns the meta object for the reference '{@link dcat.CatalogRecord#getPrimaryTopic <em>Primary Topic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Primary Topic</em>'.
	 * @see dcat.CatalogRecord#getPrimaryTopic()
	 * @see #getCatalogRecord()
	 * @generated
	 */
	EReference getCatalogRecord_PrimaryTopic();

	/**
	 * Returns the meta object for class '{@link dcat.DataService <em>Data Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Data Service</em>'.
	 * @see dcat.DataService
	 * @generated
	 */
	EClass getDataService();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DataService#getEndpointDescription <em>Endpoint Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Endpoint Description</em>'.
	 * @see dcat.DataService#getEndpointDescription()
	 * @see #getDataService()
	 * @generated
	 */
	EAttribute getDataService_EndpointDescription();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DataService#getEndpointURL <em>Endpoint URL</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Endpoint URL</em>'.
	 * @see dcat.DataService#getEndpointURL()
	 * @see #getDataService()
	 * @generated
	 */
	EAttribute getDataService_EndpointURL();

	/**
	 * Returns the meta object for the reference list '{@link dcat.DataService#getServesDataset <em>Serves Dataset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Serves Dataset</em>'.
	 * @see dcat.DataService#getServesDataset()
	 * @see #getDataService()
	 * @generated
	 */
	EReference getDataService_ServesDataset();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DataService#getFormat <em>Format</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Format</em>'.
	 * @see dcat.DataService#getFormat()
	 * @see #getDataService()
	 * @generated
	 */
	EAttribute getDataService_Format();

	/**
	 * Returns the meta object for class '{@link dcat.DcatResource <em>Resource</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Resource</em>'.
	 * @see dcat.DcatResource
	 * @generated
	 */
	EClass getDcatResource();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getIdentifier <em>Identifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Identifier</em>'.
	 * @see dcat.DcatResource#getIdentifier()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Identifier();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getTitle <em>Title</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Title</em>'.
	 * @see dcat.DcatResource#getTitle()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Title();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Description</em>'.
	 * @see dcat.DcatResource#getDescription()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Description();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getTheme <em>Theme</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Theme</em>'.
	 * @see dcat.DcatResource#getTheme()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_Theme();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getKeyword <em>Keyword</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Keyword</em>'.
	 * @see dcat.DcatResource#getKeyword()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Keyword();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Type</em>'.
	 * @see dcat.DcatResource#getType()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_Type();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getContactPoint <em>Contact Point</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Contact Point</em>'.
	 * @see dcat.DcatResource#getContactPoint()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_ContactPoint();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.DcatResource#getCreator <em>Creator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Creator</em>'.
	 * @see dcat.DcatResource#getCreator()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Creator();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.DcatResource#getPublisher <em>Publisher</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Publisher</em>'.
	 * @see dcat.DcatResource#getPublisher()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Publisher();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.DcatResource#getIssued <em>Issued</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Issued</em>'.
	 * @see dcat.DcatResource#getIssued()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Issued();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.DcatResource#getModified <em>Modified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Modified</em>'.
	 * @see dcat.DcatResource#getModified()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Modified();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getLandingPage <em>Landing Page</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Landing Page</em>'.
	 * @see dcat.DcatResource#getLandingPage()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_LandingPage();

	/**
	 * Returns the meta object for the attribute '{@link dcat.DcatResource#getAccessRights <em>Access Rights</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Access Rights</em>'.
	 * @see dcat.DcatResource#getAccessRights()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_AccessRights();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getConformsTo <em>Conforms To</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Conforms To</em>'.
	 * @see dcat.DcatResource#getConformsTo()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_ConformsTo();

	/**
	 * Returns the meta object for the containment reference '{@link dcat.DcatResource#getLicense <em>License</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>License</em>'.
	 * @see dcat.DcatResource#getLicense()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_License();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getRights <em>Rights</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Rights</em>'.
	 * @see dcat.DcatResource#getRights()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Rights();

	/**
	 * Returns the meta object for the attribute '{@link dcat.DcatResource#getHasPolicy <em>Has Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Has Policy</em>'.
	 * @see dcat.DcatResource#getHasPolicy()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_HasPolicy();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getQualifiedAttribution <em>Qualified Attribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Qualified Attribution</em>'.
	 * @see dcat.DcatResource#getQualifiedAttribution()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_QualifiedAttribution();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getQualifiedRelation <em>Qualified Relation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Qualified Relation</em>'.
	 * @see dcat.DcatResource#getQualifiedRelation()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_QualifiedRelation();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getRelation <em>Relation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Relation</em>'.
	 * @see dcat.DcatResource#getRelation()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_Relation();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getIsReferencedBy <em>Is Referenced By</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Is Referenced By</em>'.
	 * @see dcat.DcatResource#getIsReferencedBy()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_IsReferencedBy();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getLanguage <em>Language</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Language</em>'.
	 * @see dcat.DcatResource#getLanguage()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_Language();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getContributorID <em>Contributor ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Contributor ID</em>'.
	 * @see dcat.DcatResource#getContributorID()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_ContributorID();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getApplicableLegislation <em>Applicable Legislation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Applicable Legislation</em>'.
	 * @see dcat.DcatResource#getApplicableLegislation()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_ApplicableLegislation();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getOriginator <em>Originator</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Originator</em>'.
	 * @see dcat.DcatResource#getOriginator()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Originator();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getCustodian <em>Custodian</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Custodian</em>'.
	 * @see dcat.DcatResource#getCustodian()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Custodian();

	/**
	 * Returns the meta object for the attribute list '{@link dcat.DcatResource#getPoliticalGeocodingLevelURI <em>Political Geocoding Level URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Political Geocoding Level URI</em>'.
	 * @see dcat.DcatResource#getPoliticalGeocodingLevelURI()
	 * @see #getDcatResource()
	 * @generated
	 */
	EAttribute getDcatResource_PoliticalGeocodingLevelURI();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getAdmsIdentifier <em>Adms Identifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Adms Identifier</em>'.
	 * @see dcat.DcatResource#getAdmsIdentifier()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_AdmsIdentifier();

	/**
	 * Returns the meta object for the containment reference list '{@link dcat.DcatResource#getProvenance <em>Provenance</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Provenance</em>'.
	 * @see dcat.DcatResource#getProvenance()
	 * @see #getDcatResource()
	 * @generated
	 */
	EReference getDcatResource_Provenance();

	/**
	 * Returns the meta object for class '{@link dcat.DatasetSeries <em>Dataset Series</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Dataset Series</em>'.
	 * @see dcat.DatasetSeries
	 * @generated
	 */
	EClass getDatasetSeries();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	DcatFactory getDcatFactory();

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
		 * The meta object literal for the '{@link dcat.impl.CatalogImpl <em>Catalog</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see dcat.impl.CatalogImpl
		 * @see dcat.impl.DcatPackageImpl#getCatalog()
		 * @generated
		 */
		EClass CATALOG = eINSTANCE.getCatalog();

		/**
		 * The meta object literal for the '<em><b>Catalog</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG__CATALOG = eINSTANCE.getCatalog_Catalog();

		/**
		 * The meta object literal for the '<em><b>Record</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG__RECORD = eINSTANCE.getCatalog_Record();

		/**
		 * The meta object literal for the '<em><b>Dataset</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG__DATASET = eINSTANCE.getCatalog_Dataset();

		/**
		 * The meta object literal for the '<em><b>Service</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG__SERVICE = eINSTANCE.getCatalog_Service();

		/**
		 * The meta object literal for the '<em><b>Theme Taxonomy</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CATALOG__THEME_TAXONOMY = eINSTANCE.getCatalog_ThemeTaxonomy();

		/**
		 * The meta object literal for the '<em><b>Has Part</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CATALOG__HAS_PART = eINSTANCE.getCatalog_HasPart();

		/**
		 * The meta object literal for the '<em><b>Homepage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CATALOG__HOMEPAGE = eINSTANCE.getCatalog_Homepage();

		/**
		 * The meta object literal for the '{@link dcat.impl.DatasetImpl <em>Dataset</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see dcat.impl.DatasetImpl
		 * @see dcat.impl.DcatPackageImpl#getDataset()
		 * @generated
		 */
		EClass DATASET = eINSTANCE.getDataset();

		/**
		 * The meta object literal for the '<em><b>Distribution</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DATASET__DISTRIBUTION = eINSTANCE.getDataset_Distribution();

		/**
		 * The meta object literal for the '<em><b>Spatial Resolution In Meters</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATASET__SPATIAL_RESOLUTION_IN_METERS = eINSTANCE.getDataset_SpatialResolutionInMeters();

		/**
		 * The meta object literal for the '<em><b>Temporal Resolution</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATASET__TEMPORAL_RESOLUTION = eINSTANCE.getDataset_TemporalResolution();

		/**
		 * The meta object literal for the '<em><b>Accrual Periodicity</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATASET__ACCRUAL_PERIODICITY = eINSTANCE.getDataset_AccrualPeriodicity();

		/**
		 * The meta object literal for the '<em><b>Spatial</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DATASET__SPATIAL = eINSTANCE.getDataset_Spatial();

		/**
		 * The meta object literal for the '<em><b>Temporal</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DATASET__TEMPORAL = eINSTANCE.getDataset_Temporal();

		/**
		 * The meta object literal for the '<em><b>Was Generated By</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATASET__WAS_GENERATED_BY = eINSTANCE.getDataset_WasGeneratedBy();

		/**
		 * The meta object literal for the '<em><b>In Series</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DATASET__IN_SERIES = eINSTANCE.getDataset_InSeries();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATASET__VERSION = eINSTANCE.getDataset_Version();

		/**
		 * The meta object literal for the '<em><b>Has Version</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATASET__HAS_VERSION = eINSTANCE.getDataset_HasVersion();

		/**
		 * The meta object literal for the '{@link dcat.impl.DistributionImpl <em>Distribution</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see dcat.impl.DistributionImpl
		 * @see dcat.impl.DcatPackageImpl#getDistribution()
		 * @generated
		 */
		EClass DISTRIBUTION = eINSTANCE.getDistribution();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__TITLE = eINSTANCE.getDistribution_Title();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__DESCRIPTION = eINSTANCE.getDistribution_Description();

		/**
		 * The meta object literal for the '<em><b>Access Service</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__ACCESS_SERVICE = eINSTANCE.getDistribution_AccessService();

		/**
		 * The meta object literal for the '<em><b>Format</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__FORMAT = eINSTANCE.getDistribution_Format();

		/**
		 * The meta object literal for the '<em><b>Media Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__MEDIA_TYPE = eINSTANCE.getDistribution_MediaType();

		/**
		 * The meta object literal for the '<em><b>Package Format</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__PACKAGE_FORMAT = eINSTANCE.getDistribution_PackageFormat();

		/**
		 * The meta object literal for the '<em><b>Byte Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__BYTE_SIZE = eINSTANCE.getDistribution_ByteSize();

		/**
		 * The meta object literal for the '<em><b>Compress Format</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__COMPRESS_FORMAT = eINSTANCE.getDistribution_CompressFormat();

		/**
		 * The meta object literal for the '<em><b>Spatial Resolution In Meters</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__SPATIAL_RESOLUTION_IN_METERS = eINSTANCE.getDistribution_SpatialResolutionInMeters();

		/**
		 * The meta object literal for the '<em><b>Temporal Resolution</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__TEMPORAL_RESOLUTION = eINSTANCE.getDistribution_TemporalResolution();

		/**
		 * The meta object literal for the '<em><b>Access Rights</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__ACCESS_RIGHTS = eINSTANCE.getDistribution_AccessRights();

		/**
		 * The meta object literal for the '<em><b>License</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__LICENSE = eINSTANCE.getDistribution_License();

		/**
		 * The meta object literal for the '<em><b>Conforms To</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__CONFORMS_TO = eINSTANCE.getDistribution_ConformsTo();

		/**
		 * The meta object literal for the '<em><b>Rights</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__RIGHTS = eINSTANCE.getDistribution_Rights();

		/**
		 * The meta object literal for the '<em><b>Has Policy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__HAS_POLICY = eINSTANCE.getDistribution_HasPolicy();

		/**
		 * The meta object literal for the '<em><b>Issued</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__ISSUED = eINSTANCE.getDistribution_Issued();

		/**
		 * The meta object literal for the '<em><b>Modified</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__MODIFIED = eINSTANCE.getDistribution_Modified();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__NODE_ID = eINSTANCE.getDistribution_NodeID();

		/**
		 * The meta object literal for the '<em><b>License Attribution By Text</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__LICENSE_ATTRIBUTION_BY_TEXT = eINSTANCE.getDistribution_LicenseAttributionByText();

		/**
		 * The meta object literal for the '<em><b>Availability</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__AVAILABILITY = eINSTANCE.getDistribution_Availability();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__STATUS = eINSTANCE.getDistribution_Status();

		/**
		 * The meta object literal for the '<em><b>Checksum</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DISTRIBUTION__CHECKSUM = eINSTANCE.getDistribution_Checksum();

		/**
		 * The meta object literal for the '<em><b>Applicable Legislation</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__APPLICABLE_LEGISLATION = eINSTANCE.getDistribution_ApplicableLegislation();

		/**
		 * The meta object literal for the '<em><b>Download URL</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__DOWNLOAD_URL = eINSTANCE.getDistribution_DownloadURL();

		/**
		 * The meta object literal for the '<em><b>Access URL</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DISTRIBUTION__ACCESS_URL = eINSTANCE.getDistribution_AccessURL();

		/**
		 * The meta object literal for the '{@link dcat.impl.RelationshipImpl <em>Relationship</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see dcat.impl.RelationshipImpl
		 * @see dcat.impl.DcatPackageImpl#getRelationship()
		 * @generated
		 */
		EClass RELATIONSHIP = eINSTANCE.getRelationship();

		/**
		 * The meta object literal for the '<em><b>Had Role</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RELATIONSHIP__HAD_ROLE = eINSTANCE.getRelationship_HadRole();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RELATIONSHIP__DESCRIPTION = eINSTANCE.getRelationship_Description();

		/**
		 * The meta object literal for the '<em><b>Node ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RELATIONSHIP__NODE_ID = eINSTANCE.getRelationship_NodeID();

		/**
		 * The meta object literal for the '{@link dcat.impl.CatalogRecordImpl <em>Catalog Record</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see dcat.impl.CatalogRecordImpl
		 * @see dcat.impl.DcatPackageImpl#getCatalogRecord()
		 * @generated
		 */
		EClass CATALOG_RECORD = eINSTANCE.getCatalogRecord();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG_RECORD__TITLE = eINSTANCE.getCatalogRecord_Title();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG_RECORD__DESCRIPTION = eINSTANCE.getCatalogRecord_Description();

		/**
		 * The meta object literal for the '<em><b>Issued</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG_RECORD__ISSUED = eINSTANCE.getCatalogRecord_Issued();

		/**
		 * The meta object literal for the '<em><b>Modified</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG_RECORD__MODIFIED = eINSTANCE.getCatalogRecord_Modified();

		/**
		 * The meta object literal for the '<em><b>Conforms To</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG_RECORD__CONFORMS_TO = eINSTANCE.getCatalogRecord_ConformsTo();

		/**
		 * The meta object literal for the '<em><b>Language</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CATALOG_RECORD__LANGUAGE = eINSTANCE.getCatalogRecord_Language();

		/**
		 * The meta object literal for the '<em><b>Primary Topic</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CATALOG_RECORD__PRIMARY_TOPIC = eINSTANCE.getCatalogRecord_PrimaryTopic();

		/**
		 * The meta object literal for the '{@link dcat.impl.DataServiceImpl <em>Data Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see dcat.impl.DataServiceImpl
		 * @see dcat.impl.DcatPackageImpl#getDataService()
		 * @generated
		 */
		EClass DATA_SERVICE = eINSTANCE.getDataService();

		/**
		 * The meta object literal for the '<em><b>Endpoint Description</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SERVICE__ENDPOINT_DESCRIPTION = eINSTANCE.getDataService_EndpointDescription();

		/**
		 * The meta object literal for the '<em><b>Endpoint URL</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SERVICE__ENDPOINT_URL = eINSTANCE.getDataService_EndpointURL();

		/**
		 * The meta object literal for the '<em><b>Serves Dataset</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DATA_SERVICE__SERVES_DATASET = eINSTANCE.getDataService_ServesDataset();

		/**
		 * The meta object literal for the '<em><b>Format</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SERVICE__FORMAT = eINSTANCE.getDataService_Format();

		/**
		 * The meta object literal for the '{@link dcat.impl.DcatResourceImpl <em>Resource</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see dcat.impl.DcatResourceImpl
		 * @see dcat.impl.DcatPackageImpl#getDcatResource()
		 * @generated
		 */
		EClass DCAT_RESOURCE = eINSTANCE.getDcatResource();

		/**
		 * The meta object literal for the '<em><b>Identifier</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__IDENTIFIER = eINSTANCE.getDcatResource_Identifier();

		/**
		 * The meta object literal for the '<em><b>Title</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__TITLE = eINSTANCE.getDcatResource_Title();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__DESCRIPTION = eINSTANCE.getDcatResource_Description();

		/**
		 * The meta object literal for the '<em><b>Theme</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__THEME = eINSTANCE.getDcatResource_Theme();

		/**
		 * The meta object literal for the '<em><b>Keyword</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__KEYWORD = eINSTANCE.getDcatResource_Keyword();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__TYPE = eINSTANCE.getDcatResource_Type();

		/**
		 * The meta object literal for the '<em><b>Contact Point</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__CONTACT_POINT = eINSTANCE.getDcatResource_ContactPoint();

		/**
		 * The meta object literal for the '<em><b>Creator</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__CREATOR = eINSTANCE.getDcatResource_Creator();

		/**
		 * The meta object literal for the '<em><b>Publisher</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__PUBLISHER = eINSTANCE.getDcatResource_Publisher();

		/**
		 * The meta object literal for the '<em><b>Issued</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__ISSUED = eINSTANCE.getDcatResource_Issued();

		/**
		 * The meta object literal for the '<em><b>Modified</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__MODIFIED = eINSTANCE.getDcatResource_Modified();

		/**
		 * The meta object literal for the '<em><b>Landing Page</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__LANDING_PAGE = eINSTANCE.getDcatResource_LandingPage();

		/**
		 * The meta object literal for the '<em><b>Access Rights</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__ACCESS_RIGHTS = eINSTANCE.getDcatResource_AccessRights();

		/**
		 * The meta object literal for the '<em><b>Conforms To</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__CONFORMS_TO = eINSTANCE.getDcatResource_ConformsTo();

		/**
		 * The meta object literal for the '<em><b>License</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__LICENSE = eINSTANCE.getDcatResource_License();

		/**
		 * The meta object literal for the '<em><b>Rights</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__RIGHTS = eINSTANCE.getDcatResource_Rights();

		/**
		 * The meta object literal for the '<em><b>Has Policy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__HAS_POLICY = eINSTANCE.getDcatResource_HasPolicy();

		/**
		 * The meta object literal for the '<em><b>Qualified Attribution</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__QUALIFIED_ATTRIBUTION = eINSTANCE.getDcatResource_QualifiedAttribution();

		/**
		 * The meta object literal for the '<em><b>Qualified Relation</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__QUALIFIED_RELATION = eINSTANCE.getDcatResource_QualifiedRelation();

		/**
		 * The meta object literal for the '<em><b>Relation</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__RELATION = eINSTANCE.getDcatResource_Relation();

		/**
		 * The meta object literal for the '<em><b>Is Referenced By</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__IS_REFERENCED_BY = eINSTANCE.getDcatResource_IsReferencedBy();

		/**
		 * The meta object literal for the '<em><b>Language</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__LANGUAGE = eINSTANCE.getDcatResource_Language();

		/**
		 * The meta object literal for the '<em><b>Contributor ID</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__CONTRIBUTOR_ID = eINSTANCE.getDcatResource_ContributorID();

		/**
		 * The meta object literal for the '<em><b>Applicable Legislation</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__APPLICABLE_LEGISLATION = eINSTANCE.getDcatResource_ApplicableLegislation();

		/**
		 * The meta object literal for the '<em><b>Originator</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__ORIGINATOR = eINSTANCE.getDcatResource_Originator();

		/**
		 * The meta object literal for the '<em><b>Custodian</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__CUSTODIAN = eINSTANCE.getDcatResource_Custodian();

		/**
		 * The meta object literal for the '<em><b>Political Geocoding Level URI</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DCAT_RESOURCE__POLITICAL_GEOCODING_LEVEL_URI = eINSTANCE.getDcatResource_PoliticalGeocodingLevelURI();

		/**
		 * The meta object literal for the '<em><b>Adms Identifier</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__ADMS_IDENTIFIER = eINSTANCE.getDcatResource_AdmsIdentifier();

		/**
		 * The meta object literal for the '<em><b>Provenance</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DCAT_RESOURCE__PROVENANCE = eINSTANCE.getDcatResource_Provenance();

		/**
		 * The meta object literal for the '{@link dcat.impl.DatasetSeriesImpl <em>Dataset Series</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see dcat.impl.DatasetSeriesImpl
		 * @see dcat.impl.DcatPackageImpl#getDatasetSeries()
		 * @generated
		 */
		EClass DATASET_SERIES = eINSTANCE.getDatasetSeries();

	}

} //DcatPackage
