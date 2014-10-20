package de.geops.geoserver.documentor.directive;


public class Directive {
	private String name;

	private String argument;

	public Directive(String name, String argument) {
		super();
		this.setName(name);
		this.setArgument(argument);
	}
	
	public Directive(String name) {
		super();
		this.setName(name);
	}
	
	public Directive() {
		super();
	}
	
	public String getArgument() {
		return argument;
	}

	public String getName() {
		return name;
	}

	public void setArgument(String argument) {
		this.argument = argument;
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name.toLowerCase();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder()
			.append("[@")
			.append(DirectiveParser.markerName)
			.append(" ")
			.append(this.name);
		if (this.argument != null && !this.argument.equals("")) {
			sb.append(" ")
				.append(this.argument);
		}
		sb.append("]");
		return sb.toString();
	}
}