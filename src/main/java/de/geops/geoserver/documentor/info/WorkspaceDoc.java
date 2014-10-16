/**
 * MEMO:
 * geoserver set names/aliases of pojos in ./main/src/main/java/org/geoserver/config/util/XStreamPersister.java
 */

package de.geops.geoserver.documentor.info;

import java.io.Serializable;

public class WorkspaceDoc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7291438343211349301L;

	protected String name;

	protected String id;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

}
