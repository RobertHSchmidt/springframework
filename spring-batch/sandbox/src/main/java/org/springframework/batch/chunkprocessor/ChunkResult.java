package org.springframework.batch.chunkprocessor;

import org.springframework.core.enums.ShortCodedLabeledEnum;

public class ChunkResult {

	public static final ChunkResultType SUCCESS = new ChunkResultType(0, "Success");

	public static final ChunkResultType FAILURE = new ChunkResultType(1, "Failure");

	private final ChunkResultType resultType;

	private final Long chunkId;

	public ChunkResult(ChunkResultType resultType, Long chunkId) {
		this.resultType = resultType;
		this.chunkId = chunkId;
	}

	public ChunkResultType getResultType() {
		return resultType;
	}

	public Long getChunkId() {
		return chunkId;
	}

	private static class ChunkResultType extends ShortCodedLabeledEnum {

		public ChunkResultType(int code, String label) {
			super(code, label);
		}

	}
}
