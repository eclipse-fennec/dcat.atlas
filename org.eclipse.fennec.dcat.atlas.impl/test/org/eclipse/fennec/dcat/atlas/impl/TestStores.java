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

import org.apache.jena.shacl.ValidationReport;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.store.DcatGitStoreService;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreConfig;

/**
 * The store the entity services bind, for a test that builds one by hand.
 *
 * <h2>Why a validation service is always supplied</h2>
 *
 * The store refuses to start when {@code validateOnWrite} is on and nothing is bound —
 * see {@code StoreValidationRequirementTest}. A runtime always has a
 * {@code DcatValidationService}, because the validation bundle is in every bndrun and its
 * component activates whether or not the shapes directories exist; what an operator turns
 * off is {@code isWriteEnforced()}. So the default here mirrors that: a service is bound
 * and enforces nothing, which is the shape the suite has always run in.
 */
public final class TestStores {

	private TestStores() {
	}

	/** Model constraints on, SHACL bound but not enforcing — the shipped default. */
	public static DcatGitStoreService at(Path directory) {
		return at(directory, true, notEnforcing());
	}

	/** For the tests that are about {@code validateOnWrite} itself. */
	public static DcatGitStoreService at(Path directory, boolean validateOnWrite) {
		return at(directory, validateOnWrite, notEnforcing());
	}

	/** For the tests that are about SHACL: {@code validation} decides what a write is checked against. */
	public static DcatGitStoreService at(Path directory, DcatValidationService validation) {
		return at(directory, true, validation);
	}

	private static DcatGitStoreService at(Path directory, boolean validateOnWrite, DcatValidationService validation) {
		return new DcatGitStoreService(TestGitStore.at(directory), validation, TestResourceSets.factory(),
				config(validateOnWrite));
	}

	/** Bound, so the store starts; enforcing nothing, so only the model constraints refuse a write. */
	private static DcatValidationService notEnforcing() {
		return new DcatValidationService() {

			@Override
			public ValidationReport validate(EObject entity) {
				return ValidationReport.create().build();
			}

			@Override
			public boolean isWriteEnforced() {
				return false;
			}
		};
	}

	/**
	 * A component property type is an annotation, and outside OSGi there is nothing to
	 * materialise one from a map — the same reason {@link TestGitStore} builds its
	 * {@code GitConfig} by hand.
	 */
	public static StoreConfig config(boolean validateOnWrite) {
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
}
