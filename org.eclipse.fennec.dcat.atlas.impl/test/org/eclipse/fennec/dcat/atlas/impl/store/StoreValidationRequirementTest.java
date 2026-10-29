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
package org.eclipse.fennec.dcat.atlas.impl.store;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.nio.file.Path;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.TestGitStore;
import org.eclipse.fennec.dcat.atlas.impl.TestResourceSets;
import org.eclipse.fennec.jgit.api.GitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.apache.jena.shacl.ValidationReport;

/**
 * A store that says it validates on write will not start without something to validate with.
 *
 * <h2>The switch this replaces</h2>
 *
 * Enforcement used to be required by raising the DS reference's cardinality from
 * configuration ({@code validationService.cardinality.minimum=1}) on each of the five
 * admin services. It is {@code validateOnWrite} now, on the one store component: a second
 * attribute would only have let the two disagree, and a deployment that wants its writes
 * validated wants both floors, not one.
 *
 * <h2>Why the store refuses rather than carries on</h2>
 *
 * The model floor needs no service, but the SHACL one does, and
 * {@code ShaclValidation.check} reads an absent service as "no enforcement". A store that
 * stayed up would accept unchecked writes while its configuration said it checked them.
 * Failing activation is the loud version, and the health check names it.
 */
public class StoreValidationRequirementTest {

	@TempDir
	Path storage;

	@Test
	void aValidatingStoreWillNotStartWithoutTheValidationService() {
		IllegalStateException refused = assertThrows(IllegalStateException.class,
				() -> store(true, null));

		assertTrue(refused.getMessage().contains("DcatValidationService"),
				"the message has to name what is missing: " + refused.getMessage());
	}

	@Test
	void aValidatingStoreStartsWhenTheServiceIsThere() {
		assertDoesNotThrow(() -> store(true, new FakeValidation()));
	}

	/**
	 * {@code MODEL_VALIDATE=false} is how a corpus written before a constraint existed gets
	 * imported. It asks for no validation, so it may not have a validator either.
	 */
	@Test
	void aStoreThatDoesNotValidateStartsWithoutIt() {
		assertDoesNotThrow(() -> store(false, null));
	}

	private DcatGitStoreService store(boolean validateOnWrite, DcatValidationService validation) {
		GitService gitService = TestGitStore.at(storage);
		return new DcatGitStoreService(gitService, validation, TestResourceSets.factory(),
				config(validateOnWrite));
	}

	/**
	 * A component property type is an annotation, and outside OSGi there is nothing to
	 * materialise one from a map — the same reason {@code TestGitStore} builds its
	 * {@code GitConfig} by hand.
	 */
	private static StoreConfig config(boolean validateOnWrite) {
		return new StoreConfig() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return StoreConfig.class;
			}

			@Override
			public String basePath() {
				return TestGitStore.BASE_PATH;
			}

			@Override
			public boolean validateOnWrite() {
				return validateOnWrite;
			}

			@Override
			public String gitService_target() {
				return "";
			}
		};
	}

	/** Never consulted here: these tests are about activation, not about a write. */
	private static final class FakeValidation implements DcatValidationService {

		@Override
		public ValidationReport validate(EObject entity) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isWriteEnforced() {
			return true;
		}
	}
}
