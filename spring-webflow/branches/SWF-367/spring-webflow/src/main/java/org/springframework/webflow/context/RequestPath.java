package org.springframework.webflow.context;

/**
 * A representation of a flow request path. In a flow definition request URI,the request path is the portion after the
 * flow definition identifier. For example, in a URI of http://host/booking-flow/1 the request path is "/1".
 * 
 * @author Keith Donald
 */
public interface RequestPath {

	/**
	 * Get the elements of the request path.
	 * @return parsed request path elements
	 */
	public String[] getElements();

	/**
	 * Gets the path element at the specified index.
	 * @param index the element index
	 * @return the element
	 * @throws IndexOutOfBoundsException if the index is out of bounds
	 */
	public String getElement(int index) throws IndexOutOfBoundsException;

	/**
	 * Return the string representation of the request path; for example "/1".
	 * @return the request path as a string
	 */
	public String toString();
}
