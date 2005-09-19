package org.springframework.binding.support;

import org.springframework.binding.AttributeSource;

/**
 * A attribute source that queries an ordered set of attribute sources until a
 * match is found for a given attribute, or the set is exhausted.
 * @author Keith
 */
public class ChainedAttributeSource implements AttributeSource {

	/**
	 * The set of sources.
	 */
	public AttributeSource[] sources;

	/**
	 * Create a chained attribute source.
	 * @param sources the sources
	 */
	public ChainedAttributeSource(AttributeSource[] sources) {
		Assert.notNull(sources, "At least one source is required");
		assertElementsNotNull(sources);
		this.sources = sources;
	}

	public void assertElementsNotNull(AttributeSource[] sources) {
		for (int i = 0; i < sources.length; i++) {
			if (sources[i] == null) {
				throw new IllegalArgumentException("Null element at index [" + i  + "] not allowed");
			}
		}
	}

	public boolean containsAttribute(String attributeName) {
		for (int i = 0; i < sources.length; i++) {
			if (sources[i].containsAttribute(attributeName)) {
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