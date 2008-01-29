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

import java.io.Serializable;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.chunkprocessor.ChunkCompletionCallback;
import org.springframework.batch.chunkprocessor.ChunkProcessor;
import org.springframework.batch.itemreader.RecoveryManager;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.util.CollectionUtils;

public class ChunkingStepExecutorTests extends TestCase {

	protected void setUp() throws Exception {
	}

	public void testRecoveryCommit() {
		StubRecoveryManager recoveryManager = new StubRecoveryManager();
		ChunkingStepExecutor executor = new ChunkingStepExecutor(new StubChunkReader(1), new StubChunkProcessor(false),
		        new ResourcelessTransactionManager(), recoveryManager);
		executor.execute();
		assertTrue(recoveryManager.getMarkCalled());
		assertFalse(recoveryManager.getResetCalled());
	}

	public void testRecoveryRollback() {
		StubRecoveryManager recoveryManager = new StubRecoveryManager();
		ChunkingStepExecutor executor = new ChunkingStepExecutor(new StubChunkReader(1), new StubChunkProcessor(true),
		        new ResourcelessTransactionManager(), recoveryManager);
		try {
			executor.execute();
			fail();
		} catch (RuntimeException e) {
			assertFalse(recoveryManager.getMarkCalled());
			assertTrue(recoveryManager.getResetCalled());
		}

	}

	private class StubChunkProcessor implements ChunkProcessor {

		private boolean throwException = false;

		public StubChunkProcessor() {
			this(false);
		}

		public StubChunkProcessor(boolean throwException) {
			this.throwException = throwException;
		}

		public void process(Chunk chunk) {
			if (throwException) {
				throw new RuntimeException();
			}
		}

		public void process(Chunk chunk, ChunkCompletionCallback callback) {
			if (throwException) {
				throw new RuntimeException();
			}
		}

	}

	private class StubChunkReader implements ChunkReader {

		private int numberOfChunks;

		public StubChunkReader(int numberOfChunks) {
			this.numberOfChunks = numberOfChunks;
		}

		public void close() {
		}

		public Chunk read(int size, ReadContext readContext) {
			if (numberOfChunks-- > 0) {
				return new Chunk(new Long(0), getItemList(size));
			} else {
				return null;
			}
		}

		private List getItemList(int size) {
			Object[] items = new Object[size];
			for (int i = 0; i < size; i++) {
				items[i] = new Serializable() {
				};
			}
			return CollectionUtils.arrayToList(items);
		}

	}

	private class StubRecoveryManager implements RecoveryManager {

		private boolean markCalled = false;

		private boolean resetCalled = false;

		public boolean getMarkCalled() {
			return markCalled;
		}

		public boolean getResetCalled() {
			return resetCalled;
		}

		public void mark() {
			markCalled = true;
		}

		public void reset() {
			resetCalled = true;
		}
	}

}
