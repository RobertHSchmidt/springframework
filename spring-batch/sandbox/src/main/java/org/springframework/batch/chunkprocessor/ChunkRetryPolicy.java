package org.springframework.batch.chunkprocessor;

public interface ChunkRetryPolicy {

	void registerFailure(Object item, Exception exception, ChunkContext chunkContext);

	boolean shouldRetry(ChunkContext chunkContext);
}
