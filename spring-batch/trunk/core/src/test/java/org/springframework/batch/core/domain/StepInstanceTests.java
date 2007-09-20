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

import java.util.Properties;

import org.springframework.batch.restart.GenericRestartData;

import junit.framework.TestCase;

/**
 * @author Dave Syer
 *
 */
public class StepInstanceTests extends TestCase {

	StepInstance instance = new StepInstance(new Long(13));

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepInstance#StepInstance()}.
	 */
	public void testStepInstance() {
		assertNull(new StepInstance().getId());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepInstance#getStepExecutionCount()}.
	 */
	public void testGetStepExecutionCount() {
		assertEquals(0, instance.getStepExecutionCount());
		instance.setStepExecutionCount(23);
		assertEquals(23, instance.getStepExecutionCount());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepInstance#getRestartData()}.
	 */
	public void testGetRestartData() {
		assertNotNull(instance.getRestartData());
		assertTrue(instance.getRestartData().getProperties().isEmpty());
		instance.setRestartData(new GenericRestartData(new Properties() {{
			setProperty("foo", "bar");
		}}));
		assertEquals("bar", instance.getRestartData().getProperties().getProperty("foo"));
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepInstance#getStatus()}.
	 */
	public void testGetStatus() {
		assertEquals(null, instance.getStatus());
		instance.setStatus(BatchStatus.COMPLETED);
		assertEquals(BatchStatus.COMPLETED, instance.getStatus());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepInstance#getJob()}.
	 */
	public void testGetJob() {
		assertEquals(null, instance.getJob());
		JobInstance job = new JobInstance();
		instance.setJob(job);
		assertEquals(job, instance.getJob());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepInstance#getStepExecution()}.
	 */
	public void testGetStepExecution() {
		assertEquals(null, instance.getStepExecution());
		StepExecution execution = new StepExecution(instance.getId(), new Long(111));
		instance.setStepExecution(execution);
		assertNotNull(execution.getJobExecutionId());
		assertEquals(execution.getJobExecutionId(), instance.getStepExecution().getJobExecutionId());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepInstance#getName()}.
	 */
	public void testGetName() {
		assertEquals(null, instance.getName());
		instance.setName("foo");
		assertEquals("foo", instance.getName());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepInstance#getJobId()}.
	 */
	public void testGetJobId() {
		assertEquals(null, instance.getJobId());
		instance.setJob(new JobInstance(new Long(23)));
		assertEquals(23, instance.getJobId().longValue());
	}

	public void testEqualsWithSameIdentifier() throws Exception {
		JobInstance job = new JobInstance(new Long(100));
		StepInstance step1 = new StepInstance(new Long(0));
		StepInstance step2 = new StepInstance(new Long(0));
		step1.setJob(job);
		step2.setJob(job);
		String stepName = "foo";
		step1.setName(stepName);
		step2.setName(stepName);
		assertEquals(step1, step2);
	}

	public void testToString() throws Exception {
		assertTrue("Should contain name", instance.toString().indexOf("name=")>=0);
		assertTrue("Should contain status", instance.toString().indexOf("status=")>=0);
	}

}
