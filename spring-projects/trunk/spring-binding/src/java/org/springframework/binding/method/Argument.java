package org.springframework.binding.method;

import org.springframework.core.style.ToStringCreator;

/**
 * A named method argument. Each argument has an identifying name and is of a
 * specified type (class).
 * 
 * @author Keith
 */
public class Argument {

	/**
	 * The class of the argument, e.g "springbank.AccountNumber".
	 */
	private Class type;

	/**
	 * The name of the argument, e.g "accountNumber".
	 */
	private String name;

	/**
	 * Create a new named argument definition.
	 * 
	 * @param type the type
	 * @param name the name
	 */
	public Argument(Class type, String name) {
		this.name = name;
		this.type = type;
	}

	public Class getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Argument)) {
			return false;
		}
		Argument other = (Argument)obj;
		return type.equals(other.type) && name.equals(other.name);
	}

	public int hashCode() {
		return type.hashCode() + name.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("type", type).append("name", name).toString();
	}
}