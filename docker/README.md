# Running DCAT.Atlas in a container

This directory holds the container packaging for the DCAT.Atlas runtime (F-23/F-24).

| Path | What it is |
|---|---|
| `dcatatlas/Dockerfile` | Two-stage image: a temurin stage that lays out the directory tree, a distroless stage that runs it |
| `dcatatlas/build.gradle` | `prepareDocker` — stages the exported runtime jar into `dcatatlas/content/` |
| `dockercompose/docker-compose.yml` | Single-container deployment, local bare repository — **the supported default** |
| `dockercompose/docker-compose.remote.yml` | Single-container deployment, local repository **mirrored** to a remote over SSH |

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

## Published images

CI builds and pushes the image; you do not have to build it yourself to run it. A verified
push to `snapshot` publishes the `snapshot` tag, a verified push to `main` the `latest` tag.
Both also push an immutable tag carrying the bundle version bnd stamped into the build
(`1.0.0.<yyyyMMddHHmm>-SNAPSHOT`), and both go to Docker Hub and GHCR:

```bash
docker pull docker.io/eclipsefennec/dcat.atlas:snapshot
docker pull ghcr.io/eclipse-fennec/dcat.atlas:snapshot
```

Images are `linux/amd64` and `linux/arm64/v8`. The workflows are
`.github/workflows/snapshot.yml` and `release.yml`, both calling the repo-local
`reusable-container.yml` — the same layout as event.atlas and model.atlas, except that the
runtime jar is exported in the container job itself (with the two Gradle commands above)
rather than downloaded from a Maven release build. The Docker Hub push needs the
`DOCKER_USERNAME` and `DOCKER_API_TOKEN` secrets, inherited by the reusable from the
calling workflow.

The compose files below name the locally built `eclipse-fennec/dcat.atlas:latest`; point
their `image:` at one of the tags above to run a published image instead.

## Run

```bash
docker run -d --name dcat-atlas -p 8080:8080 \
  -v dcat-store:/opt/dcat/store.git \
  -v /path/to/shapes:/opt/dcat/shapes:ro \
  -v /path/to/vocabularies:/opt/dcat/vocabularies:ro \
  eclipse-fennec/dcat.atlas:latest
```

or, from `dockercompose/`, `docker compose up` (set `DCAT_SHAPES_DIR`, `DCAT_VOCAB_DIR` and
`PUBLIC_BASE_URL` first). For a remote store see [Remote repositories](#remote-repositories)
below and use `docker compose -f docker-compose.remote.yml up`.

## Configuration

Every setting is env-driven, interpolated into the OSGi configuration by the Felix ConfigAdmin
interpolation plugin. The configuration itself lives in
`org.eclipse.fennec.dcat.atlas.config.docker/configs/config.json`.

| Variable | Default | Meaning |
|---|---|---|
| `HTTP_PORT` | `8080` | Port the runtime listens on inside the container |
| `CONTEXT_PATH` | `/` | Servlet context path prefix |
| `GIT_REPO` | `/opt/dcat/store.git` | The bare git repository holding the store. A path keeps it on disk; a URL switches to an in-memory mirror of that remote |
| `GIT_REMOTE` | *(empty)* | Remote URL an on-disk `GIT_REPO` is mirrored to. Commits stay durable locally **and** are pushed there. Refused if `GIT_REPO` is itself a URL |
| `GIT_BRANCH` | `main` | Branch the store commits onto |
| `GIT_PUSH_ON_COMMIT` | `true` | Push after every commit. A no-op for a local repository; **do not turn it off for a remote** — a remote is held in memory, so an unpushed commit is lost on restart. The container default is `true` although the component's own default is `false`, so the dangerous combination cannot arise by omission |
| `GIT_SSH_PRIVATE_KEY` | *(empty)* | **Path to** the private key file authenticating against an SSH remote. Empty means no key, and then the SSH stack is never built at all |
| `GIT_SSH_PRIVATE_KEY_PWD` | *(empty)* | Passphrase, if the key is encrypted |
| `GIT_SSH_KNOWN_HOSTS` | *(empty)* | **Path to** an OpenSSH `known_hosts` file verifying the git host. Empty falls back to `~/.ssh/known_hosts`, which does not exist in a distroless image |
| `GIT_USERNAME` / `GIT_PASSWORD` | *(empty)* | Credentials for an `https://` remote instead. Unused for SSH |
| `STORE_BASE_PATH` | `dcat` | Folder inside the repository the collections live under |
| `PUBLIC_BASE_URL` | **none — required** | Base that stored, host-free identities are rendered under at read time. Must be the absolute URL clients actually reach the portal on (behind a proxy, *not* the address the container sees). There is deliberately no default: it becomes the host in every rendered `about`, so a wrong one silently advertises somebody else's IRIs. Unset, the container fails to start. |
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

`/opt/dcat/store.git` is the only writable mount: a **bare** git repository, so commits go
straight into the object database and no working tree is ever checked out. The collection
folders under `dcat/` (`catalogs/`, `datasets/`, `data-services/`, `dataset-series/`) appear
as tree entries on first write; distributions have no folder, because `dcat:distribution` is
containment and they live in their Dataset's blob (FR-10).

The image **ships an initialised bare repository** at that path, owned by uid 65532. This is
not an optimisation: the runtime stage is distroless, so there is no shell and no `git` to run
`git init` in, and `GitServiceImpl` only ever *searches* for a repository, never creates one.
Pointed at a path that is not a repository it refuses to activate with a message naming the
path and the `git init --bare` command that would fix it (EU-11 in
[`../docs/emf-util-git-issues.md`](../docs/emf-util-git-issues.md)) — clear, but still a
container that does not come up.

Which mount type you use therefore matters:

- a **named volume** (`-v dcat-store:/opt/dcat/store.git`) is seeded by Docker from the image
  content the first time it is used, so the container is self-starting;
- a **bind mount** is never seeded and shadows the image's repository with the host
  directory. Initialise it yourself first:

  ```bash
  git init --bare -b main /srv/dcat-store.git
  sudo chown -R 65532:65532 /srv/dcat-store.git
  ```

To inspect what the container has written, from the host:

```bash
docker run --rm -v dcat-store:/repo alpine/git -C /repo log --stat main
```

**The SHACL shapes are not in the image.** They are AGPL-3.0 (see `NOTICE.md`) and the
operator mounts them read-only. Start the container without that mount and the readiness
check reports CRITICAL, `/health/ready` returns 503, and the container never becomes ready —
deliberately. A portal that silently validates nothing must not be routed traffic.

## Remote repositories

There are three modes, and `GIT_REMOTE` is what distinguishes the last two:

| `GIT_REPO` | `GIT_REMOTE` | repository | durability |
|---|---|---|---|
| a path (default) | empty | on the volume | durable when written; no remote |
| a path | a URL | on the volume | durable when written **and** pushed upstream |
| a URL | must be empty | in the JVM heap | durable only once pushed |

**Prefer the middle row** — `docker-compose.remote.yml` is a worked SSH example of it. It
gives up nothing: a commit is written into the volume's object database first and pushed
after, so a failed push leaves the commit durable locally and surfaces as a `503` on the
write path rather than as lost data. The remote is not a startup dependency either: an
unreachable remote at activation logs a warning, the container serves the volume as it is,
and `fetch()` retries. And nothing is in the heap, so [EU-7](../docs/emf-util-git-issues.md)
does not apply.

The third row — `GIT_REPO` pointing at a URL — is the in-memory mirror. It suits a
read-mostly consumer of a repository somebody else maintains, and it has real costs for a
writer: the whole store lives in the JVM heap and grows as commits accumulate (EU-7, still
open), an unpushed commit dies with the process, and the activation fetch is a hard
dependency, so an unreachable remote means no `GitService`, every store check CRITICAL and a
container that never becomes ready. The image sets `-XX:MaxRAMPercentage=75.0` so the heap
follows the container limit; give it a `mem_limit` you have measured.

Setting `GIT_REPO` to a URL *and* `GIT_REMOTE` is refused at activation, with a message
saying so, rather than resolved by guessing which one you meant.

The key and `known_hosts` variables are **paths to files**, not the key material or the host
keys. Mount them read-only as single files, readable by uid 65532:

```bash
DCAT_GIT_REMOTE=git@gitlab.example.de:opendata/dcat-store.git \
DCAT_SSH_KEY=$HOME/.ssh/dcat_store_ed25519 \
DCAT_KNOWN_HOSTS=$HOME/.ssh/known_hosts \
PUBLIC_BASE_URL=https://opendata.example.de/dcat/rest/ \
docker compose -f docker-compose.remote.yml up
```

The full SSH stack — Apache MINA sshd and BouncyCastle — is already in the image, so
configuring a remote needs no rebuild.

### Reading the readiness line in this mode

Readiness appends a divergence note when the branch and the remote's copy of it disagree:

```
; WARNING: the branch differs from the remote's copy (<id>): pushes are not landing, so the
  mirror is behind. The commits are durable in this repository and a later push completes them
```

The signal is trustworthy: a successful push advances the remote-tracking ref, so the note
means pushes are genuinely not landing rather than being an artefact of a stale ref. The
wording follows the mode — with a URL `GIT_REPO` it instead says the repository is held in
memory and an unpushed commit is lost on restart, which for that mode is the truth.

Measured for the on-disk-plus-mirror mode by stopping the remote: the write returned `503`,
the commit was on the volume regardless, it survived a container restart (activation logged
`Unable to fetch … serving /opt/dcat/store.git as it is on disk, fetch() retries` and served
normally), and the next successful write pushed both the backlog and itself, after which the
warning cleared.

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
