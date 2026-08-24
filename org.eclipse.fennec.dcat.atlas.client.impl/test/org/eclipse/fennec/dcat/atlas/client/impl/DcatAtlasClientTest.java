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
package org.eclipse.fennec.dcat.atlas.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.dcat.atlas.client.api.BadRequestException;
import org.eclipse.fennec.dcat.atlas.client.api.ClientConfiguration;
import org.eclipse.fennec.dcat.atlas.client.api.ConflictException;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;
import org.eclipse.fennec.dcat.atlas.client.api.DcatCollection;
import org.eclipse.fennec.dcat.atlas.client.api.DcatModelConstraintException;
import org.eclipse.fennec.dcat.atlas.client.api.DcatShaclException;
import org.eclipse.fennec.dcat.atlas.client.api.DeleteMode;
import org.eclipse.fennec.dcat.atlas.client.api.NotFoundException;
import org.eclipse.fennec.dcat.atlas.client.api.PreconditionFailedException;
import org.eclipse.fennec.dcat.atlas.client.api.Registration;
import org.eclipse.fennec.dcat.atlas.client.api.RetryableException;
import org.eclipse.fennec.dcat.atlas.client.api.TransportException;
import org.eclipse.fennec.dcat.atlas.client.impl.StubPortal.Received;
import org.eclipse.fennec.dcat.atlas.client.impl.StubPortal.Reply;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dcat.Catalog;
import dcat.Dataset;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * The client driven end to end against a stub portal: a real Jersey client, a real socket,
 * real XMI.
 * <p>
 * What this suite is for is the two things P1 exists to settle — the transport and the
 * error mapping — and it asserts them on the wire rather than on the client's internals.
 */
class DcatAtlasClientTest {

	private static final String XMI = "application/xmi";

	private StubPortal portal;
	private DcatAtlasClient client;

	@BeforeEach
	void startPortal() throws IOException {
		portal = new StubPortal();
		client = DcatAtlasClient.builder().baseUri(portal.baseUri()).build();
	}

	@AfterEach
	void stopPortal() {
		client.close();
		portal.close();
	}

	// --- transport --------------------------------------------------------

	/**
	 * A registration is a {@code PUT} to the admin path, XMI in both directions, and the
	 * body really is the entity the caller handed over.
	 */
	@Test
	void registeringADatasetPutsXmiToTheAdminPath() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(dataset("Luftqualität 2026"))));

		Dataset stored = client.registerDataset("luftqualitaet-2026", dataset("Luftqualität 2026")).entity();

		Received request = portal.lastRequest();
		assertEquals("PUT", request.method());
		assertEquals("/rest/admin/datasets/luftqualitaet-2026", request.path());
		assertEquals(XMI, request.header("content-type"));
		assertTrue(request.bodyAsString().contains("Luftqualität 2026"),
				"the entity should be in the body: " + request.bodyAsString());
		assertEquals("Luftqualität 2026", titleOf(stored), "the portal's answer should come back as an EObject");
	}

	/**
	 * A write asks for XMI and nothing else.
	 *
	 * <h2>Why this is worth a test of its own</h2>
	 *
	 * The admin endpoints {@code @Produces} XMI, JSON, XML and RDF/XML — <b>not</b> Turtle.
	 * Asking for Turtle on a write is answered {@code 406} before the resource is entered,
	 * which reads like a rejected registration and is not one. It is an easy mistake
	 * because every <em>read</em> endpoint does offer Turtle.
	 */
	@Test
	void aWriteAsksForXmiAndNeverForTurtle() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(dataset("x"))));

		client.registerDataset("d1", dataset("x"));

		String accept = portal.lastRequest().header("accept");
		assertTrue(accept.contains(XMI), "a write must ask for XMI, was: " + accept);
		assertFalse(accept.contains("turtle"), "a write must not ask for Turtle, was: " + accept);
	}

	/** Every entity type reaches its own collection, hyphen included. */
	@Test
	void eachEntityTypeGoesToItsOwnCollection() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(catalog())));
		client.registerCatalog("c1", catalog());
		assertEquals("/rest/admin/catalogs/c1", portal.lastRequest().path());

		portal.enqueue(Reply.of(200, XMI, xmiOf(series())));
		client.registerDatasetSeries("s1", series());
		assertEquals("/rest/admin/dataset-series/s1", portal.lastRequest().path(),
				"the segment is hyphenated, which is why it is not derived from the enum name");

		portal.enqueue(Reply.of(200, XMI, xmiOf(service())));
		client.registerDataService("api", service());
		assertEquals("/rest/admin/data-services/api", portal.lastRequest().path());
	}

	/**
	 * A distribution is addressed through its dataset, because {@code dcat:distribution} is
	 * containment — it has no collection of its own.
	 */
	@Test
	void aDistributionIsNestedUnderItsDataset() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(distribution())));

		client.registerDistribution("luftqualitaet-2026", "csv", distribution());

		assertEquals("/rest/admin/datasets/luftqualitaet-2026/distributions/csv", portal.lastRequest().path());
	}

	/**
	 * A membership link sends no body at all: the path names both ends, and the endpoint
	 * declares no {@code @Consumes}. Sending one would be inventing a payload the API does
	 * not accept.
	 */
	@Test
	void aMembershipLinkNamesBothEndsInThePathAndSendsNoBody() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(catalog())));

		client.linkDatasetToCatalog("scope-a", "luftqualitaet-2026");

		Received request = portal.lastRequest();
		assertEquals("PUT", request.method());
		assertEquals("/rest/admin/catalogs/scope-a/datasets/luftqualitaet-2026", request.path());
		assertEquals(0, request.body().length, "a link carries no body");
	}

	/** The four membership shapes the model.atlas mapping needs, each on its own path. */
	@Test
	void everyMembershipKindHasItsOwnPath() {
		portal.enqueue(Reply.of(204));
		client.linkDataServiceToCatalog("scope-a", "api");
		assertEquals("/rest/admin/catalogs/scope-a/services/api", portal.lastRequest().path());

		portal.enqueue(Reply.of(204));
		client.linkSubCatalog("root", "scope-a");
		assertEquals("/rest/admin/catalogs/root/catalogs/scope-a", portal.lastRequest().path());

		portal.enqueue(Reply.of(204));
		client.linkDatasetToSeries("my-model", "my-model-released");
		assertEquals("/rest/admin/dataset-series/my-model/datasets/my-model-released",
				portal.lastRequest().path());

		portal.enqueue(Reply.of(204));
		client.linkDatasetToDataService("api", "my-model-released");
		assertEquals("/rest/admin/data-services/api/datasets/my-model-released", portal.lastRequest().path());

		portal.enqueue(Reply.of(204));
		client.linkAccessService("my-model-released", "xmi", "api");
		assertEquals("/rest/admin/datasets/my-model-released/distributions/xmi/access-service/api",
				portal.lastRequest().path());
	}

	@Test
	void unlinkingIsADeleteOnTheSamePath() {
		portal.enqueue(Reply.of(204));

		client.unlinkDatasetFromCatalog("scope-a", "luftqualitaet-2026");

		Received request = portal.lastRequest();
		assertEquals("DELETE", request.method());
		assertEquals("/rest/admin/catalogs/scope-a/datasets/luftqualitaet-2026", request.path());
	}

	/** A read goes to the public path, without the {@code /admin} segment. */
	@Test
	void aReadUsesThePublicPath() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(dataset("Luftqualität 2026"))));

		Dataset found = client.dataset("luftqualitaet-2026").orElseThrow();

		assertEquals("/rest/datasets/luftqualitaet-2026", portal.lastRequest().path());
		assertEquals("Luftqualität 2026", titleOf(found));
	}

	/** A missing resource is an empty Optional, not an exception — it is a normal answer. */
	@Test
	void aMissingResourceReadsAsEmpty() {
		portal.enqueue(Reply.of(404, "text/plain", ""));

		assertTrue(client.dataset("nope").isEmpty());
	}

	// --- error mapping ----------------------------------------------------

	/**
	 * A SHACL refusal carries the report through untouched, with the media type it arrived
	 * in, so a caller can log or display it without bringing Jena.
	 */
	@Test
	void aShaclRefusalCarriesTheReportRaw() {
		String report = """
				@prefix sh: <http://www.w3.org/ns/shacl#> .
				[] a sh:ValidationReport ; sh:conforms false .
				""";
		portal.enqueue(Reply.of(422, "text/turtle", report).withHeader("X-SHACL-Conforms", "false"));

		DcatShaclException refusal = assertThrows(DcatShaclException.class,
				() -> client.registerDataset("d1", dataset("no publisher")));

		assertTrue(refusal.getReport().contains("sh:conforms"), refusal.getReport());
		assertEquals("text/turtle", refusal.getReportMediaType());
	}

	/**
	 * The two {@code 422}s are told apart by the header, not the body's media type.
	 *
	 * <h2>Why that distinction matters</h2>
	 *
	 * The SHACL branch can itself answer {@code text/plain} — it does when there is a
	 * violation but no report object. So a client that sniffed the media type would call
	 * that an OCL failure. Only {@code X-SHACL-Conforms} separates them, and it is present
	 * on both SHACL shapes.
	 */
	@Test
	void aModelConstraintRefusalIsADifferentExceptionFromAShaclOne() {
		portal.enqueue(Reply.of(422, "text/plain", "Dataset: title must not be empty\nDataset: needs a publisher"));

		DcatModelConstraintException refusal = assertThrows(DcatModelConstraintException.class,
				() -> client.registerDataset("d1", dataset("x")));

		assertEquals(List.of("Dataset: title must not be empty", "Dataset: needs a publisher"),
				refusal.getViolations());
	}

	/** A text/plain 422 that carries the header is still a SHACL refusal. */
	@Test
	void aPlainTextShaclRefusalIsStillAShaclRefusal() {
		portal.enqueue(Reply.of(422, "text/plain", "dcat:Dataset: dcterms:title MUSS vorhanden sein")
				.withHeader("X-SHACL-Conforms", "false"));

		DcatShaclException refusal = assertThrows(DcatShaclException.class,
				() -> client.registerDataset("d1", dataset("x")));
		assertEquals("text/plain", refusal.getReportMediaType());
	}

	@Test
	void everyStatusMapsToItsOwnType() {
		assertThrows(BadRequestException.class, () -> failWith(400));
		assertThrows(NotFoundException.class, () -> failWith(404));
		assertThrows(ConflictException.class, () -> failWith(409));
		assertThrows(PreconditionFailedException.class, () -> failWith(412));
		assertThrows(TransportException.class, () -> failWith(415));
		assertThrows(RetryableException.class, () -> failWith(503));
		assertThrows(TransportException.class, () -> failWith(500));
	}

	/**
	 * A 503 must not read as a lost registration: the portal committed the write and only
	 * its mirror push failed, so the message says so and the type is retryable.
	 */
	@Test
	void aFailedMirrorPushSaysTheCommitIsDurable() {
		portal.enqueue(Reply.of(503, "text/plain", "push to the remote failed"));

		RetryableException retryable = assertThrows(RetryableException.class, () -> client.registerDataset("d1",
				dataset("x")));

		assertTrue(retryable.getMessage().contains("durable"),
				"the message must not read as data loss: " + retryable.getMessage());
	}

	/** No answer at all is a different thing from any status, and gets its own type. */
	@Test
	void anUnreachablePortalIsATransportFailure() {
		DcatAtlasClient unreachable = DcatAtlasClient.builder() //
				.baseUri(URI.create("http://127.0.0.1:1/rest/")) //
				.connectTimeoutMs(250) //
				.readTimeoutMs(250) //
				.build();
		try (DcatAtlasClient closeable = unreachable) {
			assertThrows(TransportException.class, () -> closeable.registerDataset("d1", dataset("x")));
		}
	}

	// --- conditional registration ------------------------------------------

	/** An ordinary registration sends no precondition: last writer wins, by design. */
	@Test
	void anUnconditionalRegistrationSendsNoIfMatch() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(dataset("x"))).withHeader("ETag", "\"v1\""));

		Registration<Dataset> registration = client.registerDataset("d1", dataset("x"));

		assertTrue(registration.applied());
		assertNull(portal.lastRequest().header("if-match"), "an unconditional write must not send If-Match");
	}

	/**
	 * The validator comes back on the write, which is what makes the next registration
	 * conditional without a read.
	 */
	@Test
	void aRegistrationHandsBackTheValidatorForTheNextOne() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(dataset("x"))).withHeader("ETag", "\"v1\""));

		assertEquals("\"v1\"", client.registerDataset("d1", dataset("x")).etag());
	}

	@Test
	void aConditionalRegistrationSendsTheValidatorItWasGiven() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(dataset("x"))).withHeader("ETag", "\"v2\""));

		Registration<Dataset> registration = client.registerDataset("d1", dataset("x"), "\"v1\"");

		assertTrue(registration.applied());
		assertEquals("\"v1\"", portal.lastRequest().header("if-match"));
		assertEquals("\"v2\"", registration.etag(), "the new validator replaces the one that was sent");
	}

	/**
	 * Somebody else wrote in the meantime: nothing is stored, and this is <em>not</em> an
	 * exception.
	 *
	 * <h2>Why not</h2>
	 *
	 * The 412 is the outcome the validator was sent to produce. A registration loop that
	 * unwound on it would stop publishing every later resource because one of them had been
	 * edited elsewhere. So the client logs it and reports it, and the caller decides.
	 */
	@Test
	void aConditionalRegistrationIsNotAppliedWhenSomebodyElseWrote() {
		portal.enqueue(Reply.of(412));

		Registration<Dataset> registration = client.registerDataset("d1", dataset("x"), "\"stale\"");

		assertFalse(registration.applied(), "nothing should have been written");
	}

	/**
	 * Asking for the entity of a write that did not happen is a programming error, not a
	 * null — a null here would surface much later and somewhere else.
	 */
	@Test
	void askingForTheResultOfARefusedRegistrationFailsLoudly() {
		portal.enqueue(Reply.of(412));

		Registration<Dataset> registration = client.registerDataset("d1", dataset("x"), "\"stale\"");

		assertThrows(IllegalStateException.class, registration::entity);
		assertThrows(IllegalStateException.class, registration::etag);
	}

	/**
	 * A 412 on an <em>unconditional</em> write is a different matter: no precondition was
	 * sent, so the portal cannot have evaluated one, and something is wrong rather than
	 * merely contended. That still throws.
	 */
	@Test
	void anUnexpected412OnAnUnconditionalWriteStillThrows() {
		portal.enqueue(Reply.of(412));

		assertThrows(PreconditionFailedException.class, () -> client.registerDataset("d1", dataset("x")));
	}

	/** Every entity type takes a validator, not just Dataset. */
	@Test
	void everyEntityTypeCanBeRegisteredConditionally() {
		portal.enqueue(Reply.of(412));
		assertFalse(client.registerCatalog("c1", catalog(), "\"stale\"").applied());

		portal.enqueue(Reply.of(412));
		assertFalse(client.registerDatasetSeries("s1", series(), "\"stale\"").applied());

		portal.enqueue(Reply.of(412));
		assertFalse(client.registerDataService("api", service(), "\"stale\"").applied());

		portal.enqueue(Reply.of(412));
		assertFalse(client.registerDistribution("d1", "csv", distribution(), "\"stale\"").applied());
	}

	// --- validators -------------------------------------------------------

	/** The validator comes from a HEAD on the public read path — header only, no entity. */
	@Test
	void etagOfFetchesTheValidatorWithoutTheEntity() {
		portal.enqueue(Reply.of(200).withHeader("ETag", "\"v7\""));

		assertEquals("\"v7\"", client.etagOf(DcatCollection.DATASETS, "d1").orElseThrow());

		Received request = portal.lastRequest();
		assertEquals("HEAD", request.method());
		assertEquals("/rest/datasets/d1", request.path());
	}

	@Test
	void etagOfADistributionUsesTheNestedPath() {
		portal.enqueue(Reply.of(200).withHeader("ETag", "\"v8\""));

		assertEquals("\"v8\"", client.etagOfDistribution("d1", "csv").orElseThrow());
		assertEquals("/rest/datasets/d1/distributions/csv", portal.lastRequest().path());
	}

	/** A resource that is not there has no validator. */
	@Test
	void etagOfIsEmptyForAMissingResource() {
		portal.enqueue(Reply.of(404));

		assertTrue(client.etagOf(DcatCollection.DATASETS, "nope").isEmpty());
	}

	/**
	 * A resource that exists but carries no validator is empty too. Collapsing the two is
	 * deliberate: in both cases there is nothing to guard a write with, so a caller feeding
	 * the result to a conditional register does the right thing without branching.
	 */
	@Test
	void etagOfIsEmptyWhenTheResponseCarriesNoValidator() {
		portal.enqueue(Reply.of(200));

		assertTrue(client.etagOf(DcatCollection.DATASETS, "d1").isEmpty());
	}

	/** A real failure is still a failure — empty means "no validator", not "something broke". */
	@Test
	void etagOfStillThrowsOnARealFailure() {
		portal.enqueue(Reply.of(500, "text/plain", "boom"));

		assertThrows(TransportException.class, () -> client.etagOf(DcatCollection.DATASETS, "d1"));
	}

	/**
	 * The pattern this method exists for: a publisher that has restarted, holding no
	 * validator of its own, picks up the current one and registers conditionally against
	 * it — no unconditional first write, so a foreign edit cannot be clobbered on startup.
	 */
	@Test
	void aRestartedPublisherCanRegisterConditionallyWithoutClobbering() {
		portal.enqueue(Reply.of(200).withHeader("ETag", "\"current\""));
		portal.enqueue(Reply.of(200, XMI, xmiOf(dataset("x"))).withHeader("ETag", "\"next\""));

		String validator = client.etagOf(DcatCollection.DATASETS, "d1").orElse(null);
		Registration<Dataset> result = client.registerDataset("d1", dataset("x"), validator);

		assertTrue(result.applied());
		assertEquals("HEAD", portal.received().get(0).method());
		assertEquals("\"current\"", portal.received().get(1).header("if-match"),
				"the write must be guarded by the validator the HEAD just reported");
		assertEquals("\"next\"", result.etag());
	}

	/**
	 * And the same pattern on a resource that does not exist yet: the empty validator makes
	 * the write unconditional, which is what a create needs.
	 */
	@Test
	void theSamePatternCreatesAResourceThatIsNotThereYet() {
		portal.enqueue(Reply.of(404));
		portal.enqueue(Reply.of(201, XMI, xmiOf(dataset("x"))).withHeader("ETag", "\"first\""));

		String validator = client.etagOf(DcatCollection.DATASETS, "d1").orElse(null);
		Registration<Dataset> result = client.registerDataset("d1", dataset("x"), validator);

		assertTrue(result.applied());
		assertNull(portal.received().get(1).header("if-match"), "a create must not send a precondition");
	}

	// --- deletion ---------------------------------------------------------

	/**
	 * A cascade reports what it unlinked, and the client hands that on: every one of those
	 * resources has a new ETag, so a caller holding one needs to know.
	 */
	@Test
	void aCascadeDeleteReturnsTheResourcesItUnlinked() {
		portal.enqueue(Reply.of(200, "text/plain",
				"http://portal/rest/catalogs/scope-a\nhttp://portal/rest/dataset-series/my-model\n"));

		List<String> unlinked = client.delete(DcatCollection.DATASETS, "d1", DeleteMode.CASCADE);

		assertEquals(List.of("http://portal/rest/catalogs/scope-a", "http://portal/rest/dataset-series/my-model"),
				unlinked);
		assertEquals("cascade=true", portal.lastRequest().query());
	}

	/** A plain delete says {@code cascade=false} and reports nothing, because nothing else moved. */
	@Test
	void aPlainDeleteAsksForNoCascadeAndReportsNothing() {
		portal.enqueue(Reply.of(204));

		assertEquals(List.of(), client.delete(DcatCollection.DATASETS, "d1", DeleteMode.SINGLE));
		assertEquals("cascade=false", portal.lastRequest().query());
	}

	/** A cascade with nothing to unlink is a 204, and an empty list rather than an error. */
	@Test
	void aCascadeWithNothingToUnlinkIsEmpty() {
		portal.enqueue(Reply.of(204));

		assertEquals(List.of(), client.delete(DcatCollection.DATASETS, "d1", DeleteMode.CASCADE));
	}

	/** Refusing to delete something still referenced is a conflict, not a transport error. */
	@Test
	void deletingAReferencedResourceWithoutCascadeIsAConflict() {
		portal.enqueue(Reply.of(409, "text/plain", "referenced by http://dcat.atlas/catalogs/scope-a"));

		assertThrows(ConflictException.class,
				() -> client.delete(DcatCollection.DATASETS, "d1", DeleteMode.SINGLE));
	}

	// --- portal state -----------------------------------------------------

	/**
	 * Readiness lives <em>beside</em> the REST application, not under it: with a base of
	 * {@code …/rest/} the checks are at {@code …/health/ready}. Getting this wrong asks the
	 * REST application for a path it does not serve and reads as "not ready" for ever.
	 */
	@Test
	void readinessIsCheckedBesideTheRestApplicationNotUnderIt() {
		portal.enqueue(Reply.of(200, "application/json", "{\"overallResult\":\"OK\"}"));

		assertTrue(client.ready());
		assertEquals("/health/ready", portal.lastRequest().path());
	}

	@Test
	void aNotReadyPortalIsFalseRatherThanAnException() {
		portal.enqueue(Reply.of(503, "application/json", "{\"overallResult\":\"CRITICAL\"}"));

		assertFalse(client.ready());
	}

	/** Unreachable is not ready. A gate must not throw, or it cannot gate anything. */
	@Test
	void anUnreachablePortalIsSimplyNotReady() {
		DcatAtlasClient unreachable = DcatAtlasClient.builder() //
				.baseUri(URI.create("http://127.0.0.1:1/rest/")) //
				.connectTimeoutMs(250) //
				.readTimeoutMs(250) //
				.build();
		try (DcatAtlasClient closeable = unreachable) {
			assertFalse(closeable.ready());
		}
	}

	@Test
	void aboutForComputesThePublicIriThePortalWouldMint() {
		assertEquals(URI.create(portal.baseUri() + "datasets/luftqualitaet-2026"),
				client.aboutFor(DcatCollection.DATASETS, "luftqualitaet-2026"));
		assertEquals(URI.create(portal.baseUri() + "dataset-series/my-model"),
				client.aboutFor(DcatCollection.DATASET_SERIES, "my-model"));
	}

	// --- configuration ----------------------------------------------------

	@Test
	void aBaseUriIsRequired() {
		assertThrows(IllegalStateException.class, () -> DcatAtlasClient.builder().build());
		assertThrows(IllegalStateException.class, () -> ClientConfiguration.builder().build());
	}

	@Test
	void aBaseUriWithoutATrailingSlashStillResolvesCorrectly() {
		URI noSlash = URI.create(portal.baseUri().toString().replaceAll("/$", ""));
		try (DcatAtlasClient lenient = DcatAtlasClient.builder().baseUri(noSlash).build()) {
			portal.enqueue(Reply.of(200, XMI, xmiOf(dataset("x"))));
			lenient.registerDataset("d1", dataset("x"));
			assertEquals("/rest/admin/datasets/d1", portal.lastRequest().path());
		}
	}

	@Test
	void anIdIsRequiredAndNotSilentlyDropped() {
		assertThrows(IllegalArgumentException.class, () -> client.registerDataset("", dataset("x")));
		assertThrows(IllegalArgumentException.class, () -> client.registerDataset(null, dataset("x")));
	}

	// --- XMI --------------------------------------------------------------

	/** The round trip this client is built on: EObject to XMI and back. */
	@Test
	void xmiRoundTripsThroughTheCodec() {
		Dataset original = dataset("Luftqualität 2026");

		Dataset reparsed = XmiCodec.read(XmiCodec.write(original), Dataset.class, "test");

		assertEquals("Luftqualität 2026", titleOf(reparsed));
		assertEquals("de", reparsed.getTitle().get(0).getLang());
	}

	/**
	 * Serialising must not move the caller's object out of its own resource.
	 *
	 * <h2>The bug this prevents</h2>
	 *
	 * Adding an {@code EObject} to a {@code Resource} <em>re-parents</em> it. A client that
	 * did that would silently empty a caller's own model as a side effect of registering
	 * from it — which is why {@link XmiCodec} copies first.
	 */
	@Test
	void serialisingDoesNotStealTheCallersObject() {
		ResourceSet callersOwn = new ResourceSetImpl();
		callersOwn.getResourceFactoryRegistry().getExtensionToFactoryMap() //
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		Resource callersResource = callersOwn
				.createResource(org.eclipse.emf.common.util.URI.createURI("callers.xmi"));
		Dataset entity = dataset("mine");
		callersResource.getContents().add(entity);

		XmiCodec.write(entity);

		assertEquals(1, callersResource.getContents().size(), "the caller's resource must still hold its object");
		assertSame(entity, callersResource.getContents().get(0));
	}

	@Test
	void anUnexpectedTypeInTheResponseIsAnError() {
		portal.enqueue(Reply.of(200, XMI, xmiOf(catalog())));

		// A Catalog *is* a Dataset in this model, so ask for something it is not.
		assertThrows(org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClientException.class,
				() -> client.registerDataService("api", service()));
	}

	// --- helpers ----------------------------------------------------------

	private void failWith(int status) {
		portal.enqueue(Reply.of(status, "text/plain", "nope"));
		client.registerDataset("d1", dataset("x"));
	}

	private static byte[] xmiOf(org.eclipse.emf.ecore.EObject entity) {
		return XmiCodec.write(entity);
	}

	private static String titleOf(dcat.DcatResource entity) {
		assertFalse(entity.getTitle().isEmpty(), "expected a title");
		return entity.getTitle().get(0).getValue();
	}

	private static Dataset dataset(String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.getTitle().add(literal(title, "de"));
		return dataset;
	}

	private static Catalog catalog() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.getTitle().add(literal("Scope A", "en"));
		return catalog;
	}

	private static dcat.DatasetSeries series() {
		dcat.DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		series.getTitle().add(literal("My model", "en"));
		return series;
	}

	private static dcat.DataService service() {
		dcat.DataService service = DcatFactory.eINSTANCE.createDataService();
		service.getTitle().add(literal("model.atlas API", "en"));
		return service;
	}

	private static dcat.Distribution distribution() {
		dcat.Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setTitle(literal("XMI", "en"));
		return distribution;
	}

	private static PlainLiteral literal(String value, String lang) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setValue(value);
		literal.setLang(lang);
		return literal;
	}
}
