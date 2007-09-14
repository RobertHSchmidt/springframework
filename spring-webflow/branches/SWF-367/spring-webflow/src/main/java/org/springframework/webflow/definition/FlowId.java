package org.springframework.webflow.definition;

import java.io.Serializable;

import org.springframework.util.Assert;

public class FlowId implements Serializable {
	private String namespace;
	private String shortName;

	public FlowId(String namespace, String shortName) {
		Assert.notNull(namespace, "The namespace is required");
		Assert.hasText(shortName, "The short name must have text");
		this.namespace = namespace;
		this.shortName = shortName;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getShortName() {
		return shortName;
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowId)) {
			return false;
		}
		FlowId id = (FlowId) o;
		return namespace.equals(id.namespace) && shortName.equals(id.shortName);
	}

	public int hashCode() {
		return namespace.hashCode() + shortName.hashCode();
	}

	public static FlowId valueOf(String id) {
		return new FlowId("", id);
	}

	public String toString() {
		if (namespace.length() == 0) {
			return shortName;
		} else {
			return namespace + "/" + shortName;
		}
	}

}