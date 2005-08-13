package org.springframework.webflow.action.bean;

import org.springframework.core.style.ToStringCreator;

/**
 * A named method argument. Each argument has an identifying name and is of a
 * specified type (class).
 * 
 * @author Keith
 */
public class Argument {
	
	/**
	 * The name of the argument, e.g "accountNumber".
	 */
	private String name;

	/**
	 * The class of the argument, e.g "springbank.AccountNumber".
	 */
	private Class type;

	/**
	 * Create a new named argument definition.
	 * @param name the name
	 * @param type the type
	 */
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