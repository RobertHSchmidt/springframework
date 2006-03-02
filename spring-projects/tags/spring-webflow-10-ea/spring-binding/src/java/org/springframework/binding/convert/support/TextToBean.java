/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.convert.support;

import java.util.Map;

import org.springframework.beans.factory.BeanFactory;

/**
 * Converter that converts a bean name to a bean instance.
 * 
 * @author Keith Donald
 */
public class TextToBean extends AbstractConverter {

	private BeanFactory beanFactory;

	public TextToBean(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Object.class };
	}

	protected Object doConvert(Object source, Class targetClass, Map context) throws Exception {
		return beanFactory.getBean((String)source);
	}
}