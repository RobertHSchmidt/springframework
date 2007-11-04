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

import org.springframework.context.MessageSource;

/**
 * @author Rod Johnson
 * 
 */
public class MessageSourcePropertiesSource extends AbstractStringBasedPropertySource {

	private final MessageSource messageSource;

	/**
	 * @param properties
	 */
	public MessageSourcePropertiesSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String getString(String name) throws PropertyDefinitionException {
		// TODO what to do with locale?
		String value = messageSource.getMessage(name, null, null);
		if (value == null) {
			throw new PropertyDefinitionException(name, "No definition in properties file");
		}
		return value;
	}

}
