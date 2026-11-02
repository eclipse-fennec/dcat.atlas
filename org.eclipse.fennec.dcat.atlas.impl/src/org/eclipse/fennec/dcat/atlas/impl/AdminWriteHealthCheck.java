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

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Explains a write path that has disappeared (F-25).
 *
 * <h2>The failure it exists to name</h2>
 *
 * A deployment can hold the write side to a promise it would otherwise break silently: a
 * store configured with {@code validateOnWrite} refuses to start without a
 * {@link DcatValidationService}, and a {@code DcatAdminService} configured with
 * {@code sparqlSupported} refuses to start without a projection. Those are the right
 * switches — nothing slips through unchecked. But the symptom is unhelpful. The JAX-RS
 * whiteboard unregisters an admin resource whose service reference cannot be satisfied, so
 * {@code POST /admin/datasets} answers <b>404</b> — indistinguishable from a mistyped URL —
 * while the read endpoints keep serving normally.
 * <p>
 * This check turns that into a sentence: that the write side is missing, and where to look.
 *
 * <h2>One service, not five</h2>
 *
 * The handlers behind {@link DcatAdminService} are plain objects built by it, so they
 * arrive and leave together. There is no longer a state in which catalogs can be written
 * and datasets cannot, and the check no longer pretends there is.
 *
 * <h2>CRITICAL, not WARN</h2>
 *
 * A portal that cannot accept writes is not serving its purpose, and no retry fixes it —
 * it is a missing bundle or a shapes directory that failed to load. Same reasoning as the
 * per-collection store checks, and the opposite of the SPARQL projection's WARN, where
 * REST still serves the data.
 * <p>
 * Both references are optional and dynamic; a check that could itself go unsatisfied would
 * vanish exactly when it is needed.
 */
@Component(name = "DcatAdminWriteHealthCheck", service = HealthCheck.class, property = {
		HealthCheck.NAME + "=admin-write", HealthCheck.TAGS + "=ready" })
public class AdminWriteHealthCheck implements HealthCheck {

	@Reference(name = "dcatAdminService", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	void setDcatAdminService(DcatAdminService dcatAdminService) {
		this.dcatAdminService = dcatAdminService;
	}

	void unsetDcatAdminService(DcatAdminService dcatAdminService) {
		if (this.dcatAdminService == dcatAdminService) {
			this.dcatAdminService = null;
		}
	}

	private volatile DcatAdminService dcatAdminService;

	@Reference(name = "validation", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	void setValidation(DcatValidationService validation) {
		this.validation = validation;
	}

	void unsetValidation(DcatValidationService validation) {
		if (this.validation == validation) {
			this.validation = null;
		}
	}

	private volatile DcatValidationService validation;

	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		if (dcatAdminService != null) {
			log.info("the admin write services are available");
			log.info("SHACL enforcement: {}", enforcementState());
			return new Result(log);
		}

		log.critical("admin writes are unavailable: no DcatAdminService is registered");
		log.critical("the REST admin endpoints answer 404, not 503 - the JAX-RS whiteboard "
				+ "unregisters a resource whose service reference is unsatisfied");
		log.critical("check, in this order: that a DcatAdminService configuration exists at all "
				+ "(the component is ConfigurationPolicy.REQUIRE, so without one it never starts); "
				+ "that its storeService.target matches a DcatStoreService that is itself satisfied; "
				+ "and that any reference it was configured to require is bound");
		if (validation == null) {
			log.critical("no DcatValidationService is registered either; if the store was configured with "
					+ "validateOnWrite and validationService.cardinality.minimum=1 then the store is "
					+ "unsatisfied and the admin service is waiting on it - check that the validation "
					+ "bundle is present and its shapes directory is readable");
		} else {
			log.critical("a DcatValidationService IS registered, so the cause is elsewhere: check the "
					+ "DcatAdminService and DcatGitStoreService components in the SCR console");
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
