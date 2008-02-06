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

import static java.lang.String.format;

import java.util.Collection;

import org.springframework.beans.factory.BeanFactory;

/**
 * Exception representing an illegal configuration where more than one bean of
 * the same type is marked as
 * {@link org.springframework.config.java.annotation.Primary} within a given
 * {@link BeanFactory}.
 * 
 * @see org.springframework.config.java.annotation.Primary
 * 
 * @author Chris Beams
 */
@SuppressWarnings("serial")
class MultiplePrimaryBeanDefinitionException extends AmbiguousBeanLookupException {

	/**
	 * Create a new instance.
	 * @param type the type that was being searched for
	 * @param beanNames names of all beans marked as primary. Size of this array
	 * must be > 1
	 */
	public MultiplePrimaryBeanDefinitionException(Class<?> type, Collection<String> beanNames) {
		super(format("expected single bean of type [%s] but found %d: %s", type, beanNames.size(), beanNames));
	}

}
