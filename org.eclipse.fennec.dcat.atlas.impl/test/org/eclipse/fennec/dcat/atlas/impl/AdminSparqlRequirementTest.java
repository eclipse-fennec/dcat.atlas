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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.eclipse.fennec.dcat.atlas.api.graph.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A portal that says it supports SPARQL will not start without something to project into.
 *
 * <h2>Why a flag and not a dynamic reference</h2>
 *
 * Whether there is a SPARQL endpoint is decided when the deployment is assembled, not while
 * it runs: either the sparql bundle is installed or it is not. A dynamic reference models a
 * service that comes and goes, and paid for that with handlers that could not simply hold
 * what they were given. The flag says the same thing the store's {@code validateOnWrite}
 * says — this deployment makes a promise, so it does not start unless it can keep it.
 *
 * <h2>Why the facade refuses rather than carries on</h2>
 *
 * {@code reproject} reads an absent graph service as "no projection", so a portal that
 * stayed up would answer SPARQL queries from a projection nothing was updating. Failing
 * activation is the loud version.
 */
public class AdminSparqlRequirementTest {

	@TempDir
	Path storage;

	@Test
	void aSparqlPortalWillNotStartWithoutTheGraphService() {
		IllegalStateException refused = assertThrows(IllegalStateException.class,
				() -> facade(true, null));

		assertTrue(refused.getMessage().contains("DcatGraphService"),
				"the message has to name what is missing: " + refused.getMessage());
	}

	@Test
	void aSparqlPortalStartsWhenTheServiceIsThere() {
		assertDoesNotThrow(() -> facade(true, new FakeGraph()));
	}

	/** The default deployment: no sparql bundle, and nothing to bind. */
	@Test
	void aPortalWithoutSparqlStartsWithoutIt() {
		assertDoesNotThrow(() -> facade(false, null));
	}

	/**
	 * The flag says what this deployment promises, not what happens to be installed. A
	 * graph service that is bound anyway is simply used.
	 */
	@Test
	void aPortalWithoutSparqlStartsWithItToo() {
		assertDoesNotThrow(() -> facade(false, new FakeGraph()));
	}

	private DcatAdminServiceImpl facade(boolean sparqlSupported, DcatGraphService graphService) {
		return TestAdminServices.facade(storage, sparqlSupported, graphService);
	}

	/** Never consulted here: these tests are about activation, not about a write. */
	private static final class FakeGraph implements DcatGraphService {

		@Override
		public void invalidate(DcatEntity entity, String parentId, String id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void rebuild() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isReady() {
			return true;
		}
	}
}
