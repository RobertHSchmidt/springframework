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
import org.springframework.batch.LineItem;
import org.springframework.batch.StubItemWriter;

/**
 * @author Ben Hale
 */
public class SynchronousChunkProcessor<T extends Item> implements ChunkProcessor<T> {

	private final ItemWriter<T> itemWriter;

	public SynchronousChunkProcessor(ItemWriter<T> itemWriter) {
		this.itemWriter = itemWriter;
	}

	public void process(Chunk<T> chunk, ChunkCompletionCallback callback) throws ItemWriteException {
		List<T> items = chunk.getItems();
		for (T item : items) {
			itemWriter.write(item);
		}
	}
	
	public static void main(String[] args) {
	    new SynchronousChunkProcessor<LineItem>(new StubItemWriter());
    }
}
