package de.geops.geoserver.documentor.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LayerDoc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String name;
	
	protected String nativeName;

	protected String workspaceName;

	protected String title;

	protected String description;

	protected String type;

	protected FeatureTypeDoc featureType;

	protected StoreDoc store;

	protected boolean isAdvertized;

	protected boolean isEnabled;
	
	protected List<String> keywords = new ArrayList<String>();
	
	public String getDescription() {
		return description;
	}
	
	public FeatureTypeDoc getFeatureType() {
		return featureType;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public String getName() {
		return name;
	}

	public String getNativeName() {
		return nativeName;
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

	public boolean isAdvertized() {
		return isAdvertized;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setAdvertized(boolean isAdvertized) {
		this.isAdvertized = isAdvertized;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setFeatureType(FeatureTypeDoc featureTypeDoc) {
		this.featureType = featureTypeDoc;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
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
