The following libraries are included in the Spring JavaConfig Framework distribution because they are
required either for building the framework or for running the sample apps. Note that each
of these libraries is subject to the respective license; check the respective project
distribution/website before using any of them in your own applications.

* aopalliance/aopalliance.jar
- AOP Alliance 1.0 (http://aopalliance.sourceforge.net)
- required for building the framework
- included in spring.jar and spring-aop.jar

* asm/asm*-2.2.3.jar
- ObjectWeb ASM 2.2.3 (http://asm.objectweb.org)
- required for building the framework
- required at runtime when using parameter name discovery with AspectJ

* aspectj/aspectjweaver.jar, aspectj/aspectjrt.jar, (aspectj/aspectjtools.jar)
- AspectJ 1.5.3 (http://www.aspectj.org)
- required for building the framework
- required at runtime when using Spring's AspectJ support

* cglib/cglib-nodep-2.1_3.jar
- CGLIB 2.1_3 with ObjectWeb ASM 1.5.3 (http://cglib.sourceforge.net)
- required for building the framework
- required at runtime when proxying full target classes via Spring AOP

* groovy/groovy-1.0.jar
- Groovy 1.0 final (http://groovy.codehaus.org)
- required for building the framework
- required at runtime when using Spring's Groovy support

* hibernate/hibernate3.jar
- Hibernate 3.2.3 (http://www.hibernate.org)
- required for building the framework
- required at runtime when using Spring's Hibernate 3.x support

* hsqldb/hsqldb.jar
- HSQLDB 1.8.0.1 (http://hsqldb.sourceforge.net)
- required for running JPetStore and PetClinic

* j2ee/servlet-api.jar
- Servlet API 2.4 (http://java.sun.com/products/servlet)
- required for building the framework
- required at runtime when using Spring's web support

* jakarta-commons/commons-dbcp.jar
- Commons DBCP 1.2.1 (http://jakarta.apache.org/commons/dbcp)
- required for building the framework
- required at runtime when using Spring's CommonsDbcpNativeJdbcExtractor
- required for running JPetStore

* jakarta-commons/commons-logging.jar
- Commons Logging 1.1 (http://jakarta.apache.org/commons/logging)
- required for building the framework
- required at runtime, as Spring uses it for all logging

* jakarta-commons/commons-pool.jar
- Commons Pool 1.3 (http://jakarta.apache.org/commons/pool)
- required for running JPetStore and Image Database (by Commons DBCP)

* junit/junit.jar
- JUnit 3.8.1 (http://www.junit.org)
- required for building the test suite

* log4j/log4j-1.2.14.jar
- Log4J 1.2.14 (http://logging.apache.org/log4j)
- required for building the framework
- required at runtime when using Spring's Log4jConfigurer