/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.dcat.atlas.sparql;

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.core.Quad;

/**
 * A read view of a dataset whose default graph is the union of its named graphs,
 * the behaviour Fuseki calls {@code unionDefaultGraph}.
 * <p>
 * The projection keeps one named graph per resource (see
 * {@link DcatGraphServiceImpl}), so its real default graph is always empty. A
 * client written against the SPARQL protocol, however, expects
 * {@code SELECT * WHERE { ?s ?p ?o }} to see the data: it neither knows nor
 * should know our graph naming. Through this view such a query reads every
 * resource, while {@code GRAPH ?g} and {@code GRAPH <iri>} still address the
 * individual resource graphs, and an explicit {@code FROM} / {@code FROM NAMED}
 * is honoured by ARQ as before.
 * <p>
 * Writes are not redirected: the projection is maintained on the wrapped dataset,
 * and nothing is ever written to the default graph.
 * <p>
 * {@link DatasetGraphWrapperView} is essential, not decoration: ARQ's query engine
 * unwraps any plain {@code DatasetGraphWrapper} before executing and would query
 * the underlying dataset, whose default graph is empty. The marker tells it that
 * this wrapper changes what is seen.
 */
final class UnionDefaultGraph extends DatasetGraphWrapper implements DatasetGraphWrapperView {

	/**
	 * Held once rather than created per call: the engine decides between its
	 * default-graph fast path and the generic quad path by identity comparison
	 * against {@code getDataset().getDefaultGraph()}.
	 */
	private final Graph union;

	UnionDefaultGraph(DatasetGraph dsg) {
		super(dsg);
		this.union = dsg.getUnionGraph();
	}

	@Override
	public Graph getDefaultGraph() {
		return union;
	}

	@Override
	public Iterator<Quad> find(Quad quad) {
		return find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
	}

	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		return super.find(unionIfDefault(g), s, p, o);
	}

	@Override
	public boolean contains(Quad quad) {
		return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		return super.contains(unionIfDefault(g), s, p, o);
	}

	/**
	 * {@code null} and {@code ANY} are left alone: they mean 'all quads', which
	 * already spans every named graph.
	 */
	private static Node unionIfDefault(Node g) {
		return g != null && Quad.isDefaultGraph(g) ? Quad.unionGraph : g;
	}
}
