package org.springframework.batch.chunkprocessor;

import org.springframework.batch.chunk.Chunk;

public interface ChunkProcessor {

	void process(Chunk chunk);

}
