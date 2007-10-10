package org.springframework.webflow.config;

import java.util.Collections;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

class FlowBuilderInfo {

	private String id;

	private String className;

	private Set attributes;

	public FlowBuilderInfo(String id, String className, Set attributes) {
		Assert.hasText(className, "The FlowBuilder class name is required");
		this.className = className;
		setId(id);
		this.attributes = (attributes != null ? attributes : Collections.EMPTY_SET);
	}

	private void setId(String id) {
		if (StringUtils.hasText(id)) {
			this.id = id;
		} else {
			this.id = StringUtils.uncapitalize(StringUtils.delete(ClassUtils.getShortName(className), "FlowBuilder"));
		}
	}

	public String getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}

	public Set getAttributes() {
		return attributes;
	}
}
