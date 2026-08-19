# Issue sketches for `eclipse-fennec/emf.util`

Ten issues found in `org.eclipse.fennec.jgit` while designing the DCAT.Atlas git-backed
store ([`persistence-phase2-git-store.md`](persistence-phase2-git-store.md)). They are
written to be filed as-is upstream; the DCAT-side references are there so whoever picks one
up can see what it is for, not because the fix should be DCAT-specific.

All line numbers are against `/opt/git/fennec-emf.util` as of 2026-08-19.

**Status 2026-08-19: EU-1 … EU-6, EU-9 and EU-10 are implemented.** EU-7 and EU-8 remain
open by choice. EU-9 and EU-10 were found after the first six, while putting the store on top
of the new build and then testing it against a running portal.

| # | Title | Kind | Status |
|---|---|---|---|
| EU-1 | Reads fail on a repository with no commits | Bug | ✅ Fixed |
| EU-2 | A blank `privateKey` is treated as configured | Bug | ✅ Fixed |
| EU-3 | `GitService` exposes no blob id | Enhancement | ✅ Fixed |
| EU-4 | `GitService` has no `exists(path)` | Enhancement | ✅ Fixed |
| EU-5 | `commit()` writes an empty commit when nothing changed | Bug | ✅ Fixed |
| EU-6 | The fetch refspec force-updates, so "fetch and retry" loses commits | Bug | ✅ Fixed |
| EU-7 | An in-memory repository grows without bound in a long-running writer | Design | Open — documented limit (D7 defaults to a local repo) |
| EU-8 | No EMF write path over git anywhere in the ecosystem | Enhancement | Open by choice — built in DCAT.Atlas first (W2) |
| EU-9 | The SSH stack is required to load the service, even for a local repository | Bug | ✅ Fixed |
| EU-10 | `GitWriteException` conflates a failed commit with a failed push | Enhancement | ✅ Fixed |

All fixes landed upstream on 2026-08-19 and are exercised by the build in `cnf/local`; the
descriptions below are kept as filed, with a note on each fixed one saying how it was
resolved.

---

## EU-1 · Reads fail on a repository with no commits

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

---

## EU-8 · No EMF write path over git anywhere in the ecosystem

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
