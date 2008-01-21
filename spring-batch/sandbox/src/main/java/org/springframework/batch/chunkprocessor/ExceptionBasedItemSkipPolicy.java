package org.springframework.batch.chunkprocessor;

import java.util.ArrayList;
import java.util.List;

public class ExceptionBasedItemSkipPolicy implements ItemSkipPolicy {

	private final List skippedItems = new ArrayList();

	public void registerFailure(Object item, Exception exception) {
		skippedItems.add(item);
	}

	public boolean shouldSkip(Object item) {
		return skippedItems.contains(item);
	}

}
