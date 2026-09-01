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
package org.eclipse.fennec.dcat.atlas.client.osgi;

import org.eclipse.fennec.dcat.atlas.client.api.AuthType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * One portal's worth of configuration. A factory PID, so several portals can be connected
 * at once, each as its own component instance with its own client.
 * <p>
 * The attribute names map onto the dotted property names {@code ClientConfiguration}
 * documents, so the two cannot drift: {@code base_uri()} is {@code base.uri}, and so on.
 */
@ObjectClassDefinition(name = "DCAT.Atlas client", //
		description = "Registers DCAT elements with one DCAT.Atlas portal. One configuration per portal.")
public @interface DcatAtlasClientConfig {

	/**
	 * The name this portal is known by, published as a service property so a consumer can
	 * say which one it means: {@code @Reference(target = "(dcat.portal=jena)")}.
	 * <p>
	 * Required, and deliberately so. With one portal it is redundant; the moment there are
	 * two, a consumer that did not name one gets whichever bound first, which is the kind
	 * of bug that only shows up in the deployment that has two.
	 */
	String dcat_portal();

	/**
	 * {@code base.uri} — the portal's REST base, the URL this runtime can actually reach it
	 * on, e.g. {@code http://dcat:8080/rest/}. Behind a proxy this is the internal address;
	 * it is not the same thing as the portal's own {@code PUBLIC_BASE_URL}, which is what
	 * the portal stamps into the identities it serves.
	 */
	String base_uri();

	/**
	 * {@code public.base.uri} — the base the portal serves its identities under, where that
	 * is not {@link #base_uri()}.
	 * <p>
	 * Leave it unset in a direct deployment, where the two are one URL. Set it wherever a
	 * proxy or a container network means this runtime connects on one URL and clients
	 * dereference another: it is what {@code aboutFor} computes from, and {@code base.uri}
	 * cannot serve both purposes — it has to stay the address this runtime can reach.
	 */
	String public_base_uri() default "";

	/** {@code connect.timeout.ms} */
	int connect_timeout_ms() default 5_000;

	/** {@code read.timeout.ms} */
	int read_timeout_ms() default 30_000;

	/**
	 * Whether to check {@code /health/ready} at activation.
	 * <p>
	 * On by default because a portal whose shapes are not mounted refuses writes in ways
	 * that look like the caller's fault; one check turns a confusing cascade into one log
	 * line. It does <em>not</em> fail activation — see {@link #require_ready()}.
	 */
	boolean check_ready() default true;

	/**
	 * Whether a portal that is not ready prevents this component from activating.
	 * <p>
	 * Off by default: a portal that is merely not ready <em>yet</em> is normal during a
	 * co-ordinated start-up, and failing activation would need something to retry it. Turn
	 * it on where publishing into an unprepared portal would be worse than not starting.
	 */
	boolean require_ready() default false;

	/** {@code auth.type} — {@link AuthType#NONE} is all the portal understands today. */
	AuthType auth_type() default AuthType.NONE;

	/** {@code auth.token.env} — environment variable holding the bearer token. */
	String auth_token_env() default "";

	/** {@code auth.apikey.header} */
	String auth_apikey_header() default "X-API-Key";

	/** {@code auth.apikey.env} — environment variable holding the API key. */
	String auth_apikey_env() default "";

	/** {@code auth.keystore.path} — mTLS only. */
	String auth_keystore_path() default "";

	/** {@code auth.keystore.password} — mTLS only. */
	@AttributeDefinition(type = org.osgi.service.metatype.annotations.AttributeType.PASSWORD)
	String auth_keystore_password() default "";

	/** {@code auth.keystore.type} */
	String auth_keystore_type() default "PKCS12";

	/** {@code auth.truststore.path} — mTLS only. */
	String auth_truststore_path() default "";

	/** {@code auth.truststore.password} — mTLS only. */
	@AttributeDefinition(type = org.osgi.service.metatype.annotations.AttributeType.PASSWORD)
	String auth_truststore_password() default "";

	/** {@code auth.truststore.type} */
	String auth_truststore_type() default "PKCS12";
}
