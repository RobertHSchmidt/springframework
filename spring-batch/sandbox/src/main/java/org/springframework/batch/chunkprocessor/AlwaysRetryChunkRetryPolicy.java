package org.springframework.batch.chunkprocessor;

public class AlwaysRetryChunkRetryPolicy implements ChunkRetryPolicy {

	public boolean shouldRetry() {
	    return true;
    }

}
