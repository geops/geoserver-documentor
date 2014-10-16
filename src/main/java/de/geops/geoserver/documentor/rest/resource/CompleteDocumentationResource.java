package de.geops.geoserver.documentor.rest.resource;

import java.util.List;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.rest.AbstractCatalogResource;
import org.geoserver.rest.format.DataFormat;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import de.geops.geoserver.documentor.Harvester;
import de.geops.geoserver.documentor.info.DocumentationDoc;

public class CompleteDocumentationResource  extends AbstractCatalogResource {
	
	public CompleteDocumentationResource(final Context context, final Request request,
			final Response response, final Catalog catalog) {
		super(context, request, response, DocumentationDoc.class, catalog);
	}

	@Override
	protected List<DataFormat> createSupportedFormats(Request request,
			Response response) {
		List<DataFormat> formats = super.createSupportedFormats(request, response);
		return formats;
	}

	@Override
	protected Object handleObjectGet() throws Exception {
		Harvester havester = new Harvester(this.catalog);
		
		DocumentationDoc doc = new DocumentationDoc();
		doc.setWorkspaces(havester.getComplete());
		return doc;
	}
	
	@Override
	public boolean allowPost() {
		return false;
	}
}
