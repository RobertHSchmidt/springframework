package org.springframework.batch.chunkprocessor;

public interface ChunkCompletionCallback {

	void chunkCompleted(ChunkResult result);
}
