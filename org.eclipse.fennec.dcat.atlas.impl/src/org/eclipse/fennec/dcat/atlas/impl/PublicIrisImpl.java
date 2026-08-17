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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.PublicIris;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The configured logical &harr; public mapping. See {@link PublicIris} for what it
 * is for; this holds the bases and the segment-boundary matching.
 */
@Component(name = "PublicIris", service = PublicIris.class)
@Designate(ocd = PublicIrisConfig.class)
public class PublicIrisImpl implements PublicIris {

	/** The base clients see. Always ends in {@code /}. */
	private final String publicBase;
	/** Bases whose IRIs are ours: the public base, the logical base, and any migration aids. */
	private final List<String> ownedBases;

	@Activate
	public PublicIrisImpl(PublicIrisConfig config) {
		this(config.publicBaseUrl(), config.additionalOwnedBases());
	}

	/** Package-visible for tests. */
	PublicIrisImpl(String publicBaseUrl, String... additionalOwnedBases) {
		this.publicBase = withTrailingSlash(publicBaseUrl);
		List<String> owned = new ArrayList<>();
		owned.add(this.publicBase);
		owned.add(StoreLayout.LOGICAL_BASE);
		if (additionalOwnedBases != null) {
			for (String base : additionalOwnedBases) {
				if (base != null && !base.isBlank()) {
					owned.add(withTrailingSlash(base));
				}
			}
		}
		this.ownedBases = List.copyOf(owned);
	}

	@Override
	public String toPublic(String iri) {
		String tail = tailUnder(iri, StoreLayout.LOGICAL_BASE);
		return tail == null ? iri : publicBase + tail;
	}

	@Override
	public String toLogical(String iri) {
		for (String owned : ownedBases) {
			String tail = tailUnder(iri, owned);
			if (tail != null) {
				return StoreLayout.LOGICAL_BASE + tail;
			}
		}
		return iri;
	}

	@Override
	public boolean isOwned(String iri) {
		return ownedBases.stream().anyMatch(base -> tailUnder(iri, base) != null);
	}

	@Override
	public String publicBase() {
		return publicBase;
	}

	/**
	 * The part of {@code iri} below {@code base}, or {@code null} if it is not under
	 * it.
	 * <p>
	 * {@code base} ends in {@code /}, so a prefix match here <em>is</em> a
	 * segment-boundary match: {@code https://example.org/dcat/} cannot match
	 * {@code https://example.org/dcatalog/air}, because the character after
	 * {@code dcat} is {@code a}, not {@code /}. The bare base itself is not a
	 * resource identity and does not match.
	 */
	private static String tailUnder(String iri, String base) {
		if (iri == null || !iri.startsWith(base)) {
			return null;
		}
		String tail = iri.substring(base.length());
		return tail.isEmpty() ? null : tail;
	}

	private static String withTrailingSlash(String base) {
		if (base == null || base.isBlank()) {
			throw new IllegalArgumentException("A base URL is required");
		}
		return base.endsWith("/") ? base : base + "/";
	}
}
