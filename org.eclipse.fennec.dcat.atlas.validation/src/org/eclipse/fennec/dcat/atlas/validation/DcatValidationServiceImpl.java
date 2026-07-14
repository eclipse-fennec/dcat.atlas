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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.ReportEntry;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.ValidationResult;
import org.eclipse.fennec.dcat.atlas.api.Violation;
import org.eclipse.fennec.dcat.atlas.msg.body.readerwriter.EObjectRDFModelBuilder;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * SHACL validation over the DCAT-AP.de shapes (FR-4/FR-5), backed by Jena's SHACL
 * engine. The entity is serialized to an RDF graph via {@link EObjectRDFModelBuilder}
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
 * would report a false violation.
 */
@Component(name = "DcatValidationService", service = DcatValidationService.class)
@Designate(ocd = ShapesConfig.class)
public class DcatValidationServiceImpl implements DcatValidationService {

	private static final Logger LOGGER = System.getLogger(DcatValidationServiceImpl.class.getName());

	private final ResourceSetFactory resourceSetFactory;
	/** Parsed shapes; an empty shapes set (everything conforms) when none are configured. */
	private final Shapes shapes;
	/** Controlled-vocabulary reference data merged into the data graph (F-22); empty when none configured. */
	private final Model vocabulary;
	/** Operator policy: block non-conformant writes with 422 (FR-4). */
	private final boolean enforceOnWrite;

	@Activate
	public DcatValidationServiceImpl(@Reference ResourceSetFactory resourceSetFactory, ShapesConfig config) {
		this(resourceSetFactory, directoryOf(config.shapesDirectory()), directoryOf(config.vocabularyDirectory()),
				config.enforceOnWrite());
	}

	/** Package-visible for tests; no vocabulary, enforcement off. */
	DcatValidationServiceImpl(ResourceSetFactory resourceSetFactory, Path shapesDirectory) {
		this(resourceSetFactory, shapesDirectory, null, false);
	}

	/** Package-visible for tests; no vocabulary. */
	DcatValidationServiceImpl(ResourceSetFactory resourceSetFactory, Path shapesDirectory, boolean enforceOnWrite) {
		this(resourceSetFactory, shapesDirectory, null, enforceOnWrite);
	}

	/** Package-visible for tests; with controlled-vocabulary data (F-22). */
	DcatValidationServiceImpl(ResourceSetFactory resourceSetFactory, Path shapesDirectory, Path vocabularyDirectory) {
		this(resourceSetFactory, shapesDirectory, vocabularyDirectory, false);
	}

	/** Package-visible for tests. */
	DcatValidationServiceImpl(ResourceSetFactory resourceSetFactory, Path shapesDirectory, Path vocabularyDirectory,
			boolean enforceOnWrite) {
		this.resourceSetFactory = resourceSetFactory;
		this.shapes = loadShapes(shapesDirectory);
		this.vocabulary = loadVocabulary(vocabularyDirectory);
		this.enforceOnWrite = enforceOnWrite;
	}

	@Override
	public ValidationReport validate(EObject entity) {
		Model data = EObjectRDFModelBuilder.toModel(entity, resourceSetFactory.createResourceSet());
		// Union the reference vocabularies in (read-only) so the CV shapes' skos:inScheme /
		// sh:class checks can resolve against the authority-table triples (F-22). When no
		// shapes are configured the graph is validated against an empty shapes set, which
		// always conforms — so callers that gate writes on validation don't block.
		Graph dataGraph = vocabulary.isEmpty() ? data.getGraph()
				: ModelFactory.createUnion(data, vocabulary).getGraph();
		return ShaclValidator.get().validate(shapes, dataGraph);
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

	// --- helpers -----------------------------------------------------------

	private static Path directoryOf(String directory) {
		return directory == null || directory.isBlank() ? null : Path.of(directory);
	}

	private static Shapes loadShapes(Path directory) {
		if (directory == null || !Files.isDirectory(directory)) {
			LOGGER.log(Level.WARNING,
					"No SHACL shapes directory configured (or not a directory): {0}. No shapes loaded (everything conforms).",
					directory);
			return emptyShapes();
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
			return emptyShapes();
		}
		LOGGER.log(Level.INFO, "Loaded {0} SHACL shape file(s) from {1}.", count, directory);
		return Shapes.parse(shapesModel.getGraph());
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
}
