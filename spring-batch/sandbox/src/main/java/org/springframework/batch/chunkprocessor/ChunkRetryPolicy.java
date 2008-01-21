package org.springframework.batch.chunkprocessor;

public interface ChunkRetryPolicy {

	boolean shouldRetry();
}
