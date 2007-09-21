package org.springframework.webflow.config;

import java.util.Collections;
import java.util.Set;

import org.springframework.util.Assert;

public class FlowLocation {
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

	public static class Attribute {
		private String name;
		private String value;
		private String type;

		public Attribute(String name, String value, String type) {
			Assert.hasText(name, "The name is required");
			Assert.hasText(value, "The value is required");
			this.name = name;
			this.value = value;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public String getType() {
			return type;
		}
	}
}
