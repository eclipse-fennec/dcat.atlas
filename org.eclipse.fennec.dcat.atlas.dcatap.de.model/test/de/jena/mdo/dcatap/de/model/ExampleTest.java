package de.jena.mdo.dcatap.de.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import adms.AdmsPackage;
import adms.impl.AdmsPackageImpl;
import dcat.Catalog;
import dcat.CatalogRecord;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetContainer;
import dcat.DatasetSeries;
import dcat.DcatFactory;
import dcat.DcatPackage;
import dcat.Distribution;
import dcat.impl.DcatPackageImpl;
import dcatde.ContributorID;
import dcatde.DcatDEPackage;
import dcatde.impl.DcatDEPackageImpl;
import foaf.Agent;
import foaf.FoafFactory;
import foaf.FoafPackage;
import foaf.impl.FoafPackageImpl;
import locn.LocnPackage;
import locn.impl.LocnPackageImpl;
import odrl.OdrlPackage;
import odrl.impl.OdrlPackageImpl;
import owl.OwlPackage;
import owl.impl.OwlPackageImpl;
import prov.ProvPackage;
import prov.impl.ProvPackageImpl;
import rdf.DateOrDateTimeLiteral;
import rdf.PlainLiteral;
import rdf.RDFRoot;
import rdf.RdfFactory;
import rdf.RdfPackage;
import rdf.impl.RdfPackageImpl;
import rdf.util.RdfResourceFactoryImpl;
import schema.SchemaPackage;
import schema.impl.SchemaPackageImpl;
import skos.Concept;
import skos.SkosFactory;
import skos.SkosPackage;
import skos.impl.SkosPackageImpl;
import spdx.Checksum;
import spdx.SpdxFactory;
import spdx.SpdxPackage;
import spdx.impl.SpdxPackageImpl;
import terms.TermsFactory;
import terms.TermsPackage;
import terms.impl.TermsPackageImpl;
import vcard.Organization;
import vcard.OrganizationType;
import vcard.VcardPackage;
import vcard.impl.VcardPackageImpl;

public class ExampleTest {
	
	private ResourceSet resourceSet;
	
	@BeforeEach
	protected void before() {
		resourceSet = new ResourceSetImpl();
		Registry packageRegistry = resourceSet.getPackageRegistry();
		packageRegistry.put(SchemaPackage.eNS_URI, SchemaPackageImpl.init());
		packageRegistry.put(TermsPackage.eNS_URI, TermsPackageImpl.init());
		packageRegistry.put(FoafPackage.eNS_URI, FoafPackageImpl.init());
		packageRegistry.put(AdmsPackage.eNS_URI, AdmsPackageImpl.init());
		packageRegistry.put(DcatPackage.eNS_URI, DcatPackageImpl.init());
		packageRegistry.put(DcatDEPackage.eNS_URI, DcatDEPackageImpl.init());
		packageRegistry.put(LocnPackage.eNS_URI, LocnPackageImpl.init());
		packageRegistry.put(OdrlPackage.eNS_URI, OdrlPackageImpl.init());
		packageRegistry.put(OwlPackage.eNS_URI, OwlPackageImpl.init());
		packageRegistry.put(ProvPackage.eNS_URI, ProvPackageImpl.init());
		packageRegistry.put(RdfPackage.eNS_URI, RdfPackageImpl.init());
		packageRegistry.put(SkosPackage.eNS_URI, SkosPackageImpl.init());
		packageRegistry.put(VcardPackage.eNS_URI, VcardPackageImpl.init());
		packageRegistry.put(SpdxPackage.eNS_URI, SpdxPackageImpl.init());
		packageRegistry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		packageRegistry.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
		packageRegistry.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
		org.eclipse.emf.ecore.resource.Resource.Factory.Registry resourceFactoryRegistry = resourceSet.getResourceFactoryRegistry();
		RdfResourceFactoryImpl rdfResourceFactoryImpl = new RdfResourceFactoryImpl();
		resourceFactoryRegistry.getExtensionToFactoryMap().put("rdf", rdfResourceFactoryImpl);
		resourceFactoryRegistry.getContentTypeToFactoryMap().put(RdfPackage.eCONTENT_TYPE, rdfResourceFactoryImpl);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadResource01() {
		Resource resource = resourceSet.createResource(URI.createFileURI("dcat_ap_de_01.rdf"));
		assertNotNull(resource);
		InputStream inputStream = getClass().getResourceAsStream("dcat_ap_de_01.rdf");
		assertNotNull(inputStream);
		try {
			resource.load(inputStream, null);
		} catch (IOException e) {
			fail("IO Exception " + e.getMessage(), e);
		}
		assertFalse(resource.getContents().isEmpty());
		RDFRoot rdfRoot = (RDFRoot) resource.getContents().get(0);
		EList<AnyType> roots = rdfRoot.getRDF();
		assertNotNull(roots);
		assertFalse(roots.isEmpty());
		AnyType root = roots.get(0);
		assertFalse(root.getAny().isEmpty());
		
		List<Catalog> catalogs = (List<Catalog>) root.eGet(DcatPackage.Literals.DCATAP_ROOT__CATALOG);
		assertNotNull(catalogs);
		assertFalse(catalogs.isEmpty());
		Catalog catalog = catalogs.get(0);
		assertEquals("https://govdata.de#catalog", catalog.getAbout());
		EList<PlainLiteral> titles = catalog.getTitle();
		assertFalse(titles.isEmpty());
		assertEquals("GovData", titles.get(0).getValue());
		EList<PlainLiteral> descriptions = catalog.getDescription();
		assertFalse(descriptions.isEmpty());
		assertEquals("Das Datenportal für Deutschland - Open Government: Verwaltungsdaten transparent, offen und frei nutzbar.", descriptions.get(0).getValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadResource02() {
		Resource resource = resourceSet.createResource(URI.createFileURI("dcat_ap_de_02.rdf"));
		assertNotNull(resource);
		InputStream inputStream = getClass().getResourceAsStream("dcat_ap_de_02.rdf");
		assertNotNull(inputStream);
		try {
			resource.load(inputStream, null);
		} catch (IOException e) {
			fail("IO Exception " + e.getMessage(), e);
		}
		assertFalse(resource.getContents().isEmpty());
		RDFRoot rdfRoot = (RDFRoot) resource.getContents().get(0);
		EList<AnyType> roots = rdfRoot.getRDF();
		assertNotNull(roots);
		assertFalse(roots.isEmpty());
		AnyType root = roots.get(0);
		assertFalse(root.getAny().isEmpty());
		
		List<Catalog> catalogs = (List<Catalog>) root.eGet(DcatPackage.Literals.DCATAP_ROOT__CATALOG);
		assertNotNull(catalogs);
		assertFalse(catalogs.isEmpty());
		Catalog catalog = catalogs.get(0);
		assertEquals("https://govdata.de#catalog", catalog.getAbout());
		EList<PlainLiteral> titles = catalog.getTitle();
		assertFalse(titles.isEmpty());
		assertEquals("GovData", titles.get(0).getValue());
		EList<PlainLiteral> descriptions = catalog.getDescription();
		assertFalse(descriptions.isEmpty());
		assertEquals("Das Datenportal für Deutschland - Open Government: Verwaltungsdaten transparent, offen und frei nutzbar.", descriptions.get(0).getValue());
		EList<DatasetContainer> datasets = catalog.getDataset();
		assertFalse(datasets.isEmpty());
		DatasetContainer container = datasets.get(0);
		Dataset dataset = container.getDataset();
		assertEquals("https://ckan.govdata.de/dataset/d4ce4e6e-ab89-44cb-bf5c-33a162c234de#dataset", dataset.getAbout());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadResource03() {
		Resource resource = resourceSet.createResource(URI.createFileURI("dcat_ap_de_03.rdf"));
		assertNotNull(resource);
		InputStream inputStream = getClass().getResourceAsStream("dcat_ap_de_03.rdf");
		assertNotNull(inputStream);
		try {
			resource.load(inputStream, null);
		} catch (IOException e) {
			for (Diagnostic d : resource.getErrors()) {
				System.out.println(d.getLocation() + ":" + d.getMessage() + ":[" + d.getLine() + "," + d.getColumn() + "]");
			}
			fail("IO Exception " + e.getMessage(), e);
		}
		assertFalse(resource.getContents().isEmpty());
		RDFRoot rdfRoot = (RDFRoot) resource.getContents().get(0);
		EList<AnyType> roots = rdfRoot.getRDF();
		assertNotNull(roots);
		assertFalse(roots.isEmpty());
		AnyType root = roots.get(0);
		assertFalse(root.getAny().isEmpty());
		
		List<Catalog> catalogs = (List<Catalog>) root.eGet(DcatPackage.Literals.DCATAP_ROOT__CATALOG);
		assertNotNull(catalogs);
		assertFalse(catalogs.isEmpty());
		Catalog catalog = catalogs.get(0);
		assertEquals("https://govdata.de#catalog", catalog.getAbout());
		EList<PlainLiteral> titles = catalog.getTitle();
		assertFalse(titles.isEmpty());
		assertEquals("GovData", titles.get(0).getValue());
		EList<PlainLiteral> descriptions = catalog.getDescription();
		assertFalse(descriptions.isEmpty());
		assertEquals("Das Datenportal für Deutschland - Open Government: Verwaltungsdaten transparent, offen und frei nutzbar.", descriptions.get(0).getValue());
		EList<DatasetContainer> datasets = catalog.getDataset();
		assertFalse(datasets.isEmpty());
		DatasetContainer container = datasets.get(0);
		Dataset dataset = container.getDataset();
		assertEquals("https://ckan.govdata.de/dataset/d4ce4e6e-ab89-44cb-bf5c-33a162c234de#dataset", dataset.getAbout());
		ContributorID contributorID = dataset.getContributorID();
		assertNotNull(contributorID);
		assertEquals("http://dcat-ap.de/def/contributors/transparenzportalHamburg", contributorID.getResource());
		Organization organizations = dataset.getContactPoint().get(0);
		OrganizationType organization = organizations.getIndividual();
		assertEquals("ub85bL31C21", organization.getNodeID());
		assertEquals("Meister, Thomas, Herr", organization.getFn());
		// hasTelephone became multi-valued (0..n) in DCAT-AP.de 3.0 (spec §4.10).
		assertEquals(1, organization.getHasTelephone().size());
		assertEquals("+49 40 123 45678", organization.getHasTelephone().get(0));
		
		assertEquals("Darstellung der Badegewässer und ihrer Überwachungsmessstellen im Internet.", dataset.getDescription().get(0).getValue());
		
		Agent publisher = catalog.getPublisher();
		assertNotNull(publisher);
		foaf.Organization publisherOrga = publisher.getOrganization();
		assertNotNull(publisherOrga);
		assertEquals("https://www.govdata.de/web/guest/impressum#publisher", publisherOrga.getAbout());
		assertEquals("Geschäfts- und Koordinierungsstelle GovData", publisherOrga.getName().getValue());
		
		assertEquals("GovData", catalog.getTitle().get(0).getValue());
		
		List<Distribution> distributions = (List<Distribution>) root.eGet(DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION);
		assertNotNull(distributions);
		assertFalse(distributions.isEmpty());
		Distribution distribution = distributions.get(0);
		assertEquals("https://ckan.govdata.de/dataset/d4ce4e6e-ab89-44cb-bf5c-33a162c234de/resource/a289c289-55c9-410f-b4c7-f88e5f6f4e47#distribution", distribution.getAbout());
		assertEquals("http://daten-hamburg.de/umwelt_klima/badegewaesser/Badegewaesser_HH_2004-2013.zip", distribution.getDownloadURL().get(0).getResource());
		assertEquals("https://ckan.govdata.de/dataset/d4ce4e6e-ab89-44cb-bf5c-33a162c234de/resource/a289c289-55c9-410f-b4c7-f88e5f6f4e47", distribution.getAccessURL().get(0).getResource());
		assertEquals("Freie und Hansestadt Hamburg, Behoerde für Umwelt und Energie", distribution.getLicenseAttributionByText().getValue());
		assertEquals("http://dcat-ap.de/def/licenses/dl-by-de/2.0", distribution.getLicense().getResource());
		assertEquals(dataset.getDistribution().get(0).getResource(), distribution.getAbout());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadResource04() {
		Resource resource = resourceSet.createResource(URI.createFileURI("eudata01.rdf"));
		assertNotNull(resource);
		InputStream inputStream = getClass().getResourceAsStream("eudata01.rdf");
		assertNotNull(inputStream);
		try {
			resource.load(inputStream, null);
		} catch (IOException e) {
			for (Diagnostic d : resource.getErrors()) {
				System.out.println(d.getLocation() + ":" + d.getMessage() + ":[" + d.getLine() + "," + d.getColumn() + "]");
			}
//			fail("IO Exception " + e.getMessage(), e);
		}
		System.out.println("Test Data");
		assertFalse(resource.getContents().isEmpty());
		RDFRoot rdfRoot = (RDFRoot) resource.getContents().get(0);
		EList<AnyType> roots = rdfRoot.getRDF();
		assertNotNull(roots);
		assertFalse(roots.isEmpty());
		AnyType root = roots.get(0);
		assertFalse(root.getAny().isEmpty());
		
		List<CatalogRecord> catalogRecords = (List<CatalogRecord>) root.eGet(DcatPackage.Literals.DCATAP_ROOT__CATALOG_RECORD);
		
	}
	
	@Test
	@Disabled
	public void testSaveResource01() {
		Resource resource = resourceSet.createResource(URI.createFileURI("sample01.rdf"));
//		Resource resource = resourceSet.createResource(URI.createURI("http://localhost:8081/catalogues/sample01"), RdfPackage.eCONTENT_TYPE);
		
		RDFRoot rdfRoot = RdfFactory.eINSTANCE.createRDFRoot();
		resource.getContents().add(rdfRoot);
		
		AnyType anyType = XMLTypeFactory.eINSTANCE.createAnyType();
		resource.getContents().add(anyType);
		rdfRoot.getRDF().add(anyType);
		
		
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		EList<Catalog> calalogs = ECollections.singletonEList(catalog);
		anyType.eSet(DcatPackage.Literals.DCATAP_ROOT__CATALOG, calalogs);
		catalog.getTitle().add(createLiteral("DE", "Ein Test-Titel"));
		catalog.getTitle().add(createLiteral("EN", "A Test-Title"));
		catalog.getDescription().add(createLiteral("DE", "Eine Test-Titel Beschreibung"));
		catalog.getDescription().add(createLiteral("EN", "A Test-title description"));
		catalog.getLanguage().add(createRDFResource("http://publications.europa.eu/resource/authority/language/ENG"));
		catalog.getLanguage().add(createRDFResource("http://publications.europa.eu/resource/authority/language/DEU"));
		
		Dataset dataSet = DcatFactory.eINSTANCE.createDataset();
		EList<Dataset> dataSets = ECollections.singletonEList(dataSet);
		anyType.eSet(DcatPackage.Literals.DCATAP_ROOT__DATASET, dataSets);
		dataSet.getTitle().add(createLiteral("DE", "Beipiel Dataset 1"));
		dataSet.getTitle().add(createLiteral("EN", "Example Dataset 1"));
		dataSet.getDescription().add(createLiteral("DE", "Das ist ein Beipiel-Datenset"));
		dataSet.getDescription().add(createLiteral("EN", "This is an example Dataset"));
		dataSet.getDistribution().add(createRDFResource("https://example.io/set/distribution/1"));
		DateOrDateTimeLiteral issued = RdfFactory.eINSTANCE.createDateOrDateTimeLiteral();
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date;
		try {
			date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			issued.setValue(date);
			dataSet.setIssued(issued);
		} catch (DatatypeConfigurationException e1) {
			System.out.println("Issued error " + e1.getMessage());
		}
		
		
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout("https://example.io/set/distribution/1");
		EList<Distribution> distributions = ECollections.singletonEList(distribution);
		anyType.eSet(DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION, distributions);
		distribution.getAccessURL().add(createRDFResource("http://a-csv-file.com"));
		Concept format = SkosFactory.eINSTANCE.createConcept();
		format.setResource("http://publications.europa.eu/resource/authority/file-type/CSV");
		distribution.setFormat(format);
		distribution.setTitle(createLiteral("DE", "Beispiel Distribution"));
		
		try {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			resource.save(baos, null);
			URL url = new URL("http://localhost:8081/catalogues/sample01");
		    final URLConnection urlConnection = url.openConnection();
		    urlConnection.setDoOutput(true);
		    urlConnection.setDoInput(true);
		    if (urlConnection instanceof HttpURLConnection) {
		        final HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
		        httpURLConnection.addRequestProperty("Authorization", "bfd33c68-c2fe-428e-85dc-1cd3e5e2f1be");
		        httpURLConnection.setRequestMethod("PUT");
		        resource.save(urlConnection.getOutputStream(), null);
		        int responseCode = httpURLConnection.getResponseCode();
//		        assertEquals(200, responseCode);
		        Object content = httpURLConnection.getContent();
		        assertNotNull(content);
		    }
		} catch (IOException e) {
			e.printStackTrace();
			for (Diagnostic d : resource.getErrors()) {
				System.out.println(d.getLocation() + ":" + d.getMessage() + ":[" + d.getLine() + "," + d.getColumn() + "]");
			}
		}

		
	}
	
	// ---------------------------------------------------------------------
	// DCAT-AP.de 3.0 additions
	// ---------------------------------------------------------------------

	/** DatasetSeries is new in v3 (§4.5); a Dataset links to it via dcat:inSeries (§4.3). */
	@Test
	public void testDatasetSeriesAndInSeries() {
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		series.setAbout("https://example.org/series/air-quality");
		series.getTitle().add(createLiteral("EN", "Air quality time series"));
		series.getDescription().add(createLiteral("EN", "Yearly air quality measurements"));
		// publisher is now mandatory (1..1) on every dcat:Resource (§9.1) and inherited here.
		series.setPublisher(FoafFactory.eINSTANCE.createAgent());
		Concept frequency = SkosFactory.eINSTANCE.createConcept();
		frequency.setResource("http://publications.europa.eu/resource/authority/frequency/ANNUAL");
		series.setAccrualPeriodicity(frequency);
		series.getSpatial().add(TermsFactory.eINSTANCE.createLocation());
		series.getTemporal().add(TermsFactory.eINSTANCE.createPeriodOfTime());
		series.getApplicableLegislation().add(createRDFResource("http://data.europa.eu/eli/reg/2019/1024/oj"));

		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.getInSeries().add(series);

		assertEquals(1, dataset.getInSeries().size());
		assertSame(series, dataset.getInSeries().get(0));
		assertEquals("Air quality time series", series.getTitle().get(0).getValue());
		assertNotNull(series.getPublisher());
		assertEquals("http://publications.europa.eu/resource/authority/frequency/ANNUAL",
				series.getAccrualPeriodicity().getResource());
		assertEquals(1, series.getSpatial().size());
		assertEquals(1, series.getTemporal().size());
		assertEquals(1, series.getApplicableLegislation().size());
	}

	/** dcat:version / dcat:hasVersion and geodcatap:originator / custodian are new on Dataset in v3 (§4.3). */
	@Test
	public void testDatasetVersioningAndRoles() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		assertNull(dataset.getVersion());
		dataset.setVersion("2.0");
		dataset.getHasVersion().add(DcatFactory.eINSTANCE.createDataset());
		dataset.getOriginator().add(FoafFactory.eINSTANCE.createAgent());
		dataset.getCustodian().add(FoafFactory.eINSTANCE.createAgent());

		assertEquals("2.0", dataset.getVersion());
		assertEquals(1, dataset.getHasVersion().size());
		assertEquals(1, dataset.getOriginator().size());
		assertEquals(1, dataset.getCustodian().size());
	}

	/** Distribution gains spdx:checksum (§4.12), dcatap:availability, adms:status and applicableLegislation;
	 *  dcat:byteSize is now an integer (§9.1.13). */
	@Test
	public void testDistributionV3Features() {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();

		Checksum checksum = SpdxFactory.eINSTANCE.createChecksum();
		checksum.setAlgorithm(createRDFResource("http://spdx.org/rdf/terms#checksumAlgorithm_sha256"));
		byte[] value = new byte[] { 0x0a, 0x0b, 0x0c, 0x0d };
		checksum.setChecksumValue(value);
		distribution.setChecksum(checksum);

		Concept availability = SkosFactory.eINSTANCE.createConcept();
		availability.setResource("http://publications.europa.eu/resource/authority/planned-availability/AVAILABLE");
		distribution.setAvailability(availability);

		Concept status = SkosFactory.eINSTANCE.createConcept();
		status.setResource("http://publications.europa.eu/resource/authority/distribution-status/COMPLETED");
		distribution.setStatus(status);

		distribution.getApplicableLegislation().add(createRDFResource("http://data.europa.eu/eli/reg/2019/1024/oj"));
		distribution.setByteSize(BigInteger.valueOf(4096));

		assertNotNull(distribution.getChecksum());
		assertEquals("http://spdx.org/rdf/terms#checksumAlgorithm_sha256",
				distribution.getChecksum().getAlgorithm().getResource());
		assertArrayEquals(value, distribution.getChecksum().getChecksumValue());
		assertEquals("http://publications.europa.eu/resource/authority/planned-availability/AVAILABLE",
				distribution.getAvailability().getResource());
		assertEquals("http://publications.europa.eu/resource/authority/distribution-status/COMPLETED",
				distribution.getStatus().getResource());
		assertEquals(1, distribution.getApplicableLegislation().size());
		assertEquals(BigInteger.valueOf(4096), distribution.getByteSize());
	}

	/** dcterms:format is newly added to DataService in v3 (§4.4). */
	@Test
	public void testDataServiceFormat() {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		Concept format = SkosFactory.eINSTANCE.createConcept();
		format.setResource("http://publications.europa.eu/resource/authority/file-type/JSON");
		service.getFormat().add(format);

		assertEquals(1, service.getFormat().size());
		assertEquals("http://publications.europa.eu/resource/authority/file-type/JSON",
				service.getFormat().get(0).getResource());
	}

	/** dcatap:applicableLegislation is added on DcatResource, so every resource type inherits it (§9.1.9–§9.1.12). */
	@Test
	public void testApplicableLegislationInheritedByAllResources() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		DataService service = DcatFactory.eINSTANCE.createDataService();
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();

		String eli = "http://data.europa.eu/eli/reg/2019/1024/oj";
		catalog.getApplicableLegislation().add(createRDFResource(eli));
		dataset.getApplicableLegislation().add(createRDFResource(eli));
		service.getApplicableLegislation().add(createRDFResource(eli));
		series.getApplicableLegislation().add(createRDFResource(eli));

		assertEquals(1, catalog.getApplicableLegislation().size());
		assertEquals(1, dataset.getApplicableLegislation().size());
		assertEquals(1, service.getApplicableLegislation().size());
		assertEquals(1, series.getApplicableLegislation().size());
	}

	/** End-to-end: build a v3 model, serialize it to RDF/XML and parse it back, asserting the new
	 *  elements (dcat:version, dcat:inSeries + DatasetSeries, spdx:checksum, dcat:byteSize) survive. */
	@SuppressWarnings("unchecked")
	@Test
	public void testV3RoundTrip() throws IOException {
		Resource resource = resourceSet.createResource(URI.createFileURI("v3-roundtrip.rdf"));
		RDFRoot rdfRoot = RdfFactory.eINSTANCE.createRDFRoot();
		resource.getContents().add(rdfRoot);
		AnyType root = XMLTypeFactory.eINSTANCE.createAnyType();
		resource.getContents().add(root);
		rdfRoot.getRDF().add(root);

		// A dataset with a version, applicable legislation and a contained dataset series.
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout("https://example.org/dataset/1");
		dataset.getTitle().add(createLiteral("EN", "V3 dataset"));
		dataset.getDescription().add(createLiteral("EN", "A dataset exercising v3 features"));
		dataset.setPublisher(FoafFactory.eINSTANCE.createAgent());
		dataset.setVersion("3.0");
		dataset.getApplicableLegislation().add(createRDFResource("http://data.europa.eu/eli/reg/2019/1024/oj"));
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		series.setAbout("https://example.org/series/1");
		series.getTitle().add(createLiteral("EN", "V3 series"));
		series.getDescription().add(createLiteral("EN", "A series exercising v3 features"));
		series.setPublisher(FoafFactory.eINSTANCE.createAgent());
		dataset.getInSeries().add(series);
		root.eSet(DcatPackage.Literals.DCATAP_ROOT__DATASET, ECollections.singletonEList(dataset));

		// A distribution with a checksum and an integer byte size.
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout("https://example.org/dist/1");
		distribution.getAccessURL().add(createRDFResource("https://example.org/file.csv"));
		distribution.setByteSize(BigInteger.valueOf(4096));
		Checksum checksum = SpdxFactory.eINSTANCE.createChecksum();
		checksum.setAlgorithm(createRDFResource("http://spdx.org/rdf/terms#checksumAlgorithm_sha256"));
		checksum.setChecksumValue(new byte[] { 0x0a, 0x0b, 0x0c, 0x0d });
		distribution.setChecksum(checksum);
		root.eSet(DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION, ECollections.singletonEList(distribution));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		String xml = out.toString("UTF-8");
		assertTrue(xml.contains("inSeries"), "serialized XML should contain dcat:inSeries:\n" + xml);
		assertTrue(xml.contains("checksum"), "serialized XML should contain spdx:checksum:\n" + xml);

		// Parse it back into a fresh resource.
		Resource reloaded = resourceSet.createResource(URI.createFileURI("v3-reloaded.rdf"));
		reloaded.load(new ByteArrayInputStream(out.toByteArray()), null);
		assertFalse(reloaded.getContents().isEmpty());
		RDFRoot reloadedRoot = (RDFRoot) reloaded.getContents().get(0);
		AnyType any = reloadedRoot.getRDF().get(0);

		List<Dataset> datasets = (List<Dataset>) any.eGet(DcatPackage.Literals.DCATAP_ROOT__DATASET);
		assertFalse(datasets.isEmpty());
		Dataset parsedDataset = datasets.get(0);
		assertEquals("3.0", parsedDataset.getVersion());
		assertEquals(1, parsedDataset.getApplicableLegislation().size());
		assertFalse(parsedDataset.getInSeries().isEmpty());
		assertEquals("V3 series", parsedDataset.getInSeries().get(0).getTitle().get(0).getValue());

		List<Distribution> distributions = (List<Distribution>) any.eGet(DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION);
		assertFalse(distributions.isEmpty());
		Distribution parsedDistribution = distributions.get(0);
		assertEquals(BigInteger.valueOf(4096), parsedDistribution.getByteSize());
		assertNotNull(parsedDistribution.getChecksum());
		assertEquals("http://spdx.org/rdf/terms#checksumAlgorithm_sha256",
				parsedDistribution.getChecksum().getAlgorithm().getResource());
		assertArrayEquals(new byte[] { 0x0a, 0x0b, 0x0c, 0x0d },
				parsedDistribution.getChecksum().getChecksumValue());
	}

	rdf.Resource createRDFResource(String value) {
		rdf.Resource r = RdfFactory.eINSTANCE.createResource();
		r.setResource(value);
		return r;
	}
	
	PlainLiteral createLiteral(String lang, String value) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang(lang.toUpperCase());
		literal.setValue(value);
		return literal;
	}

}
