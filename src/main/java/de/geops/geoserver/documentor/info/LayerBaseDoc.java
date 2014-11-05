package de.geops.geoserver.documentor.info;

abstract class LayerBaseDoc extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6159443678363380758L;

	
	protected String title;

	protected String description;
	
	public String getDescription() {
		return description;
	}
	
	public String getTitle() {
		return title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
