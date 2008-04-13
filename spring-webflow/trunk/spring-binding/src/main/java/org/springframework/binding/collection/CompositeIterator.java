/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.binding.collection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.util.Assert;

/**
 * Iterator that combines multiple other iterators. This is a simple implementation that just maintains a list of
 * iterators which are invoked in sequence untill all iterators are exhausted.
 * 
 * @author Erwin Vervaet
 */
public class CompositeIterator implements Iterator {

	private List iterators = new LinkedList();

	private boolean inUse = false;

	/**
	 * Create a new composite iterator. Add iterators using the {@link #add(Iterator)} method.
	 */
	public CompositeIterator() {
	}

	/**
	 * Add given iterator to this composite.
	 */
	public void add(Iterator iterator) {
		Assert.state(!inUse, "You can no longer add iterator to a composite iterator that's already in use");
		if (iterators.contains(iterator)) {
			throw new IllegalArgumentException("You cannot add the same iterator twice");
		}
		iterators.add(iterator);
	}

	public boolean hasNext() {
		inUse = true;
		for (Iterator it = iterators.iterator(); it.hasNext();) {
			if (((Iterator) it.next()).hasNext()) {
				return true;
			}
		}
		return false;
	}

	public Object next() {
		inUse = true;
		for (Iterator it = iterators.iterator(); it.hasNext();) {
			Iterator iterator = (Iterator) it.next();
			if (iterator.hasNext()) {
				return iterator.next();
			}
		}
		throw new NoSuchElementException("Exhaused all iterators");
	}

	public void remove() {
		throw new UnsupportedOperationException("Remove is not supported");
	}
}