package org.springframework.batch.chunkprocessor;

import java.util.List;

public class SuccessChunkResult extends ChunkResult {

	public SuccessChunkResult(Long chunkId, List skippedItems) {
		super(SUCCESS, chunkId, skippedItems);
	}

}
