package de.geops.geoserver.documentor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.web.GeoServerApplication;
import org.geotools.util.logging.Logging;

public class Harvester {

	final Catalog catalog;
	
	private static final Logger LOGGER = Logging.getLogger(Harvester.class);
	
	public Harvester() {
		catalog = GeoServerApplication.get().getCatalog();
	}
	
	public void ping() {
		LOGGER.severe("Found "+catalog.getWorkspaces().size()+" workspaces");
	}
	
	public List<WorkspaceInfo> getWorkspaces() {
		return catalog.getWorkspaces();
	}
	
}
