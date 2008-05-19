SPRING WEB SERVICES 1.5.2 (May 2008)
-------------------------------
http://www.springframework.org/spring-ws
http://forum.springframework.org/forumdisplay.php?f=39

1. INTRODUCTION

Spring Web Services (Spring-WS) is a product of the Spring community focused on creating document-driven Web services.
Spring-WS aims to facilitate contract-first SOAP service devel1.5.2opment, allowing for the creation of flexible web services
using one of the many ways to manipulate XML payloads.

Spring-WS consists of two major modules: a flexible Object/XML Mapping abstraction with support for JAXB 1 and 2,
XMLBeans, Castor, JiBX and XStream; and a Web service framework that resembles Spring MVC.

2. RELEASE INFO

Spring-WS requires J2SE 1.4 or higher and J2EE 1.4 or higher.  J2SE 1.6 is required for building.

Release contents:

"." contains Spring-WS distribution units (jars and source zip archives), readme, and copyright
"dist" contains the Spring-WS distribution
"dist/modules" contains the Spring-WS modules

The -with-dependencies distribution contains the following additional content:

"dist/module-sources" contains the Spring-WS modules
"docs" contains the Spring-WS reference manual and API Javadocs
"samples" contains buildable Spring-WS sample application sources
"lib" contains the Spring-WS dependencies

See the readme.txt within the above directories for additional information.

Spring-WS is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list specifies the respective contents and
third-party dependencies. Libraries in [brackets] are optional, i.e. just necessary for certain functionality. For an 
exact list of Spring-WS project dependencies see the respective Maven2 pom.xml files.

* spring-oxm-1.5.2.jar
- Contents: The Spring Object/XML Mapping framework
- Dependencies: Commons Logging, spring-beans, spring-core
                [Log4J, JAXB 1, Castor, XMLBeans, StAX, JiBX, XStream]

* spring-oxm-tiger-1.5.2.jar
- Contents: The Spring Object/XML Mapping framework for Java 5
- Dependencies: Commons Logging, spring-beans, spring-core, JAXB 2

* spring-ws-core-1.5.2.jar
- Contents: The Spring-WS Core
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-oxm
                [Log4J, spring-web, spring-webmvc, SAAJ, JDOM, StAX, Servlet API, JAF, Axiom, DOM4J, XOM, WSDL4J]

* spring-ws-core-tiger-1.5.2.jar
- Contents: The Spring-WS Core for Java 5
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-core

* spring-ws-support-1.5.2.jar
- Contents: The Spring-WS Support
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-core
                [JMS, JavaMail]

* spring-ws-security-1.5.2.jar
- Contents: Spring-WS Security integration
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-ws-core
                [Log4J, xmlsdig, xmlsec, XWS-security, Acegi, WSS4J]

* spring-xml-1.5.2.jar
- Contents: Spring XML utility framework
- Dependencies: Commons Logging, spring-beans, spring-core
                [StAX, Xalan, Jaxen]

* spring-ws-1.5.2.jar
- Contents: Convenient al-in-one jar containing all of the jars described above


4. WHERE TO START

This distribution contains documentation and two sample applications illustrating the features of Spring-WS.

A great way to get started is to review and run the sample applications, supplementing with reference manual
material as needed. You will require Maven 2, which can be downloaded from http://maven.apache.org/, for building
Spring-WS. To build deployable .war files for all samples, simply access the "samples" directory and
execute the "mvn package" command, or run "mvn jetty:run" to run the samples directly in a Jetty 6 Web container.

More information on deploying Spring-WS sample applications can be found at:
	samples/readme.txt

5. ADDITIONAL RESOURCES

The Spring-WS homepage is located at:

    http://www.springframework.org/spring-ws

Spring-WS support forums are located at:

    http://forum.springframework.org/forumdisplay.php?f=39

The Spring Framework portal is located at:

	http://www.springframework.org
