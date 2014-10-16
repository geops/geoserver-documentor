package de.geops.geoserver.documentor.info;

import java.util.List;

public class FeatureTypeDoc {

	protected List<PropertyDoc> properties;

	protected SqlViewDoc sqlView;

	protected List<TableDoc> relatedTables;

	public List<PropertyDoc> getProperties() {
		return properties;
	}

	public List<TableDoc> getRelatedTables() {
		return relatedTables;
	}

	public SqlViewDoc getSqlView() {
		return sqlView;
	}

	public void setProperties(List<PropertyDoc> properties) {
		this.properties = properties;
	}

	public void setRelatedTables(List<TableDoc> relatedTables) {
		this.relatedTables = relatedTables;
	}

	public void setSqlView(SqlViewDoc sqlViewDoc) {
		this.sqlView = sqlViewDoc;
	}
}
