package org.springframework.config.java.type;

import org.springframework.util.Assert;

public class Class {

	private final String name;
	private String pkg;

	public Class(String name) {
		this.name = name;
	}

	public Class setPackage(String pkg) {
		this.pkg = pkg;
		return this;
	}

	public String getPackage() {
		return this.pkg;
	}

	public String getName() {
		return name;
	}

	/**
	 * Create a new ConfigurationClass for a given {@link java.lang.Class}.
	 * A very limited subset of data is populated for the class, just class name
	 * and package name.
	 */
	public static Class forClass(java.lang.Class<?> clazz) {
		return new Class(clazz.getSimpleName()).setPackage(clazz.getPackage().getName());
	}

	public String getFullyQualifiedName() {
		Assert.notNull("package must be non-null", getPackage());
		return getPackage().concat(".").concat(getName());
	}


}
