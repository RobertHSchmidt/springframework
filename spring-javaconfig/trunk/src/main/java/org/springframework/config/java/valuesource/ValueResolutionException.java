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

package org.springframework.config.java.valuesource;

import org.springframework.beans.factory.BeanDefinitionStoreException;

/**
 * @author Rod Johnson
 * 
 */
public class ValueResolutionException extends BeanDefinitionStoreException {

	private static final long serialVersionUID = 1L;

	private final String name;

	/**
	 * @param msg
	 * @param ex
	 */
	public ValueResolutionException(String name, String message) {
		super(message);
		this.name = name;
	}

	public ValueResolutionException(String name, Throwable t) {
		super(name, t);
		this.name = name;
	}

	public String getValueName() {
		return this.name;
	}

}
