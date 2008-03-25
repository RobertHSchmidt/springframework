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
package org.springframework.config.java.core;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.valuesource.ValueResolutionException;

public class AutoBeanMethodProcessor extends AbstractBeanMethodProcessor {

	protected AutoBeanMethodProcessor() {
		super(AutoBean.class);
	}

	public Object processMethod(Method m) throws ValueResolutionException {
		throw new UnsupportedOperationException("not implemented");
	}

	public static boolean isAutoBeanCreationMethod(Method candidateMethod) {
		return new AutoBeanMethodProcessor().understands(candidateMethod);
	}

	/**
	 * Find all methods that are annotated with {@link AutoBean}.
	 * 
	 * @param configurationClass
	 * @return collection of all methods annotated with {@link AutoBean}
	 */
	public static Collection<Method> findAutoBeanCreationMethods(Class<?> configurationClass) {
		return new AutoBeanMethodProcessor().findMatchingMethods(configurationClass);
	}

}
