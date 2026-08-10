/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.shacl.model.shacl.configuration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;

import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;

import org.eclipse.fennec.shacl.model.shacl.SHACLPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>EPackageConfiguration</b> and <b>ResourceFactoryConfigurator</b> for the model.
 * The package will be registered into a OSGi base model registry.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Results subset of the W3C Shapes Constraint Language (SHACL) vocabulary
 * (http://www.w3.org/ns/shacl#). Only the SHACL *validation report* vocabulary is
 * modelled here (sh:ValidationReport / sh:ValidationResult and the result properties),
 * not the constraint vocabulary (sh:Shape, sh:NodeShape, constraint components, ...),
 * since this bundle exists to represent the report returned by validation (FR-4/FR-5,
 * FR-19), not to author shapes. URI-valued result properties reuse rdf:Resource and
 * lang-tagged messages reuse rdf:PlainLiteral from the DCAT-AP model bundle so the
 * report serialises as spec-correct RDF (rdf:resource references, xml:lang literals).
 * <!-- end-model-doc -->
 * @see EPackageConfigurator
 * @generated
 */
public class SHACLEPackageConfigurator implements EPackageConfigurator {
	
	private SHACLPackage ePackage;

	protected SHACLEPackageConfigurator(SHACLPackage ePackage){
		this.ePackage = ePackage;
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.EPackageRegistryConfigurator#configureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 * @generated
	 */
	@Override
	public void configureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
		registry.put(SHACLPackage.eNS_URI, ePackage);
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.EPackageRegistryConfigurator#unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 * @generated
	 */
	@Override
	public void unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
		registry.remove(SHACLPackage.eNS_URI);
	}
	
	/**
	 * A method providing the Properties the services around this Model should be registered with.
	 * @generated
	 */
	public Map<String, Object> getServiceProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EMFNamespaces.EMF_NAME, SHACLPackage.eNAME);
		properties.put(EMFNamespaces.EMF_MODEL_NSURI, SHACLPackage.eNS_URI);
		properties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_PROVIDED);
		properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "shacl");
		properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0");
		return properties;
	}
}