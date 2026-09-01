# Fennec DCAT.Atlas — Client Guide

> Status: draft. This guide is for **consumers** — a Data-Atlas, a Model-Atlas, or any
> application that wants to register DCAT metadata with a DCAT.Atlas portal. For the
> portal itself, its REST surface and its validation, see the
> [User Guide](./opendata-portal-user-guide.md).

## Overview

The client hands **EMF objects** to a portal rather than composing HTTP requests: you
build a `dcat.Dataset`, and the client serialises it, addresses it, sends it and maps
the portal's answer onto a typed exception. It comes in three bundles:

| Bundle | What it is |
|---|---|
| `org.eclipse.fennec.dcat.atlas.client.api` | The `DcatAtlasClient` contract, `ClientConfiguration`, the exception hierarchy. No OSGi runtime dependencies — consumable as a plain library. |
| `org.eclipse.fennec.dcat.atlas.client.impl` | The implementation: XMI (de)serialisation, status mapping, the default Jakarta RS client. Reached through `ServiceLoader` on a plain classpath, and through SPI-Fly in OSGi. |
| `org.eclipse.fennec.dcat.atlas.client.osgi` | The OSGi front-end: a ConfigAdmin **factory** component that publishes one configured portal as a service. |

There are two ways in, and they share everything below the Jakarta RS seam:

- **Plain Java** — `DcatAtlasClient.builder()`, you own the client and close it.
- **OSGi** — a factory configuration under PID `org.eclipse.fennec.dcat.atlas.client`
  publishes a `DcatAtlasClient` (and an `AsyncDcatAtlasClient`) into the service
  registry. You never construct or close it.

**One client addresses one portal.** Publishing to two is an explicit loop over two
clients, because "it worked" has no single meaning when one portal accepts and another
refuses.

## Plain Java

```java
import java.net.URI;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;

try (DcatAtlasClient client = DcatAtlasClient.builder()
        .baseUri(URI.create("http://localhost:8085/dcat/rest/"))
        .connectTimeoutMs(5_000)
        .readTimeoutMs(30_000)
        .build()) {

    if (!client.ready()) {
        // The portal answers but is not fit to be written to — see Readiness below.
    }
    client.registerDataset("air-quality", dataset);
}
```

`builder()` finds the implementation through `ServiceLoader`, so
`org.eclipse.fennec.dcat.atlas.client.impl` must be on the classpath together with a
Jakarta RS implementation (the test suites use Jersey plus its HK2 injection). Without
the impl bundle, `builder()` throws `IllegalStateException` naming what it looked for.

> The generated `META-INF/services` descriptor reaches the impl **jar** only, never a
> classes directory. A build that puts the impl on the classpath as loose classes cannot
> reach it through `builder()`.

The builder's convenience setters cover the two bases and the two timeouts —
`baseUri`, `publicBaseUri` (see [Two bases](#two-bases-where-you-connect-and-what-the-portal-serves)),
`connectTimeoutMs`, `readTimeoutMs`. **Authentication and `read.accept` are set through
`ClientConfiguration`:**

```java
ClientConfiguration configuration = ClientConfiguration.builder()
        .baseUri(URI.create("https://dcat.example.org/rest/"))
        .authType(AuthType.BEARER)
        .authTokenEnv("DCAT_TOKEN")   // the env var holding the token, not the token
        .build();

DcatAtlasClient client = DcatAtlasClient.builder()
        .configuration(configuration)
        .build();
```

`configuration(…)` replaces everything set so far; later convenience setters refine it.

## OSGi

### One configuration, one portal

The component's PID is a ConfigAdmin **factory** PID:

```
org.eclipse.fennec.dcat.atlas.client
```

Each configuration becomes an independent component instance with its own client, and
publishes both service interfaces:

- `org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient` — synchronous
- `org.eclipse.fennec.dcat.atlas.client.osgi.AsyncDcatAtlasClient` — `Promise`-based

`configurationPolicy = REQUIRE` and there is no `@Modified` method: changing a
configuration tears the instance down, closing its client, and activates a fresh one.

### A configuration, with the OSGi Configurator

```json
{
  ":configurator:resource-version": 1,

  "org.eclipse.fennec.dcat.atlas.client~portal": {
    "dcat.portal": "portal",
    "base.uri": "http://dcat:8080/rest/",
    "connect.timeout.ms": 5000,
    "read.timeout.ms": 30000,
    "check.ready": true,
    "require.ready": false,
    "auth.type": "NONE"
  }
}
```

The `~portal` suffix is the Configurator's factory-instance name; it is not the portal
name — `dcat.portal` is, and it has to be set explicitly.

### The same thing through ConfigurationAdmin

```java
Configuration configuration = configAdmin
        .createFactoryConfiguration("org.eclipse.fennec.dcat.atlas.client", "?");
Hashtable<String, Object> properties = new Hashtable<>();
properties.put("dcat.portal", "portal");
properties.put("base.uri", "http://dcat:8080/rest/");
properties.put("check.ready", Boolean.TRUE);
configuration.update(properties);
```

### Selecting a portal

`dcat.portal` is published as a service property, so a consumer says which portal it
means:

```java
@Component
public class CatalogPublisher {

    @Reference(target = "(dcat.portal=portal)")
    DcatAtlasClient dcat;
}
```

`dcat.portal` is **required, deliberately**. With one portal it is redundant; the moment
there are two, a consumer that did not name one binds whichever arrived first — a bug
that only appears in the deployment that has two. `base.uri` is published as a service
property too, which is useful for logging what a component actually bound to.

### The asynchronous face

`AsyncDcatAtlasClient` exists to keep a DS `@Activate` from blocking on a portal, not to
push throughput. It has one method, and that is on purpose: a publishing sequence is
several requests, so a promise per call would resolve at the wrong moment. Put the whole
sequence in one `submit`:

```java
Promise<String> published = async.submit(client -> {
    client.registerDataset(id, dataset);
    client.registerDistribution(id, "xmi", distribution);
    client.linkDatasetToCatalog("gov", id);
    return id;
});
```

Work runs on one daemon thread per portal (`dcat-atlas-client-<portal>`), so a caller's
ordering is preserved.

### Do not close an injected client

Both published services are one shared client owned by the component. `close()` on the
injected `DcatAtlasClient` is deliberately a **no-op**, precisely because
`DcatAtlasClient` is `AutoCloseable` and try-with-resources on an injected service is a
plausible reflex — it would otherwise take the portal away from every other consumer.
The real close happens when the configuration goes away.

## Authentication

> **The portal has no authentication today**: no security filter, no credentials, and
> the admin API is open to anything that can reach the port. `auth.type` exists so
> consumers are configured through it from the start and need no rewrite when a policy
> enforcement point appears in front of the portal. `NONE` is the only mode the portal
> actually understands.

| `auth.type` | What the client sends | Keys it reads |
|---|---|---|
| `NONE` (default) | nothing | — |
| `BEARER` | `Authorization: Bearer <token>` | `auth.token.env` |
| `API_KEY` | a fixed header, e.g. `X-API-Key: <key>` | `auth.apikey.header`, `auth.apikey.env` |
| `MTLS` | a mutual-TLS connection | `auth.keystore.*`, `auth.truststore.*` |

**Credentials are read from the environment, never stored in the configuration.**
`auth.token.env` and `auth.apikey.env` name an *environment variable*; the client calls
`System.getenv` on it when it builds the HTTP client. So a configuration file can be
committed and a container gets its secret through its environment.

```json
"org.eclipse.fennec.dcat.atlas.client~portal": {
  "dcat.portal": "portal",
  "base.uri": "https://dcat.example.org/rest/",
  "auth.type": "BEARER",
  "auth.token.env": "DCAT_TOKEN"
}
```

```json
"org.eclipse.fennec.dcat.atlas.client~piveau": {
  "dcat.portal": "piveau",
  "base.uri": "https://piveau.example.org/rest/",
  "auth.type": "API_KEY",
  "auth.apikey.header": "X-API-Key",
  "auth.apikey.env": "PIVEAU_API_KEY"
}
```

```json
"org.eclipse.fennec.dcat.atlas.client~mtls": {
  "dcat.portal": "mtls",
  "base.uri": "https://dcat.internal/rest/",
  "auth.type": "MTLS",
  "auth.keystore.path": "/run/secrets/client.p12",
  "auth.keystore.password": "…",
  "auth.keystore.type": "PKCS12",
  "auth.truststore.path": "/run/secrets/truststore.p12",
  "auth.truststore.password": "…",
  "auth.truststore.type": "PKCS12"
}
```

The two failure modes differ on purpose:

- **A missing token or API key logs a warning and the client proceeds
  unauthenticated.** Against today's portal that works, and failing hard would be a
  self-inflicted outage.
- **An unloadable key or trust store throws.** mTLS was asked for explicitly, and
  connecting without it would not be what the operator configured.

Both store passwords are declared as metatype `PASSWORD` attributes, so a management
agent that respects that will not display them. `ClientConfiguration.toString()` omits
every credential field.

## Readiness

A portal whose SHACL shapes are not mounted, or whose store is unreachable, reports
CRITICAL and then refuses writes in ways that read like the caller's fault. Two settings
decide what the client does about it at activation:

| Key | Default | Effect |
|---|---|---|
| `check.ready` | `true` | Probe `GET /health/ready` once, at activation, and log the answer. |
| `require.ready` | `false` | A portal that is not ready **prevents activation** (`IllegalStateException`). |

`check.ready` is on because one log line beats a confusing cascade later.
`require.ready` is off because a portal that is merely not ready *yet* is normal during
a co-ordinated start-up, and failing activation would need something to retry it. Turn
it on where publishing into an unprepared portal would be worse than not starting at
all.

`ready()` is a gate, not a diagnostic: it returns `false` for `503` **and** for a portal
it cannot reach, and never throws. It resolves `../health/ready` against `base.uri`, so
with a base of `http://host:8085/dcat/rest/` the probe goes to
`http://host:8085/dcat/health/ready` — health is a sibling of the REST application, not
a child of it.

With `require.ready` on, a portal that is not ready leaves **no service in the
registry**. A consumer whose `@Reference` is unsatisfied is seeing exactly that.

## Two bases: where you connect, and what the portal serves

**`base.uri` is a transport address.** Every request is targeted at it and
`/health/ready` is probed relative to it, so it has to be the URL *this runtime* can
reach the portal on — behind a reverse proxy, the internal one.

**`public.base.uri` is what the portal serves its identities under**, and what
`aboutFor(collection, id)` computes from. In a direct deployment the two are the same URL
and you leave it unset; `aboutFor` falls back to `base.uri`.

Behind a proxy they are two different URLs and one value cannot be both, so set it:

```json
"org.eclipse.fennec.dcat.atlas.client~portal": {
  "dcat.portal": "portal",
  "base.uri": "http://dcat:8080/rest/",
  "public.base.uri": "https://data.example.org/rest/"
}
```

```
aboutFor(DATASETS, "air-quality")  ->  https://data.example.org/rest/datasets/air-quality
requests still go to              ->  http://dcat:8080/rest/admin/datasets/air-quality
readiness still probes            ->  http://dcat:8080/health/ready
```

Leaving it unset in a proxied deployment is what [#42] was about: `aboutFor` then returns
an IRI under the internal hostname, which the portal refuses with **400** if you send it
as `rdf:about`, and which is *silently* unresolvable if you merely record it as what you
published.

A trailing slash is optional on both — the client normalises them.

> **After a write you do not need either setting.** The registration response carries the
> stored entity with its `about` already rendered under the portal's public base, so
> `registration.entity().getAbout()` is correct whatever is configured. Prefer it where
> you have it; `aboutFor` is for the window before the first write, and for setting
> `about` yourself.

If you would rather not configure a second base, the alternatives are to leave `about`
unset (the normal case — the path carries the id) or to add the internal base to the
portal's `PublicIris.additionalOwnedBases`, so IRIs under it are recognised as its own.

[#42]: https://github.com/eclipse-fennec/dcat.atlas/issues/42

## What a write must contain

The portal validates writes against the model's own constraints before they reach the
store (`StoreConfig.validateOnWrite` defaults to `true`), so entities cannot be minimal.
These are the model's obligations, not the client's:

| Type | Required |
|---|---|
| `Dataset`, `Catalog`, `DatasetSeries` | `title` (≥1), `publisher` (a `foaf:Agent` whose `name` is required in turn, so an empty Agent will not do), `description` (≥1, OCL invariant `HasDescription`) |
| `DataService` | `title` (≥1), `publisher` |
| `Distribution` | `accessURL` (≥1), `license`; it is not a `DcatResource`, so it needs no publisher |

Leaving any of them out is a `422` — `DcatModelConstraintException`, whose
`getViolations()` names them.

```java
Dataset dataset = DcatFactory.eINSTANCE.createDataset();
dataset.getTitle().add(literal("Air quality", "de"));
dataset.getDescription().add(literal("Hourly measurements, all stations", "de"));
dataset.setPublisher(publisher());   // foaf:Agent with a name

Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
distribution.setTitle(literal("XMI", "en"));          // single-valued here
distribution.getAccessURL().add("https://dcat.example.org/rest/datasets/air-quality");
distribution.setLicense(license());
```

Note the asymmetry: `title` and `description` are multi-valued on a `DcatResource` and
**single-valued on a `Distribution`**.

`publisher` is a *containment* reference, so one shared `Agent` instance would be
**moved** from entity to entity rather than copied into each. Build a fresh one per
entity.

Where SHACL enforcement is on, the DCAT-AP.de profile asks for more — title,
description, publisher, licence and theme. See
[Validating metadata](./opendata-portal-user-guide.md#validating-metadata).

## The registration loop

Every `registerX` is a `PUT` to `/admin/{collection}/{id}`, where the **path** decides
identity. Consumers arrive with their own stable identifier, so create-or-replace with
no bookkeeping is exactly the primitive they need, and re-running a loop is free. There
is no `POST` on this interface: a repeat `POST` carrying an identity the portal already
holds is a `409` by design.

A `PUT` *replaces* the resource, which has two consequences worth planning for:

1. its **distributions go away** — `dcat:distribution` is containment, and a body
   without them says it has none;
2. its **membership links go away** — `dcat:inSeries`, `dcat:dataset`,
   `dcat:servesDataset`.

So publishing is three steps, all idempotent:

```java
client.registerDataset(id, dataset);                       // 1. the entity
client.registerDistribution(id, "xmi", distribution);      // 2. its distributions
client.linkDatasetToCatalog("gov", id);                    // 3. its links
```

Running the whole loop again is safe and is the intended usage — this is not a
workaround.

> **Read-modify-write does not work here**, which is why the interface offers no helper
> for it. Reading a linked resource and `PUT`ting it back is refused by SHACL: the body
> carries `dcat:inSeries <series>`, but nothing in that graph says `<series>` is a
> `dcat:DatasetSeries`, so the profile's "must reference a resource of type …" rule
> fails. Build the entity from your own source of truth and re-register it.

### Membership

Membership endpoints let a member be attached without re-sending the container, which
matters when the container is large or two consumers write to it.

| Call | Reference |
|---|---|
| `linkDatasetToCatalog(catalogId, datasetId)` | `dcat:dataset` |
| `linkDataServiceToCatalog(catalogId, serviceId)` | `dcat:service` |
| `linkSubCatalog(catalogId, subCatalogId)` | `dcat:catalog` |
| `linkDatasetToSeries(seriesId, datasetId)` | `dcat:inSeries` |
| `linkDatasetToDataService(serviceId, datasetId)` | `dcat:servesDataset` |
| `linkAccessService(datasetId, distributionId, serviceId)` | `dcat:accessService` |

Each has an `unlink…` twin; unlinking something that was not linked is not an error.
Linking to a container that does not exist, or naming a member that does not, is a
`404`.

`linkDatasetToSeries` hides an asymmetry: `inSeries` lives on the **dataset**, so the
call edits the dataset and it is the *dataset's* ETag that moves, not the series'.

### Conditional registration

Pass the validator from the previous `Registration` to make the next write conditional:

```java
Registration<Dataset> first = client.registerDataset(id, dataset);
Registration<Dataset> next = client.registerDataset(id, changed, first.etag());

if (!next.applied()) {
    // Somebody else wrote to the resource. Nothing was written; decide what that means.
}
```

A refused conditional write comes back `applied() == false` rather than as an exception
— that is the whole point of sending the validator, so a registration loop can carry on
with the next resource. The client logs it at `WARNING`, because the portal's copy now
diverges from what this publisher intended and somebody ought to see that. `entity()` and `etag()` throw `IllegalStateException` on a
refused registration, so check `applied()` first. The portal's `412` has no body and no
`ETag`, so a refusal cannot tell you the current validator.

Across a restart the in-memory validator is gone. `etagOf` is the way back in — a `HEAD`,
so nothing is parsed and nothing can be rejected:

```java
String validator = client.etagOf(DcatCollection.DATASETS, id).orElse(null);
Registration<Dataset> result = client.registerDataset(id, dataset, validator);
```

An empty `etagOf` makes that write unconditional, which is right in both empty cases
(the resource does not exist, or carries no validator), so the pattern needs no branch.

### Deleting

```java
List<String> rewritten = client.delete(DcatCollection.DATASETS, id, DeleteMode.CASCADE);
```

`SINGLE` refuses with `409` while anything still references the resource; `CASCADE`
unlinks the referrers first, as a single commit, and returns the public IRIs of what it
rewrote. Every one of those has a new ETag, so a caller holding any of them should
invalidate it. `deleteDistribution` needs no mode — a distribution is contained in its
dataset, so there is nothing to cascade.

## What it throws

Everything is a `DcatAtlasClientException` (unchecked). Catch that to treat any portal
failure alike; catch a subtype to act on one.

| Exception | Status | Means |
|---|---|---|
| `DcatShaclException` | 422 | Well-formed, but does not conform to the DCAT-AP.de profile. |
| `DcatModelConstraintException` | 422 | The model's own OCL constraints refused it; `getViolations()` lists them. |
| `BadRequestException` | 400 | Malformed request — including an `about` belonging to somebody else. |
| `NotFoundException` | 404 | The resource, or its parent, does not exist. |
| `ConflictException` | 409 | The portal's existing state refuses it: a repeat identity, or a reference that would dangle. |
| `PreconditionFailedException` | 412 | The `If-Match` no longer matches. A *conditional registration* never raises this — it comes back `applied() == false` instead; anything else that sends a validator does. |
| `RetryableException` | 503 | **Not a lost write.** The commit is durable; the portal's push to its git remote failed and will be retried. |
| `TransportException` | — / 415 | Timeout, refused connection, unreadable body — or `application/xml` sent where `application/xmi` was meant. |

The two most worth handling separately are `DcatShaclException` (the metadata needs
fixing) and `RetryableException` (nothing needs fixing).

`409` and `400` each cover more than one server-side cause with nothing on the wire to
separate them, so the message carries the portal's own text.

## Runtime requirements

The client reads and writes `application/xmi` — **never** `application/xml`, which is a
different codec and a `415`. That is a single constant in the client, so it cannot be
got wrong per call site.

In a plain classpath, register nothing: the codec registers every `EPackage` the
DCAT-AP.de model spans (`dcat`, `foaf`, `rdf`, `terms`, `adms`, `spdx`, `vcard`) on a
fresh `ResourceSet` per call. Entities you hand in are **copied** first, so a
registration never re-parents your object graph.

In OSGi, three things are load-bearing in a consumer's `bndrun`:

- **The Jakarta RS Whiteboard**, for its `ClientBuilder`. The front-end's `@Reference`
  to it is mandatory: with no Whiteboard there is nothing to build a client with, and
  failing to activate says so more clearly than a later `NullPointerException`. Using
  the Whiteboard's builder is the point — the runtime's own HTTP client and registered
  providers then apply.
- **SPI-Fly** (`org.apache.aries.spifly.dynamic.bundle`). `client.impl` declares its
  factory with bnd's `@ServiceProvider`, so `DcatAtlasClientFactory` reaches the
  registry through the SPI-Fly bridge rather than through SCR. It is an
  `effective:=resolve` extender requirement, so the resolver enforces it — SPI-Fly
  cannot be dropped from `-runbundles`.
- **Do not set `-resolve.effective: active`.** It makes the resolver consider
  `effective:=active` requirements, which includes the `osgi.service` one DS generates
  for the mandatory `ClientBuilder` reference. Nothing can satisfy that: the Whiteboard
  registers the `ClientBuilder` programmatically and declares no matching
  `@Capability` for it, so the resolve fails on a requirement that is satisfied
  perfectly well at runtime. `client.osgi.tests/test.bndrun` omits the instruction for
  this reason, and says so in a comment.

## Configuration reference

Factory PID `org.eclipse.fennec.dcat.atlas.client`. Metatype attribute names map onto
these dotted keys (`base_uri()` is `base.uri`), so the annotation and the property
cannot drift.

| Key | Type | Default | What it is |
|---|---|---|---|
| `dcat.portal` | String | *required* | The portal's name, published as a service property for `@Reference(target = "(dcat.portal=…)")`. |
| `base.uri` | String | *required* | The portal's REST base — the URL this runtime reaches it on, e.g. `http://dcat:8080/rest/`. |
| `public.base.uri` | String | `""` (means `base.uri`) | The base the portal serves its identities under, where that differs. What `aboutFor` computes from. |
| `connect.timeout.ms` | int | `5000` | Connect timeout. |
| `read.timeout.ms` | int | `30000` | Read timeout. |
| `check.ready` | boolean | `true` | Probe `/health/ready` at activation. |
| `require.ready` | boolean | `false` | A portal that is not ready prevents activation. |
| `auth.type` | `NONE` · `BEARER` · `API_KEY` · `MTLS` | `NONE` | See [Authentication](#authentication). |
| `auth.token.env` | String | `""` | Environment variable holding the bearer token. |
| `auth.apikey.header` | String | `X-API-Key` | Header name for `API_KEY`. |
| `auth.apikey.env` | String | `""` | Environment variable holding the API key. |
| `auth.keystore.path` | String | `""` | Client keystore; `MTLS` only. |
| `auth.keystore.password` | String (password) | `""` | `MTLS` only. |
| `auth.keystore.type` | String | `PKCS12` | |
| `auth.truststore.path` | String | `""` | `MTLS` only. |
| `auth.truststore.password` | String (password) | `""` | `MTLS` only. |
| `auth.truststore.type` | String | `PKCS12` | |

A blank string is how metatype spells "unset", and the client treats it as such.

**Not an OSGi key:** `read.accept` (default `application/xmi`) exists on
`ClientConfiguration` for a plain-Java caller that wants to pin content negotiation
rather than rely on the server's preference. Only XMI comes back as EMF objects, so the
typed read methods require it, and the OSGi front-end does not expose it.

## Further reading

- [User Guide](./opendata-portal-user-guide.md) — the portal, its REST surface, content
  negotiation, SPARQL and validation.
- [`GET /openapi.json`](./opendata-portal-user-guide.md#endpoint-reference) — the
  generated OpenAPI descriptor.
- [issue #27](https://github.com/eclipse-fennec/dcat.atlas/issues/27) — the client's
  design and the measurements behind the registration loop.
