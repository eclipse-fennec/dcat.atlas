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
package org.eclipse.fennec.dcat.atlas.impl.helper;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.fennec.dcat.atlas.api.ModelConstraintException;

import rdf.IdentifiedResource;

/**
 * The model's own constraints, checked against an entity before it is stored.
 *
 * <h2>Why the Diagnostician, and why it has to be called explicitly</h2>
 *
 * The OCL invariants live in the ecore as delegate annotations
 * ({@code http://www.eclipse.org/fennec/m2x/ocl/1.0}) and are evaluated by the
 * {@code emf.m2x} engine. EMF only reaches them through {@link Diagnostician} —
 * {@code Resource.save()} never validates — so an annotated model enforces
 * <em>nothing</em> until something calls this. That is the whole reason the check sits
 * at the write boundary rather than being left to a validation delegate.
 *
 * <h2>Two kinds of failure, one call</h2>
 *
 * {@code Diagnostician} runs both halves and neither is redundant:
 * <ul>
 * <li><b>Multiplicities</b> the ecore declares ({@code title [1..*]},
 * {@code publisher [1]}, {@code accessURL [1..*]}), via
 * {@code validate_EveryMultiplicityConforms}. Not OCL at all — the ecore is the
 * statement, and this is what finally enforces it.</li>
 * <li><b>OCL invariants</b> for what the ecore cannot say: that a stored entity carries
 * an {@code about} at all ({@code about} is {@code [0..1]} on the shared supertype
 * because value nodes are legitimately blank), that an {@code AnyURI} attribute holds an
 * absolute IRI, and that {@code description} is mandatory on Dataset/Catalog/DatasetSeries
 * but not on DataService.</li>
 * </ul>
 *
 * <h2>It fails closed</h2>
 *
 * If the OCL delegate is missing — the engine bundle absent or not yet started — EMF does
 * <em>not</em> quietly pass the constraint. {@code EObjectValidator} reports a
 * {@code constraint delegate not found} diagnostic at {@code ERROR}, which lands in the
 * chain and rejects the write exactly as a violation would. That is deliberate and worth
 * keeping: the alternative is the silent-failure class this project already hit twice —
 * an unconfigured SHACL service and an empty SPARQL graph, both of which answered
 * successfully while checking nothing.
 */
public final class ModelValidation {

	/** Enough violations to diagnose the problem, few enough to keep the response readable. */
	private static final int MAX_REPORTED = 20;

	private ModelValidation() {
	}

	/**
	 * Throws {@link ModelConstraintException} if {@code object} — or anything it
	 * contains — violates a model constraint.
	 * <p>
	 * Contained objects matter: a Dataset carries its Distributions (FR-10), so a
	 * Distribution with no {@code accessURL} has to fail the Dataset's write.
	 * {@code Diagnostician.validate} walks {@code eAllContents} for exactly that.
	 */
	public static void check(EObject object) {
		BasicDiagnostic diagnostic = Diagnostician.INSTANCE.createDefaultDiagnostic(object);
		Map<Object, Object> context = new HashMap<>();
		context.put(EValidator.SubstitutionLabelProvider.class, LABELS);
		Diagnostician.INSTANCE.validate(object, diagnostic, context);
		if (diagnostic.getSeverity() < Diagnostic.ERROR) {
			return;
		}
		List<String> violations = messages(diagnostic);
		throw new ModelConstraintException(summary(object, violations), violations);
	}

	/**
	 * The leaf messages at {@code ERROR} or worse. Leaves only: the root's own message is
	 * boilerplate ("Diagnosis of ...") and an intermediate node repeats what its children
	 * already say.
	 */
	private static List<String> messages(Diagnostic diagnostic) {
		Set<String> collected = new LinkedHashSet<>();
		collect(diagnostic, collected);
		return List.copyOf(collected);
	}

	private static void collect(Diagnostic diagnostic, Set<String> into) {
		if (diagnostic.getSeverity() < Diagnostic.ERROR) {
			return;
		}
		List<Diagnostic> children = diagnostic.getChildren();
		if (children.isEmpty()) {
			if (diagnostic.getMessage() != null) {
				into.add(diagnostic.getMessage());
			}
			return;
		}
		for (Diagnostic child : children) {
			collect(child, into);
		}
	}

	/**
	 * Names the offending object the way a caller can act on it.
	 * <p>
	 * EMF's default label is the implementation object's {@code toString()} —
	 * {@code dcat.impl.DatasetImpl@96def03{#http://dcat.atlas/datasets/air}} — which leaks
	 * the generated class name and an identity hash into an API response body. The class
	 * and the identity are the only parts a client can use.
	 */
	private static final EValidator.SubstitutionLabelProvider LABELS = new EValidator.SubstitutionLabelProvider() {

		@Override
		public String getObjectLabel(EObject eObject) {
			String type = eObject.eClass().getName();
			return eObject instanceof IdentifiedResource identified && identified.getAbout() != null
					? type + " " + identified.getAbout()
					: type;
		}

		@Override
		public String getFeatureLabel(EStructuralFeature eStructuralFeature) {
			return eStructuralFeature.getName();
		}

		@Override
		public String getValueLabel(EDataType eDataType, Object value) {
			return String.valueOf(value);
		}
	};

	private static String summary(EObject object, List<String> violations) {
		StringBuilder message = new StringBuilder(object.eClass().getName());
		if (object instanceof IdentifiedResource identified && identified.getAbout() != null) {
			message.append(" ").append(identified.getAbout());
		}
		message.append(violations.size() == 1 ? " violates a model constraint: "
				: " violates " + violations.size() + " model constraints: ");
		List<String> reported = violations.size() > MAX_REPORTED ? violations.subList(0, MAX_REPORTED) : violations;
		message.append(String.join("; ", reported));
		if (reported.size() < violations.size()) {
			message.append("; ... (").append(violations.size() - reported.size()).append(" more)");
		}
		return message.toString();
	}
}
