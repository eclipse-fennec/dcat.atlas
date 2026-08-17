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

import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;

import adms.AdmsPackage;
import adms.impl.AdmsPackageImpl;
import dcat.DcatPackage;
import dcat.impl.DcatPackageImpl;
import foaf.FoafPackage;
import foaf.impl.FoafPackageImpl;
import rdf.RdfPackage;
import rdf.impl.RdfPackageImpl;
import spdx.SpdxPackage;
import spdx.impl.SpdxPackageImpl;
import terms.TermsPackage;
import terms.impl.TermsPackageImpl;
import vcard.VcardPackage;
import vcard.impl.VcardPackageImpl;

/**
 * Test stand-in for what the fennec EMF-OSGi runtime provides: a
 * {@link ResourceSetFactory} handing out resource sets that know every DCAT-AP
 * package.
 * <p>
 * The resource <em>factory</em> is deliberately not configured here — the store
 * installs its own XMI factory and URI map on top, so configuring one would only
 * mask a mistake in {@code StoreResourceSets}.
 */
public final class TestResourceSets {

	private TestResourceSets() {
	}

	public static ResourceSetFactory factory() {
		return TestResourceSets::newResourceSet;
	}

	public static ResourceSet newResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		Registry registry = resourceSet.getPackageRegistry();
		registry.put(TermsPackage.eNS_URI, TermsPackageImpl.init());
		registry.put(FoafPackage.eNS_URI, FoafPackageImpl.init());
		registry.put(AdmsPackage.eNS_URI, AdmsPackageImpl.init());
		registry.put(DcatPackage.eNS_URI, DcatPackageImpl.init());
		registry.put(RdfPackage.eNS_URI, RdfPackageImpl.init());
		registry.put(VcardPackage.eNS_URI, VcardPackageImpl.init());
		registry.put(SpdxPackage.eNS_URI, SpdxPackageImpl.init());
		registry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		registry.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
		registry.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
		return resourceSet;
	}
}
