# Running DCAT.Atlas in a container

This directory holds the container packaging for the DCAT.Atlas runtime (F-23/F-24).

| Path | What it is |
|---|---|
| `dcatatlas/Dockerfile` | Two-stage image: a temurin stage that lays out the directory tree, a distroless stage that runs it |
| `dcatatlas/build.gradle` | `prepareDocker` — stages the exported runtime jar into `dcatatlas/content/` |
| `dockercompose/docker-compose.yml` | Single-container deployment |

## Build

Export and stage are **two separate commands**. They cannot be wired together: the bnd
workspace *settings* plugin only includes the bnd projects it believes the requested task
paths need, so invoking `:docker:dcatatlas:prepareDocker` on its own yields a project graph
of just `[cnf, docker]` — any `dependsOn` on the runtime project fails at configuration time
with `Project with path ... could not be found`.

```bash
./gradlew :org.eclipse.fennec.dcat.atlas.runtime:export.docker
./gradlew :docker:dcatatlas:prepareDocker
docker build -t eclipse-fennec/dcat.atlas:latest docker/dcatatlas
```

`export.docker` does not track the resolved run bundles as task inputs, so it will report
`UP-TO-DATE` after you change a bundle or a `.bndrun`. When in doubt:

```bash
rm -rf org.eclipse.fennec.dcat.atlas.runtime/generated/distributions
./gradlew :org.eclipse.fennec.dcat.atlas.runtime:export.docker --rerun-tasks
```

## Run

```bash
docker run -d --name dcat-atlas -p 8080:8080 \
  -v dcat-data:/opt/dcat/data \
  -v /path/to/shapes:/opt/dcat/shapes:ro \
  -v /path/to/vocabularies:/opt/dcat/vocabularies:ro \
  eclipse-fennec/dcat.atlas:latest
```

or, from `dockercompose/`, `docker compose up` (set `DCAT_SHAPES_DIR` and `DCAT_VOCAB_DIR`
first).

## Configuration

Every setting is env-driven, interpolated into the OSGi configuration by the Felix ConfigAdmin
interpolation plugin. The configuration itself lives in
`org.eclipse.fennec.dcat.atlas.config.docker/configs/config.json`.

| Variable | Default | Meaning |
|---|---|---|
| `HTTP_PORT` | `8080` | Port the runtime listens on inside the container |
| `CONTEXT_PATH` | `/` | Servlet context path prefix |
| `STORE_FOLDER` | `/opt/dcat/data` | Parent of the per-entity store directories |
| `SHACL_SHAPES_DIR` | `/opt/dcat/shapes` | Directory of `*.ttl` SHACL shapes |
| `SHACL_VOCAB_DIR` | `/opt/dcat/vocabularies` | Directory of controlled-vocabulary files |
| `SHACL_ENFORCE` | `true` | Reject writes that do not conform |

## Endpoints

With the defaults above:

| Endpoint | Purpose |
|---|---|
| `GET /health/live` | Liveness — 200 while the process is up. Never depends on the store or the shapes; a restart does not mount a volume. |
| `GET /health/ready` | Readiness — 200 when the stores are writable and shapes are loaded, else 503. |
| `/rest/...` | The read API (`/rest/catalogs`, `/rest/datasets`, ...) |
| `/rest/admin/...` | The write API |

## Volumes

`/opt/dcat/data` is the only writable mount. The per-entity subdirectories (`catalogs/`,
`datasets/`, `data-services/`, `dataset-series/`, `distributions/`) are created lazily on
first write. The image pre-creates `/opt/dcat/data` owned by uid 65532, so a fresh named
volume inherits that ownership and the distroless `nonroot` user can write to it.

**The SHACL shapes are not in the image.** They are AGPL-3.0 (see `NOTICE.md`) and the
operator mounts them read-only. Start the container without that mount and the readiness
check reports CRITICAL, `/health/ready` returns 503, and the container never becomes ready —
deliberately. A portal that silently validates nothing must not be routed traffic.

## Probes

The image is distroless: no shell, no `curl`, no `wget`. An in-container `HEALTHCHECK` has
nothing to run, which is why the compose file does not define one. Probe over HTTP from
outside instead. Under Kubernetes these map to `httpGet` probes, which need no in-image
binary:

```yaml
livenessProbe:  { httpGet: { path: /health/live,  port: 8080 } }
readinessProbe: { httpGet: { path: /health/ready, port: 8080 } }
```

## A note on the HTTP service

`base.bndrun` sets the framework property `org.osgi.service.http.port=-1`. Do not remove it.

Felix's `JettyActivator` starts a *default* Jetty instance from framework properties in
addition to any `org.apache.felix.http~*` factory-configured instance. With the container's
port at the default 8080 the two collide, the default instance wins the bind, and the
Jakarta-RS whiteboard — which targets `(id=dcatHttp)` — ends up on a runtime nothing can
reach. The symptom is health endpoints answering 200 while every `/rest/...` path returns
404. Local development never hit this only because it uses port 8085.

`-1` makes the default instance bind no port. The `dcatHttp` factory configuration sets its
own `org.osgi.service.http.port` and is unaffected. This mirrors
`modelatlas.runtime_base.bndrun` in `model.atlas`.
