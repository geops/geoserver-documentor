package de.geops.geoserver.documentor.info;

import java.util.List;

public class TableDoc {

	protected String tableName;
	protected String tableSchema;
	protected String type;
	protected String comment;
	protected String definition;
	protected boolean isMainTable = false;
	protected List<PropertyDoc> columns;

	/**
	 * only compares by name and schema. 
	 * 
	 * this is the desired behavior and is expected to work this way in the postgresqlanalyzer
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableDoc other = (TableDoc) obj;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		if (tableSchema == null) {
			if (other.tableSchema != null)
				return false;
		} else if (!tableSchema.equals(other.tableSchema))
			return false;
		return true;
	}

	public List<PropertyDoc> getColumns() {
		return columns;
	}

	public String getComment() {
		return comment;
	}

	public String getDefinition() {
		return definition;
	}

	public String getTableName() {
		return tableName;
	}

	public String getTableSchema() {
		return tableSchema;
	}

	public String getType() {
		return type;
	}

	/**
	 * only includes by name and schema. 
	 * 
	 * this is the desired behavior and is expected to work this way in the postgresqlanalyzer
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
		result = prime * result
				+ ((tableSchema == null) ? 0 : tableSchema.hashCode());
		return result;
	}

	public boolean isMainTable() {
		return isMainTable;
	}

	public void setColumns(List<PropertyDoc> columns) {
		this.columns = columns;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public void setMainTable(boolean isMainTable) {
		this.isMainTable = isMainTable;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setTableSchema(String tableSchema) {
		this.tableSchema = tableSchema;
	}

	public void setType(String type) {
		this.type = type;
	}
}
