/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.dcat.atlas.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.odata.csdl.EcoreToEdmConverter;
import org.eclipse.fennec.odata.csdl.EntityContainers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TComplexType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TProperty;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TVersion;

import adms.AdmsPackage;
import dcat.DcatPackage;
import foaf.FoafPackage;
import rdf.RdfPackage;
import spdx.SpdxPackage;
import terms.TermsPackage;
import vcard.VcardPackage;

/**
 * What the DCAT model looks like once {@code emf.odata} has resolved it into CSDL
 * (AP-DCAT-2, work package 1 of the OData plan).
 * <p>
 * This is a measurement, not a feature: it runs the conversion the {@code ODataServlet}
 * runs — one {@code <Schema>} per registered {@link EPackage}, then one merged
 * {@code <EntityContainer>} — and pins down the four questions the plan could not answer
 * by reading alone: which classes become entities and which become complex types, what
 * the key is, how the XMLType datatypes map, and what happens to the two references typed
 * {@code Ecore#//EObject}.
 * <p>
 * The one thing it deliberately does NOT reproduce is the narrowing: the servlet prunes
 * the schemas to the type closure of the configured entity sets, and that code is
 * internal to the runtime bundle. So this is the <em>widest</em> document the four sets
 * could ever produce, which is the right side to be surprised on.
 */
public class MetadataSpikeTest {

	/** The seven EPackages the model bundle registers — the other ecores are not generated. */
	private static final List<EPackage> MODEL = List.of(DcatPackage.eINSTANCE, RdfPackage.eINSTANCE,
			FoafPackage.eINSTANCE, TermsPackage.eINSTANCE, VcardPackage.eINSTANCE,
			SpdxPackage.eINSTANCE, AdmsPackage.eINSTANCE);

	// --- 1. entity vs complex, and where the key sits -------------------------------

	@Test
	@DisplayName("about on rdf.IdentifiedResource is the one key, and it is the root entity type")
	void theKeyIsAbout() {
		List<SchemaType> schemas = schemas();

		TEntityType identified = entityType(schemas, "rdf", "IdentifiedResource");
		assertNotNull(identified, "rdf.IdentifiedResource must be an entity type");
		assertEquals(1, identified.getKey().size(), "exactly one <Key> element");
		assertEquals(List.of("about"), identified.getKey().get(0).getPropertyRef().stream()
				.map(ref -> String.valueOf(ref.getName())).toList());

		// the key is declared ONCE, on the root: subtypes must not redeclare it
		for (String name : List.of("Catalog", "Dataset", "DatasetSeries", "DataService",
				"Distribution", "CatalogRecord", "Relationship")) {
			TEntityType type = entityType(schemas, "dcat", name);
			assertNotNull(type, "dcat." + name + " must be an entity type");
			assertTrue(type.getKey().isEmpty(), "dcat." + name + " must not redeclare the key");
		}
	}

	@Test
	@DisplayName("the key-less literal classes become complex types, not entities")
	void literalsAreComplexTypes() {
		List<SchemaType> schemas = schemas();

		for (String name : List.of("PlainLiteral", "TypedLiteral", "DateOrDateTimeLiteral")) {
			assertNotNull(complexType(schemas, "rdf", name), "rdf." + name + " must be complex");
			assertNull(entityType(schemas, "rdf", name), "rdf." + name + " must not be an entity");
		}
		// ...while everything deriving from IdentifiedResource is an entity, across packages
		assertNotNull(entityType(schemas, "foaf", "Agent"));
		assertNotNull(entityType(schemas, "terms", "LicenseDocument"));
		assertNotNull(entityType(schemas, "vcard", "Organization"));
		assertNotNull(entityType(schemas, "spdx", "Checksum"));
		assertNotNull(entityType(schemas, "adms", "Identifier"));
	}

	// --- 2. navigation shape --------------------------------------------------------

	@Test
	@DisplayName("distribution is a contained navigation, title a complex-typed property")
	void containmentBecomesContainsTarget() {
		List<SchemaType> schemas = schemas();
		TEntityType dataset = entityType(schemas, "dcat", "Dataset");

		TNavigationProperty distribution = navigation(schemas, dataset, "distribution");
		assertNotNull(distribution, "Dataset.distribution must be a navigation property");
		assertEquals("Collection(dcat.Distribution)", String.valueOf(distribution.getType()));
		assertTrue(distribution.isContainsTarget(), "containment maps to ContainsTarget");

		// a reference to a key-less class is NOT a navigation at all — it is a property
		assertNull(navigation(schemas, dataset, "title"), "title targets a complex type");
		TProperty title = property(schemas, dataset, "title");
		assertNotNull(title, "Dataset.title must be a property");
		assertEquals("Collection(rdf.PlainLiteral)", String.valueOf(title.getType()));
	}

	@Test
	@DisplayName("primaryTopic targets the abstract DcatResource and stays unbound")
	void unboundNavigationIsTolerated() {
		List<SchemaType> schemas = schemas();
		TEntityContainer container = EntityContainers.merge(schemas);
		assertNotNull(container, "the merged service container");

		TEntitySet records = container.getEntitySet().stream()
				.filter(set -> "CatalogRecord".equals(set.getName())).findFirst().orElse(null);
		assertNotNull(records, "an unnarrowed container has a set per concrete type");
		assertFalse(records.getNavigationPropertyBinding().stream()
				.anyMatch(binding -> "primaryTopic".equals(String.valueOf(binding.getPath()))),
				"DcatResource is abstract and has no set, so primaryTopic cannot be bound");
	}

	// --- 3. datatypes ---------------------------------------------------------------

	@Test
	@DisplayName("numeric XMLType datatypes keep a numeric OData type")
	void xmlTypesMapToComparableTypes() {
		List<SchemaType> schemas = schemas();
		TEntityType dataset = entityType(schemas, "dcat", "Dataset");
		TEntityType distribution = entityType(schemas, "dcat", "Distribution");

		// AnyURI as a string is fine - an IRI IS a string
		assertEquals("Collection(Edm.String)", String.valueOf(property(schemas, dataset, "theme").getType()));
		assertEquals("Edm.String", String.valueOf(property(schemas, dataset, "accrualPeriodicity").getType()));

		// These three used to widen to Edm.String, which made them compare
		// lexicographically: `byteSize gt 1000` ranked '999' above '1000'. The resolver
		// maps them to their own types now, so the comparison is numeric.
		assertEquals("Edm.Decimal",
				String.valueOf(property(schemas, distribution, "byteSize").getType()), "NonNegativeInteger");
		assertEquals("Edm.Decimal",
				String.valueOf(property(schemas, dataset, "spatialResolutionInMeters").getType()), "Decimal");
		assertEquals("Edm.Duration",
				String.valueOf(property(schemas, dataset, "temporalResolution").getType()), "Duration");
	}

	// --- 4. no property may name a type the document does not declare ------------------

	/**
	 * {@code compressFormat} and {@code hadRole} used to be {@code Ecore#//EObject} containment
	 * references. {@code EObject} carries no key, so the resolver treated it as a complex type
	 * and emitted {@code <Property Type="ecore.EObject"/>} — a property whose type no schema
	 * declares, because {@code ServiceModel.closure} skips types from unpublished packages.
	 * Both are IRIs in DCAT-AP.de (§4.6 {@code dcterms:MediaType}, §4.14.1 {@code dcat:Role})
	 * and are {@code AnyURI} attributes now, like {@code packageFormat} beside them.
	 * <p>
	 * Goes green once the model bundle has been regenerated from the corrected ecore.
	 */
	@Test
	@DisplayName("every property names a type the document declares")
	void noPropertyNamesAnUndeclaredType() {
		List<SchemaType> schemas = schemas();

		String declared = schemas.stream().map(schema -> String.valueOf(schema.getNamespace()))
				.sorted().toList().toString();
		assertFalse(declared.contains("ecore"), "no ecore schema is emitted: " + declared);

		assertEquals("Edm.String", String.valueOf(property(schemas,
				entityType(schemas, "dcat", "Distribution"), "compressFormat").getType()));
		assertEquals("Collection(Edm.String)", String.valueOf(property(schemas,
				entityType(schemas, "dcat", "Relationship"), "hadRole").getType()));

		// dct:relation was missing from dcat.Relationship entirely, though DCAT-AP.de §4.14.2
		// makes it mandatory - so every qualifiedRelation the portal emitted was SHACL-invalid
		assertEquals("Collection(Edm.String)", String.valueOf(property(schemas,
				entityType(schemas, "dcat", "Relationship"), "relation").getType()));
	}

	// --- 5. what the document got wrong on its own ------------------------------------

	@Test
	@DisplayName("the key property is not nullable, as CSDL requires")
	void theKeyIsNotNullable() {
		List<SchemaType> schemas = schemas();
		TProperty about = property(schemas, entityType(schemas, "rdf", "IdentifiedResource"), "about");

		// OData requires a key property to be non-nullable. The resolver used to derive
		// Nullable from lowerBound == 0 alone, and `about` is optional in the ecore because
		// a POST with no about mints one - so the key came out nullable and the model could
		// not fix it by raising its lower bound. The key is special-cased upstream now.
		assertEquals("false", String.valueOf(about.isNullable()));
	}

	@Test
	@DisplayName("managedBy is part of the published surface")
	void internalMachineryIsPublished() {
		List<SchemaType> schemas = schemas();

		// DCAT.Atlas machinery rather than DCAT-AP, and it reaches $metadata like any other
		// attribute. REST publishes it too, so this is a note, not a defect.
		assertNotNull(property(schemas, entityType(schemas, "rdf", "IdentifiedResource"), "managedBy"));
	}

	@Test
	@DisplayName("foaf.Organization and vcard.Organization collide on the set name")
	void setNamesCollideAcrossPackages() {
		TEntityContainer container = EntityContainers.merge(schemas());

		// One container per service (CSDL 13 MUST), sets named after their type by default -
		// so two same-named types in different packages want the same set, and the second is
		// dropped with a warning. Moot for the four sets this service publishes, but it is why
		// a fifth set has to be named explicitly.
		assertEquals(1, container.getEntitySet().stream()
				.filter(set -> "Organization".equals(set.getName())).count());
		assertEquals("foaf.Organization", container.getEntitySet().stream()
				.filter(set -> "Organization".equals(set.getName())).findFirst()
				.map(set -> String.valueOf(set.getEntityType())).orElse(null));
	}

	// --- the document itself, for reading ---------------------------------------------

	@Test
	@DisplayName("the whole document serializes, and is written out for inspection")
	void metadataSerializes() throws IOException {
		String xml = metadataXml();

		assertTrue(xml.startsWith("<?xml"), "a serialized document");
		assertTrue(xml.contains("<edmx:DataServices>"), "with the data services element");

		Path out = Path.of(System.getProperty("java.io.tmpdir"), "dcat-odata-metadata.xml");
		Files.writeString(out, xml, StandardCharsets.UTF_8);
		System.out.println("$metadata written to " + out.toAbsolutePath());
		System.out.println("schemas: " + schemas().stream()
				.map(schema -> schema.getNamespace() + " (" + schema.getEntityType().size()
						+ " entity, " + schema.getComplexType().size() + " complex)")
				.toList());
	}

	// --- the conversion the servlet runs ------------------------------------------------

	private static List<SchemaType> schemas() {
		EcoreToEdmConverter converter = new EcoreToEdmConverter();
		Map<String, String> setNames = new HashMap<>();
		MODEL.forEach(pkg -> setNames.putAll(EcoreToEdmConverter.entitySetNames(pkg)));
		List<SchemaType> schemas = new ArrayList<>();
		for (EPackage pkg : MODEL) {
			SchemaType schema = converter.toSchema(pkg);
			converter.applyEntitySetNames(setNames, schema);
			schemas.add(schema);
		}
		return schemas;
	}

	private static String metadataXml() throws IOException {
		List<SchemaType> schemas = schemas();
		TDataServices dataServices = EdmxFactory.eINSTANCE.createTDataServices();
		dataServices.getSchema().addAll(schemas);
		EntityContainers.merge(dataServices.getSchema());
		TEdmx edmx = EdmxFactory.eINSTANCE.createTEdmx();
		edmx.setVersion(TVersion._40);
		edmx.setDataServices(dataServices);
		EdmxRoot root = EdmxFactory.eINSTANCE.createEdmxRoot();
		root.setEdmx(edmx);

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
		rs.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
		rs.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);
		Resource resource = rs.createResource(URI.createURI("metadata.xml"));
		resource.getContents().add(root);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, Map.of(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE,
				XMLResource.OPTION_ENCODING, "UTF-8"));
		return out.toString(StandardCharsets.UTF_8);
	}

	// --- lookups -------------------------------------------------------------------------

	private static TEntityType entityType(List<SchemaType> schemas, String namespace, String name) {
		return schema(schemas, namespace).flatMap(schema -> schema.getEntityType().stream()
				.filter(type -> name.equals(type.getName())).findFirst()).orElse(null);
	}

	private static TComplexType complexType(List<SchemaType> schemas, String namespace, String name) {
		return schema(schemas, namespace).flatMap(schema -> schema.getComplexType().stream()
				.filter(type -> name.equals(type.getName())).findFirst()).orElse(null);
	}

	private static Optional<SchemaType> schema(List<SchemaType> schemas, String namespace) {
		return schemas.stream().filter(s -> namespace.equals(s.getNamespace())).findFirst();
	}

	/**
	 * A navigation property of the type <em>or of a base type</em> — CSDL declares each member
	 * once, on the type that introduces it, and a client resolves it along {@code BaseType}.
	 */
	private static TNavigationProperty navigation(List<SchemaType> schemas, TEntityType type,
			String name) {
		for (TEntityType current = type; current != null; current = baseType(schemas, current)) {
			TNavigationProperty found = current.getNavigationProperty().stream()
					.filter(navigation -> name.equals(navigation.getName())).findFirst().orElse(null);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/** As {@link #navigation}, for a structural property. */
	private static TProperty property(List<SchemaType> schemas, TEntityType type, String name) {
		for (TEntityType current = type; current != null; current = baseType(schemas, current)) {
			TProperty found = current.getProperty().stream()
					.filter(property -> name.equals(property.getName())).findFirst().orElse(null);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static TEntityType baseType(List<SchemaType> schemas, TEntityType type) {
		if (type.getBaseType() == null) {
			return null;
		}
		String qualified = String.valueOf(type.getBaseType());
		int dot = qualified.lastIndexOf('.');
		return dot < 0 ? null
				: entityType(schemas, qualified.substring(0, dot), qualified.substring(dot + 1));
	}

	private static void assertNull(Object value, String message) {
		assertTrue(value == null, message);
	}
}
