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

package org.springframework.config.java.valuesource;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * @author Rod Johnson
 * 
 */
public abstract class AbstractStringBasedValueSource implements ValueSource {

	private BeanWrapper beanWrapperImpl = new BeanWrapperImpl();

	@SuppressWarnings("unchecked")
	public <T> T resolve(String name, Class<?> requiredType) throws ValueResolutionException {
		String rawValue = getString(name);

		// TODO might want to allow Spring type converters to be added
		return (T) beanWrapperImpl.convertIfNecessary(rawValue, requiredType);
	}

	public abstract String getString(String name) throws ValueResolutionException;

}
