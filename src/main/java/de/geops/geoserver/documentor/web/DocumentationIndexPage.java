package de.geops.geoserver.documentor.web;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.geoserver.web.GeoServerSecuredPage;

import de.geops.geoserver.documentor.Harvester;
import de.geops.geoserver.documentor.info.WorkspaceDoc;

public class DocumentationIndexPage extends GeoServerSecuredPage {
	
	public DocumentationIndexPage() {
		Harvester harvester = new Harvester();
		
		ListView listview = new ListView("listview", harvester.getWorkspaces()) {
		    protected void populateItem(ListItem item) {
		    	WorkspaceDoc wi = (WorkspaceDoc) item.getModelObject();
				BookmarkablePageLink link = new BookmarkablePageLink("link", WorkspacePage.class);
				link.setParameter("workspaceName", wi.getName());
				link.add(new Label("workspaceName", wi.getName()));
		        item.add(link);
		    }
		};
		add(listview);

	}

	public String getAjaxIndicatorMarkupId() {
		return "ajaxFeedback";
	}

}
