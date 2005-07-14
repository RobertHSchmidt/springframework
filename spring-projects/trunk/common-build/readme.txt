Contained in this directory is the Spring Jumpstart common build system.

It is ant 1.6 based, and uses Ivy for dependency management.

Projects are expected to import master build files in this directory as needed for the 
targets they require.  As an example, here is Spring Web Flow's project build.xml:

<project name="spring-webflow" default="dist">

  <property file="build.properties"/>
  <property file="project.properties"/>
  <property file="${common.build.dir}/build.properties"/>
  <property file="${common.build.dir}/project.properties"/>
  <property file="${user.home}/build.properties"/>

  <property name="project.title" value="Spring Web Flow"/>
  <property name="project.package" value="org.springframework.webflow"/>
  
  <import file="${common.build.dir}/common-targets.xml"/>

  <import file="${common.build.dir}/clover-targets.xml"/>

</project>
