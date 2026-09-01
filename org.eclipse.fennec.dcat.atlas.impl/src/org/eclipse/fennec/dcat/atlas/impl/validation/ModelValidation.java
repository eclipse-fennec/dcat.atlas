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
package org.eclipse.fennec.dcat.atlas.impl.validation;

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
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.fennec.dcat.atlas.api.validation.ModelConstraintException;
import org.eclipse.fennec.dcat.atlas.impl.integrity.References;

import dcat.Dataset;
import dcat.Distribution;
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
		// Decided on the filtered messages rather than on the raw severity: one of EMF's
		// generic checks does not apply to this model (see isApplicable), and a diagnostic
		// carrying only that would otherwise throw with nothing to report.
		List<String> violations = messages(diagnostic);
		if (violations.isEmpty()) {
			return;
		}
		throw new ModelConstraintException(summary(object, violations), violations);
	}

	/**
	 * Refuses a write whose Distributions do not have distinct identities — the precise
	 * version of what {@link #isApplicable} drops.
	 *
	 * <h2>Why this is checked by hand rather than by EMF</h2>
	 *
	 * EMF's {@code validate_UniqueID} would catch it, and used to, but only as part of
	 * refusing every repeated {@code about} in a resource — including a licence carried by two
	 * Distributions, which is ordinary. The distinction EMF cannot draw is between an
	 * <b>entity</b>, whose {@code about} is the identity it is addressed by, and a <b>value
	 * node</b> (a licence, an agent), which is legitimately mentioned more than once. This
	 * draws it, by looking only at Distributions.
	 *
	 * <h2>Why Distributions are the whole of it</h2>
	 *
	 * A Distribution is the only entity stored inside another's file: {@code dcat:distribution}
	 * is containment (FR-10), while catalogue membership is pointer references, so members live
	 * in their own files and cannot collide within one. Stored roots are addressed by path, so
	 * they cannot collide with each other either.
	 *
	 * <h2>What it prevents</h2>
	 *
	 * Every id-based operation resolves the <em>first</em> match
	 * ({@code DistributionReadOnlyServiceImpl.find} is a {@code findFirst}), so a second
	 * Distribution under one identity is an unreachable shadow — and a {@code DELETE} of that
	 * id was answered {@code 204} while leaving the other in place, so the same {@code GET}
	 * answered {@code 200} straight afterwards. Measured, before this existed.
	 *
	 * @throws ModelConstraintException naming each repeated identity
	 */
	public static void checkDistinctDistributions(EObject object) {
		if (!(object instanceof Dataset dataset)) {
			// Catalog and DatasetSeries are Datasets, so this covers them too.
			return;
		}
		Set<String> seen = new LinkedHashSet<>();
		Set<String> repeated = new LinkedHashSet<>();
		for (Distribution distribution : dataset.getDistribution()) {
			String about = distribution.getAbout();
			if (about != null && !seen.add(about)) {
				repeated.add(about);
			}
		}
		if (repeated.isEmpty()) {
			return;
		}
		List<String> violations = repeated.stream() //
				.map(iri -> "More than one Distribution claims the identity " + iri
						+ "; a Distribution is addressed by it, so exactly one may carry it.") //
				.toList();
		throw new ModelConstraintException(summary(object, violations), violations);
	}

	/**
	 * Whether a diagnostic is one this model can be held to.
	 *
	 * <h2>{@code validate_EveryProxyResolves} is not</h2>
	 *
	 * Membership is cross-resource references, and an unresolved proxy is the normal state of
	 * two legitimate cases: a link to another of our entities that has not been touched yet in
	 * this session, and a link to a <em>foreign</em> IRI, which by definition cannot resolve
	 * and is deliberately kept ({@code upsertLeavesAForeignReferenceAlone}). EMF's generic
	 * check cannot tell either from a dangling link, where
	 * {@code References.requireResolvable} — which runs first, and knows which IRIs are ours —
	 * can, and answers 409 naming the missing member.
	 *
	 * <h2>Neither is {@code validate_UniqueID}</h2>
	 *
	 * {@code about} is declared {@code iD="true"} in {@code rdf.ecore} — the model's only ID —
	 * so EMF requires it to be unique within a resource. That is an XMI serialization rule,
	 * and RDF's is the opposite: the same IRI twice <em>is</em> the same resource, which is
	 * the whole point of an IRI.
	 * <p>
	 * The two collide over ordinary data. A Dataset's file holds its Distributions (FR-10),
	 * and two Distributions of one Dataset sharing a licence —
	 * {@code <license about="http://dcat-ap.de/def/licenses/dl-by-de/2.0"/>} on both, the most
	 * common shape there is — puts two contained {@code LicenseDocument} nodes with one IRI in
	 * one resource. EMF calls that a duplicate ID; DCAT calls it two distributions under the
	 * same licence. Held to the EMF reading, adding the second Distribution is a {@code 422},
	 * which is plainly wrong.
	 * <p>
	 * The half of it worth keeping — a Dataset body naming two Distributions with the
	 * <em>same</em> {@code about} — is kept, by
	 * {@link #checkDistinctDistributions(EObject)}, which draws the entity/value-node
	 * distinction this generic check cannot.
	 */
	private static boolean isApplicable(Diagnostic diagnostic) {
		if (!EObjectValidator.DIAGNOSTIC_SOURCE.equals(diagnostic.getSource())) {
			return true;
		}
		return diagnostic.getCode() != EObjectValidator.EOBJECT__EVERY_PROXY_RESOLVES
				&& diagnostic.getCode() != EObjectValidator.EOBJECT__UNIQUE_ID;
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
		if (diagnostic.getSeverity() < Diagnostic.ERROR || !isApplicable(diagnostic)) {
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
