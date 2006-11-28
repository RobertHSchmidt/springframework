/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowire;

/**
 * Annotation for a configuration class.
 * This annotation  is not required, however its use is
 * recommended to improve configuration processing performance.
 * Holds similar information to that held in the 
 * default values of a bean factory. Note however that the
 * information here is not used to populate the defaults
 * of the owning bean factory, which would affect other
 * configurations. In the style of the Java configuration
 * mechanism generally, each Java configuration class
 * is kept isolated.
 * 
 * @author Rod Johnson
 * 
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Configuration {
	
	/**
	 * TODO idea is to allow different variants, such as test, production etc.
	 * Default will always match
	 * @return
	 */
	String[] names() default "";
	
	Autowire defaultAutowire() default Autowire.INHERITED;
	
	DependencyCheck defaultDependencyCheck() default DependencyCheck.UNSPECIFIED;
	
	Lazy defaultLazy() default Lazy.UNSPECIFIED;
	
	/**
	 * Do we autowire with aspects from the enclosing factory scope?
	 */
	boolean useFactoryAspects() default false;
	
}
