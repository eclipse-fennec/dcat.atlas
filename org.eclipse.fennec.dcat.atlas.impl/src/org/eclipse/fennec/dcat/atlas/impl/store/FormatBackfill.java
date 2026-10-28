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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.impl.runtime.DcatChangeNotifier;
import org.eclipse.fennec.dcat.atlas.impl.store.DcatHelper.Store;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

/**
 * Completes {@code dct:format} on the distributions that are <em>already</em> stored,
 * once per start (issue #68).
 * <p>
 * {@link FormatDefaults} runs on every write, so anything written from now on is
 * complete. What it cannot reach is the content written before it existed, and a
 * portal whose entries are rarely edited would carry the gap for years. This pass
 * reads every root that can hold a Distribution — datasets, catalogs and series —
 * applies the same completion, and writes back only what changed, as one commit.
 * <p>
 * <h2>Why this is a write the server makes on its own behalf</h2>
 * The value is deterministic and the next write by anyone would add it anyway, so
 * it is staged as bookkeeping (see {@link Store#saveBookkeeping}): it neither
 * revalidates content the operator accepted long ago, nor asks who manages the
 * resource — a system's entry gets the same completion a person's does.
 * <p>
 * <h2>Why it is safe to run on every start</h2>
 * A second pass finds nothing to complete and commits nothing, so the history gains
 * at most one commit per deployment. A pass that fails — the store not reachable,
 * another instance having committed first — is logged and does not take the portal
 * down; it simply runs again next time.
 */
@Component(name = "FormatBackfill", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = FormatBackfillConfig.class)
public class FormatBackfill {

	private static final Logger LOG = Logger.getLogger(FormatBackfill.class.getName());

	/** The collections whose roots are Datasets and so can hold Distributions. */
	private static final Map<String, DcatEntity> COLLECTIONS = Map.of( //
			StoreLayout.DATASETS, DcatEntity.DATASET, //
			StoreLayout.CATALOGS, DcatEntity.CATALOG, //
			StoreLayout.DATASET_SERIES, DcatEntity.DATASET_SERIES);

	private final ResourceSetFactory resourceSetFactory;
	private final GitService gitService;
	private final String basePath;

	/** Optional: absent means no SPARQL, and nothing to re-project. */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatGraphService graphService;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatChangeNotifier changeNotifier;

	@Activate
	public FormatBackfill(@Reference ResourceSetFactory resourceSetFactory,
			@Reference(name = "gitService") GitService gitService, FormatBackfillConfig config) {
		this(resourceSetFactory, gitService, config.basePath());
		if (!config.enabled()) {
			LOG.info("[DCAT.Atlas] format backfill disabled by configuration; stored distributions are left as they are.");
			return;
		}
		try {
			run();
		} catch (RuntimeException failed) {
			// Never let a completion pass take the portal down: the data is no worse
			// than before, and the pass runs again on the next start.
			LOG.log(Level.WARNING, "[DCAT.Atlas] the format backfill did not complete; stored distributions "
					+ "keep their current dct:format until the next start or write: " + failed.getMessage(), failed);
		}
	}

	/** Package-visible for tests. */
	FormatBackfill(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath) {
		this.resourceSetFactory = resourceSetFactory;
		this.gitService = gitService;
		this.basePath = StoreLayout.requireSafeBasePath(basePath);
	}

	/**
	 * One pass over the store.
	 *
	 * @return how many distributions received a {@code dct:format}; 0 means nothing was
	 *         written and the history is untouched
	 */
	int run() {
		if (!StoreHealth.ready(gitService)) {
			LOG.warning("[DCAT.Atlas] the store is not readable yet; skipping the format backfill for this start.");
			return 0;
		}
		Store store = DcatHelper.open(resourceSetFactory, gitService, basePath, false, null, Caller.ANONYMOUS);
		List<Touched> touched = new ArrayList<>();
		int completed = 0;
		for (Map.Entry<String, DcatEntity> collection : COLLECTIONS.entrySet()) {
			for (String id : store.ids(collection.getKey())) {
				Optional<EObject> root = store.get(collection.getKey(), id);
				if (root.isEmpty()) {
					continue;
				}
				int filled = FormatDefaults.apply(root.get());
				if (filled > 0) {
					store.saveBookkeeping(root.get());
					touched.add(new Touched(collection.getKey(), collection.getValue(), id));
					completed += filled;
				}
			}
		}
		if (completed == 0) {
			LOG.info("[DCAT.Atlas] format backfill: every stored distribution with a known media type already has a dct:format.");
			return 0;
		}
		store.commit("Complete dct:format from dcat:mediaType on " + completed + " distribution(s) in " + touched.size()
				+ " resource(s) (#68)");
		for (Touched entry : touched) {
			announce(entry);
		}
		LOG.info("[DCAT.Atlas] format backfill: completed dct:format on " + completed + " distribution(s) in "
				+ touched.size() + " resource(s): " + touched.stream().map(Touched::id).toList());
		return completed;
	}

	/** What a write does after its commit, so SPARQL and the runtime inventory see it. */
	private void announce(Touched entry) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(entry.entity(), entry.id());
		}
		DcatChangeNotifier notifier = changeNotifier;
		if (notifier != null) {
			notifier.entityChanged(entry.collection(), entry.id());
		}
	}

	private record Touched(String collection, DcatEntity entity, String id) {
	}
}
