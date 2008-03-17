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

/**
 * Contains information about the result of a chunk processing attempt.
 * 
 * @author Ben Hale
 */
public class ChunkProcessingResult<T extends Item> {

	private final long id;

	/**
	 * Creates a new <code>ChunkProcessingResult</code>.
	 * 
	 * @param id the identifier for the chunk that was processed
	 */
	public ChunkProcessingResult(long id) {
		this.id = id;
	}

	/**
	 * The id for the chunk that was processed
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}
}
