# Contributing to Eclipse Fennec — DCAT.Atlas

Thank you for your interest in this project. Eclipse Fennec is an open-source
project hosted by the [Eclipse Foundation](https://www.eclipse.org) and
operated under the [Eclipse Development Process](https://www.eclipse.org/projects/dev_process/).
Contributions are welcome from the whole community.

* Project home: https://projects.eclipse.org/projects/technology.fennec
* This repository: https://github.com/eclipse-fennec/dcat.atlas
* Issue tracker: https://github.com/eclipse-fennec/dcat.atlas/issues
* Developer mailing list: https://accounts.eclipse.org/mailing-list/fennec-dev

DCAT.Atlas is an open-data portal backend: an OSGi/bnd workspace that exposes a
DCAT-AP.de catalog over a Jakarta RESTful admin and read API, with SHACL
validation backed by Apache Jena.

## Eclipse Development Process

All contributions are governed by the Eclipse Foundation Development Process.
The most important points for new contributors:

* **Eclipse Contributor Agreement (ECA).** Every contributor must have a
  signed ECA on file at the Eclipse Foundation before any contribution can be
  merged. Sign it once at https://www.eclipse.org/legal/eca.html — it covers
  all your future contributions to any Eclipse project.
* **Sign your commits (DCO).** Every commit must carry a `Signed-off-by:`
  trailer that matches the email on your Eclipse Foundation account. This is
  the project's *Developer Certificate of Origin* declaration; see
  ["Sign your work"](#sign-your-work) below.
* **License.** All contributions are licensed under the
  [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/).
* **Intellectual Property.** Third-party dependencies introduced by a
  contribution must clear Eclipse IP review. Run the
  [Eclipse Dash License Tool](https://github.com/eclipse-dash/dash-licenses)
  before opening a pull request that touches dependencies (see
  ["Adding dependencies"](#adding-dependencies)).

Background reading:

* [Eclipse Development Process](https://www.eclipse.org/projects/dev_process/)
* [Eclipse Foundation Contributor Guide](https://www.eclipse.org/projects/handbook/#contributing)
* [Eclipse Code of Conduct](https://www.eclipse.org/org/documents/Community_Code_of_Conduct.php)

## House conventions

The repository layout and the running record of development decisions live in
[`docs/development-guide.md`](docs/development-guide.md). The conventions that
matter most for contributors:

* **No hand edits in generated EMF sources.** The model code under
  `org.eclipse.fennec.dcat.atlas.dcatap.de.model/src/` and
  `org.eclipse.fennec.shacl.model/src/` is generated. Change the `.ecore` /
  `.genmodel` and regenerate — including license headers, which come from the
  genmodel's `copyrightText` property.
* **Tests with behaviour changes.** Any change to runtime behaviour ships with
  the test(s) that demonstrate the new contract.
* **Keep the bnd task graph honest.** A bundle that a `.bndrun` needs at *run*
  time but that is on no `-buildpath` must be declared with `-dependson`, or
  Gradle may resolve before it is built. Bndtools hides this — it always sees
  every workspace project — so it only shows up in CI.
* **No licensed shape files in the repository.** DCAT-AP.de SHACL shapes and
  controlled vocabularies are AGPL-3.0 and are loaded at runtime from
  operator-configured directories. See [NOTICE.md](NOTICE.md).
* **No fully qualified class names in code.** Use imports.

## Reporting issues

* Search the [issue tracker](https://github.com/eclipse-fennec/dcat.atlas/issues)
  first — your problem may already be reported.
* When filing a new issue, include the Java version, the affected bundle, the
  RDF serialization involved (`application/rdf+xml`, `text/turtle`,
  `application/ld+json`, …), and a minimal reproducer.
* Security issues must **not** be reported as public GitHub issues. Follow the
  Eclipse Foundation's coordinated-disclosure process at
  https://www.eclipse.org/security/ instead.

## Contributing code

We use a fork-and-pull-request workflow:

1. **Check the ECA.** Sign the Eclipse Contributor Agreement if you have not
   yet done so. The CI bot will block any PR without a signed ECA.
2. **Fork** this repository and create a topic branch off `snapshot`.
3. **Make focused commits.** Each commit should do one thing and keep the
   build green. Prefer several small, reviewable commits over a single large
   one. Use descriptive commit messages with a short subject line (≤ 72
   chars) and a body explaining *why* the change is needed.
4. **Add or update tests** for any behavior change.
5. **Run the build locally:**
   ```bash
   ./gradlew clean build testOSGi
   ```
6. **Push** to your fork and open a Pull Request against the `snapshot`
   branch. Link the PR to an existing issue when possible.
7. **Wait for CI.** All status checks must be green before review: the license
   header check and the Gradle build matrix on JDK 21 + 25, which also runs the
   OSGi integration tests.

### Sign your work

Every commit must include a `Signed-off-by:` line that matches the email
registered with your Eclipse Foundation account. This is the project's DCO
sign-off — it declares that you wrote the change or otherwise have the right
to contribute it under the project's license.

The easiest way is to commit with `-s`:

```bash
git commit -s -m "Reject Distribution without accessURL with 400"
```

This appends a trailer like:

```
Signed-off-by: Jane Developer <jane@example.org>
```

To sign off all commits in an existing branch, use `git rebase` with
`--signoff`:

```bash
git rebase --signoff snapshot
```

### License headers

Every new source file must start with an EPL-2.0 header. The license-header
workflow rejects PRs that introduce files without one.

```
/**
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
```

Only the four EPL lines are matched, so the copyright attribution above them
may differ between files — existing sources carry either the Eclipse Foundation
or the Data In Motion attribution, and both pass.

Excluded paths and supported file types are configured in
[`.licenserc.yaml`](.licenserc.yaml). The check is run locally with:

```bash
docker run --rm -v $(pwd):/github/workspace ghcr.io/apache/skywalking-eyes/license-eye header check
```

### Adding dependencies

Adding a new third-party library requires Eclipse IP clearance:

1. Run the Eclipse Dash License Tool over the project's dependencies:
   ```bash
   tools/dash-licenses.sh
   ```
2. Commit the regenerated [`DEPENDENCIES`](DEPENDENCIES) file at the repository
   root.
3. For dependencies that Dash marks as "restricted", file a Contribution
   Questionnaire (CQ) with the Eclipse IP team before merging the PR
   (`tools/dash-licenses.sh --review --project technology.fennec`).

## Coding style

* **Java:** 4-space indent (no tabs), opening braces on the same line, no
  wildcard imports.
* **API stability:** Public packages are tracked by BND baselining;
  bump `Bundle-Version` for any public-API change.
* **Javadoc:** Public API classes and methods require Javadoc.

### Semantic versioning

Bundle and package versions follow [OSGi semantic versioning](https://docs.osgi.org/whitepaper/semantic-versioning/):

* **MAJOR** — incompatible API changes (binary or source).
* **MINOR** — backwards-compatible API additions.
* **MICRO** — internal changes only.

BND baselining enforces this automatically during the build.

## Build prerequisites

* Java 21 (LTS). CI also runs Java 25.
* No separate Gradle install needed — the project ships the Gradle Wrapper.

Gradle compiles every bundle against Java 21 (`javac.source`/`javac.target` in
`cnf/ext/fennec.bnd`), so the bundles carry `Require-Capability: osgi.ee=JavaSE 21`.
In the IDE the same level has to come from JDT: every project keeps a
`.settings/org.eclipse.jdt.core.prefs` pinning compliance/source/target to 21. If one
is missing, Eclipse falls back to the workspace default — on a JDK 25 install that
stamps `osgi.ee=JavaSE 25` on that bundle and every Bndtools resolve that needs it
fails, while the Gradle build stays green.

```bash
./gradlew clean build testOSGi
```

## Project leads & committers

Current committers are listed on the
[Eclipse Fennec project page](https://projects.eclipse.org/projects/technology.fennec/who).
Becoming a committer follows the standard Eclipse process — sustained,
high-quality contributions over time, followed by a committer election.

## Contact

* Mailing list: [fennec-dev@eclipse.org](mailto:fennec-dev@eclipse.org)
  ([subscribe](https://accounts.eclipse.org/mailing-list/fennec-dev))
* Issues: https://github.com/eclipse-fennec/dcat.atlas/issues
