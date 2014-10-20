package de.geops.geoserver.documentor;

import de.geops.geoserver.documentor.directive.DirectiveParser;
import de.geops.geoserver.documentor.directive.Directive;
import junit.framework.TestCase;

public class DirectiveParserTest extends TestCase {

	
	public void testGetClearedInput() {
		
		String input = "u [@documentor include the-whole-wide-world]i gseh win i ungergah [@documentor include and-everything-else]\n";
		String expectedOutput = "u i gseh win i ungergah \n";
		
		DirectiveParser dp = new DirectiveParser(input);
		TestCase.assertEquals(expectedOutput, dp.getClearedInput());
	} 
	
	public void testGetDirectives() {
		
		String input = "D'w. Nuss vo Bümpliz geit dür d'Strass \n"
				+"liecht u flüchtig, wie nes gas \n"
				+"so unerreichbar höch \n"
				+" \n"
				+"Bockstössigi Himbeerbuebe \n"
				+"schüüch u brav wie Schaf \n"
				+"schön fönfriesert \n"
				+"chöme tubetänzig nöch \n"
				+" \n"
				+"U d'Spargle wachse i bluetjung Morge \n"
				+"d'Sunne chunnt 's wird langsam war \n"
				+" \n"
				+"Sie het meh als hunderching \n"
				+"u jede Früehlig git 's es nöis \n"
				+"het d'Chiuchefänschterouge off \n"
				+"u macht se zue bi jedem Kuss \n"
				+"u we sie lachet wärde Bärge zu schtoub \n"
				+"u jedes zäihe Läderhärz wird weich \n"
				+" \n"
				+"D'w. Nuss vo bümpliz \n"
				+"isch schön win es Füür i dr Nacht \n"
				+"win e Rose im Schnee \n"
				+"we se gseh duss in Bümpliz \n"
				+"de schlat mir mys Härz hert i Hals \n"
				+"u i gseh win i ungergah [@documentor ignore ]\n"
				+" \n"
				+"Siw wohnt im ne Huus us Glas \n"
				+"hinger Türe ohni Schloss \n"
				+"gseht dür jedi Muur \n"
				+"dänkt wi nes Füürwärch \n"
				+"win e Zuckerstock \n"
				+"läbt win e Wasserfrau \n"
				+"für sie git's nüt, wo's nid git \n"
				+"u aus wo's git, git's nid für ging \n"
				+"sie nimmt's wie's chunnt u lat's la gah \n"
				+" \n"
				+"D'w. Nuss vo bümpliz \n"
				+"isch schön [@documentor include and-everything-else] win es Füür i dr Nacht \n"
				+"win e Rose im Schnee \n"
				+"we se gseh duss in Bümpliz \n"
				+"de schlat mir mys Härz hert i Hals \n"
				+"u i gseh win i ungergah";
		
		DirectiveParser dp = new DirectiveParser(input);
		for (Directive d: dp.getDirectives()) {
			System.out.println(d.toString());
		};
		
		TestCase.assertTrue("Entity is marked to be ignored", dp.ignoreThisEntity());
		//fail("Not yet implemented");
	}

}
