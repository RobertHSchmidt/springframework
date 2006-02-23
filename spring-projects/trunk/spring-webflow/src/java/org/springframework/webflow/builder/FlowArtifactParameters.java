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

import org.springframework.binding.map.AttributeCollection;
import org.springframework.binding.map.AttributeMap;
import org.springframework.binding.map.EmptyAttributeCollection;
import org.springframework.binding.map.UnmodifiableAttributeMap;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

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
	private UnmodifiableAttributeMap attributes;

	/**
	 * Creates a parameters value object containing the specified id and an
	 * empty properties map.
	 * @param id the flow Id
	 */
	public FlowArtifactParameters(String id) {
		this(id, null);
	}

	/**
	 * Creates a parameters value object containing the specified id and an
	 * empty properties map.
	 * @param id the flow Id
	 */
	public FlowArtifactParameters(String id, AttributeCollection attributes) {
		Assert.hasText(id, "The id parameter is required");
		this.id = id;
		if (attributes == null) {
			attributes = EmptyAttributeCollection.INSTANCE;
		}
		this.attributes = attributes.unmodifiable();
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
	public UnmodifiableAttributeMap getAttributes() {
		return attributes;
	}

	/**
	 * Creates a copy of this flow artifact parameter object that also includes
	 * properties defined in the provided map. This method first adds the
	 * provided properties to the copy, and then applies the properties of this
	 * (original) object in 'override' fashion.
	 * @param properties the properties to apply and then override
	 * @return the artifact parameters
	 */
	public FlowArtifactParameters putAll(AttributeCollection attributes) {
		if (attributes != null) {
			return new FlowArtifactParameters(getId(), new AttributeMap(getAttributes()).putAll(attributes));
		}
		else {
			return new FlowArtifactParameters(getId(), getAttributes());
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("id", getId()).append("attributes", attributes).toString();
	}
}