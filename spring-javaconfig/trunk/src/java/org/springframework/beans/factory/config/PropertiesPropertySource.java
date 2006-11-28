/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.Properties;

/**
 * @author Rod Johnson
 *
 */
public class PropertiesPropertySource extends AbstractStringBasedPropertySource {
	
	private Properties properties;
	
	/**
	 * @param properties
	 */
	public PropertiesPropertySource(Properties properties) {
		this.properties = properties;
	}

	@Override
	public String getString(String name) throws PropertyDefinitionException {
		String value = properties.getProperty(name);
		if (value == null) {
			throw new PropertyDefinitionException(name, "No definition in properties file");
		}
		return value;
	}

	

}
