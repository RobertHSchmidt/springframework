package org.springframework.batch.chunkprocessor;

public interface ItemSkipPolicy {

	boolean shouldSkip(Object item, ChunkContext chunkContext);

	void registerFailure(Object item, Exception exception, ChunkContext chunkContext);
}
