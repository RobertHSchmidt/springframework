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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.batch.ItemWriteException;
import org.springframework.batch.ItemWriter;
import org.springframework.batch.LineItem;
import org.springframework.batch.StubChunkFactory;
import org.springframework.batch.StubItemWriter;

public class SynchronousChunkProcessorTests {

	@Test
	public void testFullChunk() throws ItemWriteException {
		StubItemWriter itemWriter = new StubItemWriter();
		StubChunkCompletionCallback callback = new StubChunkCompletionCallback();
		SynchronousChunkProcessor<LineItem> chunkProcessor = new SynchronousChunkProcessor<LineItem>(itemWriter);
		chunkProcessor.process(StubChunkFactory.getChunk(2), callback);

		assertEquals(1, callback.getIdCount());
		assertEquals(2, itemWriter.getItemCount());
	}

	@Test
	public void testEmptyChunk() throws ItemWriteException {
		StubItemWriter itemWriter = new StubItemWriter();
		StubChunkCompletionCallback callback = new StubChunkCompletionCallback();
		SynchronousChunkProcessor<LineItem> chunkProcessor = new SynchronousChunkProcessor<LineItem>(itemWriter);
		chunkProcessor.process(StubChunkFactory.getChunk(0), callback);

		assertEquals(1, callback.getIdCount());
		assertEquals(0, itemWriter.getItemCount());
	}

	@Test(expected = ItemWriteException.class)
	@SuppressWarnings("unchecked")
	public void testItemWriteException() throws ItemWriteException {
		ItemWriter<LineItem> itemWriter = createMock(ItemWriter.class);
		StubChunkCompletionCallback callback = new StubChunkCompletionCallback();
		SynchronousChunkProcessor<LineItem> chunkProcessor = new SynchronousChunkProcessor<LineItem>(itemWriter);
		Chunk<LineItem> chunk = StubChunkFactory.getChunk(1);

		itemWriter.write(chunk.getItems().get(0));
		expectLastCall().andThrow(new ItemWriteException("Dummy Exception"));
		replay(itemWriter);

		chunkProcessor.process(chunk, callback);
		verify(itemWriter);
	}

	private class StubChunkCompletionCallback implements ChunkCompletionCallback<LineItem> {

		private final List<Long> ids = new ArrayList<Long>();

		public void processed(ChunkProcessingResult<LineItem> result) {
			ids.add(result.getId());
		}

		public List<Long> getIds() {
			return Collections.unmodifiableList(ids);
		}

		public int getIdCount() {
			return ids.size();
		}

	}
}
