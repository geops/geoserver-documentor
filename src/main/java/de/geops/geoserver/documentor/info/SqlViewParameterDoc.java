package de.geops.geoserver.documentor.info;

public class SqlViewParameterDoc {
	
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
