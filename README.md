Introduction
============

Geoserver extension to provide a REST interface (XML)for extracting info for documentation generation.
This extension is currently tested with Geoserver 2.4.8 and 2.6.5. Contributions to make this extension compatible
with more recent versions of Geoserver are very welcome.

Installation
============

Building the extension
----------------------

Run

    make build
    
The generated JARs will be in the `target/` directory.


Installing into Geoserver
-------------------------

To install this extension, follow these steps:
* Drop all JARs of the `target` directory to geoservers `WEB-INF/lib/` directory. The `target` directory will also contain the dependencies which are not already bundled with geoserver.
* Restart geoserver.
* Congratulations, you're done.


Usage
=====

An easy example would be a query to the `/complete` route.

`curl -u admin:geoserver http://hostname:8080/geoserver/rest/documentor/complete.xml`

The documentor respects the permissions of the credentials that are supplied via the request.
It is advised to create a seperate account for the documentor and grant only the permission to ressources you want to
be able to query.


Documenting Postgresql relations
--------------------------------

Documentor documents the relations used by a layer by default. This also includes for example all tables referenced by a view when geoserver uses a view as its datasource.

Syntax:

     
	  Directives:
	  
	    * ignore
	      ignore the current entity
	    
	    * include-ref [reference]
	      Also load the documentation for the referenced entity
	      
	    * ignore-ref [reference]
	      Ignore the reference to the specified entity
	 
	      
	  References:
	  
	  The syntax to reference tables is
	  
	      table:[table schema].[table name]
	 
	  Quoted identifiers are allowed.
	 
	 
These directives so far are supported in comments on Postgresql tables and views.


Known issues
============


Fetching the Webservice in JSON-Format fails
--------------------------------------------

	2014-11-05 11:48:05,091 ERROR [geoserver.rest] - Exception intercepted
	java.util.EmptyStackException
	    at org.codehaus.jettison.util.FastStack.peek(FastStack.java:39)
	    at org.codehaus.jettison.mapped.MappedXMLStreamWriter.setNewValue(MappedXMLStreamWriter.ja
	    at org.codehaus.jettison.mapped.MappedXMLStreamWriter.writeCharacters(MappedXMLStreamWrite
	    at com.thoughtworks.xstream.io.xml.StaxWriter.setValue(StaxWriter.java:158)
	    at com.thoughtworks.xstream.converters.SingleValueConverterWrapper.marshal(SingleValueConv
	    at com.thoughtworks.xstream.core.TreeMarshaller.convert(TreeMarshaller.java:70)
	    at com.thoughtworks.xstream.core.TreeMarshaller.convertAnother(TreeMarshaller.java:58)
	    at com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter.marshallFiel
	    at com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter$2.writeField
	    at com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter$2.<init>(Abs
	    at com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter.doMarshal(Ab
	    at com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter.marshal(Abst
	    at com.thoughtworks.xstream.core.TreeMarshaller.convert(TreeMarshaller.java:70)
	    at com.thoughtworks.xstream.core.TreeMarshaller.convertAnother(TreeMarshaller.java:58)
	    at com.thoughtworks.xstream.core.TreeMarshaller.convertAnother(TreeMarshaller.java:43)
	    at com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter.writeItem(A
	    at org.geoserver.config.util.XStreamPersister$ProxyCollectionConverter.writeItem(XStreamPe
	    at com.thoughtworks.xstream.converters.collections.CollectionConverter.marshal(CollectionC
	    [...]

This is an issue with jettison 1.0.x. An Upgrade of the jettision jar to 1.3.x fixes this issue, but jettison changed its object and array nesting in JSON format slightly between these versions. This will affect all of geoservers REST services. The better solution would be to just use the XML format.

See http://stackoverflow.com/questions/13804878/spring-batch-spring-batch-admin-project and https://jira.codehaus.org/browse/JETTISON-70

