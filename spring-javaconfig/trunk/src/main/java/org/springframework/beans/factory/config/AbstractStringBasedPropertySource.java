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

/**
 * @author Rod Johnson
 *
 */
public abstract class AbstractStringBasedPropertySource implements PropertySource {

	public int getInt(String name) throws PropertyDefinitionException {
		return (Integer) getObject(name, Integer.class);
	}

	public abstract String getString(String name) throws PropertyDefinitionException;

	public Object getObject(String name, Class requiredType) throws PropertyDefinitionException {
		String rawValue = getString(name);		
		// TODO do proper type conversion using Spring property editors, not this horrible hack
		if (requiredType == String.class) {
			return rawValue;
		}
		if (requiredType == Integer.class) {
			return Integer.parseInt(rawValue);
		}
		throw new UnsupportedOperationException("Property conversion not yet implemented");
	}


	

}
