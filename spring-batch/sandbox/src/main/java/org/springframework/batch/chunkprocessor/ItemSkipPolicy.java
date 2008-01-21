package org.springframework.batch.chunkprocessor;

public interface ItemSkipPolicy {

	boolean shouldSkip(Object item);
	
	void registerFailure(Object item, Exception exception);
}
