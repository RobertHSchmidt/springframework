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

import java.util.List;

import org.springframework.batch.Item;
import org.springframework.batch.ItemWriteException;
import org.springframework.batch.ItemWriter;

/**
 * A synchronous implementation of {@link ChunkProcessor}. This implementation iterates over all
 * items in the chunk and then called the {@link ChunkCompletionCallback} synchronously before
 * returning.
 * 
 * @author Ben Hale
 */
public class SynchronousChunkProcessor<T extends Item> implements ChunkProcessor<T> {

	private final ItemWriter<T> itemWriter;

	/**
	 * Creates a new <code>SynchronousChunkProcessor</code>.
	 * 
	 * @param itemWriter the {@link ItemWriter} to write items to
	 */
	public SynchronousChunkProcessor(ItemWriter<T> itemWriter) {
		this.itemWriter = itemWriter;
	}

	public void process(Chunk<T> chunk, ChunkCompletionCallback<T> callback) throws ItemWriteException {
		List<T> items = chunk.getItems();
		for (T item : items) {
			itemWriter.write(item);
		}
		callback.processed(new ChunkProcessingResult<T>(chunk.getId()));
	}
}
