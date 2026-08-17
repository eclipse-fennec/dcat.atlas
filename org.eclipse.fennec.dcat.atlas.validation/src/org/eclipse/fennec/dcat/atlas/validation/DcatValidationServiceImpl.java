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
package org.eclipse.fennec.dcat.atlas.validation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.validation.ReportEntry;
import org.eclipse.emf.ecore.EObject;
import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.ValidationResult;
import org.eclipse.fennec.dcat.atlas.api.Violation;
import org.eclipse.fennec.dcat.atlas.msg.body.readerwriter.EObjectToJena;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

/**
 * SHACL validation over the DCAT-AP.de shapes (FR-4/FR-5), backed by Jena's SHACL
 * engine. The entity is serialized to an RDF graph via {@link EObjectToJena}
 * and checked against the shapes loaded (once, at activation) from the configured
 * directory. When no shapes are configured/loadable, validation is a no-op that
 * reports conformance — enforcement is a decision for the caller (the admin write
 * path), not this service.
 * <p>
 * The DCAT-AP.de controlled-vocabulary shapes (F-22) check membership by traversing
 * {@code skos:inScheme}/{@code sh:class} on the <em>value</em> node — triples that live
 * in the external authority tables, not in the submitted entity. So the reference
 * vocabulary data (also operator-configured, loaded once at activation) is unioned with
 * the entity graph before validation; without it every vocabulary-constrained value
 * would report a false violation. The union also exposes the authority tables' own defects,
 * so the report is filtered back down to the caller's triples afterwards — see
 * {@link #onlyEntityResults}.
 */
@Component(name = "DcatValidationService", service = { DcatValidationService.class, HealthCheck.class }, property = {
		HealthCheck.NAME + "=shacl", HealthCheck.TAGS + "=ready" })
@Designate(ocd = ShapesConfig.class)
public class DcatValidationServiceImpl implements DcatValidationService, HealthCheck {

	private static final Logger LOGGER = System.getLogger(DcatValidationServiceImpl.class.getName());

	/** Parsed shapes; an empty shapes set (everything conforms) when none are configured. */
	private final Shapes shapes;
	/** Controlled-vocabulary reference data merged into the data graph (F-22); empty when none configured. */
	private final Model vocabulary;
	/** Operator policy: block non-conformant writes with 422 (FR-4). */
	private final boolean enforceOnWrite;
	/** Configured shapes directory, {@code null} when the operator configured none (F-25). */
	private final Path shapesDirectory;
	/** Number of {@code *.ttl} shape files actually parsed; 0 means nothing is enforced (F-25). */
	private final int shapeFileCount;

	@Activate
	public DcatValidationServiceImpl(ShapesConfig config) {
		this(directoryOf(config.shapesDirectory()), directoryOf(config.vocabularyDirectory()),
				config.enforceOnWrite());
	}

	/** Package-visible for tests; no vocabulary, enforcement off. */
	DcatValidationServiceImpl(Path shapesDirectory) {
		this(shapesDirectory, null, false);
	}

	/** Package-visible for tests; no vocabulary. */
	DcatValidationServiceImpl(Path shapesDirectory, boolean enforceOnWrite) {
		this(shapesDirectory, null, enforceOnWrite);
	}

	/** Package-visible for tests; with controlled-vocabulary data (F-22). */
	DcatValidationServiceImpl(Path shapesDirectory, Path vocabularyDirectory) {
		this(shapesDirectory, vocabularyDirectory, false);
	}

	/** Package-visible for tests. */
	DcatValidationServiceImpl(Path shapesDirectory, Path vocabularyDirectory, boolean enforceOnWrite) {
		ShapesLoad load = loadShapes(shapesDirectory);
		this.shapes = load.shapes();
		this.shapeFileCount = load.fileCount();
		this.shapesDirectory = shapesDirectory;
		this.vocabulary = loadVocabulary(vocabularyDirectory);
		this.enforceOnWrite = enforceOnWrite;
	}

	@Override
	public ValidationReport validate(EObject entity) {
		Model data = EObjectToJena.toModel(entity);
		// With no shapes configured this validates against an empty shapes set, which always
		// conforms — so callers that gate writes on validation don't block.
		if (vocabulary.isEmpty()) {
			return ShaclValidator.get().validate(shapes, data.getGraph());
		}
		// Union the reference vocabularies in (read-only) so the CV shapes' skos:inScheme /
		// sh:class checks can resolve against the authority-table triples (F-22), then drop
		// the results that belong to the vocabularies rather than to the caller.
		Graph dataGraph = ModelFactory.createUnion(data, vocabulary).getGraph();
		ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);
		return onlyEntityResults(report, data.getGraph(), dataGraph);
	}

	@Deprecated
	@Override
	public ValidationResult validateLegacy(EObject entity) {
		ValidationReport report = validate(entity);
		List<Violation> violations = report.getEntries().stream() //
				.map(DcatValidationServiceImpl::toViolation) //
				.collect(Collectors.toList());
		return new ValidationResult(report.conforms(), violations, toTurtle(report));
	}

	@Override
	public boolean isWriteEnforced() {
		return enforceOnWrite;
	}

	// --- vocabulary-owned results (see validate) ----------------------------

	/**
	 * Drops the report entries that describe a defect in the reference vocabularies
	 * rather than in the submitted entity.
	 * <p>
	 * The union that makes the CV checks resolvable also puts the authority tables under
	 * validation: where the entity and an authority table share an IRI, SHACL sees one node
	 * carrying both sets of triples and reports violations from either. The DCAT-AP.de
	 * licence table, for instance, writes {@code dct:type "Freie Nutzung"} as a plain
	 * literal on every entry while the SEMIC shapes require an IRI there — so every write
	 * referencing a licence would be rejected for a defect in somebody else's file.
	 * <p>
	 * Attribution is per <em>triple</em>, not per focus node: a referenced licence IRI is
	 * legitimately a node in the entity graph too (the converter emits
	 * {@code <licence> a dct:LicenseDocument}), so only re-evaluating the reported result
	 * path against the entity graph alone tells the two apart.
	 * <p>
	 * Results that name no value — {@code sh:minCount}, and the node-level constraints the
	 * {@code sh:targetObjectsOf} controlled-vocabulary shapes are built from — point at no
	 * triple, so they fall back to the focus node: the entity's whenever it mentions that node
	 * (as subject <em>or</em> object — a CV concept is only ever an object) and the
	 * vocabularies do not describe it. "The concept you referenced is not in the authority
	 * table" is then still the caller's error, while a table entry failing a shape the caller
	 * cannot influence is not.
	 */
	private ValidationReport onlyEntityResults(ValidationReport report, Graph entityGraph, Graph dataGraph) {
		List<ReportEntry> entries = report.getEntries().stream() //
				.filter(entry -> isEntityResult(entry, entityGraph, vocabulary.getGraph())) //
				.collect(Collectors.toList());
		if (entries.size() == report.getEntries().size()) {
			return report;
		}
		LOGGER.log(Level.DEBUG, "Suppressed {0} SHACL result(s) originating in the reference vocabularies.",
				report.getEntries().size() - entries.size());
		ValidationReport.Builder filtered = ValidationReport.create();
		// Mirrors what the engine puts on its own report (ValidationContext).
		filtered.addPrefixes(dataGraph.getPrefixMapping());
		filtered.addPrefixes(shapes.getGraph().getPrefixMapping());
		entries.forEach(filtered::addReportEntry);
		return filtered.build();
	}

	/** True when the reported {@code (focusNode, resultPath, value)} is the entity's own. */
	private static boolean isEntityResult(ReportEntry entry, Graph entityGraph, Graph vocabularyGraph) {
		Node focusNode = entry.focusNode();
		if (focusNode == null) {
			return true;
		}
		org.apache.jena.sparql.path.Path resultPath = entry.resultPath();
		if (resultPath == null || entry.value() == null) {
			return mentions(entityGraph, focusNode) && !vocabularyGraph.contains(focusNode, Node.ANY, Node.ANY);
		}
		// ShaclPaths is what the engine itself uses to walk a path, so this asks exactly the
		// question the constraint asked, only of the entity graph.
		return ShaclPaths.valueNodes(entityGraph, focusNode, resultPath).contains(entry.value());
	}

	/** Whether {@code graph} names {@code node} at all, on either end of a triple. */
	private static boolean mentions(Graph graph, Node node) {
		return graph.contains(node, Node.ANY, Node.ANY) || graph.contains(Node.ANY, Node.ANY, node);
	}

	// --- helpers -----------------------------------------------------------

	private static Path directoryOf(String directory) {
		return directory == null || directory.isBlank() ? null : Path.of(directory);
	}

	/** Parsed shapes plus how many files they came from, so readiness can tell "none configured" from "configured but empty". */
	private record ShapesLoad(Shapes shapes, int fileCount) {
	}

	private static ShapesLoad loadShapes(Path directory) {
		if (directory == null || !Files.isDirectory(directory)) {
			LOGGER.log(Level.WARNING,
					"No SHACL shapes directory configured (or not a directory): {0}. No shapes loaded (everything conforms).",
					directory);
			return new ShapesLoad(emptyShapes(), 0);
		}
		Model shapesModel = ModelFactory.createDefaultModel();
		int count = 0;
		try (Stream<Path> files = Files.list(directory)) {
			List<Path> ttls = files.filter(Files::isRegularFile) //
					.filter(p -> p.getFileName().toString().endsWith(".ttl")) //
					.sorted() //
					.collect(Collectors.toList());
			for (Path ttl : ttls) {
				RDFDataMgr.read(shapesModel, ttl.toUri().toString(), Lang.TURTLE);
				count++;
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read SHACL shapes from " + directory, e);
		}
		if (count == 0) {
			LOGGER.log(Level.WARNING, "No *.ttl shape files found in {0}. No shapes loaded (everything conforms).",
					directory);
			return new ShapesLoad(emptyShapes(), 0);
		}
		LOGGER.log(Level.INFO, "Loaded {0} SHACL shape file(s) from {1}.", count, directory);
		return new ShapesLoad(Shapes.parse(shapesModel.getGraph()), count);
	}

	/** An empty shapes set — used when no shapes are configured, so validation conforms. */
	private static Shapes emptyShapes() {
		return Shapes.parse(ModelFactory.createDefaultModel().getGraph());
	}

	/**
	 * Loads all RDF files in {@code directory} (format detected by extension) into one
	 * model — the controlled-vocabulary reference data (F-22). Returns an empty model
	 * when no directory is configured; a configured-but-missing directory is logged
	 * (the CV shapes would then report false violations) but is not fatal.
	 */
	private static Model loadVocabulary(Path directory) {
		Model vocabulary = ModelFactory.createDefaultModel();
		if (directory == null) {
			return vocabulary;
		}
		if (!Files.isDirectory(directory)) {
			LOGGER.log(Level.WARNING, "Vocabulary directory not found: {0}. Controlled-vocabulary checks will report "
					+ "false violations if the CV shapes are loaded.", directory);
			return vocabulary;
		}
		int count = 0;
		try (Stream<Path> files = Files.list(directory)) {
			List<Path> rdfFiles = files.filter(Files::isRegularFile) //
					.filter(p -> RDFLanguages.filenameToLang(p.getFileName().toString()) != null) //
					.sorted() //
					.collect(Collectors.toList());
			for (Path rdf : rdfFiles) {
				RDFDataMgr.read(vocabulary, rdf.toUri().toString());
				count++;
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read controlled-vocabulary data from " + directory, e);
		}
		LOGGER.log(Level.INFO, "Loaded {0} controlled-vocabulary file(s) ({1} triples) from {2}.", count,
				vocabulary.size(), directory);
		return vocabulary;
	}

	private static Violation toViolation(ReportEntry entry) {
		// entry.severity() is a Jena Severity wrapper; the IRI is its level() node. A null
		// severity means the SHACL default, which is sh:Violation.
		var severity = entry.severity();
		var level = severity == null ? null : severity.level();
		String severityIri = level == null ? Violation.SH_VIOLATION : str(level);
		return new Violation(str(entry.focusNode()), str(entry.resultPath()), entry.message(), severityIri,
				str(entry.source()));
	}

	private static String toTurtle(ValidationReport report) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFDataMgr.write(out, report.getModel(), Lang.TURTLE);
		return out.toString(StandardCharsets.UTF_8);
	}

	private static String str(Object value) {
		return value == null ? null : value.toString();
	}
	// --- F-25 readiness -----------------------------------------------------

	/**
	 * Reports the shapes state (F-25), split three ways because "no shapes" and "broken
	 * shapes" are different operational situations:
	 * <ul>
	 * <li><b>OK</b> — shapes loaded.</li>
	 * <li><b>WARN</b> — no shapes directory configured. Shapes are operator-supplied
	 * deployment input and validation is then a documented no-op, so the portal is fit to
	 * serve; the servlet maps WARN to 200. It is reported so the situation is visible
	 * rather than silent.</li>
	 * <li><b>CRITICAL</b> — a directory was configured but nothing loaded from it (wrong
	 * path, no {@code *.ttl}). That is the dangerous case: an operator believes the portal
	 * validates and it silently passes everything.</li>
	 * </ul>
	 */
	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		if (shapesDirectory == null) {
			log.warn("No shapes directory configured - SHACL validation is a no-op, everything conforms");
		} else if (shapeFileCount == 0) {
			log.critical("Shapes directory configured but no *.ttl shapes loaded from {} - "
					+ "validation would silently pass everything", shapesDirectory);
		} else {
			log.info("{} shape file(s) loaded from {}", shapeFileCount, shapesDirectory);
			log.info("enforceOnWrite={}", enforceOnWrite);
		}
		return new Result(log);
	}

}
