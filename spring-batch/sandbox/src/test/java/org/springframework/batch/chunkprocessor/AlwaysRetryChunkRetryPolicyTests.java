package org.springframework.batch.chunkprocessor;

import junit.framework.TestCase;

public class AlwaysRetryChunkRetryPolicyTests extends TestCase {

	public void testNoFailures() {
		AlwaysRetryChunkRetryPolicy policy = new AlwaysRetryChunkRetryPolicy();
		assertTrue(policy.shouldRetry(new ChunkContext()));
	}

	public void testWithFailures() {
		AlwaysRetryChunkRetryPolicy policy = new AlwaysRetryChunkRetryPolicy();
		ChunkContext chunkContext = new ChunkContext();
		policy.registerFailure(new Object(), new RuntimeException(), chunkContext);
		assertTrue(policy.shouldRetry(new ChunkContext()));
	}
}
