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
package org.eclipse.fennec.dcat.atlas.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.dcat.atlas.api.admin.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DataServiceAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetSeriesAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DistributionAdminService;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Explains a write path that has disappeared (F-25).
 *
 * <h2>The failure it exists to name</h2>
 *
 * An operator can require SHACL enforcement by raising the admin services' validation
 * reference to a mandatory minimum — {@code validationService.cardinality.minimum=1}. That
 * is the right switch: without a {@link DcatValidationService} the admin components stay
 * unsatisfied and no write can slip through unvalidated. But the symptom is unhelpful. The
 * JAX-RS whiteboard unregisters an admin resource whose service reference cannot be
 * satisfied, so {@code POST /admin/datasets} answers <b>404</b> — indistinguishable from a
 * mistyped URL — while the read endpoints keep serving normally.
 * <p>
 * This check turns that into a sentence: which admin services are missing, and whether the
 * validation service they were configured to require is the reason.
 *
 * <h2>CRITICAL, not WARN</h2>
 *
 * A portal that cannot accept writes is not serving its purpose, and no retry fixes it —
 * it is a missing bundle or a shapes directory that failed to load. Same reasoning as the
 * per-collection store checks, and the opposite of the SPARQL projection's WARN, where
 * REST still serves the data.
 * <p>
 * All references are optional and dynamic; a check that could itself go unsatisfied would
 * vanish exactly when it is needed.
 */
@Component(name = "DcatAdminWriteHealthCheck", service = HealthCheck.class, property = {
		HealthCheck.NAME + "=admin-write", HealthCheck.TAGS + "=ready" })
public class AdminWriteHealthCheck implements HealthCheck {

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile CatalogAdminService catalogs;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DatasetAdminService datasets;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DatasetSeriesAdminService datasetSeries;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DataServiceAdminService dataServices;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DistributionAdminService distributions;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validation;

	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		List<String> missing = new ArrayList<>();
		record Entry(String name, Object service) {
		}
		for (Entry entry : List.of(new Entry("catalogs", catalogs), new Entry("datasets", datasets),
				new Entry("dataset-series", datasetSeries), new Entry("data-services", dataServices),
				new Entry("distributions", distributions))) {
			if (entry.service() == null) {
				missing.add(entry.name());
			}
		}

		if (missing.isEmpty()) {
			log.info("all five admin write services are available");
			log.info("SHACL enforcement: {}", enforcementState());
			return new Result(log);
		}

		log.critical("admin writes unavailable for: {}", String.join(", ", missing));
		log.critical("the REST admin endpoints for those collections answer 404, not 503 — the JAX-RS "
				+ "whiteboard unregisters a resource whose service reference is unsatisfied");
		if (validation == null) {
			log.critical("no DcatValidationService is registered; if the admin services were configured with "
					+ "validationService.cardinality.minimum=1 this is the cause — check that the validation "
					+ "bundle is present and its shapes directory is readable");
		} else {
			log.critical("a DcatValidationService IS registered, so the cause is elsewhere: check the store "
					+ "directory and the admin components in the SCR console");
		}
		return new Result(log);
	}

	private String enforcementState() {
		if (validation == null) {
			return "no validation service bound (writes are not SHACL-checked)";
		}
		return validation.isWriteEnforced() ? "on" : "off (dry-run only)";
	}
}
