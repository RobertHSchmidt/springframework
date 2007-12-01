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
package org.springframework.beans.factory;

/**
 * Makes obsolete {@link BeanFactory#getBean(String, Class)}
 * 
 * <p/> XXX: Review and Document
 * 
 * @author Chris Beams
 */
public interface TypeSafeBeanFactory {

	/**
	 * Return an instance of the given <var>type</var>. If multiple instances
	 * of the same type exist, instances are inspected to see if exactly one is
	 * marked as 'primary'.
	 * 
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
	 * 
	 * This method is similar to its predecessor
	 * {@link BeanFactory#getBean(String, Class)}, but this variant takes
	 * advantages of generics and removes the casting burden from the caller.
	 * 
	 * @throws NoSuchBeanDefinitionException if <var>beanName</var> cannot be
	 * found
	 * @throws BeanNotOfRequiredTypeException if <var>beanName</var> is found
	 * but is not of type <var>type</var>
	 */
	public <T> T getBean(Class<T> type, String beanName);
}
