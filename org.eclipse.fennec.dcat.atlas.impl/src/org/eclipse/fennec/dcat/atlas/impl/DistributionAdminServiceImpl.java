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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.DistributionAdminService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper.Store;
import org.eclipse.fennec.dcat.atlas.impl.helper.Members;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import dcat.DataService;
import dcat.Dataset;
import dcat.Distribution;

/**
 * File-backed {@link DistributionAdminService} (write side).
 *
 * <h2>Every write is a dataset write (FR-10)</h2>
 *
 * Distributions are contained in their Dataset, so creating, changing or deleting
 * one rewrites the Dataset's file — there is no second store to keep in step, and
 * no way for the two to disagree about what belongs to what. A Distribution
 * therefore cannot outlive its Dataset, which is what FR-10 asks for.
 * <p>
 * {@code dcat:accessService} is the exception: it points at a DataService, which is
 * a catalog entity in its own right, so that stays a cross-resource reference.
 */
@Component(name = "DistributionAdminService", service = DistributionAdminService.class)
@Designate(ocd = StoreConfig.class)
public class DistributionAdminServiceImpl extends DistributionReadOnlyServiceImpl implements DistributionAdminService {

	@Activate
	public DistributionAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference(name = "gitService") GitService gitService, @Reference DatasetReadOnlyService datasetService, StoreConfig config) {
		super(resourceSetFactory, gitService, datasetService, config);
	}

	/** Package-visible for tests. */
	DistributionAdminServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			DatasetReadOnlyService datasetService) {
		super(resourceSetFactory, gitService, basePath, datasetService);
	}

	/** Package-visible for tests that need the model constraints enforced. */
	DistributionAdminServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			DatasetReadOnlyService datasetService, boolean validateOnWrite) {
		super(resourceSetFactory, gitService, basePath, datasetService, validateOnWrite);
	}

	/**
	 * The RDF projection behind the SPARQL endpoint, maintained here rather than in
	 * the REST layer: this service is the persistence boundary, and REST is only one
	 * of its callers (persistence plan constraint G2). Optional and dynamic — absent
	 * simply means no SPARQL.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatGraphService graphService;

	/**
	 * On-write SHACL enforcement (FR-4), maintained here rather than in the REST layer:
	 * this service is the persistence boundary, and REST is only one of its callers.
	 * <p>
	 * Optional and dynamic, so absence simply means no enforcement — see
	 * {@link org.eclipse.fennec.dcat.atlas.impl.helper.ShaclValidation}. An operator who
	 * needs the strict reading raises this reference's minimum cardinality in
	 * configuration ({@code validationService.cardinality.minimum=1}), which makes this
	 * component unsatisfiable without a validation service instead of letting writes
	 * through unchecked.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validationService;

	/** Not an override: a Distribution has no store of its own, so this class does not extend {@code AbstractEntityStore}. */
	private DcatValidationService writeValidation() {
		return validationService;
	}

	@Override
	public Distribution upsertDistributionToDataset(String datasetId, Distribution distribution) {
		if (distribution == null) {
			throw new IllegalArgumentException("Cannot store nothing in dataset " + datasetId);
		}
		Store store = store();
		Dataset dataset = requireDataset(store, datasetId);

		// Mint only when the caller supplied no identity at all (D2/FR-3); one that is
		// not ours is refused rather than silently replaced — see DcatIds.idForWrite.
		String distributionId = DcatIds.distributionIdForWrite(datasetId, distribution.getAbout());
		distribution.setAbout(StoreLayout.distributionIri(datasetId, distributionId));

		// Replace in place so a repeated PUT does not accumulate copies.
		find(dataset, datasetId, distributionId).ifPresent(dataset.getDistribution()::remove);
		dataset.getDistribution().add(distribution);
		store.save(dataset);
		store.commit("Store distribution %s of dataset %s".formatted(distributionId, datasetId));
		reproject(datasetId, distributionId);
		return distribution;
	}

	@Override
	public void deleteDistributionFromDataset(String datasetId, String distributionId) {
		Store store = store();
		store.<Dataset>get(StoreLayout.DATASETS, datasetId).ifPresent(dataset -> {
			if (find(dataset, datasetId, distributionId).map(dataset.getDistribution()::remove).orElse(false)) {
				store.save(dataset);
				store.commit("Delete distribution %s of dataset %s".formatted(distributionId, datasetId));
				reproject(datasetId, distributionId);
			}
		});
	}

	// --- FR-10 accessService link ------------------------------------------

	@Override
	public Distribution addAccessServiceToDistribution(String datasetId, String distributionId,
			DataService dataService) {
		if (dataService == null) {
			throw new IllegalArgumentException("Cannot link nothing as an access service");
		}
		Store store = store();
		requireDistribution(store, datasetId, distributionId);
		String serviceId = DcatIds.idForWrite(StoreLayout.DATA_SERVICES, dataService.getAbout());
		store.put(StoreLayout.DATA_SERVICES, serviceId, dataService);
		return connect(store, datasetId, distributionId, serviceId,
				"Add access service %s to distribution %s of dataset %s".formatted(serviceId, distributionId,
						datasetId));
	}

	@Override
	public Distribution linkAccessServiceToDistribution(String datasetId, String distributionId,
			String dataServiceId) {
		Store store = store();
		requireDistribution(store, datasetId, distributionId);
		if (store.get(StoreLayout.DATA_SERVICES, dataServiceId).isEmpty()) {
			throw new NoSuchElementException("Unknown data service: " + dataServiceId);
		}
		return connect(store, datasetId, distributionId, dataServiceId,
				"Link access service %s to distribution %s of dataset %s".formatted(dataServiceId, distributionId,
						datasetId));
	}

	@Override
	public void deleteAccessServiceFromDistribution(String datasetId, String distributionId, String dataServiceId) {
		Store store = store();
		store.<Dataset>get(StoreLayout.DATASETS, datasetId).ifPresent(dataset -> find(dataset, datasetId,
				distributionId).ifPresent(distribution -> {
					if (Members.remove(distribution.getAccessService(), StoreLayout.DATA_SERVICES, dataServiceId)) {
						store.save(dataset);
						store.commit("Unlink access service %s from distribution %s of dataset %s"
								.formatted(dataServiceId, distributionId, datasetId));
						reproject(datasetId, distributionId);
					}
				}));
	}

	// --- helpers ------------------------------------------------------------

	private Distribution connect(Store store, String datasetId, String distributionId, String dataServiceId,
			String message) {
		Dataset dataset = requireDataset(store, datasetId);
		Distribution distribution = requireDistribution(store, datasetId, distributionId);
		if (!Members.contains(distribution.getAccessService(), StoreLayout.DATA_SERVICES, dataServiceId)) {
			distribution.getAccessService().add(store.<DataService>get(StoreLayout.DATA_SERVICES, dataServiceId)
					.orElseThrow(() -> new NoSuchElementException("Unknown data service: " + dataServiceId)));
			store.save(dataset);
		}
		// Commits whatever the operation staged, as one commit; a repeat stages nothing and
		// so leaves the dataset - and its ETag - untouched. See CatalogAdminServiceImpl.
		store.commit(message);
		reproject(datasetId, distributionId);
		return distribution;
	}

	private Store store() {
		return DcatHelper.open(resourceSetFactory, gitService, basePath, validateOnWrite, writeValidation());
	}

	private static Dataset requireDataset(Store store, String datasetId) {
		return store.<Dataset>get(StoreLayout.DATASETS, datasetId)
				.orElseThrow(() -> new NoSuchElementException("Unknown dataset: " + datasetId));
	}

	private static Distribution requireDistribution(Store store, String datasetId, String distributionId) {
		return find(requireDataset(store, datasetId), datasetId, distributionId)
				.orElseThrow(() -> new NoSuchElementException(
						"Unknown distribution: " + distributionId + " in dataset: " + datasetId));
	}

	/**
	 * Re-projects one distribution into the RDF graph, if a projection is present.
	 * The owning dataset is required: a distribution is only resolvable through it.
	 */
	private void reproject(String datasetId, String distributionId) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(DcatEntity.DISTRIBUTION, datasetId, distributionId);
		}
	}
}
