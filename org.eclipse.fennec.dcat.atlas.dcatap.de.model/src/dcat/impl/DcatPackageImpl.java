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
package dcat.impl;

import adms.AdmsPackage;

import adms.impl.AdmsPackageImpl;

import dcat.Catalog;
import dcat.CatalogRecord;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.DcatFactory;
import dcat.DcatPackage;
import dcat.DcatResource;
import dcat.Distribution;
import dcat.Relationship;

import dcat.util.DcatValidator;

import foaf.FoafPackage;

import foaf.impl.FoafPackageImpl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EValidator;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import rdf.RdfPackage;

import rdf.impl.RdfPackageImpl;

import spdx.SpdxPackage;

import spdx.impl.SpdxPackageImpl;

import terms.TermsPackage;

import terms.impl.TermsPackageImpl;

import vcard.VcardPackage;

import vcard.impl.VcardPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DcatPackageImpl extends EPackageImpl implements DcatPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass catalogEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass datasetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass distributionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass relationshipEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass catalogRecordEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dataServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dcatResourceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass datasetSeriesEClass = null;

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
	 * @see dcat.DcatPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private DcatPackageImpl() {
		super(eNS_URI, DcatFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link DcatPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static DcatPackage init() {
		if (isInited) return (DcatPackage)EPackage.Registry.INSTANCE.getEPackage(DcatPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredDcatPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		DcatPackageImpl theDcatPackage = registeredDcatPackage instanceof DcatPackageImpl ? (DcatPackageImpl)registeredDcatPackage : new DcatPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		XMLTypePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(FoafPackage.eNS_URI);
		FoafPackageImpl theFoafPackage = (FoafPackageImpl)(registeredPackage instanceof FoafPackageImpl ? registeredPackage : FoafPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(RdfPackage.eNS_URI);
		RdfPackageImpl theRdfPackage = (RdfPackageImpl)(registeredPackage instanceof RdfPackageImpl ? registeredPackage : RdfPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(SpdxPackage.eNS_URI);
		SpdxPackageImpl theSpdxPackage = (SpdxPackageImpl)(registeredPackage instanceof SpdxPackageImpl ? registeredPackage : SpdxPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(TermsPackage.eNS_URI);
		TermsPackageImpl theTermsPackage = (TermsPackageImpl)(registeredPackage instanceof TermsPackageImpl ? registeredPackage : TermsPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(VcardPackage.eNS_URI);
		VcardPackageImpl theVcardPackage = (VcardPackageImpl)(registeredPackage instanceof VcardPackageImpl ? registeredPackage : VcardPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(AdmsPackage.eNS_URI);
		AdmsPackageImpl theAdmsPackage = (AdmsPackageImpl)(registeredPackage instanceof AdmsPackageImpl ? registeredPackage : AdmsPackage.eINSTANCE);

		// Create package meta-data objects
		theDcatPackage.createPackageContents();
		theFoafPackage.createPackageContents();
		theRdfPackage.createPackageContents();
		theSpdxPackage.createPackageContents();
		theTermsPackage.createPackageContents();
		theVcardPackage.createPackageContents();
		theAdmsPackage.createPackageContents();

		// Initialize created meta-data
		theDcatPackage.initializePackageContents();
		theFoafPackage.initializePackageContents();
		theRdfPackage.initializePackageContents();
		theSpdxPackage.initializePackageContents();
		theTermsPackage.initializePackageContents();
		theVcardPackage.initializePackageContents();
		theAdmsPackage.initializePackageContents();

		// Register package validator
		EValidator.Registry.INSTANCE.put
			(theDcatPackage,
			 new EValidator.Descriptor() {
				 public EValidator getEValidator() {
					 return DcatValidator.INSTANCE;
				 }
			 });

		// Mark meta-data to indicate it can't be changed
		theDcatPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(DcatPackage.eNS_URI, theDcatPackage);
		return theDcatPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCatalog() {
		return catalogEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalog_Catalog() {
		return (EReference)catalogEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalog_Record() {
		return (EReference)catalogEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalog_Dataset() {
		return (EReference)catalogEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalog_Service() {
		return (EReference)catalogEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCatalog_ThemeTaxonomy() {
		return (EAttribute)catalogEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCatalog_HasPart() {
		return (EAttribute)catalogEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCatalog_Homepage() {
		return (EAttribute)catalogEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDataset() {
		return datasetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataset_Distribution() {
		return (EReference)datasetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataset_SpatialResolutionInMeters() {
		return (EAttribute)datasetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataset_TemporalResolution() {
		return (EAttribute)datasetEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataset_AccrualPeriodicity() {
		return (EAttribute)datasetEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataset_Spatial() {
		return (EReference)datasetEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataset_Temporal() {
		return (EReference)datasetEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataset_WasGeneratedBy() {
		return (EAttribute)datasetEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataset_InSeries() {
		return (EReference)datasetEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataset_Version() {
		return (EAttribute)datasetEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataset_HasVersion() {
		return (EAttribute)datasetEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDistribution() {
		return distributionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_Title() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_Description() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_AccessService() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_Format() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_MediaType() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_PackageFormat() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_ByteSize() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_CompressFormat() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_SpatialResolutionInMeters() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_TemporalResolution() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_AccessRights() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_License() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_ConformsTo() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_Rights() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_HasPolicy() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_Issued() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_Modified() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_NodeID() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_LicenseAttributionByText() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_Availability() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_Status() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDistribution_Checksum() {
		return (EReference)distributionEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_ApplicableLegislation() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(22);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_DownloadURL() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(23);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDistribution_AccessURL() {
		return (EAttribute)distributionEClass.getEStructuralFeatures().get(24);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRelationship() {
		return relationshipEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRelationship_HadRole() {
		return (EReference)relationshipEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRelationship_Description() {
		return (EReference)relationshipEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRelationship_NodeID() {
		return (EAttribute)relationshipEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCatalogRecord() {
		return catalogRecordEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalogRecord_Title() {
		return (EReference)catalogRecordEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalogRecord_Description() {
		return (EReference)catalogRecordEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalogRecord_Issued() {
		return (EReference)catalogRecordEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalogRecord_Modified() {
		return (EReference)catalogRecordEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalogRecord_ConformsTo() {
		return (EReference)catalogRecordEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCatalogRecord_Language() {
		return (EAttribute)catalogRecordEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCatalogRecord_PrimaryTopic() {
		return (EReference)catalogRecordEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDataService() {
		return dataServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataService_EndpointDescription() {
		return (EAttribute)dataServiceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataService_EndpointURL() {
		return (EAttribute)dataServiceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDataService_ServesDataset() {
		return (EReference)dataServiceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDataService_Format() {
		return (EAttribute)dataServiceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDcatResource() {
		return dcatResourceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Identifier() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Title() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Description() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_Theme() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Keyword() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_Type() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_ContactPoint() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Creator() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Publisher() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Issued() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Modified() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_LandingPage() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_AccessRights() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_ConformsTo() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_License() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Rights() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_HasPolicy() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_QualifiedAttribution() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_QualifiedRelation() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_Relation() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_IsReferencedBy() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_Language() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_ContributorID() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(22);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_ApplicableLegislation() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(23);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Originator() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(24);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Custodian() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(25);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDcatResource_PoliticalGeocodingLevelURI() {
		return (EAttribute)dcatResourceEClass.getEStructuralFeatures().get(26);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_AdmsIdentifier() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(27);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDcatResource_Provenance() {
		return (EReference)dcatResourceEClass.getEStructuralFeatures().get(28);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDatasetSeries() {
		return datasetSeriesEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DcatFactory getDcatFactory() {
		return (DcatFactory)getEFactoryInstance();
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
		catalogEClass = createEClass(CATALOG);
		createEReference(catalogEClass, CATALOG__CATALOG);
		createEReference(catalogEClass, CATALOG__RECORD);
		createEReference(catalogEClass, CATALOG__DATASET);
		createEReference(catalogEClass, CATALOG__SERVICE);
		createEAttribute(catalogEClass, CATALOG__THEME_TAXONOMY);
		createEAttribute(catalogEClass, CATALOG__HAS_PART);
		createEAttribute(catalogEClass, CATALOG__HOMEPAGE);

		datasetEClass = createEClass(DATASET);
		createEReference(datasetEClass, DATASET__DISTRIBUTION);
		createEAttribute(datasetEClass, DATASET__SPATIAL_RESOLUTION_IN_METERS);
		createEAttribute(datasetEClass, DATASET__TEMPORAL_RESOLUTION);
		createEAttribute(datasetEClass, DATASET__ACCRUAL_PERIODICITY);
		createEReference(datasetEClass, DATASET__SPATIAL);
		createEReference(datasetEClass, DATASET__TEMPORAL);
		createEAttribute(datasetEClass, DATASET__WAS_GENERATED_BY);
		createEReference(datasetEClass, DATASET__IN_SERIES);
		createEAttribute(datasetEClass, DATASET__VERSION);
		createEAttribute(datasetEClass, DATASET__HAS_VERSION);

		distributionEClass = createEClass(DISTRIBUTION);
		createEReference(distributionEClass, DISTRIBUTION__TITLE);
		createEReference(distributionEClass, DISTRIBUTION__DESCRIPTION);
		createEReference(distributionEClass, DISTRIBUTION__ACCESS_SERVICE);
		createEAttribute(distributionEClass, DISTRIBUTION__FORMAT);
		createEAttribute(distributionEClass, DISTRIBUTION__MEDIA_TYPE);
		createEAttribute(distributionEClass, DISTRIBUTION__PACKAGE_FORMAT);
		createEAttribute(distributionEClass, DISTRIBUTION__BYTE_SIZE);
		createEReference(distributionEClass, DISTRIBUTION__COMPRESS_FORMAT);
		createEAttribute(distributionEClass, DISTRIBUTION__SPATIAL_RESOLUTION_IN_METERS);
		createEAttribute(distributionEClass, DISTRIBUTION__TEMPORAL_RESOLUTION);
		createEAttribute(distributionEClass, DISTRIBUTION__ACCESS_RIGHTS);
		createEReference(distributionEClass, DISTRIBUTION__LICENSE);
		createEReference(distributionEClass, DISTRIBUTION__CONFORMS_TO);
		createEReference(distributionEClass, DISTRIBUTION__RIGHTS);
		createEAttribute(distributionEClass, DISTRIBUTION__HAS_POLICY);
		createEReference(distributionEClass, DISTRIBUTION__ISSUED);
		createEReference(distributionEClass, DISTRIBUTION__MODIFIED);
		createEAttribute(distributionEClass, DISTRIBUTION__NODE_ID);
		createEReference(distributionEClass, DISTRIBUTION__LICENSE_ATTRIBUTION_BY_TEXT);
		createEAttribute(distributionEClass, DISTRIBUTION__AVAILABILITY);
		createEAttribute(distributionEClass, DISTRIBUTION__STATUS);
		createEReference(distributionEClass, DISTRIBUTION__CHECKSUM);
		createEAttribute(distributionEClass, DISTRIBUTION__APPLICABLE_LEGISLATION);
		createEAttribute(distributionEClass, DISTRIBUTION__DOWNLOAD_URL);
		createEAttribute(distributionEClass, DISTRIBUTION__ACCESS_URL);

		relationshipEClass = createEClass(RELATIONSHIP);
		createEReference(relationshipEClass, RELATIONSHIP__HAD_ROLE);
		createEReference(relationshipEClass, RELATIONSHIP__DESCRIPTION);
		createEAttribute(relationshipEClass, RELATIONSHIP__NODE_ID);

		catalogRecordEClass = createEClass(CATALOG_RECORD);
		createEReference(catalogRecordEClass, CATALOG_RECORD__TITLE);
		createEReference(catalogRecordEClass, CATALOG_RECORD__DESCRIPTION);
		createEReference(catalogRecordEClass, CATALOG_RECORD__ISSUED);
		createEReference(catalogRecordEClass, CATALOG_RECORD__MODIFIED);
		createEReference(catalogRecordEClass, CATALOG_RECORD__CONFORMS_TO);
		createEAttribute(catalogRecordEClass, CATALOG_RECORD__LANGUAGE);
		createEReference(catalogRecordEClass, CATALOG_RECORD__PRIMARY_TOPIC);

		dataServiceEClass = createEClass(DATA_SERVICE);
		createEAttribute(dataServiceEClass, DATA_SERVICE__ENDPOINT_DESCRIPTION);
		createEAttribute(dataServiceEClass, DATA_SERVICE__ENDPOINT_URL);
		createEReference(dataServiceEClass, DATA_SERVICE__SERVES_DATASET);
		createEAttribute(dataServiceEClass, DATA_SERVICE__FORMAT);

		dcatResourceEClass = createEClass(DCAT_RESOURCE);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__IDENTIFIER);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__TITLE);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__DESCRIPTION);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__THEME);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__KEYWORD);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__TYPE);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__CONTACT_POINT);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__CREATOR);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__PUBLISHER);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__ISSUED);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__MODIFIED);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__LANDING_PAGE);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__ACCESS_RIGHTS);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__CONFORMS_TO);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__LICENSE);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__RIGHTS);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__HAS_POLICY);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__QUALIFIED_ATTRIBUTION);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__QUALIFIED_RELATION);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__RELATION);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__IS_REFERENCED_BY);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__LANGUAGE);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__CONTRIBUTOR_ID);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__APPLICABLE_LEGISLATION);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__ORIGINATOR);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__CUSTODIAN);
		createEAttribute(dcatResourceEClass, DCAT_RESOURCE__POLITICAL_GEOCODING_LEVEL_URI);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__ADMS_IDENTIFIER);
		createEReference(dcatResourceEClass, DCAT_RESOURCE__PROVENANCE);

		datasetSeriesEClass = createEClass(DATASET_SERIES);
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
		TermsPackage theTermsPackage = (TermsPackage)EPackage.Registry.INSTANCE.getEPackage(TermsPackage.eNS_URI);
		RdfPackage theRdfPackage = (RdfPackage)EPackage.Registry.INSTANCE.getEPackage(RdfPackage.eNS_URI);
		SpdxPackage theSpdxPackage = (SpdxPackage)EPackage.Registry.INSTANCE.getEPackage(SpdxPackage.eNS_URI);
		VcardPackage theVcardPackage = (VcardPackage)EPackage.Registry.INSTANCE.getEPackage(VcardPackage.eNS_URI);
		FoafPackage theFoafPackage = (FoafPackage)EPackage.Registry.INSTANCE.getEPackage(FoafPackage.eNS_URI);
		AdmsPackage theAdmsPackage = (AdmsPackage)EPackage.Registry.INSTANCE.getEPackage(AdmsPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		catalogEClass.getESuperTypes().add(this.getDataset());
		datasetEClass.getESuperTypes().add(this.getDcatResource());
		distributionEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());
		relationshipEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());
		catalogRecordEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());
		dataServiceEClass.getESuperTypes().add(this.getDcatResource());
		dcatResourceEClass.getESuperTypes().add(theRdfPackage.getIdentifiedResource());
		datasetSeriesEClass.getESuperTypes().add(this.getDataset());

		// Initialize classes, features, and operations; add parameters
		initEClass(catalogEClass, Catalog.class, "Catalog", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCatalog_Catalog(), this.getCatalog(), null, "catalog", null, 0, -1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalog_Record(), this.getCatalogRecord(), null, "record", null, 0, -1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalog_Dataset(), this.getDataset(), null, "dataset", null, 0, -1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalog_Service(), this.getDataService(), null, "service", null, 0, -1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCatalog_ThemeTaxonomy(), theXMLTypePackage.getAnyURI(), "themeTaxonomy", null, 0, -1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCatalog_HasPart(), theXMLTypePackage.getAnyURI(), "hasPart", null, 0, -1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCatalog_Homepage(), theXMLTypePackage.getAnyURI(), "homepage", null, 0, 1, Catalog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(datasetEClass, Dataset.class, "Dataset", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDataset_Distribution(), this.getDistribution(), null, "distribution", null, 0, -1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataset_SpatialResolutionInMeters(), theXMLTypePackage.getDecimal(), "spatialResolutionInMeters", null, 0, 1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataset_TemporalResolution(), theXMLTypePackage.getDuration(), "temporalResolution", null, 0, 1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataset_AccrualPeriodicity(), theXMLTypePackage.getAnyURI(), "accrualPeriodicity", null, 0, 1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDataset_Spatial(), theTermsPackage.getLocation(), null, "spatial", null, 0, -1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDataset_Temporal(), theTermsPackage.getPeriodOfTime(), null, "temporal", null, 0, -1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataset_WasGeneratedBy(), theXMLTypePackage.getAnyURI(), "wasGeneratedBy", null, 0, -1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDataset_InSeries(), this.getDatasetSeries(), null, "inSeries", null, 0, -1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataset_Version(), theXMLTypePackage.getString(), "version", null, 0, 1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataset_HasVersion(), theXMLTypePackage.getAnyURI(), "hasVersion", null, 0, -1, Dataset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(distributionEClass, Distribution.class, "Distribution", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDistribution_Title(), theRdfPackage.getPlainLiteral(), null, "title", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_Description(), theRdfPackage.getPlainLiteral(), null, "description", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_AccessService(), this.getDataService(), null, "accessService", null, 0, -1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_Format(), theXMLTypePackage.getAnyURI(), "format", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_MediaType(), theXMLTypePackage.getAnyURI(), "mediaType", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_PackageFormat(), theXMLTypePackage.getAnyURI(), "packageFormat", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_ByteSize(), theXMLTypePackage.getNonNegativeInteger(), "byteSize", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_CompressFormat(), ecorePackage.getEObject(), null, "compressFormat", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_SpatialResolutionInMeters(), theXMLTypePackage.getDecimal(), "spatialResolutionInMeters", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_TemporalResolution(), theXMLTypePackage.getDuration(), "temporalResolution", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_AccessRights(), theXMLTypePackage.getAnyURI(), "accessRights", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_License(), theTermsPackage.getLicenseDocument(), null, "license", null, 1, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_ConformsTo(), theTermsPackage.getStandard(), null, "conformsTo", null, 0, -1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_Rights(), theTermsPackage.getRightsStatement(), null, "rights", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_HasPolicy(), theXMLTypePackage.getAnyURI(), "hasPolicy", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_Issued(), theRdfPackage.getDateOrDateTimeLiteral(), null, "issued", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_Modified(), theRdfPackage.getDateOrDateTimeLiteral(), null, "modified", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_NodeID(), theXMLTypePackage.getNCName(), "nodeID", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_LicenseAttributionByText(), theRdfPackage.getPlainLiteral(), null, "licenseAttributionByText", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_Availability(), theXMLTypePackage.getAnyURI(), "availability", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_Status(), theXMLTypePackage.getAnyURI(), "status", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDistribution_Checksum(), theSpdxPackage.getChecksum(), null, "checksum", null, 0, 1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_ApplicableLegislation(), theXMLTypePackage.getAnyURI(), "applicableLegislation", null, 0, -1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_DownloadURL(), theXMLTypePackage.getAnyURI(), "downloadURL", null, 0, -1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDistribution_AccessURL(), theXMLTypePackage.getAnyURI(), "accessURL", null, 1, -1, Distribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(relationshipEClass, Relationship.class, "Relationship", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRelationship_HadRole(), ecorePackage.getEObject(), null, "hadRole", null, 0, 1, Relationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRelationship_Description(), theRdfPackage.getPlainLiteral(), null, "description", null, 0, 1, Relationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRelationship_NodeID(), theXMLTypePackage.getNCName(), "nodeID", null, 0, 1, Relationship.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(catalogRecordEClass, CatalogRecord.class, "CatalogRecord", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCatalogRecord_Title(), theRdfPackage.getPlainLiteral(), null, "title", null, 0, -1, CatalogRecord.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalogRecord_Description(), theRdfPackage.getPlainLiteral(), null, "description", null, 0, -1, CatalogRecord.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalogRecord_Issued(), theRdfPackage.getDateOrDateTimeLiteral(), null, "issued", null, 0, 1, CatalogRecord.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalogRecord_Modified(), theRdfPackage.getDateOrDateTimeLiteral(), null, "modified", null, 1, 1, CatalogRecord.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalogRecord_ConformsTo(), theTermsPackage.getStandard(), null, "conformsTo", null, 0, -1, CatalogRecord.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCatalogRecord_Language(), theXMLTypePackage.getAnyURI(), "language", null, 0, -1, CatalogRecord.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCatalogRecord_PrimaryTopic(), this.getDcatResource(), null, "primaryTopic", null, 1, 1, CatalogRecord.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dataServiceEClass, DataService.class, "DataService", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDataService_EndpointDescription(), theXMLTypePackage.getAnyURI(), "endpointDescription", null, 0, -1, DataService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataService_EndpointURL(), theXMLTypePackage.getAnyURI(), "endpointURL", null, 1, -1, DataService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDataService_ServesDataset(), this.getDataset(), null, "servesDataset", null, 0, -1, DataService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataService_Format(), theXMLTypePackage.getAnyURI(), "format", null, 0, -1, DataService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dcatResourceEClass, DcatResource.class, "DcatResource", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDcatResource_Identifier(), theRdfPackage.getPlainLiteral(), null, "identifier", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Title(), theRdfPackage.getPlainLiteral(), null, "title", null, 1, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Description(), theRdfPackage.getPlainLiteral(), null, "description", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_Theme(), theXMLTypePackage.getAnyURI(), "theme", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Keyword(), theRdfPackage.getPlainLiteral(), null, "keyword", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_Type(), theXMLTypePackage.getAnyURI(), "type", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_ContactPoint(), theVcardPackage.getOrganization(), null, "contactPoint", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Creator(), theFoafPackage.getAgent(), null, "creator", null, 0, 1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Publisher(), theFoafPackage.getAgent(), null, "publisher", null, 1, 1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Issued(), theRdfPackage.getDateOrDateTimeLiteral(), null, "issued", null, 0, 1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Modified(), theRdfPackage.getDateOrDateTimeLiteral(), null, "modified", null, 0, 1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_LandingPage(), theFoafPackage.getDocument(), null, "landingPage", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_AccessRights(), theXMLTypePackage.getAnyURI(), "accessRights", null, 0, 1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_ConformsTo(), theTermsPackage.getStandard(), null, "conformsTo", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_License(), theTermsPackage.getLicenseDocument(), null, "license", null, 0, 1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Rights(), theTermsPackage.getRightsStatement(), null, "rights", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_HasPolicy(), theXMLTypePackage.getAnyURI(), "hasPolicy", null, 0, 1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_QualifiedAttribution(), theXMLTypePackage.getAnyURI(), "qualifiedAttribution", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_QualifiedRelation(), this.getRelationship(), null, "qualifiedRelation", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_Relation(), theXMLTypePackage.getAnyURI(), "relation", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_IsReferencedBy(), theXMLTypePackage.getAnyURI(), "isReferencedBy", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_Language(), theXMLTypePackage.getAnyURI(), "language", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_ContributorID(), theXMLTypePackage.getAnyURI(), "contributorID", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_ApplicableLegislation(), theXMLTypePackage.getAnyURI(), "applicableLegislation", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Originator(), theFoafPackage.getAgent(), null, "originator", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Custodian(), theFoafPackage.getAgent(), null, "custodian", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDcatResource_PoliticalGeocodingLevelURI(), theXMLTypePackage.getAnyURI(), "politicalGeocodingLevelURI", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_AdmsIdentifier(), theAdmsPackage.getIdentifier(), null, "admsIdentifier", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDcatResource_Provenance(), theTermsPackage.getProvenanceStatement(), null, "provenance", null, 0, -1, DcatResource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(datasetSeriesEClass, DatasetSeries.class, "DatasetSeries", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/Ecore
		createEcoreAnnotations();
		// http://www.eclipse.org/fennec/m2x/ocl/1.0
		create_1Annotations();
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/Ecore</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createEcoreAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/Ecore";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "validationDelegates", "http://www.eclipse.org/fennec/m2x/ocl/1.0"
		   });
		addAnnotation
		  (catalogEClass,
		   source,
		   new String[] {
			   "constraints", "ThemeTaxonomyIsIri HasPartIsIri HomepageIsIri"
		   });
		addAnnotation
		  (datasetEClass,
		   source,
		   new String[] {
			   "constraints", "HasDescription AccrualPeriodicityIsIri WasGeneratedByIsIri HasVersionIsIri"
		   });
		addAnnotation
		  (distributionEClass,
		   source,
		   new String[] {
			   "constraints", "HasIdentity AccessURLIsIri DownloadURLIsIri FormatIsIri MediaTypeIsIri PackageFormatIsIri AccessRightsIsIri HasPolicyIsIri AvailabilityIsIri StatusIsIri ApplicableLegislationIsIri"
		   });
		addAnnotation
		  (catalogRecordEClass,
		   source,
		   new String[] {
			   "constraints", "LanguageIsIri"
		   });
		addAnnotation
		  (dataServiceEClass,
		   source,
		   new String[] {
			   "constraints", "EndpointURLIsIri EndpointDescriptionIsIri FormatIsIri"
		   });
		addAnnotation
		  (dcatResourceEClass,
		   source,
		   new String[] {
			   "constraints", "HasIdentity ThemeIsIri TypeIsIri AccessRightsIsIri HasPolicyIsIri QualifiedAttributionIsIri RelationIsIri IsReferencedByIsIri LanguageIsIri ContributorIDIsIri ApplicableLegislationIsIri PoliticalGeocodingLevelURIIsIri"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/fennec/m2x/ocl/1.0</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void create_1Annotations() {
		String source = "http://www.eclipse.org/fennec/m2x/ocl/1.0";
		addAnnotation
		  (catalogEClass,
		   source,
		   new String[] {
			   "ThemeTaxonomyIsIri", "self.themeTaxonomy->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "HasPartIsIri", "self.hasPart->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "HomepageIsIri", "self.homepage = null or self.homepage.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')"
		   });
		addAnnotation
		  (datasetEClass,
		   source,
		   new String[] {
			   "HasDescription", "self.description->notEmpty()",
			   "AccrualPeriodicityIsIri", "self.accrualPeriodicity = null or self.accrualPeriodicity.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "WasGeneratedByIsIri", "self.wasGeneratedBy->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "HasVersionIsIri", "self.hasVersion->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))"
		   });
		addAnnotation
		  (distributionEClass,
		   source,
		   new String[] {
			   "HasIdentity", "self.about <> null and self.about.size() > 0",
			   "AccessURLIsIri", "self.accessURL->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "DownloadURLIsIri", "self.downloadURL->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "FormatIsIri", "self.format = null or self.format.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "MediaTypeIsIri", "self.mediaType = null or self.mediaType.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "PackageFormatIsIri", "self.packageFormat = null or self.packageFormat.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "AccessRightsIsIri", "self.accessRights = null or self.accessRights.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "HasPolicyIsIri", "self.hasPolicy = null or self.hasPolicy.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "AvailabilityIsIri", "self.availability = null or self.availability.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "StatusIsIri", "self.status = null or self.status.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "ApplicableLegislationIsIri", "self.applicableLegislation->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))"
		   });
		addAnnotation
		  (catalogRecordEClass,
		   source,
		   new String[] {
			   "LanguageIsIri", "self.language->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))"
		   });
		addAnnotation
		  (dataServiceEClass,
		   source,
		   new String[] {
			   "EndpointURLIsIri", "self.endpointURL->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "EndpointDescriptionIsIri", "self.endpointDescription->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "FormatIsIri", "self.format->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))"
		   });
		addAnnotation
		  (dcatResourceEClass,
		   source,
		   new String[] {
			   "HasIdentity", "self.about <> null and self.about.size() > 0",
			   "ThemeIsIri", "self.theme->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "TypeIsIri", "self.type->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "AccessRightsIsIri", "self.accessRights = null or self.accessRights.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "HasPolicyIsIri", "self.hasPolicy = null or self.hasPolicy.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')",
			   "QualifiedAttributionIsIri", "self.qualifiedAttribution->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "RelationIsIri", "self.relation->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "IsReferencedByIsIri", "self.isReferencedBy->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "LanguageIsIri", "self.language->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "ContributorIDIsIri", "self.contributorID->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "ApplicableLegislationIsIri", "self.applicableLegislation->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))",
			   "PoliticalGeocodingLevelURIIsIri", "self.politicalGeocodingLevelURI->forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))"
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
		  (catalogEClass,
		   source,
		   new String[] {
			   "name", "Catalog",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getCatalog_Catalog(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "catalog",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getCatalog_Record(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "record",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getCatalog_Dataset(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "dataset",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getCatalog_Service(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "service",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getCatalog_ThemeTaxonomy(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "themeTaxonomy",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getCatalog_HasPart(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hasPart",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getCatalog_Homepage(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "homepage",
			   "namespace", "http://xmlns.com/foaf/0.1/"
		   });
		addAnnotation
		  (datasetEClass,
		   source,
		   new String[] {
			   "name", "Dataset",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getDataset_Distribution(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "distribution",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDataset_SpatialResolutionInMeters(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "spatialResolutionInMeters",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDataset_TemporalResolution(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "temporalResolution",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDataset_AccrualPeriodicity(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "accrualPeriodicity",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDataset_Spatial(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "spatial",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDataset_Temporal(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "temporal",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDataset_WasGeneratedBy(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "wasGeneratedBy",
			   "namespace", "http://www.w3.org/ns/prov#"
		   });
		addAnnotation
		  (getDataset_InSeries(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "inSeries",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDataset_Version(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "version",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDataset_HasVersion(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hasVersion",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (distributionEClass,
		   source,
		   new String[] {
			   "name", "Distribution",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getDistribution_Title(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "title",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_Description(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "description",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_AccessService(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "accessService",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDistribution_Format(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "format",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_MediaType(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "mediaType",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDistribution_PackageFormat(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "packageFormat",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDistribution_ByteSize(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "byteSize",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDistribution_CompressFormat(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "compressFormat",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDistribution_SpatialResolutionInMeters(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "spatialResolutionInMeters",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDistribution_TemporalResolution(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "temporalResolution",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDistribution_AccessRights(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "accessRights",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_License(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "license",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_ConformsTo(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "conformsTo",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_Rights(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "rights",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_HasPolicy(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hasPolicy",
			   "namespace", "http://www.w3.org/ns/odrl/2/"
		   });
		addAnnotation
		  (getDistribution_Issued(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "issued",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_Modified(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "modified",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDistribution_NodeID(),
		   source,
		   new String[] {
			   "kind", "attribute",
			   "name", "nodeID",
			   "namespace", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		   });
		addAnnotation
		  (getDistribution_LicenseAttributionByText(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "licenseAttributionByText",
			   "namespace", "http://dcat-ap.de/def/dcatde/"
		   });
		addAnnotation
		  (getDistribution_Availability(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "availability",
			   "namespace", "http://data.europa.eu/r5r/"
		   });
		addAnnotation
		  (getDistribution_Status(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "status",
			   "namespace", "http://www.w3.org/ns/adms#"
		   });
		addAnnotation
		  (getDistribution_Checksum(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "checksum",
			   "namespace", "http://spdx.org/rdf/terms#"
		   });
		addAnnotation
		  (getDistribution_ApplicableLegislation(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "applicableLegislation",
			   "namespace", "http://data.europa.eu/r5r/"
		   });
		addAnnotation
		  (getDistribution_DownloadURL(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "downloadURL",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDistribution_AccessURL(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "accessURL",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (relationshipEClass,
		   source,
		   new String[] {
			   "name", "Relationship",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getRelationship_HadRole(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hadRole",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getRelationship_Description(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "description",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getRelationship_NodeID(),
		   source,
		   new String[] {
			   "kind", "attribute",
			   "name", "nodeID",
			   "namespace", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		   });
		addAnnotation
		  (catalogRecordEClass,
		   source,
		   new String[] {
			   "name", "CatalogRecord",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getCatalogRecord_Title(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "title",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getCatalogRecord_Description(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "description",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getCatalogRecord_Issued(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "issued",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getCatalogRecord_Modified(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "modified",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getCatalogRecord_ConformsTo(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "conformsTo",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getCatalogRecord_Language(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "language",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getCatalogRecord_PrimaryTopic(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "primaryTopic",
			   "namespace", "http://xmlns.com/foaf/0.1/"
		   });
		addAnnotation
		  (dataServiceEClass,
		   source,
		   new String[] {
			   "name", "DataService",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getDataService_EndpointDescription(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "endpointDescription",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDataService_EndpointURL(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "endpointURL",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDataService_ServesDataset(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "servesDataset",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDataService_Format(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "format",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (dcatResourceEClass,
		   source,
		   new String[] {
			   "name", "ResourceType",
			   "kind", "elementOnly"
		   });
		addAnnotation
		  (getDcatResource_Identifier(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "identifier",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_Title(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "title",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_Description(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "description",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_Theme(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "theme",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDcatResource_Keyword(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "keyword",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDcatResource_Type(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "type",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_ContactPoint(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "contactPoint",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDcatResource_Creator(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "creator",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_Publisher(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "publisher",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_Issued(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "issued",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_Modified(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "modified",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_LandingPage(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "landingPage",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDcatResource_AccessRights(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "accessRights",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_ConformsTo(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "conformsTo",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_License(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "license",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_Rights(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "rights",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_HasPolicy(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "hasPolicy",
			   "namespace", "http://www.w3.org/ns/odrl/2/"
		   });
		addAnnotation
		  (getDcatResource_QualifiedAttribution(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "qualifiedAttribution",
			   "namespace", "http://www.w3.org/ns/prov#"
		   });
		addAnnotation
		  (getDcatResource_QualifiedRelation(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "qualifiedRelation",
			   "namespace", "##targetNamespace"
		   });
		addAnnotation
		  (getDcatResource_Relation(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "relation",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_IsReferencedBy(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "isReferencedBy",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_Language(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "language",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (getDcatResource_ContributorID(),
		   source,
		   new String[] {
			   "kind", "element",
			   "namespace", "http://dcat-ap.de/def/dcatde/",
			   "name", null
		   });
		addAnnotation
		  (getDcatResource_ApplicableLegislation(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "applicableLegislation",
			   "namespace", "http://data.europa.eu/r5r/"
		   });
		addAnnotation
		  (getDcatResource_Originator(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "originator",
			   "namespace", "http://data.europa.eu/930/"
		   });
		addAnnotation
		  (getDcatResource_Custodian(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "custodian",
			   "namespace", "http://data.europa.eu/930/"
		   });
		addAnnotation
		  (getDcatResource_PoliticalGeocodingLevelURI(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "politicalGeocodingLevelURI",
			   "namespace", "http://dcat-ap.de/def/dcatde/"
		   });
		addAnnotation
		  (getDcatResource_AdmsIdentifier(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "identifier",
			   "namespace", "http://www.w3.org/ns/adms#"
		   });
		addAnnotation
		  (getDcatResource_Provenance(),
		   source,
		   new String[] {
			   "kind", "element",
			   "name", "provenance",
			   "namespace", "http://purl.org/dc/terms/"
		   });
		addAnnotation
		  (datasetSeriesEClass,
		   source,
		   new String[] {
			   "name", "DatasetSeries",
			   "kind", "elementOnly"
		   });
	}

} //DcatPackageImpl
