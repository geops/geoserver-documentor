package de.geops.geoserver.documentor.info;

import java.util.HashMap;
import java.util.Map;

public class StoreDoc extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1464825066241300860L;

	protected String description;

	protected String type;
	
	protected Map<String, String> info = new HashMap<String, String>();

	public void addInfo(String key, String value) {
		this.info.put(key, value);
	}
	public String getDescription() {
		return description;
	}
	public Map<String, String> getInfo() {
		return info;
	}

	public String getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setInfo(Map<String, String> info) {
		this.info = info;
	}

	public void setType(String type) {
		this.type = type;
	}
}
