package de.geops.geoserver.documentor.info;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceFullDoc extends WorkspaceDoc {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7100830910319611073L;

	protected List<LayerDoc> layers = new ArrayList<LayerDoc>();
	
	protected List<LayerGroupDoc> layerGroups = new ArrayList<LayerGroupDoc>();

	public List<LayerGroupDoc> getLayerGroups() {
		return layerGroups;
	}

	public List<LayerDoc> getLayers() {
		return layers;
	}

	public void setLayerGroups(List<LayerGroupDoc> layerGroups) {
		this.layerGroups = layerGroups;
	}

	public void setLayers(List<LayerDoc> layers) {
		this.layers = layers;
	}

}
