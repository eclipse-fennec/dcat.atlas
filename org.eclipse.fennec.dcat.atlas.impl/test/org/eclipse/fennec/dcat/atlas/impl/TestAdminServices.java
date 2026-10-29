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

import java.lang.annotation.Annotation;
import java.nio.file.Path;

import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;

/**
 * Builds the admin facade the way the tests need it: over a store of their own, with no
 * projection and nothing announced.
 * <p>
 * Most tests build a single handler directly and have no use for this. It exists for the
 * two that are about the facade itself — what it refuses to start without, and what a
 * health check sees when it is missing.
 */
public final class TestAdminServices {

	private TestAdminServices() {
	}

	/** A facade over a store at {@code storage}, serving no SPARQL. */
	public static DcatAdminServiceImpl facade(Path storage) {
		return facade(storage, false, null);
	}

	public static DcatAdminServiceImpl facade(Path storage, boolean sparqlSupported, DcatGraphService graphService) {
		return new DcatAdminServiceImpl(TestNotifiers.discarding(), TestStores.at(storage), graphService,
				config(sparqlSupported));
	}

	/**
	 * A component property type is an annotation, and outside OSGi there is nothing to
	 * materialise one from a map — the same reason {@code TestGitStore} builds its
	 * {@code GitConfig} by hand.
	 */
	public static DcatAdminConfig config(boolean sparqlSupported) {
		return new DcatAdminConfig() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return DcatAdminConfig.class;
			}

			@Override
			public boolean sparqlSupported() {
				return sparqlSupported;
			}

			@Override
			public String storeService_target() {
				return "";
			}
		};
	}
}
