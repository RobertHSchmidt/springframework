package org.springframework.binding.method;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.core.style.ToStringCreator;

/**
 * An ordered list of method arguments.
 * 
 * @author Keith
 */
public class Arguments implements Serializable {

	/**
	 * Canonical instance for an empty arguments list.
	 */
	public static final Arguments NONE = new Arguments(0);

	/**
	 * The list.
	 */
	private List arguments;

	/**
	 * Create a argument list of the default size (3 elements).
	 */
	public Arguments() {
		this(3);
	}

	/**
	 * Create an argument list with the specified size.
	 * @param size the size
	 */
	public Arguments(int size) {
		this.arguments = new ArrayList(size);
	}

	/**
	 * Create an argument list with one argument.
	 * @param argument the single argument
	 */
	public Arguments(Argument argument) {
		this.arguments = new ArrayList(1);
		add(argument);
	}

	/**
	 * Create an argument list from the argument array.
	 * @param arguments the arguments
	 */
	public Arguments(Argument[] arguments) {
		this.arguments = new ArrayList(arguments.length);
		addAll(arguments);
	}

	/**
	 * Add a new argument to this list.
	 * @param argument the argument
	 */
	public boolean add(Argument argument) {
		return this.arguments.add(argument);
	}

	/**
	 * Add new arguments to this list.
	 * @param arguments the arguments
	 * @return the arguments
	 */
	public boolean addAll(Argument[] arguments) {
		return this.arguments.addAll(Arrays.asList(arguments));
	}

	/**
	 * Return an argument iterator.
	 * @return the iterator
	 */
	public Iterator iterator() {
		return arguments.iterator();
	}

	/**
	 * Get an array for each argument type.
	 * @return the types
	 */
	public Class[] getTypesArray() {
		int i = 0;
		Class[] types = new Class[arguments.size()];
		for (Iterator it = arguments.iterator(); it.hasNext();) {
			Argument arg = (Argument)it.next();
			types[i] = arg.getType();
			i++;
		}
		return types;
	}

	/**
	 * Returns the number of arguments in this list.
	 * @return the size
	 */
	public int size() {
		return arguments.size();
	}

	/**
	 * Return the argument at the provided index.
	 * @param index
	 * @return
	 */
	public Argument getArgument(int index) {
		return (Argument)arguments.get(index);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Arguments)) {
			return false;
		}
		Arguments other = (Arguments)obj;
		return arguments.equals(other.arguments);
	}

	public int hashCode() {
		return arguments.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("arguments", arguments).toString();
	}
}