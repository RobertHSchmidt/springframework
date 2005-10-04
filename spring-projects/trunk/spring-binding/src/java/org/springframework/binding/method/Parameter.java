package org.springframework.binding.method;

import java.io.Serializable;

import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;

/**
 * A named method parameter. Each parameter has an identifying name and is of a
 * specified type (class).
 * 
 * @author Keith
 */
public class Parameter implements Serializable {

	/**
	 * The class of the parameter, e.g "springbank.AccountNumber".
	 */
	private Class type;

	/**
	 * The name of the parameter, e.g "accountNumber".
	 */
	private Expression name;

	/**
	 * Create a new named argument definition.
	 * 
	 * @param name the name
	 */
	public Parameter(Expression name) {
		this(null, name);
	}

	/**
	 * Create a new named argument definition.
	 * 
	 * @param type the type
	 * @param name the name
	 */
	public Parameter(Class type, Expression name) {
		this.name = name;
		this.type = type;
	}

	public Class getType() {
		return type;
	}

	public Expression getName() {
		return name;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Parameter)) {
			return false;
		}
		Parameter other = (Parameter)obj;
		return ObjectUtils.nullSafeEquals(type, other.type) && name.equals(other.name);
	}

	public int hashCode() {
		return (type != null ? type.hashCode() : 0) + name.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("type", type).append("name", name).toString();
	}
}