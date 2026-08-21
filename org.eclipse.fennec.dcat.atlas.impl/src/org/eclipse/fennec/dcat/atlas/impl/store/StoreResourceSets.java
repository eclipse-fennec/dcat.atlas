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

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;

/**
 * Builds the {@link ResourceSet} the stores read and write through: XMI, with a
 * {@link DcatGitUriHandler} at the front of the {@code URIConverter}'s handler list, so a
 * {@code dcat:dataset} link from a Catalog resolves into the dataset store as an ordinary
 * EMF cross-resource reference and the bytes come from git.
 *
 * <h2>Why one per operation rather than one shared</h2>
 *
 * A shared {@code ResourceSet} would cache across operations, but EMF resource
 * sets are not thread-safe and five DS components serve concurrent REST requests
 * through them — so sharing means a coarse lock over all store I/O. A
 * per-operation set resolves cross-resource references exactly as correctly; the
 * only thing given up is cache reuse between operations. Revisit if profiling
 * ever says the reads matter.
 */
public final class StoreResourceSets {

	private StoreResourceSets() {
	}

	/**
	 * Options every store save must use. The {@link AbsoluteUriHandler} is not
	 * optional — see its comment.
	 */
	public static Map<Object, Object> saveOptions() {
		return Map.of(//
				XMLResource.OPTION_ENCODING, "UTF-8", //
				XMLResource.OPTION_URI_HANDLER, new AbsoluteUriHandler());
	}

	public static Map<Object, Object> loadOptions() {
		return Map.of(XMLResource.OPTION_ENCODING, "UTF-8");
	}

	/**
	 * A resource set that reads and writes the store in {@code gitService}'s repository,
	 * staging its writes in {@code pending}.
	 */
	public static ResourceSet create(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			PendingChanges pending) {
		ResourceSet resourceSet = resourceSetFactory.createResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new StoreResourceFactory());
		// At the front, because EMF asks handlers in order and the default one would claim an
		// http:// URI and try to fetch it over the network.
		resourceSet.getURIConverter().getURIHandlers().add(0,
				new DcatGitUriHandler(gitService, basePath, pending));
		return resourceSet;
	}

	/**
	 * The URI a stored resource is known by inside the resource set — its logical
	 * identity, with no file extension.
	 * <p>
	 * The extension is deliberately absent <em>here</em>. EMF derives the href of a
	 * cross-resource reference from the target's resource URI, so an {@code .xmi} in this
	 * URI would end up inside a stored (and, rebased, served) identity — naming a
	 * serialization format in something whose whole job is to identify a resource
	 * independently of how it happens to be written.
	 * <p>
	 * The stored blob does carry {@code .xmi}, which used to be impossible: while the URI
	 * map made the URI the path, one could not have an extension without the other. The
	 * handler decides the path now, so the blob is named for what it is and the identity
	 * stays free of it — see {@link StoreLayout}.
	 */
	public static URI resourceUri(String collection, String id) {
		return URI.createURI(StoreLayout.logicalIri(collection, id));
	}

	private static final class StoreResourceFactory extends XMIResourceFactoryImpl {
		@Override
		public Resource createResource(URI uri) {
			return new RootFragmentXmiResource(uri);
		}
	}

	/**
	 * Names the sole root object {@code /} instead of its {@code rdf:about}.
	 * <p>
	 * {@code about} is {@code iD="true"}, so EMF would otherwise use it as the XMI
	 * id and emit
	 * {@code href="http://dcat.atlas/datasets/air.xmi#http://dcat.atlas/datasets/air"}
	 * — the identity written twice, and a fragment that would need rebasing along
	 * with the rest. Since each store file holds exactly one root, {@code /} names
	 * it unambiguously.
	 */
	static final class RootFragmentXmiResource extends XMIResourceImpl {

		RootFragmentXmiResource(URI uri) {
			super(uri);
		}

		@Override
		public String getURIFragment(EObject eObject) {
			if (getContents().size() == 1 && getContents().get(0) == eObject) {
				return "/";
			}
			return super.getURIFragment(eObject);
		}
	}

	/**
	 * Keeps cross-resource hrefs absolute.
	 * <p>
	 * EMF deresolves an href against the saving resource's URI by default, which
	 * turns a logical identity into a relative file path
	 * ({@code href="../datasets/air#/"}). That is a storage detail leaking into
	 * something we also serve to clients, and it loses the identity we deliberately
	 * made deployment-independent. Measured, not theoretical.
	 */
	static final class AbsoluteUriHandler extends URIHandlerImpl {

		@Override
		public URI deresolve(URI uri) {
			return uri;
		}
	}
}
