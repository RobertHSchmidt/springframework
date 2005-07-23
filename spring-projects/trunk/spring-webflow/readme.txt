SPRING WEB FLOW PR5 (July 2005)
-------------------------------
http://www.springframework.org
http://opensource.atlassian.com/confluence/spring/display/WEBFLOW/Home

1. INTRODUCTION

Spring Web Flow is a core module of the Spring Framework that is a strong fit for web applications with
demanding page flow requirements.
The system lets you capture a logical page flow as a self-contained module that can be reused in different
situations in a consistent fashion. The system is ideal for web applications that execute business
processes over a series of steps spanning HTTP requests, and those demanding wizards, dynamic flows,
and/or flows whose executions need to be audited or annotated. The system exists at a higher-level of
abstraction, building on base Model 2 frameworks like Struts and Spring MVC, capturing your application's
page flow explicity in a declarative fashion.

2. RELEASE INFO

Spring Web Flow requires J2SE 1.3 and J2EE 1.3 (Servlet 2.3, JSP 1.2). J2SE 1.4 is required for building.

Release contents:
* "build-spring-webflow" contains the build system producing this distribution
* "common-build" contains a common, reusable build system based on Ant and Ivy
* "ivys" contains Ivy artifact description files
* "repository" contains the Ivy artifact (jar) repository
* "spring-binding" contains a snapshot of the Spring Data Binding project
* "spring-webflow" contains a snapshot of the Spring Web Flow project
* "spring-webflow-samples" contains a snapshot of all the Spring Web Flow samples

Spring Web Flow is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are inluded in the distribution. The following list specifies the
respective contents and third-party dependencies. Libraries in brackets are optional, i.e. just necessary
for certain functionality.

* spring-webflow-XXX.jar
- Contents: The Spring Web Flow system
- Dependencies: Commons Logging, spring-beans, spring-core, spring-context, spring-web, spring-binding,
                (Log4J, Commons Codec, OGNL, spring-webmvc, spring-mock, JUnit, Servlet, JMX, Struts)

* spring-binding-XXX.jar
- Contents: The Spring Data Binding framework
- Dependencies: Commons Logging, spring-bean, spring-core, spring-context, (Log4J)

4. WHERE TO START

The distribution contains extensive JavaDoc documentation and several sample applications illustrating
different ways to use Spring Web Flow. The Spring Web Flow homepage can be found at the following URL:

http://opensource.atlassian.com/confluence/spring/display/WEBFLOW/Home

It offers a lot of usefull resources like a 'Quick Start' guide and a 'Frequently Asked Questions'
section.
