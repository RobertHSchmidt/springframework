package org.springframework.batch.chunkprocessor;

import junit.framework.TestCase;

public class ExceptionBasedItemSkipPolicyTests extends TestCase {

	public void testNoFailures() {
		ExceptionBasedItemSkipPolicy policy = new ExceptionBasedItemSkipPolicy();
		ChunkContext chunkContext = new ChunkContext();
		assertFalse(policy.shouldSkip(new Object(), chunkContext));
	}

	public void testWithFailures() {
		ExceptionBasedItemSkipPolicy policy = new ExceptionBasedItemSkipPolicy();
		ChunkContext chunkContext = new ChunkContext();
		Object item = new Object();
		policy.registerFailure(item, new RuntimeException(), chunkContext);
		assertTrue(policy.shouldSkip(item, chunkContext));
	}

	public void testDifferentObjectWithFailures() {
		ExceptionBasedItemSkipPolicy policy = new ExceptionBasedItemSkipPolicy();
		ChunkContext chunkContext = new ChunkContext();
		Object item = new Object();
		policy.registerFailure(item, new RuntimeException(), chunkContext);
		assertFalse(policy.shouldSkip(new Object(), chunkContext));
	}
}
