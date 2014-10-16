package de.geops.geoserver.documentor.info;

public class StoreDoc {
	protected String name;
	protected String description;
	protected String type;

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}
}
