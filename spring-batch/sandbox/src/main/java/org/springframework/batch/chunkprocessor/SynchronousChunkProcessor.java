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
		ChunkContext chunkContext = new ChunkContext();
		for (;;) {
			List items = chunk.getItems();
			try {
				processChunk(items, chunkContext);
				if (callback != null) {
					callback.chunkCompleted(new SuccessChunkResult(chunk.getId()));
				}
				return;
			} catch (ChunkFailureException e) {
				chunkRetryPolicy.registerFailure(e.getItem(), e.getFailure(), chunkContext);
				if (!chunkRetryPolicy.shouldRetry(chunkContext)) {
					return;
				}
				reorderItems(e.getItem(), items);
			}
		}
	}

	private void processChunk(List items, ChunkContext chunkContext) throws ChunkFailureException {
		for (Iterator i = items.iterator(); i.hasNext();) {
			Object item = i.next();
			try {
				processItem(item, chunkContext);
			} catch (Exception e) {
				itemSkipPolicy.registerFailure(item, e, chunkContext);
				throw new ChunkFailureException(item, e);
			}
		}
	}

	private void processItem(Object item, ChunkContext chunkContext) throws Exception {
		if (!itemSkipPolicy.shouldSkip(item, chunkContext)) {
			itemProcessor.process(item);
		}
	}
	
	private void reorderItems(Object failedItem, List items) {
		items.remove(failedItem);
		items.add(0, failedItem);
	}
}
