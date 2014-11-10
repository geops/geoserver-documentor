package de.geops.geoserver.documentor.info;

import java.util.ArrayList;
import java.util.List;

abstract class LayerBaseDoc extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6159443678363380758L;

	
	protected String title;

	protected String description;
	
	/**
	 * Errors during generation of the documentations
	 */
	protected List<String> documentationErrors =  new ArrayList<String>();
	
	public void addDocumentationError(String documentationErrorMsg) {
		this.documentationErrors.add(documentationErrorMsg);
	}

	public String getDescription() {
		return description;
	}

	public List<String> getDocumentationErrors() {
		return documentationErrors;
	}
	
	public String getTitle() {
		return title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDocumentationErrors(List<String> documentationErrors) {
		this.documentationErrors = documentationErrors;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
}
