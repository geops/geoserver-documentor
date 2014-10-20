package de.geops.geoserver.documentor.directive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.util.logging.Logging;

/**
 * implementation of the domain-specific description directives used to
 * reference entities to include in the documentation
 * 
 * Example:
 *    [@documentor include the-whole-wide-world] 
 *    
 *    
 * Directives:
 * 
 *   * ignore
 *     ignore the current entity
 *   
 *   * include-ref [reference]
 *     Also load the documentation for the referenced entity
 *     
 *   * ignore-ref [reference]
 *     Ignore the reference to the specified entity
 * 
 * @author nico
 *
 */
public class DirectiveParser {

	private static final Logger LOGGER = Logging.getLogger(DirectiveParser.class);

	public static String markerName = "documentor";
	
	final private String input;
	final private HashMap<String, ArrayList<Directive>> directives;
	
	private static final String DIRECTIVE_IGNORE = "ignore"; 
	
	
	// static private Pattern directivePattern = Pattern.compile("\\[\\s*@documentor\\s+([^\\]]+)\\]");
	static private Pattern directivePattern = Pattern.compile("\\[\\s*@"+DirectiveParser.markerName+"\\s+(.+?)\\]");

	public DirectiveParser(String input) {
		this.input = input;
		this.directives = loadDirectives();
	}

	/**
	 * return the input string with all directives being removed
	 * 
	 * @return
	 */
	public String getClearedInput() {
		return this.input.replaceAll(directivePattern.pattern(), "");
	}

	/**
	 * 
	 * @return
	 */
	public List<Directive> getDirectives() {
		ArrayList<Directive> directivesList = new ArrayList<Directive>();
		for (ArrayList<Directive> dList: directives.values()) {
			for (Directive d : dList) {
				directivesList.add(d);
			}
		}
		return directivesList;
	}
	
	/**
	 * 
	 * @return
	 */
	private HashMap<String, ArrayList<Directive>> loadDirectives() {
		HashMap<String, ArrayList<Directive>> directives = new HashMap<String, ArrayList<Directive>>();
		Matcher matcher = DirectiveParser.directivePattern.matcher(this.input);
		while (matcher.find()) {
			String fullDirectiveText = matcher.group(1);
			int spacePos = fullDirectiveText.indexOf(' ');
			Directive directive = new Directive();
			if (spacePos != -1) {
				directive.setName(fullDirectiveText.substring(0, spacePos));
				if (fullDirectiveText.length()>(spacePos+1)) {
					directive.setArgument(fullDirectiveText.substring(spacePos+1, fullDirectiveText.length()).trim());
				}
			} else {
				directive.setName(fullDirectiveText.trim());
			}
			
			if (!directives.containsKey(directive.getName())) {
				directives.put(directive.getName(), new ArrayList<Directive>());
			}
			directives.get(directive.getName()).add(directive);
		}
		return directives;
	}
	
	/**
	 * returns true if the current entity should be ignored
	 * 
	 * @return
	 */
	public boolean ignoreThisEntity() {
		return this.directives.containsKey(DIRECTIVE_IGNORE);
	}
}
