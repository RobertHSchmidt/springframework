package org.springframework.batch.step;

import java.util.List;

import junit.framework.TestCase;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.chunkprocessor.ChunkCompletionCallback;
import org.springframework.batch.chunkprocessor.ChunkProcessor;
import org.springframework.batch.itemreader.RecoveryManager;
import org.springframework.batch.reader.ChunkReader;
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

		public Chunk read(int size) {
			if (numberOfChunks-- > 0) {
				return new Chunk(new Long(0), getItemList(size));
			} else {
				return null;
			}
		}

		private List getItemList(int size) {
			Object[] items = new Object[size];
			for (int i = 0; i < size; i++) {
				items[i] = new Object();
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
