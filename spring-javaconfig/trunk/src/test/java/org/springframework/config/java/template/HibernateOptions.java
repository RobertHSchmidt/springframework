package org.springframework.config.java.template;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface HibernateOptions {

	boolean showSql() default false;

	// boolean createAndDropSchema() default false;

	String propertiesLocation();

	String[] configLocations() default {};

	Class<?>[] configClasses();

}
