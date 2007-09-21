package org.springframework.webflow.config;

import org.springframework.core.io.Resource;
import org.springframework.webflow.core.collection.AttributeMap;

public class FlowDefinitionResource {
	private String id;
	private Resource path;
	private AttributeMap attributes;

	FlowDefinitionResource(String flowId, Resource path, AttributeMap attributes) {
		this.id = flowId;
		this.path = path;
		this.attributes = attributes;
	}

	public String getId() {
		return id;
	}

	public Resource getPath() {
		return path;
	}

	public AttributeMap getAttributes() {
		return attributes;
	}

	public String toString() {
		return path.toString();
	}
}
