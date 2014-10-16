package de.geops.geoserver.documentor.info;

import java.util.List;

public class SqlViewDoc {


	protected String sqlQuery;
	protected List<SqlViewParameterDoc> parameters;

	public List<SqlViewParameterDoc> getParameters() {
		return parameters;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setParameters(List<SqlViewParameterDoc> parameters) {
		this.parameters = parameters;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

}
