/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.config.java.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Annotation to be applied to methods that create beans in a Spring context.
 * The name of the bean is the method name. (It is also possible to specify
 * aliases using the aliases array on this annotation.)
 * <p>
 * Contains similar information to that held in Spring's internal BeanDefinition
 * metadata.
 * <p>
 * Bean creation methods must be non-private (default, public or protected).
 * Bean creation methods may throw any exception, which will be caught and
 * handled by the Spring container on processing of the configuration class.
 * <br>
 * Bean creation methods must return an object type. The decision to return a
 * class or an interface will be significant in the event of proxying. Bean
 * methods that return interfaces will be proxied using dynamic proxies; those
 * that return a class will require CGLIB or other subclass-based proxying. It
 * is recommended to return an interface where possible, as this is also
 * consistent with best practice around loose coupling.
 * <p>
 * Bean creation methods may reference other bean creation methods by calling
 * them directly, as follows. This ensures that references between beans are
 * strongly typed:
 * 
 * <pre class="code">
 * &#064;Bean
 * public Company springSource() {
 * 	Company company = new DefaultCompany(&quot;SpringSource&quot;);
 * 	company.setChiefTechnologyOfficer(adrian());
 * 	return company;
 * }
 * 
 * &#064;Bean
 * public Person adrian() {
 * 	return new Person(&quot;Adrian Colyer&quot;);
 * }
 * </pre>
 * 
 * <p/> If a bean creation method is protected, rather than public, the the bean
 * will be hidden. This means that the bean will be added to a child factory
 * used internally by the ConfigurationProcessor, rather than the main factory,
 * meaning it won't be visible to other definitions. This is particularly useful
 * for Spring AOP Advisors or AspectJ aspects, which might otherwise alter
 * behaviour of the owning factory as a whole.
 * 
 * <p/>TODO: consider providing a value() or name() attribute that, if set,
 * provides the name of the bean. This is useful, for example, in a web context
 * where the name of the bean may need to include slashes, etc. This could
 * currently be done through supplying a value to the aliases() array, but it is
 * more consistent with XML usage to provide a name() attribute that fully
 * overrides the method name, which in JavaConfig is equivalent to the 'id'
 * attribute of the bean element in XML. If this gets implemented, it will
 * likely affect the implementation of
 * {@link org.springframework.config.java.naming.MethodNameStrategy}
 * 
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 * 
 * @see Configuration
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

	/**
	 * Bean aliases.
	 */
	String[] aliases() default {};

	/**
	 * Scope: whether the bean is a singleton, prototype or custom scope.
	 * Default is singleton.
	 */
	String scope() default BeanDefinition.SCOPE_SINGLETON;

	/**
	 * Bean autowire strategy.
	 */
	Autowire autowire() default Autowire.INHERITED;

	/**
	 * Bean lazy strategy.
	 */
	Lazy lazy() default Lazy.UNSPECIFIED;

	/**
	 * A bean may be marked as primary, useful for disambiguation when looking
	 * up beans by type.
	 * 
	 * @see org.springframework.config.java.context.JavaConfigApplicationContext#getBean(Class);
	 */
	Primary primary() default Primary.UNSPECIFIED;

	/**
	 * Bean init method name. Normally this is not needed, as the initialization
	 * (with parameterization) can be done directly through java code.
	 */
	String initMethodName() default "";

	/**
	 * Bean destroy method name.
	 */
	String destroyMethodName() default "";

	/**
	 * Bean dependency check strategy.
	 */
	DependencyCheck dependencyCheck() default DependencyCheck.UNSPECIFIED;

	/**
	 * Beans on which the current bean depends on.
	 */
	String[] dependsOn() default {};

	/**
	 * Metadata for the current bean.
	 */
	Meta[] meta() default {};

	/**
	 * Allow the bean to be overridden in another JavaConfig, XML or other
	 * non-Java configuration. This is consistent with
	 * DefaultListableBeanFactory's allowBeanDefinitionOverriding property,
	 * which defaults to true.
	 * 
	 * @return whether overriding of this bean is allowed
	 */
	boolean allowOverriding() default true;

}
