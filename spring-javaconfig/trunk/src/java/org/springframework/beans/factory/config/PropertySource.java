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
 * Source of strongly typed property values
 * @author Rod Johnson
 */
public interface PropertySource {
	
	int getInt(String name) throws PropertyDefinitionException;
	
	String getString(String name) throws PropertyDefinitionException;
	
	/**
	 * 
	 * @param name
	 * @param requiredType
	 * @return
	 * @throws PropertyDefinitionException if the property is not in the required
	 * format or the value is not found
	 */
	// TODO parameterize
	Object getObject(String name, Class requiredType) throws PropertyDefinitionException;

}
