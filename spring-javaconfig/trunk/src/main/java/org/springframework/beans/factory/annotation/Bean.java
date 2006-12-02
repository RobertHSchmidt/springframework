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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowire;

/**
 * Annotation to be applied to methods that
 * create beans in a Spring context. The name of the bean
 * is the method name. (It is also possible to specify aliases
 * using the aliases array on this annotation.) 
 * <p>Contains similar information to that held in Spring's
 * internal BeanDefinition metadata.
 * <p>
 * Bean creation methods must be public or protected. Bean creation methods may throw any exception,
 * which will be caught and handled by the Spring container on processing
 * of the configuration class. 
 * <br>
 * Bean creation methods must return an object type. The decision to return a
 * class or an interface will be significant in the event of proxying. Bean
 * methods that return interfaces will be proxied using dynamic proxies; those that
 * return a class will require CGLIB or other subclass-based proxying. Return an
 * interface as possible, as this is also consistent with best practice around loose
 * coupling.
 * <p>Bean creation methods may reference other bean creation methods by calling them directly,
 * as follows. This ensures that references between beans are strongly typed:
 * <code>
 * @Bean
 * public Company interface21() {
 * 	    Company company = new DefaultCompany("Interface21");
 *      company.setChiefScientist(adrian());
 *      return company;
 * }
 * 
 * @Bean
 * public Person adrian() {
 * 	     return new Person("Adrian Colyer");
 * }
 * </code>
 * <p>
 * If a bean creation method is protected, rather than public, the
 * the bean will be hidden.  This means that the bean will be added to a child factory 
 * used internally by the ConfigurationProcessor,
 * rather than the main factory, meaning it won't be visible to other definitions.
 * This is particularly useful for Spring AOP Advisors or AspectJ aspects,
 * which might otherwise alter behaviour of the owning factory as a whole.
 * @author Rod Johnson
 * 
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see org.springframework.beans.factory.annotation.Configuration
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
	
	String[] aliases() default {};
	
	/**
	 * Scope: whether the bean is a singleton, prototype or
	 * conversational, managed scope. 
	 */
	Scope scope() default Scope.SINGLETON;

	Autowire autowire() default Autowire.INHERITED;
	
	Lazy lazy() default Lazy.UNSPECIFIED;
	
	boolean noproxy() default false;
	
	// TODO do we need this?
	String initMethodName() default "";
	
	String destroyMethodName() default "";
	
	DependencyCheck dependencyCheck() default DependencyCheck.UNSPECIFIED;
	
	String[] dependsOn() default {};

	Meta[] meta() default {};
	
	/**
	 * Allow the bean to be overriden in XML or other non-Java configuration
	 * @return whether overriding of this bean is allowed
	 */
	boolean allowOverriding() default false;

}
