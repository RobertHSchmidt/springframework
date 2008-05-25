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

	/**
	 * Create a new ConfigurationClass for a given {@link java.lang.Class}.
	 * A very limited subset of data is populated for the class, just class name
	 * and package name.
	 */
	public static ModelClass forClass(java.lang.Class<?> clazz) {
		return new ModelClass(clazz.getName(), clazz.isInterface());
	}

}
