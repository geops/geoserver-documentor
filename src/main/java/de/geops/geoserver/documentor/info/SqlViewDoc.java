package de.geops.geoserver.documentor.info;

import java.util.List;

public class SqlViewDoc {

	public class Parameter {
		protected String name;
		protected String defaultValue;
		protected String validator;

		public String getDefaultValue() {
			return defaultValue;
		}

		public String getName() {
			return name;
		}

		public String getValidator() {
			return validator;
		}

		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setValidator(String validator) {
			this.validator = validator;
		}
	}

	protected String sqlQuery;
	protected List<Parameter> parameters;

	public List<Parameter> getParameters() {
		return parameters;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

}
