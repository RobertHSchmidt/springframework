package org.springframework.batch.reader;

import org.springframework.batch.chunk.Chunk;

public interface ChunkReader {

	Chunk read(int size);

	void close();
}
