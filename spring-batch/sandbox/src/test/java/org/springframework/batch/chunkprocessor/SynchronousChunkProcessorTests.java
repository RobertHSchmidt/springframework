package org.springframework.batch.chunkprocessor;

import junit.framework.TestCase;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.itemprocessor.ItemProcessor;
import org.springframework.util.CollectionUtils;

public class SynchronousChunkProcessorTests extends TestCase {

	public void testProcess() {
		StubItemProcessor itemProcessor = new StubItemProcessor();
		SynchronousChunkProcessor chunkProcessor = new SynchronousChunkProcessor(itemProcessor);
		chunkProcessor.process(getChunk(10));
		assertEquals(10, itemProcessor.getProcessedCount());
	}

	public void testSkip() {
		StubItemProcessor itemProcessor = new StubItemProcessor();
		SynchronousChunkProcessor chunkProcessor = new SynchronousChunkProcessor(itemProcessor);
		chunkProcessor.setItemSkipPolicy(new StubItemSkipPolicy(true));
		chunkProcessor.process(getChunk(1));
		assertEquals(0, itemProcessor.getProcessedCount());
	}

	public void testNoSkip() {
		StubItemProcessor itemProcessor = new StubItemProcessor();
		SynchronousChunkProcessor chunkProcessor = new SynchronousChunkProcessor(itemProcessor);
		chunkProcessor.setItemSkipPolicy(new StubItemSkipPolicy(false));
		chunkProcessor.process(getChunk(1));
		assertEquals(1, itemProcessor.getProcessedCount());
	}

	public void testRetry() {
		StubItemProcessor itemProcessor = new ExceptionStubItemProcessor();
		SynchronousChunkProcessor chunkProcessor = new SynchronousChunkProcessor(itemProcessor);
		chunkProcessor.setChunkRetryPolicy(new StubChunkRetryPolicy(true));
		chunkProcessor.setItemSkipPolicy(new StubItemSkipPolicy(false));
		chunkProcessor.process(getChunk(1));
		assertEquals(1, itemProcessor.getProcessedCount());
	}

	public void testNoRetry() {
		StubItemProcessor itemProcessor = new ExceptionStubItemProcessor();
		SynchronousChunkProcessor chunkProcessor = new SynchronousChunkProcessor(itemProcessor);
		chunkProcessor.setChunkRetryPolicy(new StubChunkRetryPolicy(false));
		chunkProcessor.setItemSkipPolicy(new StubItemSkipPolicy(false));
		chunkProcessor.process(getChunk(1));
		assertEquals(0, itemProcessor.getProcessedCount());
	}

	private Chunk getChunk(int chunkSize) {
		Object[] items = new Object[chunkSize];
		for (int i = 0; i < chunkSize; i++) {
			items[i] = new Object();
		}

		return new Chunk(new Long(0), CollectionUtils.arrayToList(items));
	}

	private class StubItemProcessor implements ItemProcessor {

		private int processedCount = 0;

		public int getProcessedCount() {
			return processedCount;
		}

		public void process(Object item) throws Exception {
			processedCount++;
		}

	}

	private class ExceptionStubItemProcessor extends StubItemProcessor {

		private boolean exceptionThrown = false;

		public void process(Object item) throws Exception {
			if (exceptionThrown) {
				super.process(item);
			} else {
				exceptionThrown = true;
				throw new RuntimeException();
			}
		}
	}

	private class StubItemSkipPolicy implements ItemSkipPolicy {

		private boolean skip;

		public StubItemSkipPolicy(boolean skip) {
			this.skip = skip;
		}

		public void registerFailure(Object item, Exception exception, ChunkContext chunkContext) {
		}

		public boolean shouldSkip(Object item, ChunkContext chunkContext) {
			return skip;
		}

	}

	private class StubChunkRetryPolicy implements ChunkRetryPolicy {

		private boolean retry;

		public StubChunkRetryPolicy(boolean retry) {
			this.retry = retry;
		}

		public boolean shouldRetry(ChunkContext chunkContext) {
			return retry;
		}

		public void registerFailure(Object item, Exception exception, ChunkContext chunkContext) {
        }

	}
}
