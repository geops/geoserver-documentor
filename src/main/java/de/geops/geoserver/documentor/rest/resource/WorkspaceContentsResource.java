package de.geops.geoserver.documentor.rest.resource;

import java.util.List;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.rest.AbstractCatalogResource;
import org.geoserver.rest.format.DataFormat;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;

import de.geops.geoserver.documentor.Harvester;
import de.geops.geoserver.documentor.info.WorkspaceDoc;
import de.geops.geoserver.documentor.info.WorkspaceFullDoc;


public class WorkspaceContentsResource extends AbstractCatalogResource {

	public WorkspaceContentsResource(Context context, Request request,
			Response response, Catalog catalog) {
		super(context, request, response, WorkspaceFullDoc.class, catalog);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<DataFormat> createSupportedFormats(Request request,
			Response response) {
		List<DataFormat> formats = super.createSupportedFormats(request, response);
		return formats;
	}

	@Override
	protected Object handleObjectGet() throws Exception {
		Request req = getRequest();
		Form parameters = req.getResourceRef().getQueryAsForm();
		String workspaceName = getAttribute("workspace");
		if(workspaceName.contains(".")) {
			workspaceName = workspaceName.substring(0, workspaceName.lastIndexOf('.'));
		}
		
		Harvester havester = new Harvester(this.catalog);
		return havester.getWorkspaceFull(workspaceName);
	}
	
	@Override
	public boolean allowPost() {
		return false;
	}

}
