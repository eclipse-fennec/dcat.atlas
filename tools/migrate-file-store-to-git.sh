#!/usr/bin/env bash
#
# Copyright (c) 2012 - 2026 Data In Motion and others.
# All rights reserved.
#
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# ---------------------------------------------------------------------------------------
# Imports a pre-Phase-2 file store into a git repository as its initial commit (W9).
#
#   before   <root>/datasets/air                      (no extension - the URI was the path)
#   after    <repo>:<basePath>/datasets/air.xmi
#
# The content of each resource is copied byte for byte. Only where it lives changes, and
# the stored identity - which is inside the file - is untouched, so this is not a rewrite
# and nothing has to be re-validated afterwards.
#
# Usage:
#   tools/migrate-file-store-to-git.sh <store-root> <target-repo> [base-path] [branch]
#
#   store-root   the old STORE_FOLDER, e.g. /opt/dcat/data or /tmp/rdf
#   target-repo  the bare repository to create or populate, e.g. /opt/dcat/store.git
#   base-path    folder inside the repository (default: dcat; "" for the root)
#   branch       default: main
# ---------------------------------------------------------------------------------------
set -euo pipefail

STORE_ROOT=${1:?usage: $0 <store-root> <target-repo> [base-path] [branch]}
TARGET_REPO=${2:?usage: $0 <store-root> <target-repo> [base-path] [branch]}
BASE_PATH=${3-dcat}
BRANCH=${4-main}

# Fixed, and not a parameter: these are the collection segment of every stored identity,
# not a storage choice. See StoreLayout. Distributions are absent because dcat:distribution
# is containment - a Distribution lives inside its Dataset's file (FR-10).
COLLECTIONS=(catalogs datasets data-services dataset-series)

die() { printf 'error: %s\n' "$*" >&2; exit 1; }

[ -d "$STORE_ROOT" ] || die "store root does not exist: $STORE_ROOT"

# An initial commit, deliberately: importing on top of existing history would interleave
# a migration with real edits and leave no clean point to roll back to.
if [ -d "$TARGET_REPO" ] && git -C "$TARGET_REPO" rev-parse --verify --quiet "refs/heads/$BRANCH" >/dev/null 2>&1; then
	die "$TARGET_REPO already has commits on $BRANCH; migrate into a fresh repository"
fi
if [ ! -d "$TARGET_REPO" ]; then
	git init --bare -b "$BRANCH" "$TARGET_REPO" >/dev/null
	printf 'created bare repository %s\n' "$TARGET_REPO"
fi

# The same rule as DcatIds.isSafeId, which is the source of truth: an id may not be blank
# or contain / \ .. # ?. Anything else is refused rather than renamed - a resource whose id
# we would have to change is a resource whose identity we would be changing, and that is a
# decision for a person, not for a migration script.
is_safe_id() {
	case "$1" in
		''|*/*|*\\*|*..*|*'#'*|*'?'*) return 1 ;;
		*) return 0 ;;
	esac
}

WORKTREE=$(mktemp -d)
trap 'rm -rf "$WORKTREE"' EXIT
git -C "$WORKTREE" init -q -b "$BRANCH"

prefix=${BASE_PATH:+$BASE_PATH/}
total=0
rejected=()

for collection in "${COLLECTIONS[@]}"; do
	directory="$STORE_ROOT/$collection"
	[ -d "$directory" ] || { printf '  %-16s (absent)\n' "$collection"; continue; }
	count=0
	while IFS= read -r -d '' file; do
		id=$(basename "$file")
		if ! is_safe_id "$id"; then
			rejected+=("$collection/$id")
			continue
		fi
		mkdir -p "$WORKTREE/$prefix$collection"
		cp "$file" "$WORKTREE/$prefix$collection/$id.xmi"
		count=$((count + 1))
	done < <(find "$directory" -maxdepth 1 -type f -print0)
	printf '  %-16s %d resource(s)\n' "$collection" "$count"
	total=$((total + count))
done

# Refuse the whole migration rather than import a partial store: a catalog whose dataset
# was skipped would be a dangling reference, which the write boundary would then refuse to
# ever update.
if [ ${#rejected[@]} -gt 0 ]; then
	printf 'error: %d file(s) have names that are not usable ids:\n' "${#rejected[@]}" >&2
	printf '  %s\n' "${rejected[@]}" >&2
	die "rename or remove them and run again; nothing was imported"
fi
[ "$total" -gt 0 ] || die "no resources found under $STORE_ROOT; nothing to import"

git -C "$WORKTREE" add -A
git -C "$WORKTREE" \
	-c user.name="DCAT.Atlas migration" \
	-c user.email="dcat-atlas@example.org" \
	commit -q -m "Import the file store as the initial git-backed store

$total resource(s) from $STORE_ROOT, unchanged, at $prefix<collection>/<id>.xmi."
git -C "$WORKTREE" push -q "$TARGET_REPO" "$BRANCH:$BRANCH"

printf 'imported %d resource(s) into %s on %s\n' "$total" "$TARGET_REPO" "$BRANCH"
printf 'configure the portal with GIT_REPO=%s' "$TARGET_REPO"
[ -n "$BASE_PATH" ] && printf ' STORE_BASE_PATH=%s' "$BASE_PATH"
printf '\n'
