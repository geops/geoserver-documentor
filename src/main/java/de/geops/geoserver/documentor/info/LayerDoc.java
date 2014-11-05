package de.geops.geoserver.documentor.info;

import java.util.ArrayList;
import java.util.List;

public class LayerDoc extends LayerBaseDoc {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String nativeName;

	protected String title;

	protected String description;

	protected String type;

	protected FeatureTypeDoc featureType;

	protected StoreDoc store;

	protected boolean isAdvertized;

	protected boolean isEnabled;
	
	protected List<String> keywords = new ArrayList<String>();
	

	public FeatureTypeDoc getFeatureType() {
		return featureType;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public String getNativeName() {
		return nativeName;
	}

	public StoreDoc getStore() {
		return store;
	}

	public String getType() {
		return type;
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

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setFeatureType(FeatureTypeDoc featureTypeDoc) {
		this.featureType = featureTypeDoc;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}


	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}

	public void setStore(StoreDoc storeDoc) {
		this.store = storeDoc;
	}

	public void setType(String type) {
		this.type = type;
	}

}
