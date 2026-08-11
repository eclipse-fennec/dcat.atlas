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
ARG DATA_DIR=${APP_HOME}/data
ARG SHAPES_DIR=${APP_HOME}/shapes
ARG VOCAB_DIR=${APP_HOME}/vocabularies
ARG TMPDIR=/tmp/dcat
ARG RUNTIME_JAR=${APP_HOME}/dcat.atlas.jar

FROM eclipse-temurin:21-jre-alpine AS builder

ARG APP_HOME
ARG DATA_DIR
ARG SHAPES_DIR
ARG VOCAB_DIR
ARG TMPDIR
ARG RUNTIME_JAR

# 65532 is the uid of the distroless "nonroot" user. The store directory must be
# writable: the per-entity subdirectories (catalogs/, datasets/, ...) are created
# lazily on first write. The shapes and vocabulary directories are mount points
# only — the image never contains shapes (they are AGPL-3.0, see NOTICE.md).
RUN set -eux; \
    mkdir -p "${APP_HOME}" "${DATA_DIR}" "${SHAPES_DIR}" "${VOCAB_DIR}" "${TMPDIR}"; \
    chown -R 65532:65532 "${APP_HOME}" "${TMPDIR}"; \
    chmod -R u+rwX,go-rwx "${DATA_DIR}" "${TMPDIR}"

COPY content/docker.jar "${RUNTIME_JAR}"

RUN chown -R 65532:65532 "${APP_HOME}"

FROM gcr.io/distroless/java21-debian12:nonroot

ARG APP_HOME
ARG DATA_DIR
ARG SHAPES_DIR
ARG VOCAB_DIR
ARG TMPDIR
ARG RUNTIME_JAR

ENV APP_HOME=${APP_HOME} \
    STORE_FOLDER=${DATA_DIR} \
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
ENTRYPOINT ["java", \
    "-Dgosh.args=--nointeractive", \
    "-Djava.io.tmpdir=/tmp/dcat", \
    "-jar", "/opt/dcat/dcat.atlas.jar"]
