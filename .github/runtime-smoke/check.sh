#!/usr/bin/env bash
#
# Copyright (c) 2026 Contributors to the Eclipse Foundation.
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0 AND CC0-1.0
#
# Contributors:
#     Data In Motion - initial API and implementation
#
# Generated with the assistance of AI (Anthropic Claude); reviewed by a project
# committer before merge.
#
# Functional part of the runtime smoke test (#78): one round trip through the
# public API of a *running* DCAT.Atlas, started with the shapes in ./shapes.
#
#   1. PUT a catalog through the admin API                  -> 201 (or 200 on a re-run)
#   2. GET it back as RDF/XML                                -> 200, application/rdf+xml, our title
#   3. GET the catalog listing                               -> 200, application/xmi, X-Total-Count >= 1
#   4. PUT a catalog only the smoke SHACL shape rejects      -> 422 with a sh:ValidationReport
#   5. GET the SPARQL endpoint                               -> 200, application/sparql-results+json
#
# Usage: check.sh [base-url]     (default http://localhost:8080)
# Works against the container as well as a locally started docker.jar.

set -euo pipefail

BASE="${1:-http://localhost:8080}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

failures=0
pass() { echo "PASS  $*"; }
fail() { echo "FAIL  $*"; failures=$((failures + 1)); }

# request <name> <expected-status-regex> <expected-content-type-prefix or ""> <curl args...>
# Leaves the body in $WORK/<name>.body and the headers in $WORK/<name>.headers.
request() {
	local name="$1" want_status="$2" want_type="$3"
	shift 3
	local out status type
	out="$(curl -sS -o "$WORK/$name.body" -D "$WORK/$name.headers" \
		-w '%{http_code} %{content_type}' --max-time 30 "$@")" || {
		fail "$name: request failed"
		return 1
	}
	status="${out%% *}"
	type="${out#* }"
	if [[ ! "$status" =~ ^($want_status)$ ]]; then
		fail "$name: HTTP $status, expected $want_status"
		head -c 2000 "$WORK/$name.body" || true
		echo
		return 1
	fi
	if [[ -n "$want_type" && "$type" != "$want_type"* ]]; then
		fail "$name: Content-Type '$type', expected $want_type"
		return 1
	fi
	pass "$name: HTTP $status ${type:-(no body)}"
}

TITLE="Runtime smoke test catalog"
cat > "$WORK/catalog.xmi" <<XMI
<?xml version="1.0" encoding="UTF-8"?>
<dcat:Catalog xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#" xmi:version="2.0">
  <title lang="en" value="${TITLE}"/>
  <description lang="en" value="Written by the CI runtime smoke test"/>
  <publisher about="https://projects.eclipse.org/projects/technology.fennec">
    <name lang="en" value="Eclipse Fennec"/>
  </publisher>
</dcat:Catalog>
XMI
# Valid for the EMF model, invalid only for the smoke shape (title > 40 characters).
sed "s/value=\"${TITLE}\"/value=\"A catalog title far longer than the forty characters the smoke shape allows\"/" \
	"$WORK/catalog.xmi" > "$WORK/invalid.xmi"

# 1. write
request write '201|200' '' -X PUT "$BASE/rest/admin/catalogs/runtime-smoke" \
	-H 'Content-Type: application/xmi' --data-binary "@$WORK/catalog.xmi" || true

# 2. read back
if request read '200' 'application/rdf+xml' "$BASE/rest/catalogs/runtime-smoke" \
	-H 'Accept: application/rdf+xml'; then
	if grep -q "$TITLE" "$WORK/read.body"; then
		pass "read: body carries the title that was written"
	else
		fail "read: title '$TITLE' not in the response body"
	fi
fi

# 3. listing
if request list '200' 'application/xmi' "$BASE/rest/catalogs"; then
	total="$(grep -i '^X-Total-Count:' "$WORK/list.headers" | tr -dc '0-9')"
	if [[ -n "$total" && "$total" -ge 1 ]]; then
		pass "list: X-Total-Count=$total"
	else
		fail "list: X-Total-Count '${total}' after a successful write"
	fi
fi

# 4. SHACL enforcement on write
if request shacl '422' 'text/turtle' -X PUT "$BASE/rest/admin/catalogs/runtime-smoke-invalid" \
	-H 'Content-Type: application/xmi' --data-binary "@$WORK/invalid.xmi"; then
	if grep -q 'sh:ValidationReport' "$WORK/shacl.body"; then
		pass "shacl: rejected with a SHACL validation report"
	else
		fail "shacl: 422 without a SHACL validation report"
	fi
fi

# 5. SPARQL endpoint answers
request sparql '200' 'application/sparql-results+json' -G "$BASE/rest/sparql" \
	--data-urlencode 'query=SELECT ?s WHERE { GRAPH ?g { ?s a <http://www.w3.org/ns/dcat#Catalog> } } LIMIT 1' \
	-H 'Accept: application/sparql-results+json' || true

if ((failures > 0)); then
	echo "$failures smoke check(s) failed"
	exit 1
fi
echo "All smoke checks passed"
