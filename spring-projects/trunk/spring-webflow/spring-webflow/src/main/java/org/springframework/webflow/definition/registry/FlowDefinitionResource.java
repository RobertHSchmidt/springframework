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
package org.springframework.webflow.definition.registry;

import java.io.Serializable;

import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.support.CollectionUtils;

/**
 * A pointer to an externalized flow definition resource. Adds assigned
 * identification information about the resource including the flow id and
 * attributes.
 * 
 * @author Keith Donald
 */
public class FlowDefinitionResource implements Serializable {

	/**
	 * The identifier to assign to the flow definition.
	 */
	private String id;

	/**
	 * Attributes that can be used to affect flow construction.
	 */
	private AttributeMap attributes;

	/**
	 * The externalized location of the flow definition resource.
	 */
	private Resource location;

	/**
	 * Creates a new externalized flow definition. The flow id assigned will be
	 * the same name as the externalized resource's filename.
	 * @param location the flow resource location.
	 */
	public FlowDefinitionResource(Resource location) {
		Assert.notNull(location, "The location of the externalized flow definition is required");
		init(stripExtension(location.getFilename()), location, null);
	}

	/**
	 * Creates a new externalized flow definition.
	 * @param id the flow id to be assigned
	 * @param location the flow resource location.
	 */
	public FlowDefinitionResource(String id, Resource location) {
		init(id, location, null);
	}

	/**
	 * Creates a new externalized flow definition.
	 * @param id the flow id to be assigned
	 * @param location the flow resource location.
	 */
	public FlowDefinitionResource(String id, Resource location, AttributeMap attributes) {
		init(id, location, attributes);
	}

	/**
	 * Returns the identifier to assign to the flow definition.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the externalized flow resource location.
	 */
	public Resource getLocation() {
		return location;
	}

	/**
	 * Returns arbitrary flow definition attributes.
	 */
	public AttributeMap getAttributes() {
		return attributes;
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowDefinitionResource)) {
			return false;
		}
		FlowDefinitionResource other = (FlowDefinitionResource)o;
		return id.equals(other.id) && location.equals(other.location);
	}

	public int hashCode() {
		return id.hashCode() + location.hashCode();
	}

	private void init(String id, Resource location, AttributeMap attributes) {
		Assert.hasText(id, "The id of the externalized flow definition is required");
		Assert.notNull(location, "The location of the externalized flow definition is required");
		this.id = id;
		this.location = location;
		if (attributes != null) {
			this.attributes = attributes;
		}
		else {
			this.attributes = CollectionUtils.EMPTY_ATTRIBUTE_MAP;
		}
	}

	private String stripExtension(String fileName) {
		int extensionIndex = fileName.lastIndexOf('.');
		if (extensionIndex != -1) {
			return fileName.substring(0, extensionIndex);
		}
		else {
			return fileName;
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("location", location).append("attributes", attributes)
				.toString();
	}
}