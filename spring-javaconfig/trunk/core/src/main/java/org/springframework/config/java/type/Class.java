package org.springframework.config.java.type;

import org.springframework.util.ClassUtils;

public class Class {

	private final String name;
	private final boolean isInterface;

	public Class(String name) {
		this(name, false);
	}

	public Class(String name, boolean isInterface) {
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
	public static Class forClass(java.lang.Class<?> clazz) {
		return new Class(clazz.getName(), clazz.isInterface());
	}

}
