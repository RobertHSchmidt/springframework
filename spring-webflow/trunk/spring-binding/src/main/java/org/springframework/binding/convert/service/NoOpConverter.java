/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.binding.convert.service;

import org.springframework.binding.convert.Converter;

/**
 * Package private converter that is a "no op".
 * 
 * @author Keith Donald
 */
class NoOpConverter implements Converter {

	private Class sourceClass;

	private Class targetClass;

	/**
	 * Create a "no op" converter from given source to given target class.
	 */
	public NoOpConverter(Class sourceClass, Class targetClass) {
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
	}

	public Object convert(Object source, Class targetClass, Object context) throws Exception {
		return source;
	}

	public Class[] getSourceClasses() {
		return new Class[] { sourceClass };
	}

	public Class[] getTargetClasses() {
		return new Class[] { targetClass };
	}
}