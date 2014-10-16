package de.geops.geoserver.documentor.info;

public class PropertyDoc {

	protected String name;
	protected String type;
	protected String comment;

	public String getComment() {
		return comment;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

}
