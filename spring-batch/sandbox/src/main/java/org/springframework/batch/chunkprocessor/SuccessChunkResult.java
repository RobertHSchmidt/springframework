package org.springframework.batch.chunkprocessor;

public class SuccessChunkResult extends ChunkResult {

	public SuccessChunkResult(Long chunkId) {
		super(SUCCESS, chunkId);
	}

}
