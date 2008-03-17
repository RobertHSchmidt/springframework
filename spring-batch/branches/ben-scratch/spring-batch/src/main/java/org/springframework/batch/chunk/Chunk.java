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
package org.springframework.batch.chunk;

import java.util.Collections;
import java.util.List;

import org.springframework.batch.Item;

/**
 * A container for a 'chunk' or group of items to be processed together, typically in the same
 * transaction.
 * 
 * @author Ben Hale
 */
public class Chunk<T extends Item> {

	private final long id;

	private final List<T> items;

	/**
	 * Creates a new <code>Chunk</code>.
	 * 
	 * @param id the identifier for this chunk
	 * @param items the collection of items that comprise this chunk
	 */
	public Chunk(long id, List<T> items) {
		this.id = id;
		this.items = items;
	}

	/**
	 * The id for this chunk
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * The items that comprise this chunk. This method returns an unmodifiable <code>List</code>
	 * to prevent the collection from being mutated by consumers.
	 * 
	 * @return an unmodifiable collection of items
	 */
	public List<T> getItems() {
		return Collections.unmodifiableList(items);
	}
}
