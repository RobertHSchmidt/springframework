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
package org.springframework.webflow.registry;

import org.springframework.core.io.Resource;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.builder.FlowArtifactParameters;

/**
 * A descriptor for a Flow to be assembled from a externalized resource.
 * Describes exactly one externalized flow definition resource.
 * @author Keith Donald
 */
public class ExternalizedFlowDefinition extends FlowArtifactParameters {

	/**
	 * The externalized flow resource location.
	 */
	private Resource location;

	/**
	 * Creates a new externalized flow definition. The flow id assigned will be
	 * the same name as the externalized resource's filename.
	 * @param location the flow resource location.
	 */
	public ExternalizedFlowDefinition(Resource location) {
		super(location.getFilename());
		this.location = location;
	}

	/**
	 * Creates a new externalized flow definition.
	 * @param id the flow id to be assigned
	 * @param location the flow resource location.
	 */
	public ExternalizedFlowDefinition(String id, Resource location) {
		super(id);
		this.location = location;
	}

	/**
	 * Creates a new externalized flow definition.
	 * @param id the flow id to be assigned
	 * @param location the flow resource location.
	 */
	public ExternalizedFlowDefinition(String id, Resource location, AttributeCollection attributes) {
		super(id, attributes);
		this.location = location;
	}

	/**
	 * Returns the externalized flow resource location.
	 */
	public Resource getLocation() {
		return location;
	}
}