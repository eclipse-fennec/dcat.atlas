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
package org.eclipse.fennec.dcat.atlas.msg.body.readerwriter;

import java.util.Collection;
import java.util.List;

import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIx;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import rdf.DateOrDateTimeLiteral;
import rdf.Datatype;
import rdf.IdentifiedResource;
import rdf.PlainLiteral;
import rdf.RdfPackage;
import rdf.TypedLiteral;

/**
 * Turns DCAT-AP EMF objects into an RDF graph.
 * <p>
 * This is the single point where the EMF model becomes RDF. The model itself no
 * longer tries to serialize to valid RDF/XML — it carries the DCAT-AP.de
 * attributes and models relationships the EMF way — so every rule about what
 * legal RDF looks like lives here, and Jena, writing from triples, cannot emit a
 * malformed document. See {@code docs/DCAT-emf-native-plan.md}.
 *
 * <h2>The mapping</h2>
 * <ul>
 * <li><b>Subject</b> — {@link IdentifiedResource#getAbout()} when set, otherwise a
 * blank node. {@code about} is the node's identity, never a triple of its own.</li>
 * <li><b>rdf:type</b> — the {@code ExtendedMetaData} namespace + name of the
 * {@link EClass}.</li>
 * <li><b>Predicate</b> — the {@code ExtendedMetaData} namespace + name of the
 * feature. Those annotations stopped describing XML shape and are now purely the
 * predicate table.</li>
 * <li><b>{@code AnyURI} attribute → IRI node; any other datatype → literal.</b>
 * The EMF datatype is the marker. Getting this backwards emits
 * {@code foaf:mbox "mailto:…"} as a string literal — valid RDF, wrong graph, no
 * error — so tests must assert the object is an IRI, not merely that it parsed.
 * An {@code AnyURI} value that is <em>not</em> an absolute IRI becomes a literal
 * rather than an error; see {@link #anyUriObject}.</li>
 * <li><b>Containment reference</b> — recurse; the child becomes a nested node.</li>
 * <li><b>Non-containment reference</b> — a triple to the target's IRI. The target is
 * <em>not</em> inlined; it is stored and served in its own right.</li>
 * </ul>
 */
public final class EObjectToJena {

	private EObjectToJena() {
	}

	/**
	 * Builds the graph for one entity or a collection of them.
	 *
	 * @param entity a single {@link EObject} or a {@link Collection} of them
	 */
	public static Model toModel(Object entity) {
		Model model = ModelFactory.createDefaultModel();
		toEObjects(entity).forEach(eObject -> emitNode(model, eObject));
		return model;
	}

	@SuppressWarnings("unchecked")
	private static List<EObject> toEObjects(Object entity) {
		if (entity instanceof EObject eObject) {
			return List.of(eObject);
		}
		if (entity instanceof Collection<?> collection) {
			return List.copyOf((Collection<EObject>) collection);
		}
		throw new IllegalArgumentException(
				"Expected an EObject or a collection of them, got " + (entity == null ? "null" : entity.getClass()));
	}

	/** Emits {@code eObject} as a node and returns the resource standing for it. */
	private static Resource emitNode(Model model, EObject eObject) {
		Resource subject = subjectOf(model, eObject);
		Resource type = typeOf(model, eObject.eClass());
		if (type != null) {
			model.add(subject, RDF.type, type);
		}
		for (EStructuralFeature feature : eObject.eClass().getEAllStructuralFeatures()) {
			if (skip(feature) || !eObject.eIsSet(feature)) {
				continue;
			}
			Property predicate = predicateOf(model, feature);
			for (Object value : valuesOf(eObject, feature)) {
				RDFNode object = objectOf(model, eObject, feature, value);
				if (object != null) {
					model.add(subject, predicate, object);
				}
			}
		}
		return subject;
	}

	/**
	 * {@code about} carries the node's identity and is consumed by
	 * {@link #subjectOf}; emitting it again as a predicate would invent an
	 * {@code rdf:about} property that does not exist. Derived and transient features
	 * are computed views, not state.
	 */
	private static boolean skip(EStructuralFeature feature) {
		return feature == RdfPackage.Literals.IDENTIFIED_RESOURCE__ABOUT //
				|| feature.isDerived() || feature.isTransient();
	}

	private static Resource subjectOf(Model model, EObject eObject) {
		String about = aboutOf(eObject);
		return about == null ? model.createResource() : iri(model, about, eObject.eClass().getName() + ".about");
	}

	/**
	 * Turns a model-supplied string into an IRI node, refusing anything that is not a
	 * syntactically valid IRI with a scheme.
	 * <p>
	 * Jena's {@code createResource} validates nothing, so without this a stray space or
	 * a missing scheme becomes a resource that projects into the SPARQL graph, answers
	 * queries, and serves happily through the Turtle, N-Triples and JSON-LD writers —
	 * JSON-LD emits the offending character raw and unescaped — while only the RDF/XML
	 * writer throws, at response time. Failing here turns that into an error at the
	 * boundary the bad value enters, where it is attributable.
	 * <p>
	 * This is the converter's own guard, not the primary defence: rejecting bad data
	 * belongs in OCL at the write boundary (see {@code docs/DCAT-emf-native-plan.md} §5).
	 * It is kept because the admin services are not the only possible callers.
	 */
	/**
	 * The object of an {@code AnyURI} attribute: an IRI node when the value is one, a
	 * plain literal when it is not.
	 *
	 * <h2>Why this does not refuse a value without a scheme</h2>
	 *
	 * RDF lets the same property take an IRI on one class and a literal on another, and
	 * an {@link EAttribute} cannot say so — {@code xsd:anyURI}'s value space is a string,
	 * so both arrive here as one. The profile makes the distinction per class, in the
	 * shapes: {@code dcterms:type} MUSS be an IRI on a {@code dcat:Dataset} and on a
	 * {@code dct:LicenseDocument}, and is unconstrained on a {@code dcat:Catalog} — where
	 * the Jena city portal really does publish {@code dct:type "ckan"}, and validates
	 * clean against DCAT-AP.de 3.0.
	 * <p>
	 * So the serializer is the wrong place to decide. It used to throw, which meant a
	 * value the profile permits could be written and stored and then made every RDF read
	 * of that resource a {@code 500} — XMI reads still worked, so the resource looked
	 * healthy until somebody asked for Turtle. Emitting a literal is instead
	 * <em>lossless</em> and symmetric: {@code JenaToEObject} already reads an IRI node or
	 * a literal into an {@code AnyURI} attribute alike, so the value round-trips either
	 * way. Whether an IRI was <em>required</em> is a question for SHACL, which knows the
	 * class and is already run on write (FR-4).
	 * <p>
	 * A relative IRI is deliberately not emitted: it would resolve against whatever base
	 * the consumer happens to use, inventing an identity nobody wrote.
	 * <p>
	 * This applies to attribute <em>objects</em> only. A subject and a non-containment
	 * link target must still be real IRIs — RDF has no literal subjects, and a literal
	 * where a link belongs severs it silently — so both keep using {@link #iri}.
	 */
	private static RDFNode anyUriObject(Model model, String value) {
		try {
			if (!IRIx.create(value).isRelative()) {
				return model.createResource(value);
			}
		} catch (IRIException e) {
			// Not an IRI at all — then it is a literal, which is what it says it is.
		}
		return model.createLiteral(value);
	}

	private static Resource iri(Model model, String value, String context) {
		IRIx parsed;
		try {
			parsed = IRIx.create(value);
		} catch (IRIException e) {
			throw new IllegalArgumentException(
					"%s is not a valid IRI: <%s> (%s)".formatted(context, value, e.getMessage()), e);
		}
		// Deliberately not isAbsolute(): RFC 3986's "absolute-URI" excludes a fragment,
		// which would reject perfectly good identifiers like <https://govdata.de#catalog>.
		// What RDF actually requires of an IRI node is a scheme.
		if (parsed.isRelative()) {
			throw new IllegalArgumentException(
					"%s must be an absolute IRI with a scheme, got <%s>".formatted(context, value));
		}
		return model.createResource(value);
	}

	/** The IRI identifying {@code eObject}, or {@code null} if it is a blank node. */
	private static String aboutOf(EObject eObject) {
		if (eObject instanceof IdentifiedResource identified) {
			String about = identified.getAbout();
			return about == null || about.isBlank() ? null : about;
		}
		return null;
	}

	private static Resource typeOf(Model model, EClass eClass) {
		String namespace = namespaceOf(ExtendedMetaData.INSTANCE.getNamespace(eClass), eClass.getEPackage().getNsURI());
		String name = ExtendedMetaData.INSTANCE.getName(eClass);
		return name == null || name.isBlank() ? null : model.createResource(namespace + name);
	}

	private static Property predicateOf(Model model, EStructuralFeature feature) {
		String namespace = namespaceOf(ExtendedMetaData.INSTANCE.getNamespace(feature),
				feature.getEContainingClass().getEPackage().getNsURI());
		String name = ExtendedMetaData.INSTANCE.getName(feature);
		return model.createProperty(namespace, name == null || name.isBlank() ? feature.getName() : name);
	}

	private static String namespaceOf(String annotated, String fallback) {
		return annotated == null || annotated.isBlank() ? fallback : annotated;
	}

	@SuppressWarnings("unchecked")
	private static List<Object> valuesOf(EObject eObject, EStructuralFeature feature) {
		Object value = eObject.eGet(feature);
		if (feature.isMany()) {
			return List.copyOf((Collection<Object>) value);
		}
		return value == null ? List.of() : List.of(value);
	}

	private static RDFNode objectOf(Model model, EObject owner, EStructuralFeature feature, Object value) {
		if (feature instanceof EAttribute attribute) {
			return XMLTypePackage.Literals.ANY_URI.equals(attribute.getEAttributeType())
					? anyUriObject(model, String.valueOf(value))
					: model.createLiteral(lexical(attribute.getEAttributeType(), value));
		}
		EObject target = (EObject) value;
		Literal carried = asLiteral(model, target);
		if (carried != null) {
			return carried;
		}
		if (((EReference) feature).isContainment()) {
			return emitNode(model, target);
		}
		// A link to an independently stored resource. Without an IRI there is nothing
		// to point at, and a blank node here would silently sever the link.
		String about = aboutOf(target);
		if (about == null) {
			throw new IllegalStateException(
					"Cannot link %s.%s: the referenced %s has no rdf:about".formatted(owner.eClass().getName(),
							feature.getName(), target.eClass().getName()));
		}
		return iri(model, about, contextOf(owner, feature));
	}

	/** Names the feature a bad IRI came from, so the error says where to look. */
	private static String contextOf(EObject owner, EStructuralFeature feature) {
		return owner.eClass().getName() + "." + feature.getName();
	}

	/**
	 * The model carries a few classes that are RDF <em>literals</em> rather than
	 * nodes: they exist only to pair a lexical value with a language tag or a
	 * datatype. Everything else is a node.
	 *
	 * @return the literal, or {@code null} if {@code eObject} is not a carrier
	 */
	private static Literal asLiteral(Model model, EObject eObject) {
		if (eObject instanceof PlainLiteral plain) {
			String lang = plain.getLang();
			return lang == null || lang.isBlank() ? model.createLiteral(plain.getValue())
					: model.createLiteral(plain.getValue(), lang);
		}
		if (eObject instanceof TypedLiteral typed) {
			String datatype = typed.getDatatype();
			return datatype == null || datatype.isBlank() ? model.createLiteral(typed.getValue())
					: model.createTypedLiteral(typed.getValue(), datatype);
		}
		if (eObject instanceof DateOrDateTimeLiteral dated) {
			String lexical = lexical(RdfPackage.Literals.DATE_OR_DATE_TIME, dated.getValue());
			Datatype datatype = dated.getDatatype();
			return datatype == null ? model.createLiteral(lexical)
					: model.createTypedLiteral(lexical, datatype.getLiteral());
		}
		return null;
	}

	/** The lexical form EMF would write for {@code value}. */
	private static String lexical(EDataType type, Object value) {
		String converted = EcoreUtil.convertToString(type, value);
		return converted == null ? String.valueOf(value) : converted;
	}
}
