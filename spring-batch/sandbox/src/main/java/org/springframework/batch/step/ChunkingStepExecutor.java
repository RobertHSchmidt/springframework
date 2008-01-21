package org.springframework.batch.step;

import org.springframework.batch.chunk.Chunk;
import org.springframework.batch.chunkprocessor.ChunkProcessor;
import org.springframework.batch.reader.ChunkReader;

public class ChunkingStepExecutor implements StepExecutor {

	private final ChunkReader chunkReader;

	private final ChunkProcessor chunkProcessor;

	private final RecoveryManager recoveryManager;

	private int chunkSize = 1;

	public ChunkingStepExecutor(ChunkReader chunkReader, ChunkProcessor chunkProcessor) {
		this(chunkReader, chunkProcessor, null);
	}

	public ChunkingStepExecutor(ChunkReader chunkReader, ChunkProcessor chunkProcessor, RecoveryManager recoveryManager) {
		this.chunkReader = chunkReader;
		this.chunkProcessor = chunkProcessor;
		this.recoveryManager = recoveryManager;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public void execute() {
		Chunk chunk;
		while ((chunk = chunkReader.read(chunkSize)) != null) {
			chunkProcessor.process(chunk);

			if (chunkReader.markSupported()) {
				chunkReader.mark();
			}
		}
	}

}
