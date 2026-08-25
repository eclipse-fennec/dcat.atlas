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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

/**
 * Renders one RDF subject and everything nested under it as a standalone HTML
 * page — the {@code text/html} representation of a DCAT-AP resource (N25).
 *
 * <h2>Why this renders the graph rather than the model classes</h2>
 *
 * The page is produced from the same {@link EObjectToJena} projection that the
 * Turtle, RDF/XML, N-Triples, JSON-LD and N3 representations are produced from, so
 * the HTML view cannot disagree with the data views and cannot fall behind the
 * model: a feature added to an {@code .ecore} appears here with no change to this
 * class, and one class serves {@code Catalog}, {@code Dataset},
 * {@code DatasetSeries}, {@code DataService} and {@code Distribution} alike. The
 * alternative considered was a hand-written template per type, which reads better
 * but silently omits whatever no template names — for a *dereferenced*
 * representation that is the wrong trade. A human-facing catalog browser is
 * separate work (WP-DCAT-8).
 *
 * <h2>Ordering is alphabetical by CURIE, deliberately</h2>
 *
 * {@code EObjectToJena} emits in model feature order, but a Jena {@link Model} is a
 * set of triples and does not preserve it — {@code listStatements} order is
 * unspecified and varies between runs. Rows are therefore sorted by predicate
 * CURIE, and multiple values of one predicate by their rendered form, so the page
 * is byte-stable for a given graph. Without that a reload could reshuffle the
 * table for no reason.
 *
 * <h2>Escaping is not optional here</h2>
 *
 * Titles, descriptions and keywords are client-supplied and stored verbatim. Every
 * literal, IRI and CURIE therefore goes through {@link #escape}, and an IRI only
 * becomes an {@code href} when its scheme is one of {@link #LINKABLE_SCHEMES} —
 * otherwise a stored {@code javascript:} value in any {@code AnyURI} attribute
 * would become a working script link. The embedded JSON-LD is hardened separately;
 * see {@link SchemaOrgJsonLd}.
 */
final class RdfHtmlRenderer {

	/**
	 * Namespace to prefix, longest match wins. Written out rather than taken from the
	 * {@code EPackage} {@code nsPrefix}es because those are EMF's names, not the
	 * community's: the DCMI terms package is {@code nsPrefix="terms"}, and a page that
	 * said {@code terms:title} where every DCAT-AP document says {@code dcterms:title}
	 * would be needlessly unfamiliar. An unlisted namespace renders as the full IRI,
	 * which is correct if verbose.
	 */
	private static final Map<String, String> PREFIXES = prefixes();

	/** Schemes that may become an {@code href}. Anything else is rendered as text. */
	private static final Set<String> LINKABLE_SCHEMES = Set.of("http", "https", "mailto", "ftp", "ftps");

	/**
	 * How deep nesting is followed. Containment in DCAT-AP.de is a handful of levels at
	 * most (dataset → distribution → license); the cap is a guard against a cyclic or
	 * pathological graph rather than a modelling limit. {@code path} already stops a
	 * cycle from recursing; this stops a very deep one from producing an absurd page.
	 */
	private static final int MAX_DEPTH = 6;

	/**
	 * The other representations of this same URI. They are named rather than linked on
	 * purpose: all of them are served from this one address by content negotiation
	 * (D3 — one URI, {@code Vary: Accept}), and an {@code <a href>} cannot set an
	 * {@code Accept} header, so a link would just return this same HTML page.
	 */
	private static final List<String> DATA_MEDIA_TYPES = List.of("text/turtle", "application/rdf+xml",
			"application/ld+json", "application/n-triples", "text/n3", "application/xmi");

	private RdfHtmlRenderer() {
	}

	/**
	 * Renders {@code subject} and its nested nodes as a complete HTML document.
	 *
	 * @param model   the graph to read; not modified
	 * @param subject the node the page is about
	 */
	static String render(Model model, Resource subject) {
		StringBuilder out = new StringBuilder(4096);
		String heading = headingOf(model, subject);

		out.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
		out.append("<meta charset=\"utf-8\">\n");
		out.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
		out.append("<title>").append(escape(heading)).append("</title>\n");
		appendJsonLd(out, model, subject);
		out.append(STYLE);
		out.append("</head>\n<body>\n<main>\n");

		String types = typesOf(model, subject);
		if (!types.isEmpty()) {
			out.append("<p class=\"type\">").append(escape(types)).append("</p>\n");
		}
		out.append("<h1>").append(escape(heading)).append("</h1>\n");
		if (subject.isURIResource()) {
			out.append("<p class=\"self\">").append(nodeLink(subject.getURI())).append("</p>\n");
		}

		appendProperties(out, model, subject, new LinkedHashSet<>(), 0);
		appendFooter(out);

		out.append("</main>\n</body>\n</html>\n");
		return out.toString();
	}

	// --- head -------------------------------------------------------------

	/**
	 * The schema.org block, when the subject's type has a counterpart. Absent rather
	 * than empty for a type schema.org cannot express — an empty block would tell a
	 * crawler the page describes nothing.
	 */
	private static void appendJsonLd(StringBuilder out, Model model, Resource subject) {
		String jsonLd = SchemaOrgJsonLd.of(model, subject);
		if (jsonLd != null) {
			out.append("<script type=\"application/ld+json\">\n").append(jsonLd).append("\n</script>\n");
		}
	}

	// --- body -------------------------------------------------------------

	/**
	 * The page heading: the first {@code dcterms:title}, else the type, else the IRI.
	 * A resource with no title still needs a heading a browser tab can show.
	 * <p>
	 * {@code dcterms:title} keeps its row in the table as well. That duplication is
	 * deliberate: the table is the graph in full, with no predicate held back, which is
	 * the property that makes this renderer trustworthy. The heading is a view of it,
	 * not a replacement for it.
	 */
	private static String headingOf(Model model, Resource subject) {
		List<RDFNode> titles = sortedValues(model, subject, model.createProperty(SchemaOrgJsonLd.DCTERMS, "title"));
		for (RDFNode title : titles) {
			if (title.isLiteral()) {
				return title.asLiteral().getLexicalForm();
			}
		}
		String types = typesOf(model, subject);
		if (!types.isEmpty()) {
			return types;
		}
		return subject.isURIResource() ? subject.getURI() : "Resource";
	}

	/** The subject's {@code rdf:type}s as CURIEs, comma separated. */
	private static String typesOf(Model model, Resource subject) {
		return sortedValues(model, subject, RDF.type).stream() //
				.filter(RDFNode::isURIResource) //
				.map(node -> curie(node.asResource().getURI())) //
				.reduce((left, right) -> left + ", " + right) //
				.orElse("");
	}

	/** The property table for {@code subject}; {@code rdf:type} is shown as the eyebrow instead. */
	private static void appendProperties(StringBuilder out, Model model, Resource subject, Set<Resource> path,
			int depth) {
		List<Property> predicates = model.listStatements(subject, null, (RDFNode) null).toList().stream() //
				.map(Statement::getPredicate) //
				.filter(predicate -> !RDF.type.equals(predicate)) //
				.distinct() //
				.sorted(Comparator.comparing(predicate -> curie(predicate.getURI()))) //
				.toList();
		if (predicates.isEmpty()) {
			return;
		}
		out.append("<table class=\"props\">\n");
		for (Property predicate : predicates) {
			out.append("<tr><th>").append(escape(curie(predicate.getURI()))).append("</th><td>");
			appendValues(out, model, subject, predicate, path, depth);
			out.append("</td></tr>\n");
		}
		out.append("</table>\n");
	}

	/**
	 * The values of one predicate: everything that renders on a line first, comma
	 * separated, then the contained nodes as blocks. Grouping them rather than emitting in
	 * value order keeps the separators sane for the (rare, but possible) predicate that
	 * carries both — a comma trailing a closed block reads as a mistake.
	 */
	private static void appendValues(StringBuilder out, Model model, Resource subject, Property predicate,
			Set<Resource> path, int depth) {
		List<RDFNode> values = sortedValues(model, subject, predicate);
		List<Resource> nested = values.stream() //
				.filter(value -> value.isResource() && isNestable(model, value.asResource(), path, depth)) //
				.map(RDFNode::asResource) //
				.toList();

		boolean first = true;
		for (RDFNode value : values) {
			if (value.isResource() && nested.contains(value.asResource())) {
				continue;
			}
			if (!first) {
				out.append(", ");
			}
			first = false;
			if (value.isLiteral()) {
				appendLiteral(out, value.asLiteral());
			} else {
				appendReference(out, value.asResource());
			}
		}
		nested.forEach(resource -> appendNested(out, model, resource, path, depth));
	}

	private static void appendLiteral(StringBuilder out, Literal literal) {
		out.append(escape(literal.getLexicalForm()));
		String language = literal.getLanguage();
		if (language != null && !language.isBlank()) {
			out.append("<span class=\"lang\">@").append(escape(language)).append("</span>");
			return;
		}
		// The datatype is part of the value; xsd:string is RDF 1.1's default and saying so
		// on every plain literal would be noise.
		String datatype = literal.getDatatypeURI();
		if (datatype != null && !"http://www.w3.org/2001/XMLSchema#string".equals(datatype)) {
			out.append("<span class=\"lang\">^^").append(escape(curie(datatype))).append("</span>");
		}
	}

	/** A link to a resource served in its own right, or its IRI as text when not linkable. */
	private static void appendReference(StringBuilder out, Resource resource) {
		if (resource.isURIResource()) {
			out.append(nodeLink(resource.getURI()));
		} else {
			out.append("<span class=\"iri\">(blank node)</span>");
		}
	}

	/**
	 * Whether a referenced node is rendered inline. A node with statements of its own is
	 * a contained child ({@code EObjectToJena} inlines containment and links everything
	 * else), so it belongs on this page; a node with none is an independently served
	 * resource and gets a link. {@code path} keeps a cyclic graph from recursing.
	 */
	private static boolean isNestable(Model model, Resource resource, Set<Resource> path, int depth) {
		return depth < MAX_DEPTH && !path.contains(resource) && model.contains(resource, null, (RDFNode) null);
	}

	private static void appendNested(StringBuilder out, Model model, Resource resource, Set<Resource> path,
			int depth) {
		Set<Resource> nestedPath = new LinkedHashSet<>(path);
		nestedPath.add(resource);

		out.append("<div class=\"nested\">");
		String types = typesOf(model, resource);
		if (!types.isEmpty()) {
			out.append("<span class=\"type\">").append(escape(types)).append("</span>");
		}
		if (resource.isURIResource()) {
			out.append(nodeLink(resource.getURI()));
		}
		appendProperties(out, model, resource, nestedPath, depth + 1);
		out.append("</div>");
	}

	private static void appendFooter(StringBuilder out) {
		out.append("<footer><p>The same resource as data, at this same address — ask for one of ");
		for (int i = 0; i < DATA_MEDIA_TYPES.size(); i++) {
			if (i > 0) {
				out.append(i == DATA_MEDIA_TYPES.size() - 1 ? " or " : ", ");
			}
			out.append("<code>").append(escape(DATA_MEDIA_TYPES.get(i))).append("</code>");
		}
		out.append(" in the <code>Accept</code> header.</p></footer>\n");
	}

	// --- helpers ----------------------------------------------------------

	/**
	 * The objects of {@code subject predicate}, in a stable order: literals first by
	 * lexical form, then nodes by IRI. See the class comment on why order has to be
	 * imposed here.
	 */
	private static List<RDFNode> sortedValues(Model model, Resource subject, Property predicate) {
		return model.listObjectsOfProperty(subject, predicate).toList().stream() //
				.sorted(Comparator.comparing(RdfHtmlRenderer::sortKey)) //
				.toList();
	}

	private static String sortKey(RDFNode node) {
		if (node.isLiteral()) {
			return "0" + node.asLiteral().getLexicalForm();
		}
		return "1" + (node.isURIResource() ? node.asResource().getURI() : node.asResource().getId().getLabelString());
	}

	/** An IRI as an anchor when its scheme allows it, otherwise as inert text. */
	private static String nodeLink(String iri) {
		String escaped = escape(iri);
		if (!isLinkable(iri)) {
			return "<span class=\"iri\">" + escaped + "</span>";
		}
		return "<a class=\"iri\" href=\"" + escaped + "\">" + escaped + "</a>";
	}

	/**
	 * Whether {@code iri}'s scheme is safe to put in an {@code href}. Anything else —
	 * {@code javascript:}, {@code data:}, a relative value — is rendered as text.
	 * {@code AnyURI} attributes are stored as given (see
	 * {@code EObjectToJena#anyUriObject}), so this class cannot assume the value is a
	 * dereferenceable URL.
	 */
	private static boolean isLinkable(String iri) {
		int colon = iri.indexOf(':');
		if (colon < 1) {
			return false;
		}
		return LINKABLE_SCHEMES.contains(iri.substring(0, colon).toLowerCase(Locale.ROOT));
	}

	/** {@code iri} as {@code prefix:local} when its namespace is known, else unchanged. */
	static String curie(String iri) {
		String longest = null;
		for (String namespace : PREFIXES.keySet()) {
			if (iri.startsWith(namespace) && (longest == null || namespace.length() > longest.length())) {
				longest = namespace;
			}
		}
		return longest == null ? iri : PREFIXES.get(longest) + ":" + iri.substring(longest.length());
	}

	/**
	 * Escapes for both element text and double-quoted attribute values — one escaper
	 * rather than two, so no call site can pick the weaker one.
	 */
	static String escape(String value) {
		StringBuilder escaped = new StringBuilder(value.length() + 16);
		for (int i = 0; i < value.length(); i++) {
			char character = value.charAt(i);
			switch (character) {
			case '&' -> escaped.append("&amp;");
			case '<' -> escaped.append("&lt;");
			case '>' -> escaped.append("&gt;");
			case '"' -> escaped.append("&quot;");
			case '\'' -> escaped.append("&#39;");
			default -> escaped.append(character);
			}
		}
		return escaped.toString();
	}

	private static Map<String, String> prefixes() {
		Map<String, String> prefixes = new LinkedHashMap<>();
		prefixes.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
		prefixes.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
		prefixes.put("http://www.w3.org/2001/XMLSchema#", "xsd");
		prefixes.put("http://www.w3.org/ns/dcat#", "dcat");
		prefixes.put("http://purl.org/dc/terms/", "dcterms");
		prefixes.put("http://xmlns.com/foaf/0.1/", "foaf");
		prefixes.put("http://www.w3.org/ns/adms#", "adms");
		prefixes.put("http://spdx.org/rdf/terms#", "spdx");
		prefixes.put("http://www.w3.org/2006/vcard/ns#", "vcard");
		prefixes.put("http://www.w3.org/ns/locn#", "locn");
		prefixes.put("http://www.w3.org/ns/odrl/2/", "odrl");
		prefixes.put("http://www.w3.org/2002/07/owl#", "owl");
		prefixes.put("http://www.w3.org/ns/prov#", "prov");
		prefixes.put("http://www.w3.org/2004/02/skos/core#", "skos");
		prefixes.put("http://schema.org/", "schema");
		return Map.copyOf(prefixes);
	}

	/**
	 * The page's stylesheet, inline because the representation has to be one
	 * self-contained response — there is no static-resource route in this application,
	 * and a portal is read by clients that fetch exactly the URI they were given.
	 */
	private static final String STYLE = """
			<style>
			:root { --ground:#fff; --ink:#1a1a1a; --soft:#666; --rule:#e0e0e0; --link:#1a4c86; }
			@media (prefers-color-scheme: dark) {
			  :root { --ground:#1c2228; --ink:#e8eaed; --soft:#9aa4ae; --rule:#333b44; --link:#7fb2e8; }
			}
			* { box-sizing:border-box; }
			body { margin:0; background:var(--ground); color:var(--ink); line-height:1.5;
			  font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Arial,sans-serif; }
			main { max-width:60rem; margin:0 auto; padding:2rem 1.25rem 4rem; }
			a { color:var(--link); }
			h1 { font-size:1.6rem; font-weight:600; margin:.2rem 0 .4rem; }
			p { margin:0 0 .5rem; }
			code { font-family:ui-monospace,SFMono-Regular,Menlo,monospace; font-size:.85em; }
			.type { font-family:ui-monospace,SFMono-Regular,Menlo,monospace; font-size:.75rem;
			  letter-spacing:.04em; text-transform:uppercase; color:var(--soft); margin:0; }
			.self { font-family:ui-monospace,SFMono-Regular,Menlo,monospace; font-size:.78rem;
			  word-break:break-all; margin-bottom:1.5rem; }
			table.props { border-collapse:collapse; width:100%; }
			table.props th, table.props td { text-align:left; vertical-align:top;
			  padding:.4rem .9rem .4rem 0; border-bottom:1px solid var(--rule); font-weight:400; }
			table.props th { font-family:ui-monospace,SFMono-Regular,Menlo,monospace; font-size:.8rem;
			  color:var(--soft); white-space:nowrap; width:1%; padding-right:1.5rem; }
			.lang { font-family:ui-monospace,SFMono-Regular,Menlo,monospace; font-size:.7rem;
			  color:var(--soft); margin-left:.35rem; }
			.iri { font-family:ui-monospace,SFMono-Regular,Menlo,monospace; font-size:.78rem;
			  word-break:break-all; }
			.nested { margin:.5rem 0 .25rem; padding:.6rem 0 .6rem .9rem; border-left:2px solid var(--rule); }
			.nested > .type { display:block; margin-bottom:.15rem; }
			.nested table.props tr:last-child th, .nested table.props tr:last-child td { border-bottom:0; }
			footer { margin-top:2rem; padding-top:1rem; border-top:1px solid var(--rule);
			  font-size:.8rem; color:var(--soft); }
			</style>
			""";
}
