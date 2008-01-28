package org.springframework.batch.chunkprocessor;

public class ExceptionBasedItemSkipPolicy implements ItemSkipPolicy {

	public void registerFailure(Object item, Exception exception, ChunkContext chunkContext) {
		chunkContext.addSkippedItem(item);
	}

	public boolean shouldSkip(Object item, ChunkContext chunkContext) {
		return chunkContext.containsSkippedItem(item);
	}

}
