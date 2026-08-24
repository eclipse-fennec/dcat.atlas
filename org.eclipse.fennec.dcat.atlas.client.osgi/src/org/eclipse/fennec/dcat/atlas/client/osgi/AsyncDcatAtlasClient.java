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

import java.util.function.Function;

import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.util.promise.Promise;

/**
 * The asynchronous face of the same client, published alongside the synchronous one.
 *
 * <h2>What async is for here, and what it is not for</h2>
 *
 * Registration happens when a model is released or an adapter deployed — never per message —
 * so this exists to keep a DS {@code @Activate} from blocking on a portal, not to push
 * throughput. That is why {@code Promise} is the only async flavour offered and there is no
 * {@code CompletionStage} twin: all three consumers are OSGi, {@code Promise} is the idiom
 * there, and a plain-Java caller that wants async can wrap the synchronous client in its own
 * executor.
 *
 * <h2>Why there is one method and not a mirror of the whole surface</h2>
 *
 * {@link #submit(Function)} covers every operation, and — more to the point — it composes a
 * <em>sequence</em> into one promise. That is what a publisher actually needs: registering a
 * resource is "store the entity, store its distributions, assert its links", so a promise per
 * call would resolve when the first of several requests returned, which is not the moment
 * anybody cares about.
 * <p>
 * There is also a hard reason. A per-operation mirror cannot exist alongside the synchronous
 * interface on one object: {@code Registration<Dataset> registerDataset(String, Dataset,
 * String)} and {@code Promise<Registration<Dataset>> registerDataset(String, Dataset,
 * String)} differ only in return type, which Java forbids in one class — and the same is true
 * of {@code ready()}. Mirroring would mean either {@code …Async} suffixes on every method or a
 * second object registered by hand. One general method is better than either, so the
 * collision only confirmed the design.
 *
 * <h2>Do not close what you did not open</h2>
 *
 * Neither this nor the synchronous service should be closed by a consumer: both are one
 * shared client owned by the component, and it is released when the configuration goes away.
 * {@code DcatAtlasClient.close()} on the published service is deliberately a no-op.
 */
@ProviderType
public interface AsyncDcatAtlasClient {

	/**
	 * Run {@code work} against the client off the calling thread.
	 * <p>
	 * The whole publishing sequence belongs in one call — register, distributions, links —
	 * so the promise resolves when the resource is actually published rather than when the
	 * first of three requests returns:
	 *
	 * <pre>
	 * client.submit(dcat -&gt; {
	 *     dcat.registerDataset(id, dataset);
	 *     dcat.registerDistribution(id, "xmi", xmi);
	 *     dcat.linkDatasetToSeries(seriesId, id);
	 *     return id;
	 * });
	 * </pre>
	 *
	 * @param <T>  what the work produces
	 * @param work given the client; anything it throws fails the promise
	 * @return a promise of the result
	 */
	<T> Promise<T> submit(Function<DcatAtlasClient, T> work);

}
