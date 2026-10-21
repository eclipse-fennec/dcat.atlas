/**
 * What this instance is configured to do and what it holds, as a read-only snapshot —
 * and every configured thing that is not live, with the reason.
 * <p>
 * Plain {@link org.osgi.dto.DTO}s and nothing else: a watcher reporting to a metrics
 * backend should not need EMF, the codec or the DCAT-AP profile to read a number. The
 * shape is the one {@code emf.services} uses for {@code BrokerRuntime} and
 * {@code ClientRuntime}, so a reader who knows one knows this one.
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0")
package org.eclipse.fennec.dcat.atlas.api.runtime;
