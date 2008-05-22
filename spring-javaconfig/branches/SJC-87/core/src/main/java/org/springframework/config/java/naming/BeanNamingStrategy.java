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
package org.springframework.config.java.naming;

import java.lang.reflect.Method;

import org.springframework.config.java.model.ModelMethod;

/**
 * Strategy interface for constructing a bean name from the java method.
 *
 * @author Costin Leau
 */
public interface BeanNamingStrategy {

	/**
	 * Create the bean name based on the given method. If more contextual
	 * information is needed, consider using the Method API to get access to it
	 * or provide hooks inside the implementations.
	 *
	 * @param beanCreationMethod the method which creates the actual bean
	 * instance
	 * @return the bean name
	 */
	@Deprecated
	String getBeanName(Method beanCreationMethod);

	String getBeanName(ModelMethod beanMethod);
}
