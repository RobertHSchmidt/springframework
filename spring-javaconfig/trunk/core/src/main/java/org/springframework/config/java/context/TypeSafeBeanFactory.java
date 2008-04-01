/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.config.java.context;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Provides type-safe {@link #getBean(Class) getBean} methods to allow retrieval
 * of beans from a context without casting or relying on fragile string-based
 * bean identifiers. Requires Java5 or better.
 * 
 * <p/> XXX: Review
 * 
 * @author Chris Beams
 */
interface TypeSafeBeanFactory extends BeanFactory {

	/**
	 * Return an instance of the given <var>type</var>. If multiple instances
	 * of the same type exist, instances are inspected to see if exactly one is
	 * marked as {@link org.springframework.config.java.annotation.Primary}
	 * 
	 * @see org.springframework.config.java.annotation.Primary
	 * @param type desired instance type
	 * @throws NoSuchBeanDefinitionException if no instance matches <var>type</var>
	 * @throws AmbiguousBeanLookupException if more than one instance is found
	 * and no single instance is marked as primary
	 * @throws MultiplePrimaryBeanDefinitionException if more than one instance
	 * is found and more than one instance is marked as primary
	 * @return instance matching <var>type</var>
	 */
	public <T> T getBean(Class<T> type);

	/**
	 * Return an instance named <var>beanName</var> and of type <var>type</var>.
	 * Useful in disambiguation cases where there is more than one bean of a
	 * given type within the factory and none is marked as
	 * {@link org.springframework.config.java.annotation.Primary}
	 * 
	 * <p/>This method is similar to its predecessor
	 * {@link BeanFactory#getBean(String, Class)}, but this variant takes
	 * advantages of generics and removes the casting burden from the caller.
	 * 
	 * @throws NoSuchBeanDefinitionException if <var>beanName</var> cannot be
	 * found
	 * @throws BeanNotOfRequiredTypeException if <var>beanName</var> is found
	 * but is not of type <var>type</var>
	 * @returns instance of <var>type</var> named <var>beanName</var>
	 */
	public <T> T getBean(Class<T> type, String beanName);
}
