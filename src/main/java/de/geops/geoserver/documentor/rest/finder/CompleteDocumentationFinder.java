package de.geops.geoserver.documentor.rest.finder;

import org.geoserver.catalog.Catalog;
import org.restlet.Finder;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

import de.geops.geoserver.documentor.rest.resource.CompleteDocumentationResource;

public class CompleteDocumentationFinder extends Finder {
	
	protected Catalog catalog;
	
	protected CompleteDocumentationFinder(Catalog catalog) {
		this.catalog = catalog;
	}
	
	
	public Resource findTarget(Request request, Response response) {
		return new CompleteDocumentationResource(getContext(), request, response, this.catalog);

	}
}
