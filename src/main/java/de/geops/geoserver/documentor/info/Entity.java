package de.geops.geoserver.documentor.info;

import java.io.Serializable;

public class Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5481172692588831337L;

	protected String workspaceName;

	protected String name;

	public String getName() {
		return name;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

}
