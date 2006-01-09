SPRING WEB FLOW 1.0 M1 (January 2006)
-------------------------------
http://www.springframework.org
http://opensource.atlassian.com/confluence/spring/display/WEBFLOW/Home

1. INTRODUCTION

Spring Web Flow (SWF) is a core module of the Spring Framework focused on the definition
and orchestration of page flow within a web application.

The system allows you to capture logical page flows as self-contained modules that are
reusable in different situations. The system is ideal for web applications that guide
the user through controlled navigations that drive business processes.  These processes
typically span HTTP requests and may be dynamic in nature.

SWF exists at a higher-level of abstraction, integrating with existing frameworks like
Struts, Spring MVC, and JSF, capturing your application's page flow explicity in a
declarative fashion.  SWF is a very a powerful framework based on a finite-state
machine for the definition, execution, and management of a web conversation.

2. RELEASE INFO

Spring Web Flow requires J2SE 1.3 and J2EE 1.3 (Servlet 2.3, JSP 1.2).
J2SE 1.4 is required for building.

SWF release contents:

* "build-spring-webflow" contains the build system producing this distribution
* "common-build" contains a common, reusable build system based on Ant 1.6 and Ivy
* "ivys" contains Ivy artifact description files
* "repository" contains the master spring-project artifact (jar) repository
* "spring-binding" contains the Spring Data Binding project sources
* "spring-webflow" contains the Spring Web Flow project sources
* "spring-webflow-samples" contains the Spring Web Flow sample application sources

Spring Web Flow is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list
specifies the respective contents and third-party dependencies. Libraries in brackets are
optional, i.e. just necessary for certain functionality.

* spring-webflow-1.0-m1.jar
- Contents: The Spring Web Flow system
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-web, spring-binding,
                (Log4J, Commons Codec, OGNL, spring-webmvc, spring-mock, JUnit, Servlet, JMX, Struts)
              
* spring-binding-1.0-m1.jar
- Contents: The Spring Data Binding framework
- Dependencies: Commons Logging, spring-bean, spring-core, spring-context, (Log4J)

4. WHERE TO START

The distribution contains extensive JavaDoc documentation and several sample applications
illustrating different ways to use Spring Web Flow. The Spring Web Flow homepage can be
found at the following URL:

http://opensource.atlassian.com/confluence/spring/display/WEBFLOW/Home

There you will find resources such as a 'Quick Start' guide and a 'Frequently Asked Questions'
section.