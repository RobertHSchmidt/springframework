package org.springframework.config.java.model;

import org.springframework.util.ClassUtils;

public class ModelClass {

	private final String name;
	private final boolean isInterface;

	public ModelClass(String name) {
		this(name, false);
	}

	public ModelClass(String name, boolean isInterface) {
		this.name = name;
		this.isInterface = isInterface;
	}

	public String getName() {
		return name;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public String getSimpleName() {
		return ClassUtils.getShortName(name);
	}

	@Override
	public String toString() {
		return String.format("%s: name=%s", getClass().getSimpleName(), getSimpleName());
	}

	/**
	 * Create a new ConfigurationClass for a given {@link java.lang.Class}.
	 * A very limited subset of data is populated for the class, just class name
	 * and package name.
	 */
	public static ModelClass forClass(java.lang.Class<?> clazz) {
		return new ModelClass(clazz.getName(), clazz.isInterface());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isInterface ? 1231 : 1237);
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
		ModelClass other = (ModelClass) obj;
		if (isInterface != other.isInterface)
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
