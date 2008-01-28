package org.springframework.batch.chunkprocessor;

public class ChunkFailureException extends RuntimeException {

	private final Object item;

	private final Exception failure;

	public ChunkFailureException(Object item, Exception failure) {
		this.item = item;
		this.failure = failure;
	}

	public Object getItem() {
		return item;
	}

	public Exception getFailure() {
		return failure;
	}
}
