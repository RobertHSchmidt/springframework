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
					callback.chunkCompleted(new SuccessChunkResult(chunk.getId(), chunkContext.getSkippedItems()));
				}
				return;
			} catch (ChunkFailureException e) {
				chunkRetryPolicy.registerFailure(e.getItem(), e.getFailure(), chunkContext);
				if (!chunkRetryPolicy.shouldRetry(chunkContext)) {
					if (callback != null) {
						callback.chunkCompleted(new FailureChunkResult(chunk.getId()));
					}
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
