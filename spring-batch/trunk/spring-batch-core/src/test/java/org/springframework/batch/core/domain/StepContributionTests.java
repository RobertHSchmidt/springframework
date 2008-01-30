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

import junit.framework.TestCase;

import org.springframework.batch.support.PropertiesConverter;

/**
 * @author Dave Syer
 *
 */
public class StepContributionTests extends TestCase {

	private StepExecution execution = new StepExecution();
	private StepContribution contribution = new StepContribution(execution);
	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepContribution#incrementTaskCount()}.
	 */
	public void testIncrementTaskCount() {
		assertEquals(0, contribution.getTaskCount());
		contribution.incrementTaskCount();
		assertEquals(1, contribution.getTaskCount());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepContribution#setStatistics(java.util.Properties)}.
	 */
	public void testSetStatistics() {
		assertEquals(null, contribution.getStatistics());
		contribution.setStatistics(PropertiesConverter.stringToProperties("foo=bar"));
		assertEquals(1, contribution.getStatistics().size());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepContribution#incrementCommitCount()}.
	 */
	public void testIncrementCommitCount() {
		assertEquals(0, contribution.getCommitCount());
		contribution.incrementCommitCount();
		assertEquals(1, contribution.getCommitCount());
	}

	/**
	 * Test method for {@link org.springframework.batch.core.domain.StepContribution#isTerminateOnly()}.
	 */
	public void testIsTerminateOnly() {
		assertFalse(contribution.isTerminateOnly());
		execution.setTerminateOnly();
		assertTrue(contribution.isTerminateOnly());
	}

}
