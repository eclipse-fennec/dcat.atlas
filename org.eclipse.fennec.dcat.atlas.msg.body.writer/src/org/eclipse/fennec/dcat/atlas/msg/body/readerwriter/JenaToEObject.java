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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import rdf.DateOrDateTimeLiteral;
import rdf.Datatype;
import rdf.IdentifiedResource;
import rdf.PlainLiteral;
import rdf.RdfFactory;
import rdf.RdfPackage;
import rdf.TypedLiteral;

/**
 * Turns an RDF graph back into DCAT-AP EMF objects — the inverse of
 * {@link EObjectToJena}.
 * <p>
 * Not needed in production: the admin endpoints accept XMI, and harvesting of
 * foreign catalogues is out of scope. It exists so the forward direction can be
 * tested honestly — write, read back, write again, and compare the two graphs with
 * {@code isIsomorphicWith}. It also makes the committed DCAT-AP.de reference
 * documents usable as a conformance corpus.
 *
 * <h2>What is a root</h2>
 * A subject is returned as a top-level entity only if the graph <em>describes</em>
 * it — that is, it carries an {@code rdf:type} this model knows. A bare link target
 * ({@code <catalog> dcat:dataset <d1>} with nothing said about {@code d1}) becomes a
 * stub carrying only {@code about}, wired into the reference but not returned as a
 * root. That asymmetry is what keeps the round trip isomorphic: the forward
 * direction does not describe link targets either.
 *
 * <h2>Limits</h2>
 * Deliberately narrow. No RDF collections or containers, no reification, one type
 * per subject, and blank-node cycles are not detected.
 */
public final class JenaToEObject {

	private final Map<String, EClass> byTypeIri = new HashMap<>();
	private final Map<EClass, Map<String, EStructuralFeature>> byPredicate = new HashMap<>();

	private JenaToEObject(Collection<EPackage> packages) {
		packages.forEach(this::index);
	}

	/**
	 * Builds a converter over the given metamodel. The index is derived from the same
	 * {@code ExtendedMetaData} annotations the forward direction uses, so the two
	 * cannot drift apart.
	 */
	public static JenaToEObject over(EPackage... packages) {
		return new JenaToEObject(List.of(packages));
	}

	private void index(EPackage ePackage) {
		for (EClassifier classifier : ePackage.getEClassifiers()) {
			if (!(classifier instanceof EClass eClass) || eClass.isAbstract()) {
				continue;
			}
			String iri = iriOf(ExtendedMetaData.INSTANCE.getNamespace(eClass), ePackage.getNsURI(),
					ExtendedMetaData.INSTANCE.getName(eClass));
			if (iri != null) {
				byTypeIri.putIfAbsent(iri, eClass);
			}
			Map<String, EStructuralFeature> features = new LinkedHashMap<>();
			for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
				if (feature == RdfPackage.Literals.IDENTIFIED_RESOURCE__ABOUT || feature.isDerived()
						|| feature.isTransient()) {
					continue;
				}
				String predicate = iriOf(ExtendedMetaData.INSTANCE.getNamespace(feature),
						feature.getEContainingClass().getEPackage().getNsURI(),
						nameOf(feature));
				if (predicate != null) {
					features.putIfAbsent(predicate, feature);
				}
			}
			byPredicate.put(eClass, features);
		}
	}

	private static String nameOf(EStructuralFeature feature) {
		String name = ExtendedMetaData.INSTANCE.getName(feature);
		return name == null || name.isBlank() ? feature.getName() : name;
	}

	private static String iriOf(String annotated, String fallback, String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		String namespace = annotated == null || annotated.isBlank() ? fallback : annotated;
		return namespace + name;
	}

	/** Every entity the graph describes, in the order their subjects appear. */
	public List<EObject> parse(Model model) {
		Map<Resource, EObject> described = new LinkedHashMap<>();
		model.listStatements(null, RDF.type, (RDFNode) null).forEachRemaining(statement -> {
			if (!statement.getObject().isURIResource()) {
				return;
			}
			EClass eClass = byTypeIri.get(statement.getObject().asResource().getURI());
			if (eClass != null) {
				described.computeIfAbsent(statement.getSubject(), s -> create(eClass, s));
			}
		});
		// Wire only what the graph described; children get contained during wiring and
		// so drop out of the root list.
		List.copyOf(described.keySet()).forEach(subject -> wire(model, subject, described.get(subject), described));
		return described.values().stream().filter(eObject -> eObject.eContainer() == null).toList();
	}

	private EObject create(EClass eClass, Resource subject) {
		EObject eObject = EcoreUtil.create(eClass);
		if (eObject instanceof IdentifiedResource identified && subject.isURIResource()) {
			identified.setAbout(subject.getURI());
		}
		return eObject;
	}

	private void wire(Model model, Resource subject, EObject owner, Map<Resource, EObject> described) {
		Map<String, EStructuralFeature> features = byPredicate.getOrDefault(owner.eClass(), Map.of());
		for (Statement statement : model.listStatements(subject, null, (RDFNode) null).toList()) {
			if (RDF.type.equals(statement.getPredicate())) {
				continue;
			}
			EStructuralFeature feature = features.get(statement.getPredicate().getURI());
			if (feature == null) {
				continue; // a predicate this model does not carry
			}
			Object value = valueFor(model, feature, statement.getObject(), described);
			if (value == null) {
				continue;
			}
			if (feature.isMany()) {
				@SuppressWarnings("unchecked")
				Collection<Object> many = (Collection<Object>) owner.eGet(feature);
				many.add(value);
			} else {
				owner.eSet(feature, value);
			}
		}
	}

	private Object valueFor(Model model, EStructuralFeature feature, RDFNode object,
			Map<Resource, EObject> described) {
		if (feature instanceof EAttribute attribute) {
			if (XMLTypePackage.Literals.ANY_URI.equals(attribute.getEAttributeType())) {
				return object.isURIResource() ? object.asResource().getURI() : object.asLiteral().getLexicalForm();
			}
			return object.isLiteral()
					? EcoreUtil.createFromString(attribute.getEAttributeType(), object.asLiteral().getLexicalForm())
					: null;
		}
		EReference reference = (EReference) feature;
		EObject carrier = asLiteralCarrier(reference.getEReferenceType(), object);
		if (carrier != null) {
			return carrier;
		}
		if (!object.isResource()) {
			return null;
		}
		Resource target = object.asResource();
		if (reference.isContainment()) {
			// Described children were created up front; undescribed blank nodes are
			// materialised here from the feature's declared type.
			EObject child = described.get(target);
			if (child == null) {
				child = create(reference.getEReferenceType(), target);
				described.put(target, child);
				wire(model, target, child, described);
			}
			return child;
		}
		EObject existing = described.get(target);
		if (existing != null) {
			return existing;
		}
		if (!target.isURIResource()) {
			return null;
		}
		// A link to something this document does not describe: a stub carrying only the
		// IRI, which is exactly what the forward direction emits for such a link.
		EObject stub = create(reference.getEReferenceType(), target);
		return stub;
	}

	/**
	 * The inverse of {@link EObjectToJena}'s literal carriers: an RDF literal landing
	 * in a slot typed {@code PlainLiteral} / {@code TypedLiteral} /
	 * {@code DateOrDateTimeLiteral} is wrapped rather than treated as a node.
	 */
	private EObject asLiteralCarrier(EClass eType, RDFNode object) {
		if (!object.isLiteral()) {
			return null;
		}
		Literal literal = object.asLiteral();
		if (RdfPackage.Literals.PLAIN_LITERAL.isSuperTypeOf(eType)) {
			PlainLiteral plain = RdfFactory.eINSTANCE.createPlainLiteral();
			plain.setValue(literal.getLexicalForm());
			if (!literal.getLanguage().isBlank()) {
				plain.setLang(literal.getLanguage());
			}
			return plain;
		}
		if (RdfPackage.Literals.TYPED_LITERAL.isSuperTypeOf(eType)) {
			TypedLiteral typed = RdfFactory.eINSTANCE.createTypedLiteral();
			typed.setValue(literal.getLexicalForm());
			typed.setDatatype(literal.getDatatypeURI());
			return typed;
		}
		if (RdfPackage.Literals.DATE_OR_DATE_TIME_LITERAL.isSuperTypeOf(eType)) {
			DateOrDateTimeLiteral dated = RdfFactory.eINSTANCE.createDateOrDateTimeLiteral();
			dated.setValue((XMLGregorianCalendar) EcoreUtil
					.createFromString(RdfPackage.Literals.DATE_OR_DATE_TIME, literal.getLexicalForm()));
			Datatype datatype = Datatype.get(literal.getDatatypeURI());
			if (datatype != null) {
				dated.setDatatype(datatype);
			}
			return dated;
		}
		return null;
	}

}
