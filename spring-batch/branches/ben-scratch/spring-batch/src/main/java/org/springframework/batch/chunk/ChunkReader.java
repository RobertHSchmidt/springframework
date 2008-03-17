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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.Item;
import org.springframework.batch.ItemReadException;
import org.springframework.batch.ItemReader;

/**
 * Reads multiple items from an {@link ItemReader} and packages them into a {@link Chunk} for
 * distribution to a {@link ChunkProcessor}.
 * 
 * @author Ben Hale
 * @see Chunk
 * @see ChunkProcessor
 */
public class ChunkReader<T extends Item> {

	private final Logger logger = LoggerFactory.getLogger(ChunkReader.class);

	private final ItemReader<T> itemReader;

	private long chunkCounter = 0;

	/**
	 * Creates a new <code>ChunkReader</code>.
	 * 
	 * @param itemReader the {@link ItemReader} to read items from
	 */
	public ChunkReader(ItemReader<T> itemReader) {
		this.itemReader = itemReader;
	}

	/**
	 * Reads a number of items from an {@link ItemReader} and packages them up into a {@link Chunk}.
	 * If the <code>ItemReader</code> returns <code>null</code> (indicating that it is finished
	 * returning items), this <code>ChunkReader</code> will return a <code>Chunk</code> with as
	 * many items as it was able to read. If the <code>ItemReader</code> returns no items, then
	 * <code>null</code> will be returned indicating that the item pool in the
	 * <code>ItemReader</code> has been exhausted.
	 * 
	 * @param size the number of items in the chunk
	 * @return a <code>Chunk</code> with the requested number of items, as many items as could be
	 *         read, or null if no items could be read
	 * @throws ItemReadException for any error condition while reading an item.
	 */
	public Chunk<T> read(int size) throws ItemReadException {
		List<T> items = new ArrayList<T>(size);
		for (int i = 0; i < size; i++) {
			T item = itemReader.read();
			logger.debug("Read item {}", item.getId());
			if (item == null) {
				break;
			}
			items.add(item);
		}

		if (items.size() == 0) {
			return null;
		}
		return new Chunk<T>(chunkCounter++, items);
	}
}
