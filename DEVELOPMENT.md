Development
===========

Project structure
-----------------

The main part of this project are the REST WebServices it registers in GeoServer. This 
registration is made in src/main/resources/applicationContext.xml which is probably the best place to
start investigating when you want to improve this extension.

The most part what this extension does is harvesting the contents of geoservers catalog and returning
that information in a structured way.

There is also the functionality to analyze PostgreSQL relations which is mostly implemented in 
the de.geops.geoserver.documentor.postgresql package. The syntax for the documentation directives mentioned
in the README.md file is implemented in the de.geops.geoserver.documentor.directive package.


Developing and Debugging using eclipse
--------------------------------------


### Loading the extension into GeoServer

* Enter the directory of the source of the RT and run `mvn eclipse:eclipse`.
* Load the extension into eclipse using the menu "File" -> "Import". There choose "General" -> "Existing projects into workspace".

After eclipse has rebuild its workspace Right-click the "web-app" project again and open "Properties" there you need to

* make sure the documentor project has been added to the "Java Build Path". Click "Add" to see a list of missing projects of the current workspace.
* make sure the project is also part of the "Project References".

GeoServer can now be launched by right-clicking org.geoserver.web.Start in the "web-app"-project (`src/test/java` directory) and selecting "Run As" -> "Java application". The menu of documentor should now show up in the "About & Status" section of the GUI when you are logged in.

Changes to the projects `pom.xml` file require a new run of `mvn eclipse:eclipse` and refreshing the project inside eclipse.


## Version information

The Jars build using `make` contain the git commit hash of the source. This can be viewed by opening the file `geoserver-gitbackedconfig.gitversion` bundled in the JAR.
