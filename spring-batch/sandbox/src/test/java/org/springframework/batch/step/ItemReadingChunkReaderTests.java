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
package org.springframework.batch.step;

import junit.framework.TestCase;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.itemreader.MockItemReader;

public class ItemReadingChunkReaderTests extends TestCase {

	public void testSizeNegative() {
		try {
			ReadContext readContext = new ReadContext();
			MockItemReader itemReader = new MockItemReader(10);
			ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
			chunkReader.read(-1, readContext);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSizeZero() {
		try {
			ReadContext readContext = new ReadContext();
			MockItemReader itemReader = new MockItemReader(10);
			ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
			chunkReader.read(0, readContext);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSizePositive() {
		ReadContext readContext = new ReadContext();
		MockItemReader itemReader = new MockItemReader(10);
		ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
		Chunk chunk = chunkReader.read(10, readContext);
		assertEquals(10, chunk.getItems().size());
	}

	public void testIncompleteChunk() {
		ReadContext readContext = new ReadContext();
		MockItemReader itemReader = new MockItemReader(5);
		ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
		Chunk chunk = chunkReader.read(10, readContext);
		assertEquals(5, chunk.getItems().size());
	}

	public void testPolicyNoContinue() {
		ReadContext readContext = new ReadContext();
		MockItemReader itemReader = new MockItemReader(1);
		ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
		chunkReader.setReadFailurePolicy(new StubReadFailurePolicy(true));
		try {
			chunkReader.read(10, readContext);
			fail();
		} catch (RuntimeException e) {
		}
	}

	public void testPolicyContinueWithFailure() {
		ReadContext readContext = new ReadContext();
		MockItemReader itemReader = new MockItemReader(1);
		itemReader.setFail(true);
		ItemReadingChunkReader chunkReader = new ItemReadingChunkReader(itemReader);
		chunkReader.setReadFailurePolicy(new StubReadFailurePolicy(false));
		Chunk chunk = chunkReader.read(1, readContext);
		assertEquals(1, chunk.getItems().size());
	}

	private class StubReadFailurePolicy implements ReadFailurePolicy {

		private final boolean fail;

		public StubReadFailurePolicy(boolean fail) {
			this.fail = fail;
		}

		public ReadFailureException getException(ReadContext readContext) {
			throw new RuntimeException();
		}

		public void registerFailure(Exception exception, ReadContext readContext) {
		}

		public boolean shouldContinue(ReadContext readContext) {
			return !fail;
		}
	}
}
