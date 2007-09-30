/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.domain;

import java.sql.Timestamp;

import junit.framework.TestCase;

import org.springframework.batch.core.runtime.SimpleJobIdentifier;
import org.springframework.batch.repeat.context.RepeatContextSupport;

/**
 * @author Dave Syer
 *
 */
public class JobExecutionTests extends TestCase {

	private JobExecution execution = new JobExecution(new JobInstance(null, new Long(11)));
	private JobExecution context = new JobExecution(new JobInstance(new SimpleJobIdentifier("foo"), new Long(11)));
	
	/**
	 * Test method for {@link org.springframework.batch.core.domain.JobExecution#JobExecution()}.
	 */
	public void testJobExecution() {
		assertNull(new JobExecution().getId());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.JobExecution#getEndTime()}.
	 */
	public void testGetEndTime() {
		assertNull(execution.getEndTime());
		execution.setEndTime(new Timestamp(100L));
		assertEquals(100L, execution.getEndTime().getTime());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.JobExecution#getStartTime()}.
	 */
	public void testGetStartTime() {
		assertNotNull(execution.getStartTime());
		execution.setStartTime(new Timestamp(0L));
		assertEquals(0L, execution.getStartTime().getTime());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.JobExecution#getStatus()}.
	 */
	public void testGetStatus() {
		assertEquals(BatchStatus.STARTING, execution.getStatus());
		execution.setStatus(BatchStatus.COMPLETED);
		assertEquals(BatchStatus.COMPLETED, execution.getStatus());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.JobExecution#getJobId()}.
	 */
	public void testGetJobId() {
		assertEquals(11, execution.getJobId().longValue());
		execution = new JobExecution(new JobInstance(null, new Long(23)));
		assertEquals(23, execution.getJobId().longValue());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.JobExecution#getExitCode()}.
	 */
	public void testGetExitCode() {
		assertEquals("", execution.getExitCode());
		execution.setExitCode("23");
		assertEquals("23", execution.getExitCode());
	}

	public void testContextContainsInfo() throws Exception {
		assertEquals("foo", context.getJobIdentifier().getName());
	}

	public void testNullContexts() throws Exception {
		assertEquals(0, context.getStepContexts().size());
		assertEquals(0, context.getChunkContexts().size());
	}
	
	public void testStepContext() throws Exception {
		context.registerStepContext(new RepeatContextSupport(null));
		assertEquals(1, context.getStepContexts().size());
	}

	public void testAddAndRemoveStepContext() throws Exception {
		context.registerStepContext(new RepeatContextSupport(null));
		assertEquals(1, context.getStepContexts().size());
		context.unregisterStepContext(new RepeatContextSupport(null));
		assertEquals(0, context.getStepContexts().size());
	}

	public void testAddAndRemoveStepExecution() throws Exception {
		assertEquals(0, context.getStepExecutions().size());
		context.registerStepExecution(new StepExecution(null, null));
		assertEquals(1, context.getStepExecutions().size());
	}

	public void testAddAndRemoveChunkContext() throws Exception {
		context.registerChunkContext(new RepeatContextSupport(null));
		assertEquals(1, context.getChunkContexts().size());
		context.unregisterChunkContext(new RepeatContextSupport(null));
		assertEquals(0, context.getChunkContexts().size());
	}

	public void testRemoveChunkContext() throws Exception {
		context.unregisterChunkContext(new RepeatContextSupport(null));
		assertEquals(0, context.getChunkContexts().size());
	}
	
}
