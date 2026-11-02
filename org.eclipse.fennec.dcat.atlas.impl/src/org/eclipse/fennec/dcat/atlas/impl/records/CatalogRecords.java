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
package org.eclipse.fennec.dcat.atlas.impl.records;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.impl.integrity.Members;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.Catalog;
import dcat.CatalogRecord;
import dcat.DcatFactory;
import dcat.DcatPackage;
import dcat.DcatResource;
import rdf.Datatype;
import rdf.DateOrDateTimeLiteral;
import rdf.RdfFactory;

/**
 * The catalogue records the server keeps for the members of a catalog (#56).
 *
 * <h2>What a record says, and why the server owns it</h2>
 *
 * A {@code dcat:CatalogRecord} describes <em>one entry in</em> a catalog — not the catalog —
 * and exists for a single distinction: {@code dcterms:modified} on a Dataset is when the
 * data changed, on its record it is when the <em>catalogue entry's metadata</em> changed.
 * That is what a harvester reads to ask "what moved since Tuesday", so it is worth only as
 * much as it is trustworthy: a date a client could set says nothing. Records are therefore
 * maintained here and never accepted from a request, the way {@code about} is re-stamped
 * rather than believed.
 *
 * <h2>When it moves</h2>
 *
 * Whenever the member enters the catalog, leaves it, or <em>is itself written</em> — the
 * last being the decision this rests on (#56). A member write never touches the catalog, so
 * keeping the date honest means finding the catalogs that hold it and re-stamping there.
 * Measured before it was built: the lookup is flat in the number of catalogs (~5-7 ms for
 * 20, against 3-4 ms for the member write itself) <em>provided no member proxy is
 * resolved</em> — iterating {@code catalog.getDataset()} normally would load every member's
 * blob and turn one write into a read of the whole store. Everything here reads references
 * the way {@code References} does: {@code eGet(feature, false)} and {@code basicIterator}.
 */
public final class CatalogRecords {

	private CatalogRecords() {
	}

	/**
	 * A member has entered {@code catalog}: give the entry its record, or re-stamp the one
	 * it already has. The caller saves the catalog — this only changes it in memory.
	 */
	public static void memberLinked(Catalog catalog, EObject member) {
		String memberIri = Members.targetIri(member);
		CatalogRecord record = recordFor(catalog, memberIri);
		if (record == null) {
			record = DcatFactory.eINSTANCE.createCatalogRecord();
			record.setIssued(now());
			record.setPrimaryTopic((DcatResource) member);
			catalog.getRecord().add(record);
		}
		record.setModified(now());
	}

	/**
	 * A member is leaving {@code catalog}: its entry is gone, so the record describing that
	 * entry goes with it. The caller saves the catalog.
	 * <p>
	 * Removed rather than kept as a tombstone: {@code foaf:primaryTopic} is mandatory on a
	 * record, so a record whose member is no longer there cannot be both kept and valid.
	 */
	public static void memberUnlinked(Catalog catalog, String memberIri) {
		CatalogRecord record = recordFor(catalog, memberIri);
		if (record != null) {
			catalog.getRecord().remove(record);
		}
	}

	/**
	 * The member named by {@code memberIri} has been written: re-stamp its record in every
	 * catalog that holds it, in the caller's own store session, so the whole thing is one
	 * commit.
	 * <p>
	 * A catalog that holds the member without having a record for it gets one. That is the
	 * migration path for everything catalogued before this existed, and it costs nothing to
	 * the common case.
	 */
	public static void memberWritten(DcatStoreSession store, String memberIri) {
		for (Catalog catalog : owners(store, memberIri)) {
			CatalogRecord record = recordFor(catalog, memberIri);
			if (record == null) {
				EObject member = memberOf(store, memberIri);
				if (member == null) {
					continue;
				}
				memberLinked(catalog, member);
			} else {
				record.setModified(now());
			}
			// Bookkeeping, not the caller's content: see DcatStoreSession.saveBookkeeping.
			store.saveBookkeeping(catalog);
		}
}

	/**
	 * The member named by {@code memberIri} is being deleted: drop its record wherever one
	 * exists. The caller saves nothing — each catalog is staged here.
	 * <p>
	 * It must run <em>before</em> {@code References.detach}, which would otherwise null the
	 * record's {@code primaryTopic} along with every other link to the member and leave a
	 * record that is both meaningless and invalid.
	 */
	public static void memberDeleted(DcatStoreSession store, String memberIri) {
		for (Catalog catalog : owners(store, memberIri)) {
			memberUnlinked(catalog, memberIri);
			store.saveBookkeeping(catalog);
		}
	}

	/**
	 * Re-derives {@code incoming}'s records from its membership, keeping what the stored
	 * catalog already said about the entries that are still there.
	 *
	 * <h2>Why a client's records are discarded</h2>
	 *
	 * Records are contained in the catalog, so they travel in its body: a client that reads
	 * a catalog and writes it back sends them along, and one that composes a body by hand
	 * sends none. Believing either would let a caller backdate a catalogue entry — which is
	 * the one thing the date is for — or erase the bookkeeping for every entry by omission.
	 * So the body's records are dropped and the set is rebuilt from the membership, exactly
	 * as {@code about} is re-stamped rather than believed.
	 *
	 * <h2>What survives</h2>
	 *
	 * An entry that is still a member keeps its {@code issued} and {@code modified}: the
	 * catalog changed, that entry's metadata did not. A member that was not there before is
	 * a new entry and gets a new record. A member that is no longer listed has no entry, so
	 * its record goes.
	 */
	public static void preserveRecords(DcatStoreSession store, String collection, String id, Catalog incoming) {
		Map<String, CatalogRecord> stored = new LinkedHashMap<>();
		store.<Catalog>get(collection, id).filter(previous -> previous != incoming)
				.ifPresent(previous -> {
					for (CatalogRecord record : previous.getRecord()) {
						EObject topic = (EObject) record.eGet(DcatPackage.Literals.CATALOG_RECORD__PRIMARY_TOPIC,
								false);
						if (topic != null) {
							stored.put(Members.targetIri(topic), record);
						}
					}
				});

		incoming.getRecord().clear();
		for (EObject member : members(incoming)) {
			CatalogRecord previous = stored.get(Members.targetIri(member));
			CatalogRecord record = DcatFactory.eINSTANCE.createCatalogRecord();
			record.setPrimaryTopic((DcatResource) member);
			record.setIssued(previous == null ? now() : EcoreUtil.copy(previous.getIssued()));
			record.setModified(previous == null ? now() : EcoreUtil.copy(previous.getModified()));
			incoming.getRecord().add(record);
		}
	}

	/** Every member of {@code catalog}, of all three kinds, without resolving one. */
	private static List<EObject> members(Catalog catalog) {
		List<EObject> members = new ArrayList<>();
		for (EList<? extends EObject> list : List.of(catalog.getDataset(), catalog.getService(),
				catalog.getCatalog())) {
			for (Iterator<EObject> it = basicIterator(list); it.hasNext();) {
				members.add(it.next());
			}
		}
		return members;
	}

	/** The catalogs holding {@code memberIri}, or holding a record that describes it. */
	private static List<Catalog> owners(DcatStoreSession store, String memberIri) {
		List<Catalog> owners = new ArrayList<>();
		for (String id : store.ids(StoreLayout.CATALOGS)) {
			Catalog catalog = store.<Catalog>get(StoreLayout.CATALOGS, id).orElse(null);
			if (catalog != null && (holds(catalog, memberIri) || recordFor(catalog, memberIri) != null)) {
				owners.add(catalog);
			}
		}
		return owners;
	}

	/** Whether {@code catalog} lists the member, as a membership of any of the three kinds. */
	private static boolean holds(Catalog catalog, String memberIri) {
		return contains(catalog.getDataset(), memberIri) || contains(catalog.getService(), memberIri)
				|| contains(catalog.getCatalog(), memberIri);
	}

	private static boolean contains(EList<? extends EObject> members, String memberIri) {
		for (Iterator<EObject> it = basicIterator(members); it.hasNext();) {
			if (memberIri.equals(Members.targetIri(it.next()))) {
				return true;
			}
		}
		return false;
	}

	/** The record describing {@code memberIri}, or {@code null}. */
	private static CatalogRecord recordFor(Catalog catalog, String memberIri) {
		for (CatalogRecord record : catalog.getRecord()) {
			EObject topic = (EObject) record.eGet(DcatPackage.Literals.CATALOG_RECORD__PRIMARY_TOPIC, false);
			if (topic != null && memberIri.equals(Members.targetIri(topic))) {
				return record;
			}
		}
		return null;
	}

	/** The stored member behind a logical IRI, in whichever collection it belongs to. */
	private static EObject memberOf(DcatStoreSession store, String memberIri) {
		for (String collection : StoreLayout.COLLECTIONS) {
			String id = StoreLayout.idOf(collection, memberIri);
			if (id != null) {
				return store.<EObject>get(collection, id).orElse(null);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static Iterator<EObject> basicIterator(EList<? extends EObject> members) {
		return ((InternalEList<EObject>) members).basicIterator();
	}

	/**
	 * Now, as an {@code xsd:dateTime} in UTC.
	 * <p>
	 * With milliseconds, and deliberately: two writes of the same member a moment apart must
	 * produce two different dates, or a harvester polling between them sees nothing happened.
	 */
	private static DateOrDateTimeLiteral now() {
		GregorianCalendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		DateOrDateTimeLiteral literal = RdfFactory.eINSTANCE.createDateOrDateTimeLiteral();
		literal.setValue(factory().newXMLGregorianCalendar(utc));
		literal.setDatatype(Datatype.HTTP_WWW_W3_ORG2001_XML_SCHEMA_DATE_TIME);
		return literal;
	}

	private static DatatypeFactory factory() {
		try {
			return DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException("No XML datatype factory available", e);
		}
	}
}
