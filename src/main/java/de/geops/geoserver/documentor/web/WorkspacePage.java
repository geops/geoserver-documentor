package de.geops.geoserver.documentor.web;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.geoserver.web.GeoServerSecuredPage;

public class WorkspacePage extends GeoServerSecuredPage {

	String workspaceName;
	
	public WorkspacePage(String workspaceName) {
		add(new Label("workspaceName", workspaceName));
	}
	
	public WorkspacePage(PageParameters params) {
		this(params.getString("workspaceName"));
	}
}
