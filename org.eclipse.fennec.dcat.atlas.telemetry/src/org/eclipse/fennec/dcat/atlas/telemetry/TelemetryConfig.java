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
package org.eclipse.fennec.dcat.atlas.telemetry;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/** What the gauges are called, and how many of them there are. */
@ObjectClassDefinition(name = "DCAT.Atlas telemetry", description = "OpenTelemetry gauges over the runtime snapshot")
public @interface TelemetryConfig {

	@AttributeDefinition(name = "Instrumentation scope", description = "The OpenTelemetry instrumentation scope name")
	String scope() default "org.eclipse.fennec.dcat.atlas";

	/**
	 * One point per catalog rather than one number for all of them.
	 * <p>
	 * Off by default, and that is a cost decision rather than a taste one: a backend billed
	 * per series pays for every catalog in the portal, forever, and the aggregate answers
	 * most questions.
	 */
	@AttributeDefinition(name = "Per-catalog series",
			description = "Report dataset counts per catalog as their own series, not only in aggregate")
	boolean catalogSeries() default false;
}
