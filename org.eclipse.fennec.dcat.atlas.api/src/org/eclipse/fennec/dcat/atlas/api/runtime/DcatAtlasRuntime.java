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
 * What this portal is configured to do and what it holds, for anything that wants to watch it.
 *
 * <p>
 * The shape OSGi uses for this: {@code JakartarsServiceRuntime} and
 * {@code ServiceComponentRuntime} both answer with DTOs, and nothing that asks has to know
 * how the thing inside works. Telemetry, a health check and an integration test all want the
 * same answer, and none of them should reach into the store layer to get it.
 * </p>
 *
 * <p>
 * <strong>It reports what is missing, too.</strong> Six states this portal can be in serve
 * {@code 200} while being wrong — the sharpest being {@code enforceOnWrite=true} with no
 * shapes loaded, where validation reports conformance for everything. A service collector can
 * only show what registered, so it can never name the thing that did not.
 * {@link DcatAtlasRuntimeDTO#failures} can, and that is half of why this exists.
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
	 * <strong>Configuration</strong> property naming the base URL this portal serves on,
	 * reported as {@link DcatAtlasRuntimeDTO#endpoints}.
	 *
	 * <p>
	 * A configuration property and not a service property, which is the same choice the
	 * Atlas runtime service makes for the same reason. {@code ChangeCountPublisher} takes
	 * its extra properties once, in the constructor, and copies them into an unmodifiable
	 * map: emf.services#188 made it possible to publish a property <em>beside</em>
	 * {@link Constants#SERVICE_CHANGECOUNT}, but not to change one afterwards. The effective
	 * endpoint is not known at activation — when nothing is configured it comes from the
	 * Jakarta RS whiteboard, which may register later — so a service property would be
	 * right only in the deployments that spelled it out and silently absent in the rest.
	 * </p>
	 *
	 * <p>
	 * Waiting for the endpoint before registering is the other way round, and worse: this
	 * service exists to be present when things have not come up, so a portal whose REST
	 * layer never started is exactly when it must still answer.
	 * </p>
	 */
	String DCAT_ATLAS_ENDPOINT = "dcat.atlas.endpoint";

	/**
	 * What this portal holds now.
	 * <p>
	 * A copy of the maintained snapshot, so the call costs a copy and nothing else: the
	 * component rebuilds it when something changes rather than assembling it per call.
	 * A reader gets its own, because a DTO has public fields and a shared instance is one
	 * a reader could change under the next one.
	 *
	 * @return the current snapshot; never {@code null}, and empty-but-valid when nothing
	 *         is configured
	 */
	DcatAtlasRuntimeDTO snapshot();

	/**
	 * Reads every stored resource and reports what is wrong with the content.
	 * <p>
	 * <strong>Not cheap, and never called from {@link #snapshot()}.</strong> This is the
	 * full scan the snapshot refuses to do: dangling references, unparseable resources, and
	 * stored content that no longer satisfies the shapes.
	 *
	 * @return what the scan found; never {@code null}
	 */
	IntegrityDTO checkIntegrity();
}
