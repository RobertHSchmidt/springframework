package org.springframework.binding.support;

import org.springframework.binding.AttributeSource;

public class ChainedAttributeSource implements AttributeSource {

	public AttributeSource[] sources;
	
	public ChainedAttributeSource(AttributeSource[] sources) {
		this.sources = sources;
	}
	
	public boolean containsAttribute(String attributeName) {
		for (int i = 0; i < sources.length; i++) {
			if (sources[0].containsAttribute(attributeName)) {
				return true;
			}
		}
		return false;
	}

	public Object getAttribute(String attributeName) {
		for (int i = 0; i < sources.length; i++) {
			if (sources[i].containsAttribute(attributeName)) {
				return sources[i].getAttribute(attributeName);
			}
		}
		return null;
	}
}