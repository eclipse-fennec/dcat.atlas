# Issue sketches for `eclipse-fennec/emf.util`

Thirteen issues found in `org.eclipse.fennec.jgit` while designing the DCAT.Atlas git-backed
store ([`persistence-phase2-git-store.md`](persistence-phase2-git-store.md)). They are
written to be filed as-is upstream; the DCAT-side references are there so whoever picks one
up can see what it is for, not because the fix should be DCAT-specific.

**The `EU-N` numbers are local to this document** — they are not used in the upstream
tracker. Every one is filed as an issue in
[eclipse-fennec/emf.util](https://github.com/eclipse-fennec/emf.util/issues), linked in the
table below and from each section heading. **The GitHub issue is authoritative for status**;
what is written here is what was true when it was verified from DCAT.Atlas.

All line numbers are against `/opt/git/fennec-emf.util` as of 2026-08-19; EU-11's are
against the same tree, unchanged, on 2026-08-20, as are EU-12's and EU-13's.

**Status 2026-08-20.** EU-1 … EU-6, EU-9 and EU-10 were fixed on 2026-08-19 (upstream
[#51](https://github.com/eclipse-fennec/emf.util/pull/51)); EU-9 and EU-10 were found after
the first six, while putting the store on top of the new build and then testing it against a
running portal. EU-11 was found on 2026-08-20 when the local runtime met a machine whose
`/tmp` had been cleared, and fixed the same day
([#57](https://github.com/eclipse-fennec/emf.util/pull/57)). EU-12 and EU-13 were found later
that day while packaging the container for a remote repository — they were the two things
that made a remote store awkward to deploy — and are **implemented in
[#60](https://github.com/eclipse-fennec/emf.util/pull/60), which is open at the time of
writing**: verified from DCAT.Atlas against a live remote, but their issues are not closed
yet. EU-7 and EU-8 remain open by choice.

| # | Upstream | Title | Kind | Status |
|---|---|---|---|---|
| EU-1 | [#41](https://github.com/eclipse-fennec/emf.util/issues/41) | Reads fail on a repository with no commits | Bug | ✅ Fixed |
| EU-2 | [#42](https://github.com/eclipse-fennec/emf.util/issues/42) | A blank `privateKey` is treated as configured | Bug | ✅ Fixed |
| EU-3 | [#43](https://github.com/eclipse-fennec/emf.util/issues/43) | `GitService` exposes no blob id | Enhancement | ✅ Fixed |
| EU-4 | [#44](https://github.com/eclipse-fennec/emf.util/issues/44) | `GitService` has no `exists(path)` | Enhancement | ✅ Fixed |
| EU-5 | [#45](https://github.com/eclipse-fennec/emf.util/issues/45) | `commit()` writes an empty commit when nothing changed | Bug | ✅ Fixed |
| EU-6 | [#46](https://github.com/eclipse-fennec/emf.util/issues/46) | The fetch refspec force-updates, so "fetch and retry" loses commits | Bug | ✅ Fixed |
| EU-7 | [#47](https://github.com/eclipse-fennec/emf.util/issues/47) | An in-memory repository grows without bound in a long-running writer | Design | Open — no longer blocking: EU-12 lets a writer stay on disk *and* mirror |
| EU-8 | [#48](https://github.com/eclipse-fennec/emf.util/issues/48) | No EMF write path over git anywhere in the ecosystem | Enhancement | Open by choice — built in DCAT.Atlas first (W2) |
| EU-9 | [#49](https://github.com/eclipse-fennec/emf.util/issues/49) | The SSH stack is required to load the service, even for a local repository | Bug | ✅ Fixed |
| EU-10 | [#50](https://github.com/eclipse-fennec/emf.util/issues/50) | `GitWriteException` conflates a failed commit with a failed push | Enhancement | ✅ Fixed |
| EU-11 | [#55](https://github.com/eclipse-fennec/emf.util/issues/55) | A missing local repository fails with a message that names neither the path nor the cause | Bug | ✅ Fixed |
| EU-12 | [#58](https://github.com/eclipse-fennec/emf.util/issues/58) | No durable-local-plus-mirror mode: a remote store is heap-only | Enhancement | Implemented in [#60](https://github.com/eclipse-fennec/emf.util/pull/60), issue still open |
| EU-13 | [#59](https://github.com/eclipse-fennec/emf.util/issues/59) | A successful push does not update the remote-tracking ref, so "diverged from the remote" false-fires | Bug | Implemented in [#60](https://github.com/eclipse-fennec/emf.util/pull/60), issue still open |

The descriptions below are kept as filed, with a note on each resolved one saying how it was
fixed. All of them are exercised from DCAT.Atlas by the jgit bundle in `cnf/local`.

---

## EU-1 · Reads fail on a repository with no commits

**Upstream:** [eclipse-fennec/emf.util#41](https://github.com/eclipse-fennec/emf.util/issues/41)

**Kind:** bug · **Severity:** high — this is the normal first-boot case

`GitServiceImpl.getFiles(String)` (line 266) and `loadFile(String, String, OutputStream)`
(line 315) both do:

```java
lastCommitId = repo.resolve("refs/heads/" + branch);
...
RevCommit commit = revWalk.parseCommit(lastCommitId);
```

`Repository.resolve` returns `null` when the branch has no commits, and
`RevWalk.parseCommit(null)` throws `NullPointerException`. The surrounding
`catch (Exception e)` wraps it as `RuntimeException("Unable to list files for basepath null")`,
which does not describe what happened.

`GitCommitWriter.commit` handles the same state correctly — `resolveHead` returning `null`
produces a parentless first commit — so only the read side is missing it.

**Why it matters.** A freshly created repository, local or remote, has no commits on the
branch. Any consumer that reads before it writes (checking whether its data is already
there, populating a cache, answering a health check) fails on first boot with a misleading
error.

**Suggested fix.** Treat an unresolvable branch as an empty tree, not an error:
`getFiles` returns a `TreeResult` with a `null` commit id and an empty list; `loadFile` and
`readFile` throw `GitFileNotFoundException`, which is what "the file is not in that commit"
already means. Note `TreeResult.getCommitId()` becoming nullable is API-visible and should be
in its javadoc.

**Verification.** A test that activates the service against an `init --bare` repository with
no commits and calls `getFiles()`, `readLatestFile("anything")`, then `commit(...)` and
`getFiles()` again.

---

## EU-2 · A blank `privateKey` is treated as configured

**Upstream:** [eclipse-fennec/emf.util#42](https://github.com/eclipse-fennec/emf.util/issues/42)

**Kind:** bug · **Severity:** high — the bundle's own shipped configuration triggers it

`GitServiceImpl.activate` line 132:

```java
if (config.privateKey() != null) {
    sshSessionFactory = createSshSessionFactory();
}
```

`createSshSessionFactory` then does `new File(config.privateKey())` and hands
`keyFile.toPath()` to `setDefaultIdentities`. With `privateKey` set to the empty string that
is `new File("")` — an empty identity path, offered to every `SshTransport`.

The empty string is not hypothetical: `org.eclipse.fennec.jgit.config/configs/config.json`
ships `"privateKey": "$[env:SSH_PRIVATE_KEY;default=]"`, so any deployment that does not set
the variable gets exactly this.

`GitConfig.privateKey()` also has no `default` and no `@AttributeDefinition(required = false)`,
unlike `password()`, so metatype treats it as required — a local repository or an https
remote has to supply a value it does not use.

**Suggested fix.** Test for blank, not `null`, and give the attribute
`default ""` plus `required = false`. Same treatment for `privateKeyPassphrase()`, whose
`ConfiguredKeyPasswordProvider` already tolerates `null` but would return an empty
passphrase array for a key that has one.

**Verification.** Activate against an https remote with `username`/`password` and
`privateKey` unset; assert no `SshdSessionFactory` is built.

---

## EU-3 · `GitService` exposes no blob id

**Upstream:** [eclipse-fennec/emf.util#43](https://github.com/eclipse-fennec/emf.util/issues/43)

**Kind:** enhancement

`TreeResult` (`api/TreeResult.java`) carries a commit id and a list of paths. There is no way
to obtain the object id of a single file's blob, although `GitServiceImpl.loadFile` already
has it in hand at line 329 (`treeWalk.getObjectId(0)`) and `getFiles`' `TreeWalk` could
produce it for every entry at no extra cost.

**Why it matters.** A blob id is a content hash of exactly one file, computed by git anyway.
It is the natural value for an HTTP `ETag`, for change detection between two commits, and for
deduplication. Without it, a consumer that needs a per-file content hash must read the whole
blob back and hash it — which is what DCAT.Atlas will do as a fallback, and what
model.atlas's `GitStorageHelper` already does (SHA-256 over the blob bytes).

A commit id is *not* a substitute: it changes for every file whenever any file changes, so
using it as an ETag invalidates every cached representation on every unrelated write.

**Suggested fix.** Either `String blobId(String commitId, String path)` on `GitService`, or —
probably better, since callers usually want it while listing — carry
`Map<String, String> pathToBlobId` (or a small `FileEntry` record of path + blob id) on
`TreeResult`. The second avoids a second tree walk per file.

**Verification.** Assert the returned id equals `git hash-object` for the same content, and
that it is unchanged by a commit touching a different path.

---

## EU-4 · `GitService` has no `exists(path)`

**Upstream:** [eclipse-fennec/emf.util#44](https://github.com/eclipse-fennec/emf.util/issues/44)

**Kind:** enhancement

Answering "is there a file at this path" requires either `readFile` and catching
`GitFileNotFoundException` — exception as control flow, and it reads the whole blob — or
`getFiles(prefix)` and scanning the returned list.

**Suggested fix.** `boolean exists(String commitId, String path)` doing a `TreeWalk` with a
`PathFilter` and no `ObjectLoader.copyTo`. The loop in `loadFile` (lines 320–334) is already
exactly this minus the read.

**Why it matters.** Any store built on `GitService` needs it on the hot path: DCAT.Atlas
checks existence before every read, every delete, and on every conditional request. It also
feeds an EMF `URIHandler.exists(URI, Map)`, which EMF calls during resource resolution.

---

## EU-5 · `commit()` writes an empty commit when nothing changed

**Upstream:** [eclipse-fennec/emf.util#45](https://github.com/eclipse-fennec/emf.util/issues/45)

**Kind:** bug · **Severity:** low, but it pollutes the history

`GitCommitWriter.applyChanges` applies `DeletePath` for a `DELETE` change. Deleting a path
that is not in the index is a no-op — which is the documented behaviour of
`CommitRequest.Builder.delete` ("a path that does not exist is silently ignored") — but the
commit is still built and inserted, with a tree identical to its parent's and a fresh
timestamp, so it gets a new object id and `updateRef` (line 93) reports `FAST_FORWARD`
rather than `NO_CHANGE`.

The same happens for a `PUT` whose content equals what is already stored.

**Why it matters.** For a consumer using commits as an audit trail, every idempotent no-op
write becomes an indistinguishable entry. It also defeats change detection based on comparing
tip commits: the tip moves although nothing changed.

**Suggested fix.** After `index.writeTree(inserter)`, compare the new tree id with the
parent's. If they are equal, skip the commit and return the parent's id. This is what
`git commit` does without `--allow-empty`. If some consumer genuinely wants empty commits,
`CommitRequest` can carry an `allowEmpty` flag, but the default should be to skip.

**Note:** the return value of `commit()` is documented as "the id of the new commit";
returning the unchanged head instead needs a javadoc update.

---

## EU-6 · The fetch refspec force-updates, so "fetch and retry" loses commits

**Upstream:** [eclipse-fennec/emf.util#46](https://github.com/eclipse-fennec/emf.util/issues/46)

**Kind:** bug · **Severity:** medium — the advertised recovery path is unsafe

`GitServiceImpl.activate` line 127 configures the fetch as:

```java
fetchCmd.setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"));
```

The leading `+` forces the update. So `fetch()` overwrites local branch refs with the
remote's, discarding any local commit that has not been pushed.

Both places that raise `GitConflictException` advise exactly that recovery:

- `GitCommitWriter.updateRef`: *"moved while the commit was being built (…), fetch and retry"*
- `GitServiceImpl.checkPushed`: *"rejected … as non-fast-forward, fetch and retry"*

Following that advice after a rejected push destroys the commit the caller was trying to
save, silently — the data is gone, and `fetch()` reports success.

**Why it matters.** This is the documented path out of the one error the write API is
designed to surface. A consumer that implements the advice as written loses writes.

**Suggested fix.** At minimum, correct the messages so they do not advise an operation that
discards local work. Better: give `fetch()` non-forced semantics for the configured branch
(or a separate `fetchRemote()` that updates remote-tracking refs only, leaving the local
branch alone), so a caller can inspect and rebase. A genuine retry needs the caller to
re-derive its changes on top of the new head, which is a decision only the caller can make —
so the API should make that possible rather than make the destructive path the easy one.

---

## EU-7 · An in-memory repository grows without bound in a long-running writer

**Upstream:** [eclipse-fennec/emf.util#47](https://github.com/eclipse-fennec/emf.util/issues/47)

**Kind:** design · **Severity:** medium for write workloads

For a remote, `activate` builds an `InMemoryRepository` and populates it by fetch. Every
commit written afterwards inserts objects into that in-heap object database. Nothing ever
prunes it, and there is no equivalent of `git gc`.

For the read-only use this bundle was first built for, that is fine: the heap holds one fetch
of a repository someone else maintains. For a writer committing on every mutation over months,
the process's heap holds the entire history it has written since the last restart.

**Why it matters.** DCAT.Atlas Phase 2 commits once per API operation, against a remote in
the deployment shape Phase 3 wants. That is an unbounded heap growth curve with a restart as
the only remedy.

**Suggested fix.** No single obvious one; this is a request for a documented answer more than
a patch. Options: document the limitation and recommend a local on-disk repository for
write-heavy consumers (what DCAT.Atlas will default to); offer a periodic
re-fetch-and-discard that rebuilds the in-memory repository from the remote after a
successful push; or support a `DfsRepository` backed by disk rather than heap.

**Update 2026-08-20:** EU-12 removes the urgency without addressing the growth itself. The
rationale above rests on a writer needing the in-memory mode to reach a remote at all, and
that is no longer true — an on-disk `repo()` with a `remote()` mirrors upstream with its
objects on disk. What remains is the narrower question this issue is really about: a
long-lived process holding an in-memory mirror still has no way to reclaim. That is a
read-mostly concern now, not the write path.

---

## EU-8 · No EMF write path over git anywhere in the ecosystem

**Upstream:** [eclipse-fennec/emf.util#48](https://github.com/eclipse-fennec/emf.util/issues/48)

**Kind:** enhancement · **Severity:** low — a gap, not a defect

Surveyed 2026-08-19 across `model.atlas`, `jena-MDO`, `org.gecko.jgit`,
`org.gecko.emf.persistence`, `geckoprojects-emf-persistence` and `avatar-modelrepo`:

- A read-only git→EMF backend exists twice and is production-shaped —
  `model.atlas/…management.git/GitURIHandler` (which already binds *this* bundle's
  `GitService`) and its ancestor `jena-MDO/de.jena.mdo.git.epackage.registry/GitURIHandler`.
  Both map `git://{commitId}/{path}` and install via
  `resourceSet.getURIConverter().getURIHandlers().add(0, handler)`.
- **Every one of them refuses to write.** model.atlas's `createOutputStream` throws
  `UnsupportedOperationException` ("Git storage is read-only; writes happen externally on the
  git host"); jena-MDO's does not override it at all.
- The write primitives — `CommitRequest` batching, `GitCommitWriter`, conflict detection,
  push — exist in this bundle and **are used by no EMF code anywhere**.

The shape a write handler needs is already established by the non-git backends:
`org.gecko.emf.persistence.mongo/.../streams/*OutputStream` and its JDBC and JPA siblings
buffer bytes and persist on `flush()`/`close()`.

**Why it matters.** DCAT.Atlas is about to write the first one. If it lives in
`org.eclipse.fennec.dcat.atlas.impl` it is private to one consumer; the next project that
wants an EMF model repository backed by git rewrites it.

**Suggested fix.** Not now — build it in DCAT.Atlas first, where it will be exercised against
a real workload, then propose extracting it as `org.eclipse.fennec.jgit.emf` once the shape
has settled. Two things to get right in advance so extraction stays possible:

1. **Do not put the commit id in the URI.** `git://{commitId}/{path}` freezes a commit into
   every stored cross-resource `href`, and re-freezes it on every write. A writable handler
   has to claim a stable logical base and resolve at branch tip. (This is why DCAT.Atlas
   diverges from model.atlas's URI scheme.)
2. **Commit granularity belongs to the caller, not the stream.** One `close()` must not mean
   one commit, or a multi-resource operation cannot be atomic. The handler buffers into a
   `CommitRequest.Builder` owned by a caller-scoped session.

Filed now so the design constraint is on record before anyone builds on the read-only
handlers.

---

## EU-9 · The SSH stack is required to load the service, even for a local repository

**Upstream:** [eclipse-fennec/emf.util#49](https://github.com/eclipse-fennec/emf.util/issues/49)

**Kind:** bug · **Severity:** medium — it makes an unrelated dependency mandatory ·
**✅ Fixed upstream 2026-08-19:** the field is now typed
`org.eclipse.jgit.transport.SshSessionFactory` (jgit core) with the sshd types confined to
`SshdSessionFactoryProvider`. Verified from dcat.atlas by removing
`org.eclipse.jgit.ssh.apache` from the unit `-testpath`: 183 tests still green.

`GitServiceImpl` declares a field of type
`org.eclipse.jgit.transport.sshd.SshdSessionFactory`, from `org.eclipse.jgit.ssh.apache`.
A field type has to resolve when the class is loaded, so the class cannot load at all
without that bundle — regardless of whether the configured repository is a remote over SSH,
a remote over https, or a bare repository on disk that never opens a connection.

Found in DCAT.Atlas: the entire unit suite failed with

```
NoClassDefFoundError: org/eclipse/jgit/transport/sshd/SshdSessionFactory
```

against a **local bare repository**, and passed as soon as `org.eclipse.jgit.ssh.apache` was
added to the test path. The activation logic is already careful to build the factory only
when a key is configured (`createSshSessionFactory` is called under a guard) — the guard
just cannot help, because the field is enough.

**Why it matters.** Every consumer must ship the Apache MINA sshd stack and BouncyCastle to
use a local repository, which is a substantial and entirely unused dependency for the
simplest deployment — and, in OSGi, an unnecessary constraint on the resolve rather than a
missing class at runtime.

**Suggested fix.** Keep the field's declared type to something already on the mandatory path
— hold the factory as `org.eclipse.jgit.transport.SshSessionFactory` (jgit core) or simply
`AutoCloseable`, and confine the `sshd`-specific types to the body of
`createSshSessionFactory`, which is only reached when a key is configured. A local method
body is not resolved until the method runs, so the dependency becomes genuinely optional. In
the bundle manifest, `org.eclipse.jgit.transport.sshd` can then be an optional import.

**Verification.** Load `GitServiceImpl` and activate it against a local bare repository with
`org.eclipse.jgit.ssh.apache` absent from the classpath; it should work.

---

## EU-10 · `GitWriteException` conflates a failed commit with a failed push

**Upstream:** [eclipse-fennec/emf.util#50](https://github.com/eclipse-fennec/emf.util/issues/50)

**Kind:** enhancement · **Severity:** low — callers cannot report the difference ·
**✅ Fixed upstream 2026-08-19** with `GitPushException extends GitWriteException`, exactly as
suggested below. DCAT.Atlas now renders 409 / 503 / 500 for conflict / push / commit failure.

`GitService.commit(CommitRequest)` does two things when `pushOnCommit` is set: it writes the
commit into the object database, and it pushes the branch. Both failures surface as
`GitWriteException`, so a caller that catches it cannot tell whether

- **nothing was written** — the object database write failed, the branch did not move; or
- **the change is recorded locally and only the copy to the remote failed** — the commit is
  durable on disk (or, for a remote, durable until the process restarts) and a later
  `push()` would complete it.

**Why it matters.** Those need different answers. DCAT.Atlas renders write failures as HTTP:
the first is a `500` (the store broke, nothing happened), the second a `503` (retry, and the
retry is cheap because the work is already committed). Unable to distinguish them, it reports
`503` for both — the safe direction, since it invites the retry that gets a local commit to
the remote, where `500` would suggest the request itself was at fault. The cost is a
misleading status code for a genuine object-database failure.

`GitConflictException` already carves the one case where the *branch moved* out of
`GitWriteException`, so the precedent for splitting by cause is there.

**Suggested fix.** A `GitPushException extends GitWriteException`, thrown by `pushInternal`
and by `checkPushed`'s non-conflict branch. Subclassing keeps existing catch blocks working
while letting callers that care be specific. `GitConflictException` on a rejected push
already communicates its own case and can stay as it is.

**Verification.** Commit successfully to a repository whose remote is unreachable with
`pushOnCommit=true`; assert a `GitPushException`, and that the commit is present locally
afterwards.

---

## EU-11 · A missing local repository fails with a message that names neither the path nor the cause

**Upstream:** [eclipse-fennec/emf.util#55](https://github.com/eclipse-fennec/emf.util/issues/55)

**Kind:** bug · **Severity:** medium — it is the first-boot case for a local repository, and
the neighbouring variant of it is silent ·
**✅ Fixed upstream 2026-08-20** ([#57](https://github.com/eclipse-fennec/emf.util/pull/57)),
both halves, as suggested below. `openOnDisk` bounds the search with
`addCeilingDirectory(parent)` — so the configured directory and a `.git` inside it are
considered and no ancestor ever is — and an unresolved `gitDir` now throws
`describeMissingRepository`, which names the path, says which of the three things was wrong
with it (absent, a file, or a directory that is not a repository) and gives the
`git init --bare` command.

The local branch of `GitServiceImpl.activate` (lines 142–146) resolves the configured
repository by searching for one:

```java
FileRepositoryBuilder builder = new FileRepositoryBuilder();
File gitDir = new File(config.repo());
repo = builder.setInitialBranch(config.branch()) // set branch
        .findGitDir(gitDir) // scan up the file system tree
        .build();
```

`findGitDir` searches; it never creates. When nothing from `config.repo()` up to the file
system root is a git repository, it leaves `gitDir` unset and `build()` throws jgit's
contract error:

```
java.lang.IllegalArgumentException: One of setGitDir or setWorkTree must be called.
	at org.eclipse.jgit.lib.BaseRepositoryBuilder.requireGitDirOrWorkTree(BaseRepositoryBuilder.java:690)
	at org.eclipse.jgit.storage.file.FileRepositoryBuilder.build(FileRepositoryBuilder.java:55)
	at org.eclipse.fennec.jgit.GitServiceImpl.activate(GitServiceImpl.java:146)
```

The message describes an internal API contract the operator never called, and does not
mention the configured path. The one line that does name it —
`Start git service with repo /tmp/dcat-store.git` — is logged at INFO before the attempt, so
in the log the path and the failure are two unrelated-looking events.

Note this is not EU-1: an existing repository with no commits now works. This is the case
where the directory is not there at all.

**The silent variant.** The upward walk is unbounded — `findGitDir` climbs to the root
unless `addCeilingDirectory` is set, and it is not. So whether a missing path fails loudly or
quietly depends on where it sits. Measured against jgit 7.7.1:

| `config.repo()` | resulting `gitDir` | bare |
|---|---|---|
| `/tmp/dcat-store.git` (an `init --bare` repo) | `/tmp/dcat-store.git` | `true` |
| `/tmp/does-not-exist.git` | `IllegalArgumentException` | — |
| `/opt/git/dcat.atlas/nonexistent-store.git` | `/opt/git/dcat.atlas/.git` | `false` |

The third row is the hazard. The service activates, logs success, and writes every commit
into the ancestor's working copy instead of the store — and because that repository is
*non-bare*, the commits it produces show the entire checked-out tree as deleted, which is
exactly the situation the DCAT.Atlas configuration comment warns against. A typo in the
configured path, or a store directory that was expected to sit beside the source, is enough
to reach it; nothing in the log distinguishes it from a correct start.

**Why it matters.** A local bare repository is the simplest and default deployment, and "the
directory is not there yet" is its normal first boot. Found in DCAT.Atlas on 2026-08-20:
`/tmp` had been cleared since the previous run, and the stack trace gave no indication that
the remedy was a one-line `git init --bare`. Every consumer that ships a local-repo default
will meet this once per fresh machine, and the diagnosis costs a read of jgit's builder.

**Suggested fix.** Decide explicitly instead of searching. In the local branch:

- if `FileKey.isGitRepository(gitDir, FS.DETECTED)`, `setGitDir(gitDir)` — no walk, so the
  configured path is the repository that gets used or none is;
- otherwise throw with the path and the remedy, e.g. *"repo /tmp/dcat-store.git is not a git
  repository; create it with `git init --bare -b main /tmp/dcat-store.git`"*.

If discovery from a working-tree path is a wanted feature, keep `findGitDir` but bound it
with `addCeilingDirectory(gitDir)` so it can never bind to an unrelated ancestor, and log the
resolved directory at a level that is visible when it differs from the configured one.

Auto-creating the repository on activation would also remove the error, but it turns a
mistyped path into a silently empty store, which is harder to notice than a failed
activation; a consumer that wants it can init before activation, as the DCAT.Atlas test
harness does.

**Verification.** Activate against (a) a path that does not exist — expect a failure naming
the path, not jgit's builder contract; (b) a path that does not exist inside a git working
copy — expect the same failure rather than a service bound to the ancestor's `.git`; (c) an
`init --bare` repository — unchanged.

---

## EU-12 · No durable-local-plus-mirror mode: a remote store is heap-only

**Upstream:** [eclipse-fennec/emf.util#58](https://github.com/eclipse-fennec/emf.util/issues/58)

**Kind:** enhancement · **Severity:** medium — it is the shape most deployments want, and
its absence forces a choice between durability and a remote ·
**Implemented upstream 2026-08-20** in
[#60](https://github.com/eclipse-fennec/emf.util/pull/60) (open at the time of writing) with
a `remote()` attribute, as suggested below. An on-disk
`repo()` plus a `remote()` URL now commits to disk and pushes; `repo()` as a URL keeps the
in-memory mirror and refuses a separate `remote()` rather than guessing. The activation fetch
is tolerant for the on-disk case (`fetchOnActivation` warns and serves the volume) and still
fatal for the in-memory one, which is the right asymmetry — a mirror with no objects of its
own cannot start degraded. Verified from DCAT.Atlas against a `git daemon` remote.

`GitServiceImpl.activate` treats local and remote as mutually exclusive, and `pushInternal`
(line 583) closes the door explicitly:

```java
private void pushInternal() {
	if (!isRemote(config.repo())) {
		// Local on-disk repo: no remote to push to, the commit is already where it belongs.
		logger.log(Level.INFO, "Skipping push for local repo {0}", config.repo());
		return;
	}
```

So a consumer picks one of two things:

| `repo()` | repository | durability |
|---|---|---|
| a path | `FileRepository` on disk | commits durable immediately; **no remote, ever** |
| a URL | `InMemoryRepository` | commits durable only once pushed; **whole store in heap** |

**Why it matters.** The deployment shape a portal actually wants is neither: a bare
repository on a volume, durable the moment a commit is written, *and* mirrored to an
upstream git host so the data is off-box, reviewable and clonable. Today choosing the
remote gives up durability and bounded memory together — the store lives in the heap and
grows as commits accumulate (EU-7), and anything unpushed dies with the process. Choosing
the local repository gives up the mirror entirely.

Found in DCAT.Atlas while packaging the container: the local mode is what we ship as the
supported default, and the remote compose file has to carry a warning block explaining that
it is not the local mode with mirroring added.

**Suggested fix.** Separate *where the repository lives* from *where it is mirrored* —
they are independent concerns that `repo()` currently conflates. A second attribute, e.g.
`remote()`, would do it:

- `repo()` a path, `remote()` empty → today's local behaviour.
- `repo()` a URL → today's in-memory behaviour (keep it; it suits a read-mostly mirror).
- `repo()` a path, `remote()` a URL → the missing mode. `pushInternal` pushes to
  `remote()` instead of returning early, and the activation fetch becomes optional
  (a local repository already carries its objects).

`pushInternal` already takes the remote as a parameter to `setRemote(...)`, so the change is
mostly which string it passes and dropping the early return. `fetch()` and `resetToRemote()`
would target `remote()` on the same rule.

**Verification.** With `repo()` a bare on-disk repository and `remote()` an unreachable
URL: a commit succeeds and is present locally, and the push fails with `GitPushException`
(the EU-10 distinction, which is exactly what this mode needs). With a reachable remote,
the commit appears on both sides.

---

## EU-13 · A successful push does not update the remote-tracking ref, so "diverged from the remote" false-fires

**Upstream:** [eclipse-fennec/emf.util#59](https://github.com/eclipse-fennec/emf.util/issues/59)

**Kind:** bug · **Severity:** medium — it makes the only signal a consumer has about
unpushed work unusable ·
**Implemented upstream 2026-08-20** in
[#60](https://github.com/eclipse-fennec/emf.util/pull/60) (open at the time of writing) with
`recordPushed(update)`, called after `checkPushed` accepts one. It covers both edge cases this report asked for: `Result.NEW` creates the
tracking ref, so a first push to an empty remote produces a signal instead of a `null` head,
and it runs for `UP_TO_DATE` as well as `OK`. A ref it cannot move is logged as a warning
naming the consequence rather than failing the push, which is right — the objects did reach
the remote.

`refs/remotes/origin/*` is written in exactly one place, the fetch refspec in `activate`
(line 121):

```java
fetchCmd.setRefSpecs(new RefSpec("+refs/heads/*:" + REMOTE_PREFIX + "*"));
```

`pushInternal` pushes `getRef():getRef()` and, on success, updates nothing locally.
`getRemoteHead()` reads `refs/remotes/origin/<branch>`, so after the first accepted push it
is stale: the branch has moved on and the remote-tracking ref still points at whatever the
activation fetch saw.

**Why it matters.** `getRemoteHead()` is the only thing the API offers for answering "is my
work on the remote yet", and the answer is wrong in the healthy case. DCAT.Atlas uses it in
its readiness detail:

```java
if (remoteHead == null || remoteHead.equals(commitId)) {
	return "";
}
return "; WARNING: the branch differs from the remote's copy (%s) as of the last fetch"
		+ " - unpushed commits are lost on restart";
```

With `pushOnCommit=true` — the setting a remote deployment *must* use, since an unpushed
commit does not survive a restart — that warning fires permanently from the first write
onwards, on a deployment where every push succeeded. A warning that is always on is a
warning nobody reads, and it masks the real case it was written for.

Note this is not the documented staleness of "as of the last fetch". A consumer can accept
that a remote may have moved under it. What it cannot work with is its own successful
pushes not being reflected.

**Suggested fix.** After `checkPushed` accepts an update, move the tracking ref to the
commit that was pushed — which is what `git push` itself does. `RemoteRefUpdate` carries
`getNewObjectId()`, and `repo.updateRef(REMOTE_PREFIX + config.branch())` is already the
idiom used elsewhere in the class (line 266). `UP_TO_DATE` should update it too: it means
the remote already has the commit.

**Verification.** Commit and push to a reachable remote with `pushOnCommit=true`, then
assert `getRemoteHead()` equals the commit id `commit()` returned. Then commit without
pushing and assert it differs — the case the warning is actually for.
