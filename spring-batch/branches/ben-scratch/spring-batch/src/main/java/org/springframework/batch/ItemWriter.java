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
 * This is the central interface for writing items from a step for a batch job. Applications will
 * implement this directly, but in some cases will use one of the provided implementations.
 * 
 * Implementations of this interface should be considered stateful and therefore not recommended for
 * multi-threaded use. In addition, it is not recommended to use implementations of this interface
 * repeatedly in a batch job. A new instance should be created for each step it is used in.
 * 
 * @author Ben Hale
 */
public interface ItemWriter<T extends Item> {

	/**
	 * Writes an item to the <code>ItemWriter</code>.
	 * 
	 * @param item the item to write
	 * @throws ItemWriteException for any error condition while writing an item. The throwing of
	 *             this exception typically invalidates the <code>ItemWriter</code>.
	 */
	void write(T item) throws ItemWriteException;
}
