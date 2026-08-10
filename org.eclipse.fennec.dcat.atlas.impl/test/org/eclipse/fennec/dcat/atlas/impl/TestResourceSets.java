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

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;

import adms.AdmsPackage;
import adms.impl.AdmsPackageImpl;
import dcat.DcatPackage;
import dcat.impl.DcatPackageImpl;
import dcatde.DcatDEPackage;
import dcatde.impl.DcatDEPackageImpl;
import foaf.FoafPackage;
import foaf.impl.FoafPackageImpl;
import locn.LocnPackage;
import locn.impl.LocnPackageImpl;
import odrl.OdrlPackage;
import odrl.impl.OdrlPackageImpl;
import owl.OwlPackage;
import owl.impl.OwlPackageImpl;
import prov.ProvPackage;
import prov.impl.ProvPackageImpl;
import rdf.RdfPackage;
import rdf.impl.RdfPackageImpl;
import rdf.util.RdfResourceFactoryImpl;
import schema.SchemaPackage;
import schema.impl.SchemaPackageImpl;
import skos.SkosPackage;
import skos.impl.SkosPackageImpl;
import spdx.SpdxPackage;
import spdx.impl.SpdxPackageImpl;
import terms.TermsPackage;
import terms.impl.TermsPackageImpl;
import vcard.VcardPackage;
import vcard.impl.VcardPackageImpl;

/**
 * Test helper: builds a stand-alone {@link ResourceSet} that knows every DCAT-AP
 * package and routes the {@code .rdf} extension to the RDF resource factory, plus
 * a {@link ResourceSetFactory} that hands out fresh ones — mirroring what the
 * fennec EMF-OSGi runtime provides to the services.
 */
public final class TestResourceSets {

	private TestResourceSets() {
	}

	/** A factory that returns a freshly configured resource set on each call. */
	public static ResourceSetFactory factory() {
		return TestResourceSets::newResourceSet;
	}

	public static ResourceSet newResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		Registry registry = resourceSet.getPackageRegistry();
		registry.put(SchemaPackage.eNS_URI, SchemaPackageImpl.init());
		registry.put(TermsPackage.eNS_URI, TermsPackageImpl.init());
		registry.put(FoafPackage.eNS_URI, FoafPackageImpl.init());
		registry.put(AdmsPackage.eNS_URI, AdmsPackageImpl.init());
		registry.put(DcatPackage.eNS_URI, DcatPackageImpl.init());
		registry.put(DcatDEPackage.eNS_URI, DcatDEPackageImpl.init());
		registry.put(LocnPackage.eNS_URI, LocnPackageImpl.init());
		registry.put(OdrlPackage.eNS_URI, OdrlPackageImpl.init());
		registry.put(OwlPackage.eNS_URI, OwlPackageImpl.init());
		registry.put(ProvPackage.eNS_URI, ProvPackageImpl.init());
		registry.put(RdfPackage.eNS_URI, RdfPackageImpl.init());
		registry.put(SkosPackage.eNS_URI, SkosPackageImpl.init());
		registry.put(VcardPackage.eNS_URI, VcardPackageImpl.init());
		registry.put(SpdxPackage.eNS_URI, SpdxPackageImpl.init());
		registry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		registry.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
		registry.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("rdf",
				new RdfResourceFactoryImpl());
		return resourceSet;
	}
}
