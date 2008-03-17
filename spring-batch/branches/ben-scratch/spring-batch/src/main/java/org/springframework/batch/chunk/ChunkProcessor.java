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

import org.springframework.batch.Item;
import org.springframework.batch.ItemWriteException;

/**
 * A facility for processing the items in a chunk. Implementations of the {@link ChunkProcessor} are
 * responsible for iteration over the collection of items in the {@link Chunk} and processing each
 * one.
 * 
 * @author Ben Hale
 */
public interface ChunkProcessor<T extends Item> {

	/**
	 * Processes the items in a {@link Chunk}.
	 * 
	 * @param chunk the {@link Chunk} to process
	 * @param callback the callback for notification of completion of this chunk
	 * @throws ItemWriteException for any error condition while writing a chunk
	 */
	void process(Chunk<T> chunk, ChunkCompletionCallback<T> callback) throws ItemWriteException;
}
