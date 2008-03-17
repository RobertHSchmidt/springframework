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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.batch.ItemReadException;
import org.springframework.batch.ItemReader;
import org.springframework.batch.LineItem;
import org.springframework.batch.StubItemReader;

public class ChunkReaderTests {

	@Test
	public void testFullChunk() throws ItemReadException {
		StubItemReader itemReader = new StubItemReader();
		ChunkReader<LineItem> chunkReader = new ChunkReader<LineItem>(itemReader);

		Chunk<LineItem> chunk = (Chunk<LineItem>) chunkReader.read(2);
		assertEquals(2, chunk.getItems().size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testPartialChunk() throws ItemReadException {
		ItemReader<LineItem> itemReader = createMock(ItemReader.class);
		ChunkReader<LineItem> chunkReader = new ChunkReader<LineItem>(itemReader);

		expect(itemReader.read()).andReturn(new LineItem(0, "Line 0"));
		expect(itemReader.read()).andReturn(null);
		replay(itemReader);

		Chunk<LineItem> chunk = (Chunk<LineItem>) chunkReader.read(2);
		assertEquals(1, chunk.getItems().size());
		verify(itemReader);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testEmptyChunk() throws ItemReadException {
		ItemReader<LineItem> itemReader = createMock(ItemReader.class);
		ChunkReader<LineItem> chunkReader = new ChunkReader<LineItem>(itemReader);

		expect(itemReader.read()).andReturn(null);
		replay(itemReader);

		Chunk<LineItem> chunk = (Chunk<LineItem>) chunkReader.read(2);
		assertNull(chunk);
		verify(itemReader);
	}

	@Test(expected = ItemReadException.class)
	@SuppressWarnings("unchecked")
	public void testItemReadException() throws ItemReadException {
		ItemReader<LineItem> itemReader = createMock(ItemReader.class);
		ChunkReader<LineItem> chunkReader = new ChunkReader<LineItem>(itemReader);

		expect(itemReader.read()).andThrow(new ItemReadException("Dummy Exception"));
		replay(itemReader);

		chunkReader.read(2);
		verify(itemReader);
	}

	@Test
	public void testChunkId() throws ItemReadException {
		StubItemReader itemReader = new StubItemReader();
		ChunkReader<LineItem> chunkReader = new ChunkReader<LineItem>(itemReader);

		Chunk<LineItem> chunk1 = (Chunk<LineItem>) chunkReader.read(2);
		assertEquals(0, chunk1.getId());
		Chunk<LineItem> chunk2 = (Chunk<LineItem>) chunkReader.read(2);
		assertEquals(1, chunk2.getId());
	}
}
