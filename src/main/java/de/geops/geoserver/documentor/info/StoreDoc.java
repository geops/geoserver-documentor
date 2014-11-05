package de.geops.geoserver.documentor.info;

public class StoreDoc extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1464825066241300860L;

	protected String description;
	protected String type;

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setType(String type) {
		this.type = type;
	}
}
