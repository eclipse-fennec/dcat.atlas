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
package org.eclipse.fennec.dcat.atlas.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.admin.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DataServiceAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetSeriesAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicView;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;

/**
 * Stores what a configured folder of XMI files says the portal should start out holding.
 *
 * <h2>What it is for</h2>
 *
 * A catalog that several generating systems publish into has to exist before any of them
 * can, and it must belong to none of them. That is what this writes: a resource with no
 * {@code managedBy}, which under the access rule (#57) every system may add members to and
 * none may fence off. The seeding is an ordinary write — the same admin service, the same
 * validation, the same commit — made as {@link Caller#ANONYMOUS}, which is also what a
 * person working through the UI is. There is no path into the store that a client does not
 * also have, so what the deployment test exercises is the real rule rather than a fixture.
 *
 * <h2>What it will not do</h2>
 *
 * It never replaces. See {@link Seeds#apply} for why that is the whole design and not a
 * precaution: by the second start the catalog is no longer what the file says, and the
 * difference is the other systems' work.
 *
 * <h2>Failing visibly</h2>
 *
 * A folder that cannot be seeded stops the framework, so a container that was told to seed
 * and could not fails at startup instead of serving a portal whose catalog never arrived.
 * It is configurable, but the default is to halt: the failure that is cheap to debug is the
 * one that happens immediately.
 */
@Component(name = InitialResourceLoader.PID, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = InitialResourceLoader.Config.class)
public class InitialResourceLoader {

	public static final String PID = "InitialDcatResources";

	private static final Logger LOG = Logger.getLogger(InitialResourceLoader.class.getName());

	private static final String SUFFIX = ".xmi";

	@Reference
	ResourceSetFactory resourceSetFactory;

	@Reference
	PublicIris publicIris;

	@Reference
	CatalogAdminService catalogs;

	@Reference
	DatasetAdminService datasets;

	@Reference
	DataServiceAdminService dataServices;

	@Reference
	DatasetSeriesAdminService datasetSeries;

	/**
	 * Seeds the folder, once, as the component activates.
	 *
	 * <p>
	 * Nothing waits for this and nothing asks it for anything afterwards — the result is
	 * resources in the store. What it does need is every admin service, and those are
	 * mandatory references, so DS does the waiting: a seed cannot run before the store
	 * and the validation behind those services are up.
	 * </p>
	 */
	@Activate
	void activate(BundleContext context, Config config) {
		try {
			seed(config.folder());
		} catch (RuntimeException failed) {
			LOG.log(Level.SEVERE, "[DCAT.Atlas] the initial resources could not be seeded: " + failed.getMessage(),
					failed);
			if (config.halt_on_error()) {
				haltFramework(context);
			}
			throw failed;
		}
	}

	private void seed(String folder) {
		if (folder == null || folder.isBlank() || folder.contains("$[")) {
			// Blank, or an un-interpolated configadmin template, which is what a deployment
			// that mounted no seeds looks like. Nothing was asked for, so nothing is wrong.
			LOG.info("[DCAT.Atlas] no initial resources folder configured, seeding nothing.");
			return;
		}
		Path directory = Path.of(folder);
		if (!Files.isDirectory(directory)) {
			throw new IllegalStateException(folder + " is not a directory. It is where the initial resources are "
					+ "read from, so a deployment that named it has to mount it.");
		}
		List<Path> files = seedFiles(directory);
		if (files.isEmpty()) {
			LOG.info("[DCAT.Atlas] no " + SUFFIX + " files in " + directory + ", seeding nothing.");
			return;
		}
		List<Seed> plan = Seeds.plan(files, this::read);
		List<Seed> written = Seeds.apply(plan, targets());
		LOG.info("[DCAT.Atlas] seeded " + written.size() + " of " + plan.size() + " initial resources from "
				+ directory + "; the other " + (plan.size() - written.size()) + " the portal already held."
				+ (written.isEmpty() ? "" : " Stored: " + named(written)));
	}

	/** The folder's {@code .xmi} files, in name order so a run is reproducible. */
	private static List<Path> seedFiles(Path directory) {
		try (Stream<Path> entries = Files.list(directory)) {
			return entries.filter(Files::isRegularFile)
					.filter(file -> file.getFileName().toString().endsWith(SUFFIX)).sorted().toList();
		} catch (IOException unreadable) {
			throw new IllegalStateException(directory + " could not be listed: " + unreadable.getMessage(),
					unreadable);
		}
	}

	/**
	 * One file, read and folded back to the identities the store uses.
	 *
	 * <p>
	 * The fold is what a request body gets from {@code PublicIriFilter}, and a seed is
	 * written by a person reading the portal — so a file may name its resources by the
	 * IRIs the portal serves rather than the host-free ones it stores. Both work, and
	 * neither puts the public host into the store.
	 * </p>
	 */
	private EObject read(Path file) {
		return PublicView.fold(SeedFiles.read(resourceSetFactory, file), publicIris);
	}

	/**
	 * The four collections a seed can go into, written as {@link Caller#ANONYMOUS}.
	 *
	 * <p>
	 * That caller is the point of the component: it stamps nothing, so what is seeded is
	 * managed by nobody, and every generating system may link its own resources into it.
	 * Seeding as a system would hand the shared catalog to whichever one happened to be
	 * named here.
	 * </p>
	 */
	private Map<String, SeedTarget> targets() {
		return Map.of(//
				DcatIds.DATASETS,
				SeedTarget.of(id -> datasets.getDataset(id).isPresent(),
						resource -> datasets.upsertDataset((Dataset) resource, Caller.ANONYMOUS)), //
				DcatIds.DATASET_SERIES,
				SeedTarget.of(id -> datasetSeries.getDatasetSeries(id).isPresent(),
						resource -> datasetSeries.upsertDatasetSeries((DatasetSeries) resource, Caller.ANONYMOUS)), //
				DcatIds.DATA_SERVICES,
				SeedTarget.of(id -> dataServices.getDataService(id).isPresent(),
						resource -> dataServices.upsertDataService((DataService) resource, Caller.ANONYMOUS)), //
				DcatIds.CATALOGS,
				SeedTarget.of(id -> catalogs.getCatalog(id).isPresent(),
						resource -> catalogs.upsertCatalog((Catalog) resource, Caller.ANONYMOUS)));
	}

	private static String named(List<Seed> seeds) {
		return seeds.stream().map(seed -> seed.collection() + "/" + seed.id()).toList().toString();
	}

	/**
	 * Stops the framework, from a thread of its own.
	 *
	 * <p>
	 * Stopping it from inside the activation would deadlock on the very activation that
	 * is failing. Not a daemon thread, so the stop outlives the component it is stopping.
	 * </p>
	 */
	private static void haltFramework(BundleContext context) {
		LOG.severe("[DCAT.Atlas] halt.on.error is set, stopping the framework so the startup fails visibly.");
		Bundle systemBundle = context.getBundle(Constants.SYSTEM_BUNDLE_LOCATION);
		Thread halter = new Thread(() -> {
			try {
				systemBundle.stop();
			} catch (BundleException wontStop) {
				LOG.log(Level.SEVERE, "[DCAT.Atlas] the framework could not be stopped", wontStop);
			}
		}, "InitialResourceLoader-halt");
		halter.setDaemon(false);
		halter.start();
	}

	@ObjectClassDefinition(name = "DCAT.Atlas Initial Resources",
			description = "Stores a folder of XMI resources on startup, unmanaged, so generating systems have "
					+ "something to publish into. Only what the portal does not already hold is written.")
	public @interface Config {

		@AttributeDefinition(name = "Initial Resources Folder", required = false,
				description = "The folder the .xmi seeds are read from, normally a read-only mount. Empty seeds "
						+ "nothing; a folder that is named and not there fails the startup.")
		String folder() default "";

		@AttributeDefinition(name = "Halt on Error",
				description = "Stop the framework when the folder cannot be seeded, so a container fails visibly "
						+ "instead of serving a portal without the catalog it was given.")
		boolean halt_on_error() default true;
	}
}
