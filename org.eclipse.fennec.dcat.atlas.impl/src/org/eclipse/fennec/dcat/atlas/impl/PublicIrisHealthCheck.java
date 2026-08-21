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
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Explains a portal that cannot render identities (F-25).
 *
 * <h2>The failure it exists to name</h2>
 *
 * {@code publicBaseUrl} has no default on purpose: it becomes the host in every
 * {@code about} handed to a client, and no value is right for more than one
 * deployment, so {@link PublicIrisImpl} refuses to activate without one. That is the
 * correct behaviour, but on its own it is close to invisible. The only trace is an
 * {@code IllegalArgumentException} from a component activation somewhere in a long
 * startup log, and until this check existed the readiness endpoint reported the
 * consequence as {@code Exception during execution of 'sparql'} — naming the one
 * consumer that happened to blow up rather than the setting that was missing.
 * <p>
 * Measured in a container started without {@code PUBLIC_BASE_URL}: liveness 200,
 * readiness 500 attributed to SPARQL, {@code admin-write} reporting OK, and
 * {@code POST /admin/catalogs} still answering <b>201</b>.
 *
 * <h2>CRITICAL, not WARN</h2>
 *
 * Without this service the REST resources do not register at all — they hold it as a
 * mandatory reference precisely so a misconfigured portal serves nothing rather than
 * serving unrebased {@code http://dcat.atlas/…} identities that a harvester could
 * take as canonical. Nothing retries its way out of it: it is a configuration value
 * that has to be supplied and the process restarted.
 * <p>
 * The reference is optional and dynamic, like the other checks' — a check that could
 * itself go unsatisfied would vanish exactly when it is needed.
 */
@Component(name = "DcatPublicIrisHealthCheck", service = HealthCheck.class, property = {
		HealthCheck.NAME + "=public-iris", HealthCheck.TAGS + "=ready" })
public class PublicIrisHealthCheck implements HealthCheck {

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile PublicIris publicIris;

	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		PublicIris iris = publicIris;

		if (iris == null) {
			log.critical("no PublicIris service: stored identities cannot be rendered for clients");
			log.critical("the usual cause is publicBaseUrl, which has no default — set PUBLIC_BASE_URL "
					+ "(or the PublicIris configuration's publicBaseUrl) to the absolute http(s) URL "
					+ "clients reach this portal on, e.g. https://opendata.example.de/dcat/rest/");
			log.critical("look for an IllegalArgumentException from the PublicIris component in the startup "
					+ "log: it says whether the value was absent, an uninterpolated $[env:...] placeholder, "
					+ "or missing a scheme");
			log.critical("every REST collection resource holds this as a mandatory reference, so while it is "
					+ "missing /rest/... answers 404 rather than serving unrebased identities");
			return new Result(log);
		}

		log.info("identities rendered under {}", iris.publicBase());
		return new Result(log);
	}
}
