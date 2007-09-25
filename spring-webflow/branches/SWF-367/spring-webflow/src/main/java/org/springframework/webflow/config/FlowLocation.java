package org.springframework.webflow.config;

import java.util.Collections;
import java.util.Set;

import org.springframework.util.Assert;

class FlowLocation {

	private String id;

	private String path;

	private Set attributes;

	public FlowLocation(String id, String path, Set attributes) {
		Assert.hasText(path, "The path is required");
		this.id = id;
		this.path = path;
		this.attributes = (attributes != null ? attributes : Collections.EMPTY_SET);
	}

	public String getId() {
		return id;
	}

	public String getPath() {
		return path;
	}

	public Set getAttributes() {
		return attributes;
	}
}
