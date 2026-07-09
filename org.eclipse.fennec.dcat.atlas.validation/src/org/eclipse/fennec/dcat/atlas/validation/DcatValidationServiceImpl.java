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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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
 */
@Component(name = "DcatValidationService", service = DcatValidationService.class)
@Designate(ocd = ShapesConfig.class)
public class DcatValidationServiceImpl implements DcatValidationService {

	private static final Logger LOGGER = System.getLogger(DcatValidationServiceImpl.class.getName());

	private final ResourceSetFactory resourceSetFactory;
	/** Parsed shapes, or {@code null} when none are configured (validation disabled). */
	private final Shapes shapes;

	@Activate
	public DcatValidationServiceImpl(@Reference ResourceSetFactory resourceSetFactory, ShapesConfig config) {
		this(resourceSetFactory, directoryOf(config));
	}

	/** Package-visible for tests. */
	DcatValidationServiceImpl(ResourceSetFactory resourceSetFactory, Path shapesDirectory) {
		this.resourceSetFactory = resourceSetFactory;
		this.shapes = loadShapes(shapesDirectory);
	}

	@Override
	public ValidationResult validate(EObject entity) {
		if (shapes == null) {
			// No shapes configured: nothing to check. Report conformance so callers
			// that gate writes on validation don't block when validation is off.
			return new ValidationResult(true, List.of(), "");
		}
		Model data = EObjectRDFModelBuilder.toModel(entity, resourceSetFactory.createResourceSet());
		ValidationReport report = ShaclValidator.get().validate(shapes, data.getGraph());
		List<Violation> violations = report.getEntries().stream() //
				.map(DcatValidationServiceImpl::toViolation) //
				.collect(Collectors.toList());
		return new ValidationResult(report.conforms(), violations, toTurtle(report));
	}

	// --- helpers -----------------------------------------------------------

	private static Path directoryOf(ShapesConfig config) {
		String directory = config.shapesDirectory();
		return directory == null || directory.isBlank() ? null : Path.of(directory);
	}

	private static Shapes loadShapes(Path directory) {
		if (directory == null || !Files.isDirectory(directory)) {
			LOGGER.log(Level.WARNING,
					"No SHACL shapes directory configured (or not a directory): {0}. Validation is disabled.",
					directory);
			return null;
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
			LOGGER.log(Level.WARNING, "No *.ttl shape files found in {0}. Validation is disabled.", directory);
			return null;
		}
		LOGGER.log(Level.INFO, "Loaded {0} SHACL shape file(s) from {1}.", count, directory);
		return Shapes.parse(shapesModel.getGraph());
	}

	private static Violation toViolation(ReportEntry entry) {
		return new Violation(str(entry.focusNode()), str(entry.resultPath()), entry.message(), str(entry.severity()),
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
