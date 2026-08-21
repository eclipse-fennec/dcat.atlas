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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.jgit.GitServiceImpl;
import org.eclipse.fennec.jgit.api.GitConfig;
import org.eclipse.fennec.jgit.api.GitService;
import org.eclipse.jgit.api.Git;

/**
 * Test stand-in for the {@code GitService} the runtime binds: a bare repository in the
 * test's temporary directory.
 *
 * <h2>Why bare, and why local</h2>
 *
 * Bare because commits are written straight into the object database and leave a working
 * tree untouched — a non-bare repository would look, to anything reading its checkout, as
 * though every file had been deleted. Local because it keeps the suite offline and fast;
 * the remote path differs only in needing a push, which belongs in an integration test.
 *
 * <h2>One repository per directory</h2>
 *
 * Tests call {@code service()} more than once per test method to get a fresh store facade,
 * and every one of those has to see the same data. Keying by directory means one JUnit
 * {@code @TempDir} is one repository, however many services are built over it, while two
 * tests still get two.
 */
public final class TestGitStore {

	/** The base path the tests configure; exercises a non-empty one rather than the root. */
	public static final String BASE_PATH = "dcat";

	private static final Map<Path, GitService> SERVICES = new ConcurrentHashMap<>();

	private TestGitStore() {
	}

	/** The git service for a bare repository in {@code directory}, creating it once. */
	public static GitService at(Path directory) {
		return SERVICES.computeIfAbsent(directory.toAbsolutePath(), TestGitStore::create);
	}

	/**
	 * The committed bytes of a stored resource.
	 * <p>
	 * Tests that assert on the stored form used to read the file off disk. There is no file
	 * any more, and reading the blob is the equivalent — with the useful property that it
	 * only sees what was actually committed, so a test cannot accidentally pass on staged
	 * bytes that never became a commit.
	 */
	public static String stored(Path directory, String collection, String id) {
		try (InputStream in = at(directory).readLatestFile(StoreLayout.repoPath(BASE_PATH, collection, id))) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read " + collection + "/" + id, e);
		}
	}

	/** Every repository path currently committed, for tests asserting on the layout itself. */
	public static java.util.List<String> paths(Path directory) {
		return at(directory).getFiles().getFiles();
	}

	private static GitService create(Path directory) {
		try {
			// A fresh repository has no commits at all, which is exactly the state the store
			// has to cope with on a first boot - so the tests start there rather than seeding
			// an initial commit that would hide it.
			Git.init().setBare(true).setInitialBranch("main").setDirectory(directory.toFile()).call().close();
			GitServiceImpl service = new GitServiceImpl();
			service.activate(config(directory));
			return service;
		} catch (Exception e) {
			throw new IllegalStateException("Could not create a test repository in " + directory, e);
		}
	}

	/**
	 * A {@link GitConfig} for a local repository. Implemented as an anonymous class because
	 * a component property type is an annotation, and outside OSGi there is nothing to
	 * materialise one from a map.
	 */
	private static GitConfig config(Path directory) {
		return new GitConfig() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return GitConfig.class;
			}

			@Override
			public String repo() {
				return directory.toAbsolutePath().toString();
			}

			@Override
			public String branch() {
				return "main";
			}

			@Override
			public String remote() {
				// A local repository standing alone: the store's own tests are about what the
				// git object database holds, not about mirroring it anywhere.
				return "";
			}

			@Override
			public String privateKey() {
				return "";
			}

			@Override
			public String privateKeyPassphrase() {
				return "";
			}

			@Override
			public String knownHosts() {
				return "";
			}

			@Override
			public String authorName() {
				return "DCAT.Atlas Tests";
			}

			@Override
			public String authorEmail() {
				return "tests@dcat.atlas";
			}

			@Override
			public boolean pushOnCommit() {
				return false;
			}

			@Override
			public String username() {
				return "";
			}

			@Override
			public String password() {
				return "";
			}
		};
	}
}
