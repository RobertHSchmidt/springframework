package org.springframework.webflow.action.bean;

import org.springframework.core.style.ToStringCreator;

public class Argument {
	private String name;

	private Class type;

	public Argument(String name, Class type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class getType() {
		return type;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Argument)) {
			return false;
		}
		Argument other = (Argument) obj;
		return name.equals(other.name) && type.equals(other.type);
	}

	public int hashCode() {
		return name.hashCode() + type.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("name", name).append("type",
				type).toString();
	}
}
