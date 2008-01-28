package org.springframework.batch.chunkprocessor;

public class FailureChunkResult extends ChunkResult {

	public FailureChunkResult(Long chunkId) {
		super(FAILURE, chunkId, null);
	}

}
