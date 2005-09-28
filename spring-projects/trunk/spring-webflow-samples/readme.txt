/*
 * spring-webflow-samples
 *
 * birthdate - demonstrates Struts integration and the MultiAction
 * fileupload - demonstrates multipart file upload with webflow
 * flowlauncher - demonstrates the different ways to launch flows from web pages
 * itemlist - demonstrates application transaction tokens and expired flow cleanup
 * numberguess - demonstrates how to play a game with spring web flow
 * phonebook - central sample demonstrating most webflow features
 * sellitem - demonstrates a wizard with conditional transitions, continuations, and jmx-enabled statistics
 */

Sample pre-requisites:
----------------------
* JDK 1.4+ must be installed with the JAVA_HOME variable set

* Ant 1.6 must be installed and in your system path

* A Servlet 2.4/JSP 2.0-capable servlet container (e.g. tomcat 5.5) must be installed for sample app deployment


To build each sample:
---------------------
1. cd to the sample root directory

2. run 'ant dist' to produce a deployable .war file


Assistance with .war deployment in tomcat:
------------------------------------------
* If you have tomcat installed on your system, you may create a build.properties file in your user home directory that defines a tomcat.dir property pointing to your installation.

* Once tomcat.dir is set, run 'ant tomcat-launch' for the sample app you wish to deploy.  This will launch tomcat from ant.  Note this target tries to start tomcat via the standard %TOMCAT_DIR%/bin/startup.bat file.