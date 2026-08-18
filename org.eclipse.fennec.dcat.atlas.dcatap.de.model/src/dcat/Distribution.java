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
package dcat;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.Duration;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

import rdf.DateOrDateTimeLiteral;
import rdf.IdentifiedResource;
import rdf.PlainLiteral;

import spdx.Checksum;

import terms.LicenseDocument;
import terms.RightsStatement;
import terms.Standard;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Distribution</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link dcat.Distribution#getTitle <em>Title</em>}</li>
 *   <li>{@link dcat.Distribution#getDescription <em>Description</em>}</li>
 *   <li>{@link dcat.Distribution#getAccessService <em>Access Service</em>}</li>
 *   <li>{@link dcat.Distribution#getFormat <em>Format</em>}</li>
 *   <li>{@link dcat.Distribution#getMediaType <em>Media Type</em>}</li>
 *   <li>{@link dcat.Distribution#getPackageFormat <em>Package Format</em>}</li>
 *   <li>{@link dcat.Distribution#getByteSize <em>Byte Size</em>}</li>
 *   <li>{@link dcat.Distribution#getCompressFormat <em>Compress Format</em>}</li>
 *   <li>{@link dcat.Distribution#getSpatialResolutionInMeters <em>Spatial Resolution In Meters</em>}</li>
 *   <li>{@link dcat.Distribution#getTemporalResolution <em>Temporal Resolution</em>}</li>
 *   <li>{@link dcat.Distribution#getAccessRights <em>Access Rights</em>}</li>
 *   <li>{@link dcat.Distribution#getLicense <em>License</em>}</li>
 *   <li>{@link dcat.Distribution#getConformsTo <em>Conforms To</em>}</li>
 *   <li>{@link dcat.Distribution#getRights <em>Rights</em>}</li>
 *   <li>{@link dcat.Distribution#getHasPolicy <em>Has Policy</em>}</li>
 *   <li>{@link dcat.Distribution#getIssued <em>Issued</em>}</li>
 *   <li>{@link dcat.Distribution#getModified <em>Modified</em>}</li>
 *   <li>{@link dcat.Distribution#getNodeID <em>Node ID</em>}</li>
 *   <li>{@link dcat.Distribution#getLicenseAttributionByText <em>License Attribution By Text</em>}</li>
 *   <li>{@link dcat.Distribution#getAvailability <em>Availability</em>}</li>
 *   <li>{@link dcat.Distribution#getStatus <em>Status</em>}</li>
 *   <li>{@link dcat.Distribution#getChecksum <em>Checksum</em>}</li>
 *   <li>{@link dcat.Distribution#getApplicableLegislation <em>Applicable Legislation</em>}</li>
 *   <li>{@link dcat.Distribution#getDownloadURL <em>Download URL</em>}</li>
 *   <li>{@link dcat.Distribution#getAccessURL <em>Access URL</em>}</li>
 * </ul>
 *
 * @see dcat.DcatPackage#getDistribution()
 * @model annotation="http://www.eclipse.org/emf/2002/Ecore constraints='HasIdentity AccessURLIsIri DownloadURLIsIri FormatIsIri MediaTypeIsIri PackageFormatIsIri AccessRightsIsIri HasPolicyIsIri AvailabilityIsIri StatusIsIri ApplicableLegislationIsIri'"
 *        annotation="http://www.eclipse.org/fennec/m2x/ocl/1.0 HasIdentity='self.about &lt;&gt; null and self.about.size() &gt; 0' AccessURLIsIri='self.accessURL-&gt;forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))' DownloadURLIsIri='self.downloadURL-&gt;forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))' FormatIsIri='self.format = null or self.format.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')' MediaTypeIsIri='self.mediaType = null or self.mediaType.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')' PackageFormatIsIri='self.packageFormat = null or self.packageFormat.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')' AccessRightsIsIri='self.accessRights = null or self.accessRights.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')' HasPolicyIsIri='self.hasPolicy = null or self.hasPolicy.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')' AvailabilityIsIri='self.availability = null or self.availability.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')' StatusIsIri='self.status = null or self.status.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\')' ApplicableLegislationIsIri='self.applicableLegislation-&gt;forAll(v | v.matches(\'[A-Za-z][A-Za-z0-9+.\\\\-]*:\\\\S*\'))'"
 *        extendedMetaData="name='Distribution' kind='elementOnly'"
 * @generated
 */
@ProviderType
public interface Distribution extends IdentifiedResource {
	/**
	 * Returns the value of the '<em><b>Title</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Title</em>' containment reference.
	 * @see #setTitle(PlainLiteral)
	 * @see dcat.DcatPackage#getDistribution_Title()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='title' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	PlainLiteral getTitle();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getTitle <em>Title</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Title</em>' containment reference.
	 * @see #getTitle()
	 * @generated
	 */
	void setTitle(PlainLiteral value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' containment reference.
	 * @see #setDescription(PlainLiteral)
	 * @see dcat.DcatPackage#getDistribution_Description()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='description' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	PlainLiteral getDescription();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getDescription <em>Description</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' containment reference.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(PlainLiteral value);

	/**
	 * Returns the value of the '<em><b>Access Service</b></em>' reference list.
	 * The list contents are of type {@link dcat.DataService}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Access Service</em>' reference list.
	 * @see dcat.DcatPackage#getDistribution_AccessService()
	 * @model extendedMetaData="kind='element' name='accessService' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<DataService> getAccessService();

	/**
	 * Returns the value of the '<em><b>Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Format</em>' attribute.
	 * @see #setFormat(String)
	 * @see dcat.DcatPackage#getDistribution_Format()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='format' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	String getFormat();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getFormat <em>Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format</em>' attribute.
	 * @see #getFormat()
	 * @generated
	 */
	void setFormat(String value);

	/**
	 * Returns the value of the '<em><b>Media Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Media Type</em>' attribute.
	 * @see #setMediaType(String)
	 * @see dcat.DcatPackage#getDistribution_MediaType()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='mediaType' namespace='##targetNamespace'"
	 * @generated
	 */
	String getMediaType();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getMediaType <em>Media Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Media Type</em>' attribute.
	 * @see #getMediaType()
	 * @generated
	 */
	void setMediaType(String value);

	/**
	 * Returns the value of the '<em><b>Package Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Package Format</em>' attribute.
	 * @see #setPackageFormat(String)
	 * @see dcat.DcatPackage#getDistribution_PackageFormat()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='packageFormat' namespace='##targetNamespace'"
	 * @generated
	 */
	String getPackageFormat();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getPackageFormat <em>Package Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Package Format</em>' attribute.
	 * @see #getPackageFormat()
	 * @generated
	 */
	void setPackageFormat(String value);

	/**
	 * Returns the value of the '<em><b>Byte Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Byte Size</em>' attribute.
	 * @see #setByteSize(BigInteger)
	 * @see dcat.DcatPackage#getDistribution_ByteSize()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NonNegativeInteger"
	 *        extendedMetaData="kind='element' name='byteSize' namespace='##targetNamespace'"
	 * @generated
	 */
	BigInteger getByteSize();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getByteSize <em>Byte Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Byte Size</em>' attribute.
	 * @see #getByteSize()
	 * @generated
	 */
	void setByteSize(BigInteger value);

	/**
	 * Returns the value of the '<em><b>Compress Format</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Compress Format</em>' containment reference.
	 * @see #setCompressFormat(EObject)
	 * @see dcat.DcatPackage#getDistribution_CompressFormat()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='compressFormat' namespace='##targetNamespace'"
	 * @generated
	 */
	EObject getCompressFormat();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getCompressFormat <em>Compress Format</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Compress Format</em>' containment reference.
	 * @see #getCompressFormat()
	 * @generated
	 */
	void setCompressFormat(EObject value);

	/**
	 * Returns the value of the '<em><b>Spatial Resolution In Meters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Spatial Resolution In Meters</em>' attribute.
	 * @see #setSpatialResolutionInMeters(BigDecimal)
	 * @see dcat.DcatPackage#getDistribution_SpatialResolutionInMeters()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Decimal"
	 *        extendedMetaData="kind='element' name='spatialResolutionInMeters' namespace='##targetNamespace'"
	 * @generated
	 */
	BigDecimal getSpatialResolutionInMeters();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getSpatialResolutionInMeters <em>Spatial Resolution In Meters</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Spatial Resolution In Meters</em>' attribute.
	 * @see #getSpatialResolutionInMeters()
	 * @generated
	 */
	void setSpatialResolutionInMeters(BigDecimal value);

	/**
	 * Returns the value of the '<em><b>Temporal Resolution</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Temporal Resolution</em>' attribute.
	 * @see #setTemporalResolution(Duration)
	 * @see dcat.DcatPackage#getDistribution_TemporalResolution()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.Duration"
	 *        extendedMetaData="kind='element' name='temporalResolution' namespace='##targetNamespace'"
	 * @generated
	 */
	Duration getTemporalResolution();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getTemporalResolution <em>Temporal Resolution</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Temporal Resolution</em>' attribute.
	 * @see #getTemporalResolution()
	 * @generated
	 */
	void setTemporalResolution(Duration value);

	/**
	 * Returns the value of the '<em><b>Access Rights</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Access Rights</em>' attribute.
	 * @see #setAccessRights(String)
	 * @see dcat.DcatPackage#getDistribution_AccessRights()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='accessRights' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	String getAccessRights();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getAccessRights <em>Access Rights</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Access Rights</em>' attribute.
	 * @see #getAccessRights()
	 * @generated
	 */
	void setAccessRights(String value);

	/**
	 * Returns the value of the '<em><b>License</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>License</em>' containment reference.
	 * @see #setLicense(LicenseDocument)
	 * @see dcat.DcatPackage#getDistribution_License()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='license' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	LicenseDocument getLicense();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getLicense <em>License</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>License</em>' containment reference.
	 * @see #getLicense()
	 * @generated
	 */
	void setLicense(LicenseDocument value);

	/**
	 * Returns the value of the '<em><b>Conforms To</b></em>' containment reference list.
	 * The list contents are of type {@link terms.Standard}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Conforms To</em>' containment reference list.
	 * @see dcat.DcatPackage#getDistribution_ConformsTo()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='conformsTo' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	EList<Standard> getConformsTo();

	/**
	 * Returns the value of the '<em><b>Rights</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Rights</em>' containment reference.
	 * @see #setRights(RightsStatement)
	 * @see dcat.DcatPackage#getDistribution_Rights()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='rights' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	RightsStatement getRights();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getRights <em>Rights</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rights</em>' containment reference.
	 * @see #getRights()
	 * @generated
	 */
	void setRights(RightsStatement value);

	/**
	 * Returns the value of the '<em><b>Has Policy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Has Policy</em>' attribute.
	 * @see #setHasPolicy(String)
	 * @see dcat.DcatPackage#getDistribution_HasPolicy()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='hasPolicy' namespace='http://www.w3.org/ns/odrl/2/'"
	 * @generated
	 */
	String getHasPolicy();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getHasPolicy <em>Has Policy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Policy</em>' attribute.
	 * @see #getHasPolicy()
	 * @generated
	 */
	void setHasPolicy(String value);

	/**
	 * Returns the value of the '<em><b>Issued</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Issued</em>' containment reference.
	 * @see #setIssued(DateOrDateTimeLiteral)
	 * @see dcat.DcatPackage#getDistribution_Issued()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='issued' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	DateOrDateTimeLiteral getIssued();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getIssued <em>Issued</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Issued</em>' containment reference.
	 * @see #getIssued()
	 * @generated
	 */
	void setIssued(DateOrDateTimeLiteral value);

	/**
	 * Returns the value of the '<em><b>Modified</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Modified</em>' containment reference.
	 * @see #setModified(DateOrDateTimeLiteral)
	 * @see dcat.DcatPackage#getDistribution_Modified()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='modified' namespace='http://purl.org/dc/terms/'"
	 * @generated
	 */
	DateOrDateTimeLiteral getModified();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getModified <em>Modified</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Modified</em>' containment reference.
	 * @see #getModified()
	 * @generated
	 */
	void setModified(DateOrDateTimeLiteral value);

	/**
	 * Returns the value of the '<em><b>Node ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node ID</em>' attribute.
	 * @see #setNodeID(String)
	 * @see dcat.DcatPackage#getDistribution_NodeID()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.NCName"
	 *        extendedMetaData="kind='attribute' name='nodeID' namespace='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
	 * @generated
	 */
	String getNodeID();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getNodeID <em>Node ID</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Node ID</em>' attribute.
	 * @see #getNodeID()
	 * @generated
	 */
	void setNodeID(String value);

	/**
	 * Returns the value of the '<em><b>License Attribution By Text</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>License Attribution By Text</em>' containment reference.
	 * @see #setLicenseAttributionByText(PlainLiteral)
	 * @see dcat.DcatPackage#getDistribution_LicenseAttributionByText()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='licenseAttributionByText' namespace='http://dcat-ap.de/def/dcatde/'"
	 * @generated
	 */
	PlainLiteral getLicenseAttributionByText();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getLicenseAttributionByText <em>License Attribution By Text</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>License Attribution By Text</em>' containment reference.
	 * @see #getLicenseAttributionByText()
	 * @generated
	 */
	void setLicenseAttributionByText(PlainLiteral value);

	/**
	 * Returns the value of the '<em><b>Availability</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * dcatap:availability — the intended availability/planned retention of the distribution. New in DCAT-AP.de 3.0 (replaces the removed dcatde:plannedAvailability).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Availability</em>' attribute.
	 * @see #setAvailability(String)
	 * @see dcat.DcatPackage#getDistribution_Availability()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='availability' namespace='http://data.europa.eu/r5r/'"
	 * @generated
	 */
	String getAvailability();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getAvailability <em>Availability</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Availability</em>' attribute.
	 * @see #getAvailability()
	 * @generated
	 */
	void setAvailability(String value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * adms:status — the maturity of the distribution, from the EU Distribution Status vocabulary. New in DCAT-AP.de 3.0.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see #setStatus(String)
	 * @see dcat.DcatPackage#getDistribution_Status()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='status' namespace='http://www.w3.org/ns/adms#'"
	 * @generated
	 */
	String getStatus();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(String value);

	/**
	 * Returns the value of the '<em><b>Checksum</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * spdx:checksum — a checksum for the distribution's file (DCAT-AP.de 3.0 §4.6/§4.12).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Checksum</em>' containment reference.
	 * @see #setChecksum(Checksum)
	 * @see dcat.DcatPackage#getDistribution_Checksum()
	 * @model containment="true"
	 *        extendedMetaData="kind='element' name='checksum' namespace='http://spdx.org/rdf/terms#'"
	 * @generated
	 */
	Checksum getChecksum();

	/**
	 * Sets the value of the '{@link dcat.Distribution#getChecksum <em>Checksum</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Checksum</em>' containment reference.
	 * @see #getChecksum()
	 * @generated
	 */
	void setChecksum(Checksum value);

	/**
	 * Returns the value of the '<em><b>Applicable Legislation</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * dcatap:applicableLegislation — the legislation that mandates the distribution. New in DCAT-AP.de 3.0.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Applicable Legislation</em>' attribute list.
	 * @see dcat.DcatPackage#getDistribution_ApplicableLegislation()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='applicableLegislation' namespace='http://data.europa.eu/r5r/'"
	 * @generated
	 */
	EList<String> getApplicableLegislation();

	/**
	 * Returns the value of the '<em><b>Download URL</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Download URL</em>' attribute list.
	 * @see dcat.DcatPackage#getDistribution_DownloadURL()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
	 *        extendedMetaData="kind='element' name='downloadURL' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getDownloadURL();

	/**
	 * Returns the value of the '<em><b>Access URL</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Access URL</em>' attribute list.
	 * @see dcat.DcatPackage#getDistribution_AccessURL()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI" required="true"
	 *        extendedMetaData="kind='element' name='accessURL' namespace='##targetNamespace'"
	 * @generated
	 */
	EList<String> getAccessURL();

} // Distribution
