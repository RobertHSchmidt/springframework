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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;

/**
 * A simple, immutable parameter object that holds information used to assist
 * with the construction of a flow artifact such as a Flow or a State
 * definition. Used only during flow construction/configuration time.
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
	private Map properties = Collections.EMPTY_MAP;

	/**
	 * Creates a parameters value object containing the specified id and an
	 * empty properties map.
	 * @param id the flow Id
	 */
	public FlowArtifactParameters(String id) {
		this.id = id;
	}

	/**
	 * Creates a parameters value object containing the specified id and an
	 * empty properties map.
	 * @param id the flow Id
	 */
	public FlowArtifactParameters(String id, Map properties) {
		this.id = id;
		if (properties != null) {
			this.properties = Collections.unmodifiableMap(properties);
		}
	}

	/**
	 * Returns the id parameter.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the properties map.
	 */
	public Map getProperties() {
		return properties;
	}

	/**
	 * Creates a copy of this flow artifact parameter object that also includes
	 * properties defined in the provided map. This method first adds the
	 * provided properties to the copy, and then applies the properties of this
	 * (original) object in 'override' fashion.
	 * @param properties the properties to apply and then override
	 * @return the artifact parameters
	 */
	public FlowArtifactParameters applyAndOverride(Map properties) {
		if (properties != null) {
			Map copyProperties = new HashMap(properties);
			copyProperties.putAll(getProperties());
			return new FlowArtifactParameters(getId(), copyProperties);
		} else {
			return new FlowArtifactParameters(getId(), getProperties());
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("id", getId()).append("properties", properties).toString();
	}
}