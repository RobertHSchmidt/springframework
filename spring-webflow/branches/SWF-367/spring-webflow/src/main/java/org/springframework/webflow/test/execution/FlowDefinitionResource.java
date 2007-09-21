package org.springframework.webflow.test.execution;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.core.collection.AttributeMap;

public class FlowDefinitionResource {
	private String id;
	private Resource path;
	private AttributeMap attributes;

	private FlowDefinitionResource(String flowId, Resource path, AttributeMap attributes) {
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

	public FlowDefinitionResource fileAt(String filePath) {
		Resource path = new FileSystemResource(new File(filePath));
		return new FlowDefinitionResource(getFlowId(path), path, null);
	}

	private String getFlowId(Resource flowResource) {
		String fileName = flowResource.getFilename();
		int extensionIndex = fileName.lastIndexOf('.');
		if (extensionIndex != -1) {
			return fileName.substring(0, extensionIndex);
		} else {
			return fileName;
		}
	}
}
