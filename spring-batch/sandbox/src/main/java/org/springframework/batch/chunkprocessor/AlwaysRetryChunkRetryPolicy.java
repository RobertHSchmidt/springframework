package org.springframework.batch.chunkprocessor;

public class AlwaysRetryChunkRetryPolicy implements ChunkRetryPolicy {

	public boolean shouldRetry(ChunkContext chunkContext) {
		return true;
	}

	public void registerFailure(Object item, Exception exception, ChunkContext chunkContext) {
	}

}
