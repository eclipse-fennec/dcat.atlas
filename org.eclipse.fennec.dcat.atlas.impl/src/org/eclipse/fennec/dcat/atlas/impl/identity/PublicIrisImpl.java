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
package org.eclipse.fennec.dcat.atlas.impl.identity;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The configured logical &harr; public mapping. See {@link PublicIris} for what it
 * is for; this holds the bases and the segment-boundary matching.
 */
/*
 * configurationPolicy = REQUIRE is load-bearing, not tidiness. Without it the component is
 * satisfied with no configuration at all, so SCR instantiates it the moment something
 * dereferences PublicIris - and publicBaseUrl, having no default, arrives absent. That is not
 * hypothetical: with GIT_REMOTE set, GitService does network I/O on ConfigAdmin's single
 * UpdateThread during activation, which delays this component's configuration past the event
 * that satisfies PublicIriFilter. The filter then binds PublicIris on that same thread and gets
 * an unconfigured instance. REQUIRE makes the component unsatisfiable until its configuration
 * exists, so the consumer waits instead of forcing a premature instance.
 */
@Component(name = "PublicIris", service = PublicIris.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
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

	/**
	 * For tests. Public rather than package-visible because the tests that build a mapping
	 * directly sit in sibling packages since the impl bundle was split by concern, and this
	 * package is not exported — "public" here means visible inside the bundle.
	 */
	public PublicIrisImpl(String publicBaseUrl, String... additionalOwnedBases) {
		this.publicBase = withTrailingSlash(requirePublicBase(publicBaseUrl));
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

	/**
	 * Refuses a public base that would render IRIs nobody can dereference.
	 * <p>
	 * The component has no default for this, so an unconfigured deployment arrives
	 * here in one of three shapes. Each gets its own message, because each has a
	 * different cause and they are not otherwise distinguishable from the outside:
	 * <ul>
	 * <li>{@code null} — no configuration carries the property at all.</li>
	 * <li>blank — a configuration carries it with nothing in it. For the shipped
	 * container configuration that means {@code PUBLIC_BASE_URL} is present in the
	 * environment but empty: the interpolation plugin substitutes an empty variable
	 * verbatim, exactly as it would any other value.</li>
	 * <li>the literal {@code $[env:...]} placeholder — the variable is not set at
	 * all, so the plugin found nothing to substitute and left the placeholder
	 * alone.</li>
	 * </ul>
	 * Telling an empty variable from an unset one is the point of the split: they
	 * arrive here differently but are equally invisible downstream, since all three
	 * shapes otherwise yield a syntactically fine {@code about} on every response,
	 * pointing somewhere useless, and nothing can tell that from a correct one.
	 *
	 * @param base the configured {@code publicBaseUrl}
	 * @return {@code base} unchanged when it is usable
	 * @throws IllegalArgumentException naming the setting and what was wrong with it
	 */
	private static String requirePublicBase(String base) {
		if (base == null) {
			throw new IllegalArgumentException(
					"publicBaseUrl is absent from the PublicIris configuration and has no default: set it"
							+ " to the absolute URL clients reach this portal on, e.g."
							+ " https://opendata.example.de/dcat/rest/. In the shipped configurations it"
							+ " comes from PUBLIC_BASE_URL.");
		}
		if (base.isBlank()) {
			throw new IllegalArgumentException(
					"publicBaseUrl is configured, but empty. It has no default, so set it to the absolute"
							+ " URL clients reach this portal on, e.g. https://opendata.example.de/dcat/rest/."
							+ " In the shipped configurations it comes from PUBLIC_BASE_URL, and an empty"
							+ " variable is not the same as an unset one: an unset one leaves the"
							+ " $[env:PUBLIC_BASE_URL] placeholder standing and is reported as such, so"
							+ " reaching this message means the variable is set and is arriving empty.");
		}
		if (base.startsWith("$[")) {
			throw new IllegalArgumentException("publicBaseUrl is still the uninterpolated placeholder " + base
					+ ": the environment variable it reads is not set. Set PUBLIC_BASE_URL to the absolute"
					+ " URL clients reach this portal on.");
		}
		if (!base.startsWith("http://") && !base.startsWith("https://")) {
			throw new IllegalArgumentException("publicBaseUrl must be an absolute http(s) URL, but was " + base
					+ ". It is the base every stored identity is rendered under, so a relative or"
					+ " scheme-less value produces IRIs that cannot be dereferenced.");
		}
		return base;
	}
}
