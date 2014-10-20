package de.geops.geoserver.documentor;

import java.util.List;

import de.geops.geoserver.documentor.info.SqlViewDoc;
import de.geops.geoserver.documentor.info.SqlViewParameterDoc;

class SqlViewQueryGenerator {

	final private SqlViewDoc sqlViewDoc;
	
	SqlViewQueryGenerator(SqlViewDoc sqlViewDoc) {
		this.sqlViewDoc = sqlViewDoc;
	}
	
	/**
	 * Return the sql query with the default parameters filled in
	 * 
	 * @return String
	 */
	String getParameterizedQuery() {
		String query = this.sqlViewDoc.getSqlQuery();
		List<SqlViewParameterDoc> params = this.sqlViewDoc.getParameters(); 
		if (query != null && params != null) {
			for (SqlViewParameterDoc param : params) {
				String defaultValue = param.getDefaultValue();
				String name = param.getName();
				if (defaultValue != null && name != null) {
					query = query.replace("%"+name+"%", defaultValue);
				}
			}
		} 
		return query;
	}

}
