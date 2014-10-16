package de.geops.geoserver.documentor.info;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceFullDoc extends WorkspaceDoc {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7100830910319611073L;

	protected List<LayerDoc> layers = new ArrayList<LayerDoc>();

	public List<LayerDoc> getLayers() {
		return layers;
	}

	public void setLayers(List<LayerDoc> layers) {
		this.layers = layers;
	}

}
