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

import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.integrity.References;
import org.eclipse.fennec.dcat.atlas.impl.store.DcatHelper.Store;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreConfig;
import org.eclipse.fennec.dcat.atlas.impl.validation.ShaclValidation;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Dataset;

/**
 * File-backed {@link DatasetAdminService} (write side).
 * <p>
 * A Dataset's Distributions are contained, so storing a Dataset stores them with
 * it — there is no separate distribution write to keep in step (FR-10).
 */
@Component(name = "DatasetAdminService", service = DatasetAdminService.class)
@Designate(ocd = StoreConfig.class)
public class DatasetAdminServiceImpl extends DatasetReadOnlyServiceImpl implements DatasetAdminService {

	@Activate
	public DatasetAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference(name = "gitService") GitService gitService, StoreConfig config) {
		super(resourceSetFactory, gitService, config);
	}

	/** Package-visible for tests. */
	DatasetAdminServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath) {
		super(resourceSetFactory, gitService, basePath);
	}

	/** Package-visible for tests that need the model constraints enforced. */
	DatasetAdminServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			boolean validateOnWrite) {
		super(resourceSetFactory, gitService, basePath, validateOnWrite);
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
	 * {@link org.eclipse.fennec.dcat.atlas.impl.validation.ShaclValidation}. An operator who
	 * needs the strict reading raises this reference's minimum cardinality in
	 * configuration ({@code validationService.cardinality.minimum=1}), which makes this
	 * component unsatisfiable without a validation service instead of letting writes
	 * through unchecked.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validationService;

	@Override
	protected DcatValidationService writeValidation() {
		return validationService;
	}

	@Override
	public Dataset upsertDataset(Dataset dataset) {
		String id = idOrMint(dataset);
		Store store = store();
		store.put(collection, id, dataset);
		store.commit("Store dataset " + id);
		reproject(id);
		return dataset;
	}

	@Override
	public List<String> deleteDataset(String id, boolean cascade) {
		Store store = store();
		List<String> unlinked = References.detach(store, collection, id, cascade);
		store.delete(collection, id);
		// One commit for the delete and every unlink it caused; see CatalogAdminServiceImpl.
		store.commit(cascade ? "Delete dataset " + id + " and unlink its referrers" : "Delete dataset " + id);
		reproject(id);
		return unlinked;
	}

	/** Re-projects one dataset into the RDF graph, if a projection is present. */
	private void reproject(String id) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(DcatEntity.DATASET, id);
		}
	}
}
