package org.eclipse.fennec.dcat.atlas.msg.body.readerwriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;
import org.eclipse.emf.ecore.xmi.XMLResource;

import dcat.DcatPackage;
import rdf.RDFRoot;
import rdf.RdfFactory;

/**
 * Bridges DCAT-AP EMF model objects and RDF, in both directions.
 * <p>
 * The DCAT-AP model is generated from an RDF/XML-compatible XSD, so its own EMF
 * serialization (a {@link XMLResource} with extended meta data) already reads and
 * writes spec-correct RDF/XML with the real vocabulary URIs, {@code rdf:about},
 * {@code xml:lang} and datatypes. We therefore:
 * <ul>
 * <li>write <b>RDF/XML</b> straight from EMF ({@link #writeRdfXml}) and read it
 * back into model objects ({@link #parse});</li>
 * <li>for the other syntaxes, let EMF produce RDF/XML and hand it to Apache Jena,
 * which re-serializes the {@link Model} as Turtle/JSON-LD/N3 ({@link #toModel}).</li>
 * </ul>
 * Objects are wrapped in an {@link RDFRoot} so EMF emits a proper {@code <rdf:RDF>}
 * document element (also required to carry more than one root object in a single
 * valid RDF/XML document).
 */
public final class EObjectRDFModelBuilder {

	private EObjectRDFModelBuilder() {
	}

	/**
	 * URI whose {@code .rdf} extension selects the DCAT resource factory (which
	 * sets the extended-meta-data / RDF/XML options).
	 */
	private static final URI RDF_URI = URI.createURI("dcatatlas.rdf");

	private static final Map<Object, Object> SAVE_OPTIONS = Map.of(//
			XMLResource.OPTION_ENCODING, "UTF-8", //
			XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE, //
			XMLResource.OPTION_SCHEMA_LOCATION, Boolean.FALSE);

	private static final Map<Object, Object> LOAD_OPTIONS = Map.of(//
			XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);

	/**
	 * Serializes the entity as RDF/XML directly through EMF (no Jena).
	 *
	 * @param entity      a single {@link EObject} or a {@link Collection} of them
	 * @param resourceSet a fresh resource set that knows the DCAT-AP packages
	 * @param out         the target stream
	 */
	public static void writeRdfXml(Object entity, ResourceSet resourceSet, OutputStream out) {
		Resource emfResource = assemble(entity, resourceSet);
		try {
			emfResource.save(out, SAVE_OPTIONS);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not serialize DCAT-AP model to RDF/XML", e);
		}
	}

	/**
	 * Reads an RDF/XML document into DCAT-AP model objects.
	 *
	 * @return every DCAT entity found at the document root (a {@code <rdf:RDF>}
	 *         holding {@code dcat:Catalog}, {@code dcat:Dataset}, &hellip;)
	 */
	public static List<EObject> parse(InputStream in, ResourceSet resourceSet) {
		Resource emfResource = resourceSet.createResource(RDF_URI);
		try {
			emfResource.load(in, LOAD_OPTIONS);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read RDF/XML document", e);
		}
		List<EObject> result = new ArrayList<>();
		emfResource.getContents().forEach(root -> collect(root, result));
		return result;
	}

	/**
	 * Builds a Jena model for the given entity (used by the Turtle/JSON-LD/N3
	 * writers). EMF renders RDF/XML, Jena parses it.
	 */
	public static Model toModel(Object entity, ResourceSet resourceSet) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		writeRdfXml(entity, resourceSet, buffer);

		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, new ByteArrayInputStream(buffer.toByteArray()), Lang.RDFXML);
		return model;
	}

	/**
	 * Assembles the {@code <rdf:RDF>} document: entities are grouped by the DCAT-AP
	 * document-root feature that fits their type (so a list of datasets ends up as
	 * sibling {@code dcat:Dataset} elements) and attached to an {@link AnyType}
	 * carried by an {@link RDFRoot}.
	 */
	private static Resource assemble(Object entity, ResourceSet resourceSet) {
		Resource emfResource = resourceSet.createResource(RDF_URI);
		RDFRoot rdfRoot = RdfFactory.eINSTANCE.createRDFRoot();
		emfResource.getContents().add(rdfRoot);
		AnyType anyType = XMLTypeFactory.eINSTANCE.createAnyType();
		rdfRoot.getRDF().add(anyType);

		Map<EReference, List<EObject>> byFeature = new LinkedHashMap<>();
		for (EObject eObject : toEObjects(entity)) {
			EReference feature = rootFeatureFor(eObject.eClass());
			// Copy to avoid detaching the object from any container it may still
			// belong to (the entity is owned by the service that produced it).
			byFeature.computeIfAbsent(feature, f -> new ArrayList<>()).add(EcoreUtil.copy(eObject));
		}
		byFeature.forEach((feature, objects) -> anyType.eSet(feature, new BasicEList<>(objects)));
		return emfResource;
	}

	/** Collects DCAT entities from a loaded document root (an {@link RDFRoot}/{@link AnyType} tree). */
	private static void collect(EObject root, List<EObject> out) {
		if (root instanceof RDFRoot rdfRoot) {
			rdfRoot.getRDF().forEach(any -> collectEntities(any, out));
		} else if (root instanceof AnyType anyType) {
			collectEntities(anyType, out);
		} else {
			// Already a typed model object at the resource root.
			out.add(root);
		}
	}

	private static void collectEntities(AnyType anyType, List<EObject> out) {
		for (EReference reference : DcatPackage.eINSTANCE.getDCATAPRoot().getEAllReferences()) {
			// Only the entity slots live in the DCAT package; xmlns/schemaLocation maps do not.
			if (!reference.isContainment() || reference.getEReferenceType().getEPackage() != DcatPackage.eINSTANCE) {
				continue;
			}
			Object value = anyType.eGet(reference);
			if (value instanceof Collection<?> collection) {
				collection.forEach(v -> out.add((EObject) v));
			} else if (value instanceof EObject eObject) {
				out.add(eObject);
			}
		}
	}

	private static List<EObject> toEObjects(Object entity) {
		List<EObject> result = new ArrayList<>();
		if (entity instanceof EObject eObject) {
			result.add(eObject);
		} else if (entity instanceof Collection<?> collection) {
			for (Object element : collection) {
				if (element instanceof EObject eObject) {
					result.add(eObject);
				}
			}
		}
		if (result.isEmpty()) {
			throw new IllegalArgumentException("No DCAT-AP model content to serialize as RDF");
		}
		return result;
	}

	/**
	 * Finds the DCAT-AP document-root ({@code DCATAPRoot}) containment feature that
	 * can hold an instance of the given class, preferring an exact type match (e.g.
	 * a {@code Catalog} goes into the {@code Catalog} feature and not into the
	 * inherited {@code Dataset} one).
	 */
	private static EReference rootFeatureFor(EClass eClass) {
		EClass rootClass = DcatPackage.eINSTANCE.getDCATAPRoot();
		EReference assignable = null;
		for (EReference reference : rootClass.getEAllReferences()) {
			if (!reference.isContainment()) {
				continue;
			}
			EClass target = reference.getEReferenceType();
			if (target == eClass) {
				return reference;
			}
			if (assignable == null && target.isSuperTypeOf(eClass)) {
				assignable = reference;
			}
		}
		if (assignable != null) {
			return assignable;
		}
		throw new IllegalArgumentException(
				"Type '" + eClass.getName() + "' cannot be serialized at the DCAT-AP document root");
	}
}
