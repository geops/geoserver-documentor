package de.geops.geoserver.documentor.info;

import java.util.ArrayList;
import java.util.List;

public class LayerGroupDoc extends LayerBaseDoc {

	/**
	 * 
	 */
	private static final long serialVersionUID = 133416941973856942L;

	protected List<Entity> layers = new ArrayList<Entity>();

	public List<Entity> getLayers() {
		return layers;
	}

	public void setLayers(List<Entity> layers) {
		this.layers = layers;
	}
}
