LDAPTEMPLATE 1.0.1 (May 2006)
-----------------------------
http://ldaptemplate.sourceforge.net

1. INTRODUCTION

LdapTemplate is a framework to simplify LDAP programming in Java, built on the same
principles as Spring Jdbc. 

The LdapTemplate class encapsulates all the plumbing work involved in traditional LDAP 
programming, such as creating, looping through NamingEnumerations, handling Exceptions
and cleaning up resources. This leaves the programmer to handle the important stuff - 
where to find data (DNs and Filters) and what do do with it (map to and from domain 
objects, bind, modify, unbind, etc.), in the same way that JdbcTemplate releives the 
programmer of all but the actual SQL and how the data maps to the domain model.

In addition to this, LdapTemplate provides Exception translation from NamingExceptions
to DataAccessExceptions, as well as several utilities for working with filters, LDAP
paths and Attributes.

2. RELEASE INFO

LdapTemplate requires J2SE 1.3. J2SE 1.4 is required for building.
J2EE 1.4 (Servlet 2.3, JSP 1.2) is required for running the example.

LdapTemplate release contents:

* "build-ldaptemplate" contains the build system producing this distribution
* "common-build" contains a common, reusable build system based on Ant 1.6 and Ivy
* "doc" contains JavaDoc API documentation
* "repository" contains the master ldaptemplate artifact (jar) repository
* "ldaptemplate" contains the LdapTemplate project sources
* "ldaptemplate-person" contains the LdapTemplate sample application sources
* "ldaptemplate-article" contains the sample source code for an article on java.net

LdapTemplate is released under the terms of the Apache Software License (see license.txt).

3. DISTRIBUTION JAR FILES

The following distinct jar files are included in the distribution. This list
specifies the respective contents and third-party dependencies.

* ldaptemplate-1.0.1.jar
- Contents: The LdapTemplate system
- Dependencies: Commons Logging, Commons Lang, Commons Collections, spring-beans,
                spring-core, spring-context, spring-dao

4. WHERE TO START

The distribution contains extensive JavaDoc documentation and a sample application
illustrating different ways to use LdapTemplate. The LdapTemplate homepage can be
found at the following URL:

http://ldaptemplate.sourceforge.net

There you will find resources related to the project.
