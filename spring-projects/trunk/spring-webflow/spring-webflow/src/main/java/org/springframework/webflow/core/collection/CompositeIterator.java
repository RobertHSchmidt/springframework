package org.springframework.webflow.core.collection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator that combines multiple other iterators
 * 
 * @author Erwin Vervaet
 */
public class CompositeIterator implements Iterator {

	private List iterators = new LinkedList();

	/**
	 * Create a new composite iterator. Add iterators using the {@link #add(Iterator)} method.
	 */
	public CompositeIterator() {
	}
	
	/**
	 * Add given iterator to this composite.
	 */
	public void add(Iterator iterator) {
		iterators.add(iterator);
	}

	public boolean hasNext() {
		for (Iterator it = iterators.iterator(); it.hasNext(); ) {
			if (((Iterator)it.next()).hasNext()) {
				return true;
			}
		}
		return false;
	}

	public Object next() {
		for (Iterator it = iterators.iterator(); it.hasNext(); ) {
			Iterator iterator = (Iterator)it.next();
			if (iterator.hasNext()) {
				return iterator.next();
			}
		}
		throw new NoSuchElementException("Exhaused all iterators");
	}

	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}		
}