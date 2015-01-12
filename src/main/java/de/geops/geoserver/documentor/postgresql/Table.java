package de.geops.geoserver.documentor.postgresql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Table {
	
	/**
	 * create a Table object from an (escaped or not) string
	 * 
	 * @param input
	 * @return
	 */
	static Table fromString(String input) {
		Matcher matcher = Table.tablePattern.matcher(input.trim());
		if (!matcher.find()) {
			throw new IllegalArgumentException("found no table in: "+input);
		}
		return new Table(matcher.group(2), matcher.group(3));
	}

	String tableName;
	
	String tableSchema = "public";

	/**
	 * regex pattern to split table and schema name with the schema being optional.
	 * 
	 * This pattern should respect postgresql quoting on identifiers.
	 */
	final static private Pattern tablePattern = Pattern.compile("(\"?([^\"]+)\"?\\.)?\"?([^\"]+)\"?");
	
	Table() {}
	
	Table(String tableSchema, String tableName) {
		this.setTableName(tableName);
		this.setTableSchema(tableSchema);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Table other = (Table) obj;
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
	
	public String getTableName() {
		return tableName;
	}

	public String getTableSchema() {
		return tableSchema;
	}

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

	public boolean isValid() {
		return (this.tableName != null && this.tableSchema != null);
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setTableSchema(String tableSchema) {
		if (tableSchema != null) {
			this.tableSchema = tableSchema;
		} else {
			this.tableSchema = "public";
		}
	}
}
