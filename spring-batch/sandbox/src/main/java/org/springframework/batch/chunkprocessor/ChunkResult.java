package org.springframework.batch.chunkprocessor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.enums.ShortCodedLabeledEnum;

public class ChunkResult {

	public static final ChunkResultType SUCCESS = new ChunkResultType(0, "Success");

	public static final ChunkResultType FAILURE = new ChunkResultType(1, "Failure");

	private final ChunkResultType resultType;

	private final Long chunkId;

	private final List skippedItems;

	public ChunkResult(ChunkResultType resultType, Long chunkId, List skippedItems) {
		this.resultType = resultType;
		this.chunkId = chunkId;
		this.skippedItems = skippedItems;
	}

	public ChunkResultType getResultType() {
		return resultType;
	}

	public Long getChunkId() {
		return chunkId;
	}

	public List getSkippedItems() {
		return new ArrayList(skippedItems);
	}

	private static class ChunkResultType extends ShortCodedLabeledEnum {

		public ChunkResultType(int code, String label) {
			super(code, label);
		}

	}
}
