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
package org.springframework.batch.core.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepExecution;

/**
 * @author Dave Syer
 * 
 */
public class AbstractJobTests {

	AbstractJob job = new StubJob("job");

	/**
	 * Test method for {@link org.springframework.batch.core.job.AbstractJob#getName()}.
	 */
	@Test
	public void testGetName() {
		job = new StubJob();
		assertNull(job.getName());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.job.AbstractJob#setBeanName(java.lang.String)}.
	 */
	@Test
	public void testSetBeanName() {
		job.setBeanName("foo");
		assertEquals("job", job.getName());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.job.AbstractJob#setBeanName(java.lang.String)}.
	 */
	@Test
	public void testSetBeanNameWithNullName() {
		job = new StubJob(null);
		assertEquals(null, job.getName());
		job.setBeanName("foo");
		assertEquals("foo", job.getName());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.job.AbstractJob#setRestartable(boolean)}.
	 */
	@Test
	public void testSetRestartable() {
		assertFalse(job.isRestartable());
		job.setRestartable(true);
		assertTrue(job.isRestartable());
	}

	@Test
	public void testToString() throws Exception {
		String value = job.toString();
		assertTrue("Should contain name: " + value, value.indexOf("name=") >= 0);
	}
	
	@Test
	public void testAfterPropertiesSet() throws Exception {
		job.setJobRepository(null);
		try {
			job.afterPropertiesSet();
			fail();
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("JobRepository"));
		}
	}

	/**
	 * @author Dave Syer
	 *
	 */
	private static class StubJob extends AbstractJob {
		/**
		 * @param name
		 */
		private StubJob(String name) {
			super(name);
		}

		/**
		 * No-name constructor
		 */
		public StubJob() {
			super();
		}

		@Override
		protected StepExecution doExecute(JobExecution execution) throws JobExecutionException {
			return null;
		}

	}

}
