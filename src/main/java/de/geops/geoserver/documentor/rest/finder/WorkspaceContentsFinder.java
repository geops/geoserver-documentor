package de.geops.geoserver.documentor.rest.finder;

import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestletException;
import org.restlet.Finder;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

import de.geops.geoserver.documentor.rest.resource.WorkspaceContentsResource;

public class WorkspaceContentsFinder extends Finder {

	protected Catalog catalog;
	
	protected WorkspaceContentsFinder(Catalog catalog) {
		this.catalog = catalog;
	}
	
	
	public Resource findTarget(Request request, Response response) {
		String workspaceName = (String) request.getAttributes().get("workspace");
		
		if (workspaceName != null && request.getMethod() == Method.GET) {
			return new WorkspaceContentsResource(getContext(), request, response, this.catalog);
		}
		
		throw new RestletException("No such workspace: " + workspaceName, Status.CLIENT_ERROR_NOT_FOUND);
	}
}
