# Container image for the DCAT.Atlas runtime (F-23).
#
# Two stages: a temurin stage that lays out the directory tree with the right
# ownership, and a distroless runtime stage that carries only the JRE and the
# application. Distroless has no shell and no package manager, which is why all
# directory creation happens in the builder.
#
# Build context is this directory; `gradlew :docker:dcatatlas:prepareDocker`
# stages content/ first.

ARG APP_HOME=/opt/dcat
ARG STORE_REPO=${APP_HOME}/store.git
ARG SHAPES_DIR=${APP_HOME}/shapes
ARG VOCAB_DIR=${APP_HOME}/vocabularies
ARG TMPDIR=/tmp/dcat
ARG RUNTIME_JAR=${APP_HOME}/dcat.atlas.jar
ARG STORE_BRANCH=main

FROM eclipse-temurin:21-jre-alpine AS builder

ARG APP_HOME
ARG STORE_REPO
ARG SHAPES_DIR
ARG VOCAB_DIR
ARG TMPDIR
ARG RUNTIME_JAR
ARG STORE_BRANCH

# 65532 is the uid of the distroless "nonroot" user. The shapes and vocabulary
# directories are mount points only — the image never contains shapes (they are
# AGPL-3.0, see NOTICE.md).
#
# The store is a *bare* git repository (persistence plan Phase 2), and it is
# created here rather than left to the operator: the runtime stage is distroless,
# so there is no shell and no git binary to run `git init` in. GitServiceImpl
# only ever *searches* for a repository, never creates one — pointed at a path
# that is not one it now refuses to activate and says so clearly (EU-11 in
# docs/emf-util-git-issues.md), but a clear refusal is still a container that
# does not start.
#
# Shipping the initialised repository in the image also makes the container
# self-starting: Docker seeds a *new named volume* from the image content at its
# mount point, so `docker compose up` gets a working store on first run. A bind
# mount is NOT seeded — for those the operator still has to run
# `git init --bare -b main` on the host and chown it to 65532.
RUN set -eux; \
    apk add --no-cache git; \
    mkdir -p "${APP_HOME}" "${SHAPES_DIR}" "${VOCAB_DIR}" "${TMPDIR}"; \
    git init --bare -b "${STORE_BRANCH}" "${STORE_REPO}"; \
    chown -R 65532:65532 "${APP_HOME}" "${TMPDIR}"; \
    chmod -R u+rwX,go-rwx "${STORE_REPO}" "${TMPDIR}"

COPY content/docker.jar "${RUNTIME_JAR}"

RUN chown -R 65532:65532 "${APP_HOME}"

FROM gcr.io/distroless/java21-debian12:nonroot

ARG APP_HOME
ARG STORE_REPO
ARG SHAPES_DIR
ARG VOCAB_DIR
ARG TMPDIR
ARG RUNTIME_JAR
ARG STORE_BRANCH

ENV APP_HOME=${APP_HOME} \
    GIT_REPO=${STORE_REPO} \
    GIT_BRANCH=${STORE_BRANCH} \
    SHACL_SHAPES_DIR=${SHAPES_DIR} \
    SHACL_VOCAB_DIR=${VOCAB_DIR} \
    TMPDIR=${TMPDIR} \
    RUNTIME_JAR=${RUNTIME_JAR}

COPY --from=builder --chown=65532:65532 ${APP_HOME} ${APP_HOME}
COPY --from=builder --chown=65532:65532 ${TMPDIR} ${TMPDIR}

EXPOSE 8080
WORKDIR ${APP_HOME}

# gosh.args: the Gogo shell is part of the runtime and would otherwise try to
# read from a stdin that a container does not have.
# MaxRAMPercentage rather than a fixed -Xmx, so the heap follows the container limit
# instead of the JVM's default share of it. It matters most for a *remote* GIT_REPO: that
# mode holds the whole repository in memory and grows as commits accumulate (EU-7 in
# docs/emf-util-git-issues.md), so the container needs a memory limit and the JVM needs to
# know about it. A local bare repository is unaffected — it keeps its objects on disk.
ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Dgosh.args=--nointeractive", \
    "-Djava.io.tmpdir=/tmp/dcat", \
    "-jar", "/opt/dcat/dcat.atlas.jar"]
