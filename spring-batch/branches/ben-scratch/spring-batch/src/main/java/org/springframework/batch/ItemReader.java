/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.batch;

/**
 * This is the central interface for reading items into a step for a batch job. Applications can
 * implement this directly, but in many cases will use one of the provided implementations.
 * 
 * Implementations of this interface should be considered stateful and therefore not recommended for
 * multi-threaded use. In addition, it is not recommended to use implementations of this interface
 * repeatedly in a batch job. A new instance should be created for each step it is used in.
 * 
 * @author Ben Hale
 */
public interface ItemReader<T extends Item> {

	/**
	 * Returns the next item from the <code>ItemReader</code>. If no item is available because
	 * end of this item pool has been reached, this method will return <code>null</code>. This
	 * method blocks until an item is available, the end of the item pool is detected, or an
	 * exception is thrown.
	 * 
	 * @return the next item, or <code>null</code> if the end of the item pool is reached
	 * @throws ItemReadException for any error condition while reading an item. The throwing of this
	 *             exception does not necessarily invalidate the <code>ItemReader</code>.
	 */
	T read() throws ItemReadException;
}
