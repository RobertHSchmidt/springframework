package org.springframework.config.java.annotation.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Aspects {

	/**
	 * imported AspectJ {@link Aspect @Aspect} classes
	 */
	Class<?>[] value();

}
