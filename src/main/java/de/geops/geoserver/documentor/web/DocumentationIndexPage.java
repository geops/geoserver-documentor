package de.geops.geoserver.documentor.web;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.geoserver.web.GeoServerSecuredPage;

import de.geops.geoserver.documentor.ExtensionInfo;
import de.geops.geoserver.documentor.Harvester;
import de.geops.geoserver.documentor.info.WorkspaceDoc;

public class DocumentationIndexPage extends GeoServerSecuredPage {
	
	public DocumentationIndexPage() {
		Harvester harvester = new Harvester();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ListView listview = new ListView("listview", harvester.getWorkspaces()) {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 2974513833506276491L;

			protected void populateItem(ListItem item) {
		    	WorkspaceDoc wi = (WorkspaceDoc) item.getModelObject();
				BookmarkablePageLink link = new BookmarkablePageLink("link", WorkspacePage.class);
				link.setParameter("workspaceName", wi.getName());
				link.add(new Label("workspaceName", wi.getName()));
		        item.add(link);
		    }
		};
		add(listview);

		ExtensionInfo info = new ExtensionInfo();
		add(new Label("documentorVersion", info.getVersion()));
		add(new Label("documentorGitVersion", info.getGitVersion()));
		add(new Label("readme", info.getReadme()));
	}

	public String getAjaxIndicatorMarkupId() {
		return "ajaxFeedback";
	}

}
