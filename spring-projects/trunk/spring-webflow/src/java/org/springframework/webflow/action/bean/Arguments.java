package org.springframework.webflow.action.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.core.style.ToStringCreator;

public class Arguments implements Serializable {

	public static final Arguments NONE = new Arguments(0);

	private Set arguments;

	public Arguments() {
		this(3);
	}

	public Arguments(int size) {
		this.arguments = CollectionFactory.createLinkedSetIfPossible(size);
	}

	public Arguments(Argument argument) {
		this.arguments = CollectionFactory.createLinkedSetIfPossible(1);
		add(argument);
	}

	public Arguments(Argument[] arguments) {
		this.arguments = CollectionFactory
				.createLinkedSetIfPossible(arguments.length);
		addAll(arguments);
	}

	public void add(Argument argument) {
		this.arguments.add(argument);
	}

	public void addAll(Argument[] arguments) {
		Collections.addAll(this.arguments, arguments);
	}

	public Iterator iterator() {
		return arguments.iterator();
	}

	public Class[] getTypesArray() {
		int i = 0;
		Class[] types = new Class[arguments.size()];
		for (Iterator it = arguments.iterator(); it.hasNext();) {
			Argument arg = (Argument) it.next();
			types[i] = arg.getType();
			i++;
		}
		return types;
	}

	public int size() {
		return arguments.size();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Arguments)) {
			return false;
		}
		Arguments other = (Arguments) obj;
		return arguments.equals(other.arguments);
	}

	public int hashCode() {
		return arguments.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("arguments", arguments)
				.toString();
	}
}