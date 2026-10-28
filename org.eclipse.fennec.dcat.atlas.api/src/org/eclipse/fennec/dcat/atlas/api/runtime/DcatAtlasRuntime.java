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
package org.eclipse.fennec.dcat.atlas.api.runtime;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.Constants;

/**
 * What this portal holds, for anything that wants to watch it.
 *
 * <p>
 * An inventory of DCAT content and nothing else: how many catalogues, datasets, data
 * services and dataset series there are, and what each one is called. It says nothing about
 * the store, validation, the SPARQL projection or the REST endpoints — those are answered by
 * the health checks and by the service component runtime, each of which already reports its
 * own half better than a second copy here would.
 * </p>
 *
 * <p>
 * The shape OSGi uses for this: {@code JakartarsServiceRuntime} and
 * {@code ServiceComponentRuntime} both answer with DTOs, and nothing that asks has to know
 * how the thing inside works. Telemetry, a dashboard and an integration test all want the
 * same answer, and none of them should reach into the store layer to get it.
 * </p>
 *
 * <p>
 * <strong>How to be told about change.</strong> The service carries
 * {@link Constants#SERVICE_CHANGECOUNT}, and its value moves whenever the snapshot would
 * differ. A consumer binds it dynamically and gets a callback:
 * </p>
 *
 * <pre>
 * &#64;Reference(name = "runtime", policy = DYNAMIC, cardinality = OPTIONAL)
 * void setRuntime(DcatAtlasRuntime runtime, Map&lt;String, Object&gt; properties) { … }
 * void updatedRuntime(DcatAtlasRuntime runtime, Map&lt;String, Object&gt; properties) { … }
 * void unsetRuntime(DcatAtlasRuntime runtime) { … }
 * </pre>
 *
 * <p>
 * <strong>Two details DS will not warn you about.</strong> The callback is
 * {@code updated<Name>}, not {@code modified<Name>}: DS calls the first when a <em>bound
 * service's</em> properties change and the second when the <em>component's own
 * configuration</em> does, so a watcher that writes {@code modified} compiles, binds, and is
 * never told anything. And the properties have to be a method parameter — a field-injected
 * reference never receives them.
 * </p>
 *
 * @since 1.0
 */
@ProviderType
public interface DcatAtlasRuntime {

	/**
	 * What this portal holds now.
	 * <p>
	 * A copy of the maintained snapshot, so the call costs a copy and nothing else: the
	 * component rebuilds it when something changes rather than assembling it per call. A
	 * reader gets its own, because a DTO has public fields and a shared instance is one a
	 * reader could change under the next one.
	 *
	 * @return the current snapshot; never {@code null}, and empty-but-valid when nothing
	 *         is stored
	 */
	DcatAtlasRuntimeDTO snapshot();
}
