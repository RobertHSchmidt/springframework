/*
 * Copyright 2006-2008 the original author or authors.
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
package org.springframework.batch.reader;

import org.springframework.batch.chunk.Chunk;

/**
 * Interface defining the contract for reading a chunk. This is most useful when
 * implementing a 'chunk-oriented' approach to processing. Implementors of this
 * class are expected to aggregate the output of an ItemReader into 'chunks'.
 * 
 * @author Ben Hale
 * @author Lucas Ward
 */
public interface ChunkReader {

	/**
	 * Read in a chunk, given the provided chunk size.
	 * 
	 * @param chunkSize
	 * @return
	 */
	public Chunk read(int chunkSize);

	public void close();
}
