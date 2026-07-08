package org.eclipse.fennec.dcat.atlas.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Shared configuration for the file-based DCAT-AP stores. Every entity service
 * (catalogs, datasets, distributions, &hellip;) {@code @Designate}s this same
 * OCD; each service keeps its own configuration PID, so each can be pointed at
 * its own storage directory (e.g. via {@code $[env:STORE_FOLDER]}).
 * <p>
 * Persistence is, for now, a flat directory of RDF/XML files, one per resource.
 */
@ObjectClassDefinition(name = "DCAT-AP Atlas file store", description = "File-system persistence for a DCAT-AP entity store")
public @interface StoreConfig {

	/**
	 * Directory under which one {@code <id>.rdf} file per resource is stored. It is
	 * created on activation if it does not exist. Configure a distinct directory per
	 * service to keep the entity types separate.
	 */
	@AttributeDefinition(name = "Storage directory", description = "Directory holding one RDF/XML file per resource")
	String directory() default "data";
}
