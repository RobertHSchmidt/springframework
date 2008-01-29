/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.chunkreader;

import junit.framework.TestCase;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.itemreader.MockItemReader;
import org.springframework.batch.reader.ItemReadingChunkReader;

public class ItemReadingChunkReaderTests extends TestCase {

	public void testSizeNegative() {
		try {
			MockItemReader itemReader = new MockItemReader(10);
			ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
			chunkReader.read(-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSizeZero() {
		try {
			MockItemReader itemReader = new MockItemReader(10);
			ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
			chunkReader.read(0);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSizePositive() {
		MockItemReader itemReader = new MockItemReader(10);
		ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
		Chunk chunk = chunkReader.read(10);
		assertEquals(10, chunk.getItems().size());
	}

	public void testIncompleteChunk() {
		MockItemReader itemReader = new MockItemReader(5);
		ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
		Chunk chunk = chunkReader.read(10);
		assertEquals(5, chunk.getItems().size());
	}
}
