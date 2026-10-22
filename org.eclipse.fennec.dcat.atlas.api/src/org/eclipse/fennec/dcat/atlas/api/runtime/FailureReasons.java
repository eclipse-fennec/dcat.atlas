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

/**
 * The permitted values of {@link FailureDTO#reason}.
 *
 * <p>
 * Strings rather than an enum, following {@code BrokerRuntimeDTO}: a DTO travels, and a reader
 * that meets a reason it does not know should be able to report it rather than fail to
 * deserialise. The first eight come from {@link DcatAtlasRuntime#snapshot()} and cost nothing;
 * the last three only from {@link DcatAtlasRuntime#checkIntegrity()}.
 * </p>
 *
 * @since 1.0
 */
public final class FailureReasons {

	private FailureReasons() {
	}

	/** Enforcement is on and no shape file was parsed, so everything conforms. */
	public static final String SHAPES_NOT_LOADED = "SHAPES_NOT_LOADED";

	/** Enforcement is configured but will not reject anything. */
	public static final String ENFORCEMENT_INEFFECTIVE = "ENFORCEMENT_INEFFECTIVE";

	/** Controlled-vocabulary shapes are loaded with no vocabulary data behind them (F-22). */
	public static final String VOCABULARY_MISSING = "VOCABULARY_MISSING";

	/** The SPARQL projection is enabled but cannot answer completely. */
	public static final String GRAPH_NOT_READY = "GRAPH_NOT_READY";

	/** Two pieces of configuration that have to agree do not. */
	public static final String CONFIG_INCONSISTENT = "CONFIG_INCONSISTENT";

	/** The store could not be read when the snapshot was built. */
	public static final String STORE_UNREADABLE = "STORE_UNREADABLE";

	/** Writes are accepted and will not survive a restart. */
	public static final String STORE_NOT_PERSISTING = "STORE_NOT_PERSISTING";

	/** A component never started — the only way to see one that was never registered. */
	public static final String UNSATISFIED_COMPONENT = "UNSATISFIED_COMPONENT";

	/** A stored resource references something that is not there. Scan only. */
	public static final String DANGLING_REFERENCE = "DANGLING_REFERENCE";

	/** A stored resource could not be read back. Scan only. */
	public static final String UNPARSEABLE_RESOURCE = "UNPARSEABLE_RESOURCE";

	/** A stored resource does not satisfy the shapes. Scan only. */
	public static final String NON_CONFORMANT_RESOURCE = "NON_CONFORMANT_RESOURCE";

	/**
	 * Every reason, in the order above.
	 *
	 * <p>
	 * Exists for the consumer that has to report a value for each of them rather than only
	 * for the ones that occurred: with a gauge, a series that stops being published is not
	 * the same as a series reporting zero, and a dashboard watching
	 * {@code failures{reason=X} > 0} would simply stop seeing X the moment it was fixed —
	 * which looks identical to the exporter having gone away.
	 * </p>
	 */
	public static final java.util.List<String> ALL = java.util.List.of(SHAPES_NOT_LOADED, ENFORCEMENT_INEFFECTIVE,
			VOCABULARY_MISSING, GRAPH_NOT_READY, CONFIG_INCONSISTENT, STORE_UNREADABLE, STORE_NOT_PERSISTING,
			UNSATISFIED_COMPONENT, DANGLING_REFERENCE, UNPARSEABLE_RESOURCE, NON_CONFORMANT_RESOURCE);
}
