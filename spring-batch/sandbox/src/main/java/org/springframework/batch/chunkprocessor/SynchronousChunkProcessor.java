package org.springframework.batch.chunkprocessor;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.itemprocessor.ItemProcessor;

public class SynchronousChunkProcessor implements ChunkProcessor {

	private final ItemProcessor itemProcessor;

	private ChunkRetryPolicy chunkRetryPolicy = new AlwaysRetryChunkRetryPolicy();

	private ItemSkipPolicy itemSkipPolicy = new ExceptionBasedItemSkipPolicy();

	public SynchronousChunkProcessor(ItemProcessor itemProcessor) {
		this.itemProcessor = itemProcessor;
	}

	public void setChunkRetryPolicy(ChunkRetryPolicy chunkRetryPolicy) {
		this.chunkRetryPolicy = chunkRetryPolicy;
	}

	public void setItemSkipPolicy(ItemSkipPolicy itemSkipPolicy) {
		this.itemSkipPolicy = itemSkipPolicy;
	}

	public void process(Chunk chunk) {
		process(chunk, null);
	}

	public void process(Chunk chunk, ChunkCompletionCallback callback) {
		for (;;) {
			try {
				processChunk(chunk.getItems());
				if (callback != null) {
					callback.chunkCompleted(new SuccessChunkResult(chunk.getId()));
				}
				return;
			} catch (ChunkFailureException e) {
				if (!chunkRetryPolicy.shouldRetry()) {
					return;
				}
			}
		}
	}

	private void processChunk(List items) throws ChunkFailureException {
		for (Iterator i = items.iterator(); i.hasNext();) {
			Object item = i.next();
			try {
				processItem(item);
			} catch (Exception e) {
				itemSkipPolicy.registerFailure(item, e);
				throw new ChunkFailureException();
			}
		}
	}

	private void processItem(Object item) throws Exception {
		if (!itemSkipPolicy.shouldSkip(item)) {
			itemProcessor.process(item);
		}
	}
}
