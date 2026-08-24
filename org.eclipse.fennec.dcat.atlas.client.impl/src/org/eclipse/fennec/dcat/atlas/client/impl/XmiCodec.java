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
package org.eclipse.fennec.dcat.atlas.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClientException;

import adms.AdmsPackage;
import dcat.DcatPackage;
import foaf.FoafPackage;
import rdf.RdfPackage;
import spdx.SpdxPackage;
import terms.TermsPackage;
import vcard.VcardPackage;

/**
 * Reads and writes DCAT entities as {@code application/xmi}.
 *
 * <h2>Why plain EMF and not the Fennec codec</h2>
 *
 * XMI is EMF's own format, so a {@link ResourceSet} with the standard XMI factory is all
 * that is needed — which keeps the codec bundle, and its transitive Jackson, out of a
 * consumer's classpath. The portal writes with the codec and reads with it, but what goes
 * over the wire is ordinary XMI: attributes as XML attributes ({@code about="…"},
 * {@code <title lang="en" value="…"/>}), multi-valued attributes as repeated elements
 * ({@code <language>…</language>}), containment as nested elements. That is exactly what
 * this produces.
 *
 * <h2>The package registry has to be explicit</h2>
 *
 * In OSGi the model's {@code EPackage}s arrive through the framework registry; in plain
 * Java nothing registers them, and an unregistered namespace makes a load fail with a
 * feature-not-found rather than anything that names the real problem. So every package the
 * DCAT-AP.de model spans is registered here, on a {@link ResourceSet} built per call.
 *
 * <h2>A fresh ResourceSet per call, deliberately</h2>
 *
 * Entities handed in by a caller are usually already attached to a resource of their own —
 * they came from that caller's own model. Adding such an object to a shared resource would
 * <em>move</em> it out of the caller's, which is the kind of side effect a client library
 * must not have. A per-call {@link ResourceSet} holding a copy avoids it, and a
 * registration is not a hot path.
 */
final class XmiCodec {

	private XmiCodec() {
	}

	/**
	 * Serialise one entity as XMI.
	 * <p>
	 * The entity is copied first, so a caller's object graph is never re-parented by
	 * this call.
	 *
	 * @param entity the entity to write
	 * @return the XMI document, UTF-8
	 */
	static byte[] write(EObject entity) {
		ResourceSet resourceSet = newResourceSet();
		// A synthetic URI: nothing is written to it, but a Resource needs one, and the
		// extension is what selects the XMI factory.
		Resource resource = resourceSet.createResource(URI.createURI("dcat-client.xmi"));
		resource.getContents().add(EcoreUtil.copy(entity));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, Map.of(XMLResource.OPTION_ENCODING, StandardCharsets.UTF_8.name()));
		} catch (IOException e) {
			throw new DcatAtlasClientException("Could not serialise a " + entity.eClass().getName() + " as XMI", e);
		}
		return out.toByteArray();
	}

	/**
	 * Parse an XMI document into one entity.
	 *
	 * @param <T>      the expected type
	 * @param xmi      the document
	 * @param expected the type the caller asked the portal for
	 * @param what     the operation, for the error message
	 * @return the parsed entity
	 * @throws DcatAtlasClientException if the document is empty or holds another type —
	 *                                  both mean the portal answered something other than
	 *                                  what was requested, which is worth an error rather
	 *                                  than a {@code null}
	 */
	static <T extends EObject> T read(byte[] xmi, Class<T> expected, String what) {
		ResourceSet resourceSet = newResourceSet();
		Resource resource = resourceSet.createResource(URI.createURI("dcat-client.xmi"));
		try {
			resource.load(new ByteArrayInputStream(xmi), Map.of());
		} catch (IOException e) {
			throw new DcatAtlasClientException(what + " — could not parse the XMI response", e);
		}
		if (resource.getContents().isEmpty()) {
			throw new DcatAtlasClientException(what + " — the portal returned an empty XMI document");
		}
		EObject root = resource.getContents().get(0);
		if (!expected.isInstance(root)) {
			throw new DcatAtlasClientException(what + " — expected a " + expected.getSimpleName() + " but the portal "
					+ "returned a " + root.eClass().getName());
		}
		return expected.cast(root);
	}

	/** A resource set that knows the XMI factory and every package of the model. */
	private static ResourceSet newResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap() //
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		Map<String, Object> packages = resourceSet.getPackageRegistry();
		register(packages, DcatPackage.eNS_URI, DcatPackage.eINSTANCE);
		register(packages, TermsPackage.eNS_URI, TermsPackage.eINSTANCE);
		register(packages, FoafPackage.eNS_URI, FoafPackage.eINSTANCE);
		register(packages, RdfPackage.eNS_URI, RdfPackage.eINSTANCE);
		register(packages, AdmsPackage.eNS_URI, AdmsPackage.eINSTANCE);
		register(packages, SpdxPackage.eNS_URI, SpdxPackage.eINSTANCE);
		register(packages, VcardPackage.eNS_URI, VcardPackage.eINSTANCE);
		return resourceSet;
	}

	private static void register(Map<String, Object> registry, String nsUri, EPackage ePackage) {
		registry.put(nsUri, ePackage);
	}
}
