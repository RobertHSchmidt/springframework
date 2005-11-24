/**
 * 
 */
package org.springframework.webflow.util;

import java.util.Enumeration;

public class EmptyEnumeration implements Enumeration {

	public static final EmptyEnumeration INSTANCE = new EmptyEnumeration();

	public boolean hasMoreElements() {
		return false;
	}

	public Object nextElement() {
		throw new UnsupportedOperationException("There are no elements");
	}
}