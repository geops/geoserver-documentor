package de.geops.geoserver.documentor.web;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.web.GeoServerBasePage;
import org.geoserver.web.GeoServerSecuredPage;

import de.geops.geoserver.documentor.Harvester;

public class DocumentationIndexPage extends GeoServerSecuredPage {
	
	public DocumentationIndexPage() {
		Harvester harvester = new Harvester();
		
		ListView listview = new ListView("listview", harvester.getWorkspaces()) {
		    protected void populateItem(ListItem item) {
		    	WorkspaceInfo wi = (WorkspaceInfo) item.getModelObject();
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
