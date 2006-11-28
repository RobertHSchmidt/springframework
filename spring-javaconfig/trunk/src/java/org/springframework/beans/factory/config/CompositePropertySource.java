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

import java.util.LinkedList;
import java.util.List;

/**
 * @author Rod Johnson
 *
 */
public class CompositePropertySource extends AbstractPropertySource {
	
	private List<PropertySource> propertySources = new LinkedList<PropertySource>();
	
	public void add(PropertySource ps) {
		this.propertySources.add(ps);
	}

	public Object getObject(String name, Class requiredType) {
		for (PropertySource ps : propertySources) {
			try {
				return ps.getObject(name, requiredType);
			}
			catch (PropertyDefinitionException ex) {
				// Keep going to next property source
			}
		}
		// If we get here, we didn't find a definition in any property source
		throw new PropertyDefinitionException(name, "Cannot resolve property");
	}

}
