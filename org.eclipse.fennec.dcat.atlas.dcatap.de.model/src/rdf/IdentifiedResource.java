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
package rdf;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Identified Resource</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link rdf.IdentifiedResource#getAbout <em>About</em>}</li>
 * </ul>
 *
 * @see rdf.RdfPackage#getIdentifiedResource()
 * @model abstract="true"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore constraints='AboutIsIri'"
 *        annotation="http://www.eclipse.org/fennec/m2x/ocl/1.0 AboutIsIri='self.about = null or self.about.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')'"
 * @generated
 */
@ProviderType
public interface IdentifiedResource extends EObject {
	/**
	 * Returns the value of the '<em><b>About</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>About</em>' attribute.
	 * @see #setAbout(String)
	 * @see rdf.RdfPackage#getIdentifiedResource_About()
	 * @model id="true" dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='attribute' name='about' namespace='##targetNamespace'"
	 * @generated
	 */
	String getAbout();

	/**
	 * Sets the value of the '{@link rdf.IdentifiedResource#getAbout <em>About</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>About</em>' attribute.
	 * @see #getAbout()
	 * @generated
	 */
	void setAbout(String value);

} // IdentifiedResource
