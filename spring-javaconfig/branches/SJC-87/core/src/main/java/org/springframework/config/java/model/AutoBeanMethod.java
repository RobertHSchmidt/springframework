package org.springframework.config.java.model;

import static java.lang.String.format;

import org.springframework.config.java.annotation.AutoBean;

public class AutoBeanMethod {

	private final String name;
	private final AutoBean metadata;
	private final String returnType;
	private final int modifiers;

	public AutoBeanMethod(String name, AutoBean metadata, String returnType, int modifiers) {
		this.name = name;
		this.metadata = metadata;
		this.returnType = returnType;
		this.modifiers = modifiers;
	}

	public String getName() {
		return name;
	}

	public AutoBean getMetadata() {
		return metadata;
	}

	public String getReturnType() {
		return returnType;
	}

	@Override
	public String toString() {
		return format("%s: name=%s; modifiers=%d",
				       getClass().getSimpleName(), name, modifiers);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + modifiers;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AutoBeanMethod other = (AutoBeanMethod) obj;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		}
		else if (!metadata.equals(other.metadata))
			return false;
		if (modifiers != other.modifiers)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

}
