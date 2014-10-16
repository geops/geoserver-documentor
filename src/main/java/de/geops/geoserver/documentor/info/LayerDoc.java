package de.geops.geoserver.documentor.info;

import java.io.Serializable;

public class LayerDoc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String name;

	protected String workspaceName;

	protected String title;

	protected String description;

	protected String type;

	protected FeatureTypeDoc featureType;

	protected StoreDoc store;

	public String getDescription() {
		return description;
	}

	public FeatureTypeDoc getFeatureType() {
		return featureType;
	}

	public String getName() {
		return name;
	}

	public StoreDoc getStore() {
		return store;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFeatureType(FeatureTypeDoc featureTypeDoc) {
		this.featureType = featureTypeDoc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStore(StoreDoc storeDoc) {
		this.store = storeDoc;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

}
