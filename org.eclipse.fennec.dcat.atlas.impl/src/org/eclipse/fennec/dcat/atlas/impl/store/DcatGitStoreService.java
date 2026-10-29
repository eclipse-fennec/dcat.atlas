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

import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.metatype.annotations.Designate;

/**
 * Opens git-backed store sessions, and holds the one configuration they share.
 * <p>
 * Stateless: a session is the per-operation object and carries everything that changes
 * ({@link GitStoreSession}). This exists to be configured once and bound by everything
 * that reads or writes, in place of the same four settings repeated per entity service.
 *
 * @author ilenia
 * @since Oct 28, 2026
 */
@Component(name = "DcatGitStoreService", service = { DcatStoreService.class, HealthCheck.class },
property = { HealthCheck.NAME + "=store", HealthCheck.TAGS + "=ready" },
configurationPid = "DcatGitStoreService", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = StoreConfig.class)
public class DcatGitStoreService implements DcatStoreService, HealthCheck {
	
	private volatile StoreConfig config;
	private GitService gitService;
	private DcatValidationService validationService;
	private ResourceSetFactory resourceSetFactory;

	
	/**
	 * The validation reference stays OPTIONAL — a deployment that never asked for SHACL
	 * runs without it — and STATIC on purpose. A static reference that loses its bound
	 * service is deactivated and reactivated by SCR, so the check below runs again and a
	 * store configured to require validation goes down with it. Made DYNAMIC, the field
	 * would simply be nulled and enforcement would stop without anything saying so.
	 */
	@Activate
	public DcatGitStoreService(@Reference(name = "gitService") GitService gitService, 
			@Reference(name = "validationService", cardinality = ReferenceCardinality.OPTIONAL) DcatValidationService validationService, 
			@Reference(name = "resourceSetFactory") ResourceSetFactory resourceSetFactory,
			StoreConfig config) {
		requireValidationWhenValidating(config, validationService);
		this.config = config;
		this.gitService = gitService;
		this.validationService = validationService;
		this.resourceSetFactory = resourceSetFactory;
	}

	/**
	 * Takes a changed configuration without being deactivated.
	 * <p>
	 * Load-bearing, not a nicety. Ten entity services hold this one statically, so without
	 * a modified method a change to {@code basePath} or {@code validateOnWrite} would
	 * deactivate every one of them and the JAX-RS resources bound to them — taking the
	 * portal's whole write surface down to flip a flag, and leaving prototype-scoped
	 * resources the whiteboard has not yet unregistered serving from disposed instances.
	 * <p>
	 * A session reads the configuration when it is opened, so one already in flight keeps
	 * what it started with.
	 */
	@Modified
	void modified(StoreConfig config) {
		requireValidationWhenValidating(config, validationService);
		this.config = config;
	}

	/**
	 * Refuses a configuration this store cannot honour — on activation, and again on every
	 * change.
	 * <p>
	 * {@code ShaclValidation} reads an absent service as "no enforcement", so a store that
	 * carried on would accept unvalidated writes while its configuration said it validated
	 * them.
	 */
	private static void requireValidationWhenValidating(StoreConfig config, DcatValidationService validationService) {
		if (config.validateOnWrite() && validationService == null) {
			throw new IllegalStateException(
					"This store validates on write, but no DcatValidationService is bound");
		}
	}
	
	@Override
	public DcatStoreSession open(Caller caller) {
		// Storage underneath, the portal's write rules on top. A second backend implements
		// StorageSession and is wrapped the same way; the rules are not its to re-implement.
		return new GuardedStoreSession(new GitStoreSession(resourceSetFactory, gitService, config), caller,
				validationService, config.validateOnWrite());
	}

	@Override
	public Optional<String> etag(String collection, String id) {
		return gitService.blobId(null, StoreLayout.repoPath(config.basePath(), collection, id));
	}

	@Override
	public boolean isReady() {
		return StoreHealth.ready(gitService);
	}

	@Override
	public String describe(String collection) {
		return StoreHealth.detail(gitService, config.basePath(), collection);
	}

	/**
	 * The commit the store's branch currently points at.
	 * <p>
	 * A commit id is exactly the property {@link DcatStoreService#revision()} asks for: it
	 * changes whenever anything in the repository changes, and not otherwise. It is the one
	 * place in this class where a commit id is the right answer — per resource it would be
	 * wrong, because it moves for every resource whenever any one of them is written, which
	 * is why {@link #etag} uses the blob id instead.
	 */
	@Override
	public Optional<String> revision() {
		// getFiles() also walks the tree, which is more than is needed - but it is the only
		// way the service exposes the tip, and it is a single in-memory walk of a small
		// repository against a poll measured in minutes. Revisit if that stops being true.
		return Optional.ofNullable(gitService.getFiles().getCommitId());
	}

	// --- F-25 readiness -----------------------------------------------------

	/**
	 * Reports whether the portal's persistence can serve (F-25). CRITICAL rather than
	 * WARN: an unreachable repository is a misconfiguration that no retry fixes, and the
	 * portal should be taken out of rotation.
	 *
	 * <h2>One check, not one per collection</h2>
	 *
	 * There used to be a {@code store:<collection>} check on each read service. They all
	 * asked this same object the same {@link #isReady()} and so could never disagree: a
	 * repository that cannot be opened takes every collection with it, and one that can is
	 * readable for all of them whether or not they hold anything yet. Four checks that
	 * always agree are four ways to read the same fact, so the collections are reported as
	 * lines of one check instead.
	 */
	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		boolean ready = isReady();
		for (String collection : StoreLayout.COLLECTIONS) {
			String detail = describe(collection);
			if (ready) {
				log.info("{}", detail);
			} else {
				log.critical("{}", detail);
			}
		}
		return new Result(log);
	}
}
