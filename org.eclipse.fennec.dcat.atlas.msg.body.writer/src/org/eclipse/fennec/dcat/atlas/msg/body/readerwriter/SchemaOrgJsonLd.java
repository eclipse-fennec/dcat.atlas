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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

/**
 * Projects a DCAT-AP subject onto schema.org, as the JSON-LD block an HTML resource
 * page carries in its {@code <head>} (N25).
 *
 * <h2>Why a subset, when the page itself is complete</h2>
 *
 * {@link RdfHtmlRenderer} shows every triple; this deliberately does not. Its reader
 * is a crawler — Google Dataset Search and similar — which understands schema.org and
 * nothing else, so a term is emitted only where the DCAT-AP property really does mean
 * what the schema.org property means. Inventing a mapping for the rest would publish
 * claims the model never made, and the full graph is one {@code Accept} header away in
 * any of the RDF syntaxes. So this file grows only when a mapping is genuinely right,
 * never for coverage's sake.
 *
 * <h2>Blank nodes are skipped, not labelled</h2>
 *
 * A contained node without an {@code rdf:about} has no identity outside this graph.
 * Emitting its Jena label ({@code _:b0}) would give a crawler an identifier that means
 * nothing and changes on every request, so a value that is neither a literal nor an IRI
 * is left out.
 *
 * <h2>Script-context hardening</h2>
 *
 * The output is embedded in {@code <script type="application/ld+json">}, where a
 * literal {@code </script>} inside a JSON string would end the element and turn stored
 * data into markup. {@link #jsonString} therefore escapes {@code <}, {@code >} and
 * {@code &} to their {@code \\u} forms, which is valid JSON and leaves no character
 * sequence that can close the element.
 */
final class SchemaOrgJsonLd {

	static final String DCTERMS = "http://purl.org/dc/terms/";
	static final String DCAT = "http://www.w3.org/ns/dcat#";
	private static final String FOAF = "http://xmlns.com/foaf/0.1/";

	/** DCAT-AP class to its schema.org counterpart. A type absent here yields no block. */
	private static final Map<String, String> TYPES = Map.of( //
			DCAT + "Dataset", "Dataset", //
			DCAT + "DatasetSeries", "Dataset", //
			DCAT + "Catalog", "DataCatalog", //
			DCAT + "DataService", "WebAPI", //
			DCAT + "Distribution", "DataDownload");

	/**
	 * Properties whose object carries over as a plain string — a literal's lexical form
	 * or an IRI. Iterated in order, so the output is stable; two DCAT properties may map
	 * onto one schema.org term, in which case the values merge.
	 */
	private static final Map<String, String> PLAIN = plainProperties();

	/**
	 * Properties whose object is an agent, rendered as a nested {@code Organization}.
	 * Order-preserving for the same reason as {@link #PLAIN}.
	 */
	private static final Map<String, String> AGENTS = agentProperties();

	private SchemaOrgJsonLd() {
	}

	/**
	 * The JSON-LD for {@code subject}, or {@code null} when its {@code rdf:type} has no
	 * schema.org counterpart — the caller then omits the block entirely.
	 */
	static String of(Model model, Resource subject) {
		String schemaType = schemaType(model, subject);
		if (schemaType == null) {
			return null;
		}

		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("@context", jsonString("https://schema.org"));
		fields.put("@type", jsonString(schemaType));
		if (subject.isURIResource()) {
			fields.put("@id", jsonString(subject.getURI()));
		}

		Map<String, List<String>> plain = new LinkedHashMap<>();
		PLAIN.forEach((iri, term) -> {
			List<String> values = plainValues(model, subject, iri);
			if (!values.isEmpty()) {
				plain.computeIfAbsent(term, key -> new ArrayList<>()).addAll(values);
			}
		});
		plain.forEach((term, values) -> fields.put(term, jsonStrings(values.stream().distinct().toList())));

		AGENTS.forEach((iri, term) -> {
			List<String> agents = agents(model, subject, iri);
			if (!agents.isEmpty()) {
				fields.put(term, jsonList(agents));
			}
		});

		List<String> distributions = distributions(model, subject);
		if (!distributions.isEmpty()) {
			fields.put("distribution", jsonList(distributions));
		}

		List<String> datasets = references(model, subject, DCAT + "dataset");
		if (!datasets.isEmpty()) {
			fields.put("dataset", jsonList(datasets));
		}

		return jsonObject(fields, true);
	}

	/** The mapped schema.org type of {@code subject}, deterministic when it has several. */
	private static String schemaType(Model model, Resource subject) {
		return model.listObjectsOfProperty(subject, RDF.type).toList().stream() //
				.filter(RDFNode::isURIResource) //
				.map(node -> node.asResource().getURI()) //
				.sorted() //
				.map(TYPES::get) //
				.filter(Objects::nonNull) //
				.findFirst() //
				.orElse(null);
	}

	/** The objects of {@code predicateIri} as strings, sorted; blank nodes dropped. */
	private static List<String> plainValues(Model model, Resource subject, String predicateIri) {
		return model.listObjectsOfProperty(subject, property(model, predicateIri)).toList().stream() //
				.map(SchemaOrgJsonLd::plainValue) //
				.filter(Objects::nonNull) //
				.sorted() //
				.toList();
	}

	private static String plainValue(RDFNode node) {
		if (node.isLiteral()) {
			return node.asLiteral().getLexicalForm();
		}
		return node.isURIResource() ? node.asResource().getURI() : null;
	}

	/**
	 * Agents as nested {@code Organization} objects. {@code foaf:Agent} is the model's
	 * default for a publisher, and schema.org has no matching abstraction — a named
	 * publisher of open data is an organisation in every case the profile cares about.
	 */
	private static List<String> agents(Model model, Resource subject, String predicateIri) {
		List<String> agents = new ArrayList<>();
		for (RDFNode node : model.listObjectsOfProperty(subject, property(model, predicateIri)).toList()) {
			if (!node.isResource()) {
				continue;
			}
			Resource agent = node.asResource();
			Map<String, String> fields = new LinkedHashMap<>();
			fields.put("@type", jsonString("Organization"));
			if (agent.isURIResource()) {
				fields.put("@id", jsonString(agent.getURI()));
			}
			List<String> names = plainValues(model, agent, FOAF + "name");
			if (!names.isEmpty()) {
				fields.put("name", jsonStrings(names));
			}
			agents.add(jsonObject(fields, false));
		}
		agents.sort(null);
		return agents;
	}

	/**
	 * Distributions as nested {@code DataDownload}s. {@code dcat:downloadURL} and
	 * {@code dcat:accessURL} both become {@code contentUrl}: schema.org has one term for
	 * "where the bytes are", and DCAT-AP allows a distribution to carry either.
	 */
	private static List<String> distributions(Model model, Resource subject) {
		List<String> distributions = new ArrayList<>();
		for (RDFNode node : model.listObjectsOfProperty(subject, property(model, DCAT + "distribution")).toList()) {
			if (!node.isResource()) {
				continue;
			}
			Resource distribution = node.asResource();
			Map<String, String> fields = new LinkedHashMap<>();
			fields.put("@type", jsonString("DataDownload"));
			if (distribution.isURIResource()) {
				fields.put("@id", jsonString(distribution.getURI()));
			}
			putIfPresent(fields, "name", plainValues(model, distribution, DCTERMS + "title"));
			putIfPresent(fields, "encodingFormat", merged(model, distribution, DCTERMS + "format", DCAT + "mediaType"));
			putIfPresent(fields, "contentUrl", merged(model, distribution, DCAT + "downloadURL", DCAT + "accessURL"));
			putIfPresent(fields, "contentSize", plainValues(model, distribution, DCAT + "byteSize"));
			distributions.add(jsonObject(fields, false));
		}
		distributions.sort(null);
		return distributions;
	}

	/** Objects of {@code predicateIri} as bare {@code @id} references. */
	private static List<String> references(Model model, Resource subject, String predicateIri) {
		return model.listObjectsOfProperty(subject, property(model, predicateIri)).toList().stream() //
				.filter(RDFNode::isURIResource) //
				.map(node -> node.asResource().getURI()) //
				.sorted() //
				.map(iri -> jsonObject(Map.of("@id", jsonString(iri)), false)) //
				.toList();
	}

	private static List<String> merged(Model model, Resource subject, String first, String second) {
		List<String> values = new ArrayList<>(plainValues(model, subject, first));
		plainValues(model, subject, second).stream().filter(value -> !values.contains(value)).forEach(values::add);
		return values;
	}

	private static void putIfPresent(Map<String, String> fields, String term, List<String> values) {
		if (!values.isEmpty()) {
			fields.put(term, jsonStrings(values));
		}
	}

	private static Property property(Model model, String iri) {
		return model.createProperty(iri);
	}

	// --- JSON ------------------------------------------------------------
	//
	// Hand-written rather than pulled from a JSON library: the issue asks for the page to
	// stay dependency-free, the shape here is a flat object of strings and small nested
	// objects, and the escaping rule below is stricter than a general-purpose writer's
	// because the result is embedded in a <script> element.

	/** One value, or a JSON array when there are several. */
	private static String jsonStrings(List<String> values) {
		if (values.size() == 1) {
			return jsonString(values.get(0));
		}
		return values.stream().map(SchemaOrgJsonLd::jsonString).reduce((left, right) -> left + ", " + right)
				.map(joined -> "[" + joined + "]").orElse("[]");
	}

	/** One pre-serialized fragment, or a JSON array when there are several. */
	private static String jsonList(List<String> fragments) {
		if (fragments.size() == 1) {
			return fragments.get(0);
		}
		return "[" + String.join(", ", fragments) + "]";
	}

	/**
	 * @param topLevel whether to put each member on its own line — the outer object is
	 *                 read in view-source, nested ones are small enough to stay inline
	 */
	private static String jsonObject(Map<String, String> fields, boolean topLevel) {
		String separator = topLevel ? ",\n  " : ", ";
		String members = fields.entrySet().stream() //
				.map(field -> jsonString(field.getKey()) + ": " + field.getValue()) //
				.reduce((left, right) -> left + separator + right) //
				.orElse("");
		return topLevel ? "{\n  " + members + "\n}" : "{" + members + "}";
	}

	/**
	 * A JSON string literal. Beyond RFC 8259's requirements this also escapes
	 * {@code <}, {@code >} and {@code &}, so that no stored value can close the
	 * surrounding {@code <script>} element or open a comment inside it.
	 */
	static String jsonString(String value) {
		StringBuilder out = new StringBuilder(value.length() + 16).append('"');
		for (int i = 0; i < value.length(); i++) {
			char character = value.charAt(i);
			switch (character) {
			case '"' -> out.append("\\\"");
			case '\\' -> out.append("\\\\");
			case '\b' -> out.append("\\b");
			case '\f' -> out.append("\\f");
			case '\n' -> out.append("\\n");
			case '\r' -> out.append("\\r");
			case '\t' -> out.append("\\t");
			case '<', '>', '&' -> out.append(String.format("\\u%04x", (int) character));
			default -> {
				if (character < 0x20) {
					out.append(String.format("\\u%04x", (int) character));
				} else {
					out.append(character);
				}
			}
			}
		}
		return out.append('"').toString();
	}

	private static Map<String, String> plainProperties() {
		Map<String, String> properties = new LinkedHashMap<>();
		properties.put(DCTERMS + "title", "name");
		properties.put(DCTERMS + "description", "description");
		properties.put(DCAT + "keyword", "keywords");
		properties.put(DCTERMS + "identifier", "identifier");
		properties.put(DCTERMS + "issued", "datePublished");
		properties.put(DCTERMS + "modified", "dateModified");
		properties.put(DCTERMS + "license", "license");
		properties.put(DCTERMS + "language", "inLanguage");
		// A dataset's landing page and a data service's endpoint are both "the page for
		// this thing" in schema.org's vocabulary; only one of them is ever set on a
		// given resource.
		properties.put(DCAT + "landingPage", "url");
		properties.put(DCAT + "endpointURL", "url");
		// Present on a Distribution, which is served as a resource in its own right.
		properties.put(DCAT + "downloadURL", "contentUrl");
		properties.put(DCAT + "accessURL", "contentUrl");
		properties.put(DCTERMS + "format", "encodingFormat");
		properties.put(DCAT + "mediaType", "encodingFormat");
		properties.put(DCAT + "byteSize", "contentSize");
		// Not Map.copyOf: that returns an unordered map, and the field order of the emitted
		// JSON-LD follows this map's iteration order.
		return Collections.unmodifiableMap(properties);
	}

	private static Map<String, String> agentProperties() {
		Map<String, String> properties = new LinkedHashMap<>();
		properties.put(DCTERMS + "publisher", "publisher");
		properties.put(DCTERMS + "creator", "creator");
		return Collections.unmodifiableMap(properties);
	}
}
