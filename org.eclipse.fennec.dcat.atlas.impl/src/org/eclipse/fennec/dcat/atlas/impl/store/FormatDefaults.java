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

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

import dcat.Dataset;
import dcat.Distribution;

/**
 * Fills a Distribution's {@code dct:format} from its {@code dcat:mediaType} when the
 * writer left it out (issue #68).
 * <p>
 * DCAT-AP recommends {@code dct:format} on every Distribution, with a value from the
 * EU Publications Office file-type authority table, and in German practice it is the
 * field portals and dashboards actually query: on govdata 97% of distributions carry
 * it, against 25% for {@code dcat:mediaType}. A client that knows only the IANA media
 * type of what it serves should not have to learn that vocabulary to be found.
 * <p>
 * The rule is deliberately narrow so that it can never surprise a writer:
 * <ul>
 * <li>an explicitly given {@code format} is never touched, whatever it says;</li>
 * <li>a media type outside the table leaves {@code format} unset — guessing a file
 * type would be worse than none;</li>
 * <li>the mapping is a fixed table, so the same input always stores the same
 * value and a re-read followed by a write is stable.</li>
 * </ul>
 * It runs on the write-side floor shared by every write path (see
 * {@code DcatHelper.Store}), so a Distribution sent on its own, embedded in a Dataset,
 * or in a Catalog or DatasetSeries is completed the same way.
 */
public final class FormatDefaults {

	/** DCAT-AP's home for {@code dcat:mediaType} values. */
	static final String IANA_MEDIA_TYPES = "http://www.iana.org/assignments/media-types/";
	/** DCAT-AP's home for {@code dct:format} values. */
	static final String EU_FILE_TYPES = "http://publications.europa.eu/resource/authority/file-type/";

	/**
	 * IANA media type → code in the EU file-type authority table. Extend here; keep the
	 * keys lower-case and parameter-free, which is what {@link #formatFor} normalises to.
	 */
	private static final Map<String, String> FILE_TYPES = Map.ofEntries(
			// structured data
			Map.entry("text/csv", "CSV"),
			Map.entry("text/tab-separated-values", "TSV"),
			Map.entry("application/json", "JSON"),
			Map.entry("application/ld+json", "JSON_LD"),
			Map.entry("application/xml", "XML"),
			Map.entry("text/xml", "XML"),
			Map.entry("application/x-yaml", "YAML"),
			Map.entry("application/vnd.apache.parquet", "PARQUET"),
			Map.entry("application/x-parquet", "PARQUET"),
			Map.entry("application/x-netcdf", "NETCDF"),
			Map.entry("application/sql", "SQL"),
			// spreadsheets and documents
			Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "XLSX"),
			Map.entry("application/vnd.ms-excel", "XLS"),
			Map.entry("application/vnd.oasis.opendocument.spreadsheet", "ODS"),
			Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "DOCX"),
			Map.entry("application/msword", "DOC"),
			Map.entry("application/vnd.oasis.opendocument.text", "ODT"),
			Map.entry("application/pdf", "PDF"),
			Map.entry("text/html", "HTML"),
			Map.entry("text/plain", "TXT"),
			// geodata
			Map.entry("application/geo+json", "GEOJSON"),
			Map.entry("application/gml+xml", "GML"),
			Map.entry("application/vnd.google-earth.kml+xml", "KML"),
			Map.entry("application/vnd.google-earth.kmz", "KMZ"),
			Map.entry("application/geopackage+sqlite3", "GPKG"),
			Map.entry("application/vnd.shp", "SHP"),
			Map.entry("application/vnd.ogc.wms_xml", "WMS_SRVC"),
			Map.entry("application/vnd.ogc.wfs_xml", "WFS_SRVC"),
			// RDF
			Map.entry("application/rdf+xml", "RDF_XML"),
			Map.entry("text/turtle", "RDF_TURTLE"),
			Map.entry("application/n-triples", "RDF_N_TRIPLES"),
			Map.entry("application/n-quads", "RDF_N_QUADS"),
			Map.entry("application/trig", "RDF_TRIG"),
			// feeds, images, archives
			Map.entry("application/atom+xml", "ATOM"),
			Map.entry("application/rss+xml", "RSS"),
			Map.entry("image/png", "PNG"),
			Map.entry("image/jpeg", "JPEG"),
			Map.entry("image/tiff", "TIFF"),
			Map.entry("image/svg+xml", "SVG"),
			Map.entry("application/zip", "ZIP"),
			Map.entry("application/gzip", "GZIP"),
			Map.entry("application/x-tar", "TAR"),
			Map.entry("application/octet-stream", "BIN"));

	private FormatDefaults() {
	}

	/**
	 * Completes every Distribution of a stored root. Catalog and DatasetSeries are
	 * Datasets, so one check covers all three; any other root has no Distributions.
	 */
	public static void apply(EObject object) {
		if (object instanceof Dataset dataset) {
			dataset.getDistribution().forEach(FormatDefaults::apply);
		}
	}

	/**
	 * @return whether {@code format} was set by this call; {@code false} when it was
	 *         already given or the media type is unknown
	 */
	public static boolean apply(Distribution distribution) {
		if (distribution.getFormat() != null && !distribution.getFormat().isBlank()) {
			return false;
		}
		Optional<String> format = formatFor(distribution.getMediaType());
		format.ifPresent(distribution::setFormat);
		return format.isPresent();
	}

	/**
	 * The EU file-type IRI for a media type, given either as DCAT-AP's IANA IRI or as the
	 * bare type. Parameters ({@code ; charset=...}) and case are ignored.
	 */
	public static Optional<String> formatFor(String mediaType) {
		if (mediaType == null) {
			return Optional.empty();
		}
		String type = mediaType.strip();
		if (type.regionMatches(true, 0, IANA_MEDIA_TYPES, 0, IANA_MEDIA_TYPES.length())) {
			type = type.substring(IANA_MEDIA_TYPES.length());
		}
		int parameters = type.indexOf(';');
		if (parameters >= 0) {
			type = type.substring(0, parameters);
		}
		type = type.strip().toLowerCase(Locale.ROOT);
		return Optional.ofNullable(FILE_TYPES.get(type)).map(code -> EU_FILE_TYPES + code);
	}
}
