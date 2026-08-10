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
import org.osgi.service.component.annotations.Component;

/**
 * Liveness signal (F-25): the framework is up and the health executor can run a check.
 * <p>
 * It deliberately inspects nothing. A failing liveness probe tells an orchestrator to
 * <em>restart</em> the container, and none of the things that can actually be wrong here
 * — an unmounted store, shapes that failed to load — are fixed by a restart. Those belong
 * to the {@code ready} tag instead.
 * <p>
 * Tagged {@code live}, which the liveness servlet selects on.
 */
@Component(name = "DcatLivenessHealthCheck", service = HealthCheck.class, property = {
		HealthCheck.NAME + "=liveness", HealthCheck.TAGS + "=live" })
public class LivenessHealthCheck implements HealthCheck {

	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		log.info("DCAT.Atlas is running");
		return new Result(log);
	}

}
