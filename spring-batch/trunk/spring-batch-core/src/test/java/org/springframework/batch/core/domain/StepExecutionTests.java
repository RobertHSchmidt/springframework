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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.batch.support.PropertiesConverter;

/**
 * @author Dave Syer
 * 
 */
public class StepExecutionTests extends TestCase {

	private StepExecution execution = newStepExecution("stepName",
			new Long(23));

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.JobExecution#JobExecution()}.
	 */
	public void testStepExecution() {
		assertNull(new StepExecution().getId());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.JobExecution#JobExecution()}.
	 */
	public void testStepExecutionWithNullId() {
		assertNull(new StepExecution(null, null).getId());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.JobExecution#getEndTime()}.
	 */
	public void testGetEndTime() {
		assertNull(execution.getEndTime());
		execution.setEndTime(new Date(0L));
		assertEquals(0L, execution.getEndTime().getTime());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.JobExecution#getStartTime()}.
	 */
	public void testGetStartTime() {
		assertNotNull(execution.getStartTime());
		execution.setStartTime(new Date(10L));
		assertEquals(10L, execution.getStartTime().getTime());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.JobExecution#getStatus()}.
	 */
	public void testGetStatus() {
		assertEquals(BatchStatus.STARTING, execution.getStatus());
		execution.setStatus(BatchStatus.COMPLETED);
		assertEquals(BatchStatus.COMPLETED, execution.getStatus());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.JobExecution#getJobId()}.
	 */
	public void testGetJobId() {
		assertEquals(23, execution.getJobExecutionId().longValue());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.JobExecution#getExitStatus()}.
	 */
	public void testGetExitCode() {
		assertEquals(ExitStatus.UNKNOWN, execution.getExitStatus());
		execution.setExitStatus(ExitStatus.FINISHED);
		assertEquals(ExitStatus.FINISHED, execution.getExitStatus());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.StepExecution#incrementCommitCount()}.
	 */
	public void testIncrementCommitCount() {
		int before = execution.getCommitCount().intValue();
		execution.incrementCommitCount();
		int after = execution.getCommitCount().intValue();
		assertEquals(before + 1, after);
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.StepExecution#incrementTaskCount()}.
	 */
	public void testIncrementLuwCount() {
		int before = execution.getTaskCount().intValue();
		execution.incrementTaskCount();
		int after = execution.getTaskCount().intValue();
		assertEquals(before + 1, after);
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.StepExecution#rollback()}.
	 */
	public void testIncrementRollbackCount() {
		int before = execution.getRollbackCount().intValue();
		execution.rollback();
		int after = execution.getRollbackCount().intValue();
		assertEquals(before + 1, after);
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.StepExecution#getCommitCount()}.
	 */
	public void testGetCommitCount() {
		execution.setCommitCount(123);
		assertEquals(123, execution.getCommitCount().intValue());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.StepExecution#getTaskCount()}.
	 */
	public void testGetTaskCount() {
		execution.setTaskCount(123);
		assertEquals(123, execution.getTaskCount().intValue());
	}

	/**
	 * Test method for
	 * {@link org.springframework.batch.core.domain.StepExecution#getRollbackCount()}.
	 */
	public void testGetRollbackCount() {
		execution.setRollbackCount(123);
		assertEquals(123, execution.getRollbackCount().intValue());
	}
	
	public void testGetJobExecution() throws Exception {
		assertNotNull(execution.getJobExecution());
	}
	
	public void testApplyContribution() throws Exception {
		StepContribution contribution = execution.createStepContribution();
		contribution.incrementCommitCount();
		execution.apply(contribution);
		assertEquals(new Integer(1), execution.getCommitCount());
	}
	
	public void testTerminateOnly() throws Exception {
		assertFalse(execution.isTerminateOnly());
		execution.setTerminateOnly();
		assertTrue(execution.isTerminateOnly());
	}

	public void testToStringWithNullName() throws Exception {
		String value = new StepExecution().toString();
		assertTrue("Should contain name=null: "+value, value.indexOf("name=null")>=0);
	}

	public void testToString() throws Exception {
		assertTrue("Should contain task count: " + execution.toString(),
				execution.toString().indexOf("task") >= 0);
		assertTrue("Should contain commit count: " + execution.toString(),
				execution.toString().indexOf("commit") >= 0);
		assertTrue("Should contain rollback count: " + execution.toString(),
				execution.toString().indexOf("rollback") >= 0);
	}

	public void testExecutionContext() throws Exception {
		assertNotNull(execution.getExecutionContext());
		ExecutionContext context = new ExecutionContext();
		context.putString("foo", "bar");
		execution.setExecutionContext(context );
		assertEquals("bar", execution.getExecutionContext().getString("foo"));
	}

	public void testEqualsWithSameIdentifier() throws Exception {
		Entity step1 = newStepExecution("stepName", new Long(11));
		Entity step2 = newStepExecution("stepName", new Long(11));
		assertEquals(step1, step2);
	}

	public void testEqualsWithNull() throws Exception {
		Entity step = newStepExecution("stepName", new Long(11));
		assertFalse(step.equals(null));
	}

	public void testEqualsWithNullIdentifiers() throws Exception {
		Entity step = newStepExecution("stepName", new Long(11));
		assertFalse(step.equals(new StepExecution()));
	}

	public void testEqualsWithNullJob() throws Exception {
		Entity step = newStepExecution(null, new Long(11));
		assertFalse(step.equals(new StepExecution()));
	}

	public void testEqualsWithNullStep() throws Exception {
		Entity step = newStepExecution("stepName", null);
		assertFalse(step.equals(new StepExecution()));
	}

	public void testEqualsWithSelf() throws Exception {
		assertTrue(execution.equals(execution));
	}

	public void testEqualsWithDifferent() throws Exception {
		Entity step = newStepExecution("foo", new Long(13));
		assertFalse(execution.equals(step));
	}

	public void testEqualsWithNullStepId() throws Exception {
		execution = newStepExecution(null, new Long(31));
		assertEquals(null, execution.getStepName());
		StepExecution step = newStepExecution(null, new Long(31));
		assertEquals(step.getJobExecutionId(), execution.getJobExecutionId());
		assertTrue(execution.equals(step));
	}

	public void testHashCode() throws Exception {
		assertTrue("Hash code same as parent", new Entity(execution.getId())
				.hashCode() != execution.hashCode());
	}

	public void testHashCodeWithNullIds() throws Exception {
		assertTrue("Hash code not same as parent",
				new Entity(execution.getId()).hashCode() != new StepExecution()
						.hashCode());
	}

	public void testHashCodeViaHashSet() throws Exception {
		Set set = new HashSet();
		set.add(execution);
		assertTrue(set.contains(execution));
		execution.setExecutionContext(new ExecutionContext(PropertiesConverter.stringToProperties("foo=bar")));
		assertTrue(set.contains(execution));
	}

	private StepExecution newStepExecution(String stepName, Long long2) {
		JobInstance job = new JobInstance(new Long(3), new JobParameters());
		StepExecution execution = new StepExecution(stepName, new JobExecution(job, long2), new Long(4));
		return execution;
	}

}
