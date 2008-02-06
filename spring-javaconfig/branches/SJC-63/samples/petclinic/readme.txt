====================================================
== Spring PetClinic JavaConfig sample application ==
====================================================

@author Costin Leau


1. INTRODUCTION

This sample, modifies the well known Spring Petclinic application configuration from
XML to JavaConfig. For more information about Petclinic itself, please refer to the
Spring sample documentation.

The current version replaces the JDBC configuration from XML files with a Java class (JdbcConfiguration)
and some parts of the web tier configuration with a Groovy script.
Thus, the Spring container configuration is moved from XML to XML + Java + Groovy.
Note this samples tries to show as much as possible various capabilities of JavaConfig - simpler configurations
just in Java or just Groovy are possible.

The main entry points are WEB-INF/java-config-jdbc.xml and WEB-INF/petclinic-servlet.xml which replace some of the 
original Petclinic XML configuration with Java and Groovy respectively.

2. BUILD AND DEPLOYMENT

In order to build the petclinic-javaconfig sample, make sure you have:

1. Petclinic war from Spring distribution

inside samples/petclinic/dist folder and

a. Spring JavaConfig distribution jar
b. Groovy all 1.1-BETA-1 (or later) jar

inside samples/petclinic/lib folder.

Run "build.bat" in this directory for available targets (e.g. "build.bat dist",
"build.bat clean").

The Ant script will build the configuration files and create a copy of the petclinic war called
petclinic-javaconfig.war.
The war can be then deployed inside a Servlet container as usual, just like the normal petclinic.
Note that the war configuration is identical so all that applies to petclinic, applies to petclinic javaconfig too.