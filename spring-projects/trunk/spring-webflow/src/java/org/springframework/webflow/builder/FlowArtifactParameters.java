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
package org.springframework.webflow.builder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;

/**
 * Simple parameter object that holds information used to assist with the
 * construction of a flow artifact such as a Flow or a State definition. Used
 * only during flow construction/configuration time.
 * 
 * @author Keith Donald
 */
public class FlowArtifactParameters implements Serializable {

	/**
	 * The flow artifact id.
	 */
	private String id;

	/**
	 * The flow artifact properties.
	 */
	private Map properties;

	/**
	 * Default constructor for bean-style usage.
	 * @see #setId(String)
	 * @see #setProperties(Map)
	 */
	public FlowArtifactParameters() {
		this.properties = new HashMap();
	}

	/**
	 * Creates a parameters value object containing the specified id and an
	 * empty properties map.
	 * @param id the flow Id
	 */
	public FlowArtifactParameters(String id) {
		setId(id);
		this.properties = new HashMap();
	}

	/**
	 * Creates a parameters value object containing the specified id and an
	 * empty properties map.
	 * @param id the flow Id
	 */
	public FlowArtifactParameters(String id, Map properties) {
		setId(id);
		setProperties(properties);
	}

	/**
	 * Returns the id parameter.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id parameter.
	 * @param id the id parameter.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the properties map.
	 */
	public Map getProperties() {
		return properties;
	}

	/**
	 * Sets the properties map.
	 * @param properties the properties map
	 */
	public void setProperties(Map properties) {
		this.properties = properties;
	}

	/**
	 * Puts properties in the provided map.
	 * @param properties the property map
	 */
	public void addProperties(Map properties) {
		if (properties != null) {
			this.properties.putAll(properties);
		}
	}

	/**
	 * Returns the property with the specified name.
	 */
	public Object getProperty(String name) {
		return properties.get(name);
	}

	/**
	 * Sets the property to the provided value.
	 */
	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

	public String toString() {
		return new ToStringCreator(this).append("id", getId()).append("properties", properties).toString();
	}
}