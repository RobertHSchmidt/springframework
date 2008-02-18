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

package org.springframework.batch.execution.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;
import org.springframework.batch.core.domain.Entity;
import org.springframework.batch.core.domain.JobExecution;
import org.springframework.batch.core.domain.JobInstance;
import org.springframework.batch.core.domain.JobParameters;
import org.springframework.batch.core.domain.JobParametersBuilder;
import org.springframework.batch.core.domain.JobSupport;
import org.springframework.batch.core.domain.Step;
import org.springframework.batch.core.domain.StepExecution;
import org.springframework.batch.core.domain.StepInstance;
import org.springframework.batch.core.domain.StepSupport;
import org.springframework.batch.core.repository.BatchRestartException;
import org.springframework.batch.execution.repository.dao.JobDao;
import org.springframework.batch.execution.repository.dao.StepExecutionDao;
import org.springframework.batch.execution.repository.dao.StepInstanceDao;
import org.springframework.batch.item.ExecutionContext;

/**
 * Test SimpleJobRepository. The majority of test cases are tested using
 * EasyMock, however, there were some issues with using it for the stepExecutionDao when
 * testing finding or creating steps, so an actual mock class had to be written.
 * 
 * @author Lucas Ward
 * 
 */
public class SimpleJobRepositoryTests extends TestCase {

	SimpleJobRepository jobRepository;

	JobSupport jobConfiguration;

	JobParameters jobParameters;

	Step stepConfiguration1;

	Step stepConfiguration2;

	MockControl jobDaoControl = MockControl.createControl(JobDao.class);

	MockControl stepExecutionDaoControl = MockControl.createControl(StepExecutionDao.class);
	
	MockControl stepInstanceDaoControl = MockControl.createControl(StepInstanceDao.class);

	JobDao jobDao;

	StepExecutionDao stepExecutionDao;
	
	StepInstanceDao stepInstanceDao;

	MockStepDao mockStepDao = new MockStepDao();

	JobInstance databaseJob;

	StepInstance databaseStep1;

	StepInstance databaseStep2;

	List steps;

	ExecutionContext executionContext;

	private JobExecution jobExecution;

	public void setUp() throws Exception {

		jobDao = (JobDao) jobDaoControl.getMock();
		stepExecutionDao = (StepExecutionDao) stepExecutionDaoControl.getMock();
		stepInstanceDao = (StepInstanceDao) stepInstanceDaoControl.getMock();

		jobRepository = new SimpleJobRepository(jobDao, jobDao, stepInstanceDao, stepExecutionDao);

		jobParameters = new JobParametersBuilder().toJobParameters();

		jobConfiguration = new JobSupport();
		jobConfiguration.setBeanName("RepositoryTest");
		jobConfiguration.setRestartable(true);

		stepConfiguration1 = new StepSupport("TestStep1");

		stepConfiguration2 = new StepSupport("TestStep2");

		List stepConfigurations = new ArrayList();
		stepConfigurations.add(stepConfiguration1);
		stepConfigurations.add(stepConfiguration2);

		jobConfiguration.setSteps(stepConfigurations);

		databaseJob = new JobInstance(new Long(1), jobParameters) {
			public JobExecution createJobExecution() {
				jobExecution = super.createJobExecution();
				return jobExecution;
			}
		};

		databaseStep1 = new StepInstance(new Long(1));
		databaseStep1.setLastExecution(new StepExecution(databaseStep1, null));
		databaseStep2 = new StepInstance(new Long(2));
		databaseStep2.setLastExecution(new StepExecution(databaseStep2, null));

		steps = new ArrayList();
		steps.add(databaseStep1);
		steps.add(databaseStep2);

		executionContext = new ExecutionContext();
	}

	/*
	 * Test a restartable job, that has not been run before.
	 */
	public void testCreateRestartableJob() throws Exception {

		List jobExecutions = new ArrayList();

		jobDao.findJobInstances(jobConfiguration.getName(), jobParameters);
		jobDaoControl.setReturnValue(jobExecutions);
		jobDao.createJobInstance(jobConfiguration.getName(), jobParameters);
		jobDaoControl.setReturnValue(databaseJob);
		stepInstanceDao.createStepInstance(databaseJob, "TestStep1");
		stepInstanceDaoControl.setReturnValue(databaseStep1);
		stepInstanceDao.createStepInstance(databaseJob, "TestStep2");
		stepInstanceDaoControl.setReturnValue(databaseStep2);
		jobDao.saveJobExecution(new JobExecution(databaseJob));
		jobDaoControl.setMatcher(new ArgumentsMatcher() {
			public boolean matches(Object[] expected, Object[] actual) {
				return ((JobExecution) actual[0]).getJobInstance().equals(databaseJob);
			}

			public String toString(Object[] arguments) {
				return "" + arguments[0];
			}
		});
		stepExecutionDaoControl.replay();
		stepInstanceDaoControl.replay();
		jobDaoControl.replay();
		JobInstance job = jobRepository.createJobExecution(jobConfiguration, jobParameters).getJobInstance();
		assertTrue(job.equals(databaseJob));
		List jobSteps = job.getStepInstances();
		Iterator it = jobSteps.iterator();
		StepInstance step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep1));
		step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep2));
	}

	public void testRestartedJob() throws Exception {

		final List executions = new ArrayList();
		JobExecution execution = databaseJob.createJobExecution();
		executions.add(execution);
		// For this test it is important that the execution is finished
		// and the executions in the list contain one with an end date
		execution.setEndTime(new Date(System.currentTimeMillis()));

		Entity databaseStep1Exec = new StepExecution(databaseStep1, execution, new Long(1));
		Entity databaseStep2Exec = new StepExecution(databaseStep2, execution, new Long(2));

		List jobs = new ArrayList();
		jobDao.findJobInstances(jobConfiguration.getName(), jobParameters);
		jobs.add(databaseJob);
		jobDaoControl.setReturnValue(jobs);
		stepInstanceDao.findStepInstance(databaseJob, "TestStep1");
		stepInstanceDaoControl.setReturnValue(databaseStep1);
		stepExecutionDao.getLastStepExecution(databaseStep1, jobExecution);
		stepExecutionDaoControl.setReturnValue(databaseStep1Exec);
		stepExecutionDao.findExecutionContext(databaseStep1Exec.getId());
		stepExecutionDaoControl.setReturnValue(executionContext);
		stepExecutionDao.getStepExecutionCount(databaseStep1);
		stepExecutionDaoControl.setReturnValue(1);
		stepInstanceDao.findStepInstance(databaseJob, "TestStep2");
		stepInstanceDaoControl.setReturnValue(databaseStep2);
		stepExecutionDao.getLastStepExecution(databaseStep2, jobExecution);
		stepExecutionDaoControl.setReturnValue(databaseStep2Exec);
		stepExecutionDao.findExecutionContext(databaseStep2Exec.getId());
		stepExecutionDaoControl.setReturnValue(executionContext);
		stepExecutionDao.getStepExecutionCount(databaseStep2);
		stepExecutionDaoControl.setReturnValue(1);
		stepExecutionDaoControl.replay();
		stepInstanceDaoControl.replay();
		jobDao.getJobExecutionCount(databaseJob.getId());
		jobDaoControl.setReturnValue(1);
		jobDao.findJobExecutions(databaseJob);
		jobDaoControl.setReturnValue(executions);
		jobDao.saveJobExecution(new JobExecution(databaseJob));
		jobDaoControl.setMatcher(new ArgumentsMatcher() {
			public boolean matches(Object[] expected, Object[] actual) {
				JobExecution execution = (JobExecution) actual[0];
				return execution.getJobInstance().equals(databaseJob);
			}

			public String toString(Object[] arguments) {
				return "" + arguments[0];
			}
		});
		jobDaoControl.setVoidCallable();
		jobDaoControl.replay();
		JobInstance job = jobRepository.createJobExecution(jobConfiguration, jobParameters).getJobInstance();
		assertTrue(job.equals(databaseJob));
		List jobSteps = job.getStepInstances();
		Iterator it = jobSteps.iterator();
		StepInstance step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep1));
		assertTrue(step.getStepExecutionCount() == 1);
		step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep2));
		assertTrue(step.getStepExecutionCount() == 1);
	}

	// Test that a restartable job that has multiple instances throws an
	// exception.
	public void testFindRestartableJobWithMultipleInstances() throws Exception {

		List jobs = new ArrayList();
		jobs.add(databaseJob);
		jobs.add(new JobInstance(new Long(127), jobParameters));
		jobDao.findJobInstances(jobConfiguration.getName(), jobParameters);
		jobDaoControl.setReturnValue(jobs);
		jobDaoControl.replay();

		try {
			jobRepository.createJobExecution(jobConfiguration, jobParameters);
			fail("Expected BatchRestartException");
		}
		catch (BatchRestartException e) {
			// expected
		}

		jobDaoControl.verify();
	}

	public void testRestartJobStartLimitExceeded() throws Exception {

		jobConfiguration.setStartLimit(1);

		List jobs = new ArrayList();
		jobDao.findJobInstances(jobConfiguration.getName(), jobParameters);
		jobs.add(databaseJob);
		jobDaoControl.setReturnValue(jobs);
		jobDao.getJobExecutionCount(databaseJob.getId());
		// return a greater execution count then the start limit, should throw
		// exception
		jobDaoControl.setReturnValue(2);
		jobDaoControl.replay();

		try {
			jobRepository.createJobExecution(jobConfiguration, jobParameters);
			fail();
		}
		catch (BatchRestartException ex) {
			// expected
		}

		jobDaoControl.verify();
	}

	public void testCreateNonRestartableJob() throws Exception {

		List jobs = new ArrayList();
		jobConfiguration.setRestartable(false);

		jobDao.findJobInstances(jobConfiguration.getName(), jobParameters);
		jobDaoControl.setReturnValue(jobs);
		jobDao.createJobInstance(jobConfiguration.getName(), jobParameters);
		jobDaoControl.setReturnValue(databaseJob);
		stepInstanceDao.createStepInstance(databaseJob, "TestStep1");
		stepInstanceDaoControl.setReturnValue(databaseStep1);
		stepInstanceDao.createStepInstance(databaseJob, "TestStep2");
		stepInstanceDaoControl.setReturnValue(databaseStep2);
		jobDao.saveJobExecution(new JobExecution(databaseJob));
		jobDaoControl.setMatcher(new ArgumentsMatcher() {
			public boolean matches(Object[] expected, Object[] actual) {
				return ((JobExecution) actual[0]).getJobInstance().equals(databaseJob);
			}

			public String toString(Object[] arguments) {
				return "" + arguments[0];
			}
		});
		stepExecutionDaoControl.replay();
		stepInstanceDaoControl.replay();
		jobDaoControl.replay();
		JobInstance job = jobRepository.createJobExecution(jobConfiguration, jobParameters).getJobInstance();
		assertTrue(job.equals(databaseJob));
		List jobSteps = job.getStepInstances();
		Iterator it = jobSteps.iterator();
		StepInstance step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep1));
		step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep2));
	}


	public void testSaveOrUpdateInvalidJobExecution() {

		// failure scenario - must have job ID
		JobExecution jobExecution = new JobExecution(null);
		try {
			jobRepository.saveOrUpdate(jobExecution);
			fail();
		}
		catch (Exception ex) {
			// expected
		}
	}

	public void testSaveOrUpdateValidJobExecution() throws Exception {

		JobExecution jobExecution = new JobExecution(new JobInstance(new Long(1), jobParameters));

		// new execution - call save on job dao
		jobDao.saveJobExecution(jobExecution);
		jobDaoControl.replay();
		jobRepository.saveOrUpdate(jobExecution);
		jobDaoControl.reset();

		// update existing execution
		jobExecution.setId(new Long(5));
		jobDao.updateJobExecution(jobExecution);
		jobDaoControl.replay();
		jobRepository.saveOrUpdate(jobExecution);
	}

	public void testUpdateStepExecution() {
		StepExecution stepExecution = new StepExecution(new StepInstance(new Long(10L)), null, new Long(1));
		stepExecution.setId(new Long(11));
		ExecutionContext executionContext = new ExecutionContext();
		stepExecution.setExecutionContext(executionContext);
		stepExecutionDao.updateStepExecution(stepExecution);
		stepExecutionDao.updateExecutionContext(stepExecution.getId(), executionContext);
		stepExecutionDaoControl.replay();
		jobRepository.saveOrUpdate(stepExecution);
		stepExecutionDaoControl.verify();
	}

	public void testSaveExistingStepExecution() {
		StepExecution stepExecution = new StepExecution(new StepInstance(new Long(10L)), new JobExecution(null), null);
		ExecutionContext executionContext = new ExecutionContext();
		stepExecution.setExecutionContext(executionContext);
		stepExecutionDao.saveStepExecution(stepExecution);
		stepExecutionDao.saveExecutionContext(stepExecution.getId(), executionContext);
		stepExecutionDaoControl.replay();
		jobRepository.saveOrUpdate(stepExecution);
		stepExecutionDaoControl.verify();
	}

	public void testSaveOrUpdateStepExecutionException() {

		StepExecution stepExecution = new StepExecution(null, null, null);

		// failure scenario -- no step id set.
		try {
			jobRepository.saveOrUpdate(stepExecution);
			fail();
		}
		catch (Exception ex) {
			// expected
		}
	}

	/*
	 * Test to ensure that if a StepDao returns invalid restart data, it is
	 * corrected.
	 */
	public void testCreateStepsFixesInvalidExecutionContext() throws Exception {

		List jobs = new ArrayList();

		jobDao.findJobInstances(jobConfiguration.getName(), jobParameters);
		jobDaoControl.setReturnValue(jobs);
		jobDao.createJobInstance(jobConfiguration.getName(), jobParameters);
		jobDaoControl.setReturnValue(databaseJob);
		stepInstanceDao.createStepInstance(databaseJob, "TestStep1");
		stepInstanceDaoControl.setReturnValue(databaseStep1);
		stepInstanceDao.createStepInstance(databaseJob, "TestStep2");
		stepInstanceDaoControl.setReturnValue(databaseStep2);
		jobDao.saveJobExecution(new JobExecution(databaseJob));
		jobDaoControl.setMatcher(new ArgumentsMatcher() {
			public boolean matches(Object[] expected, Object[] actual) {
				return ((JobExecution) actual[0]).getJobInstance().equals(databaseJob);
			}

			public String toString(Object[] arguments) {
				return "" + arguments[0];
			}
		});
		stepExecutionDaoControl.replay();
		stepInstanceDaoControl.replay();
		jobDaoControl.replay();
		JobInstance job = jobRepository.createJobExecution(jobConfiguration, jobParameters).getJobInstance();
		List jobSteps = job.getStepInstances();
		Iterator it = jobSteps.iterator();
		StepInstance step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep1));
		step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep2));
	}

	public void testFindStepsFixesInvalidExecutionContext() throws Exception {

		Entity databaseStep1Exec = new StepExecution(databaseStep1, null, new Long(1));
		Entity databaseStep2Exec = new StepExecution(databaseStep2, null, new Long(2));

		List jobs = new ArrayList();
		jobDao.findJobInstances(jobConfiguration.getName(), jobParameters);
		jobs.add(databaseJob);
		jobDaoControl.setReturnValue(jobs);
		stepInstanceDao.findStepInstance(databaseJob, "TestStep1");
		stepInstanceDaoControl.setReturnValue(databaseStep1);
		stepExecutionDao.getLastStepExecution(databaseStep1, null);
		stepExecutionDaoControl.setReturnValue(databaseStep1Exec);
		stepExecutionDao.findExecutionContext(databaseStep1Exec.getId());
		stepExecutionDaoControl.setReturnValue(executionContext);
		stepExecutionDao.getStepExecutionCount(databaseStep1);
		stepExecutionDaoControl.setReturnValue(1);
		stepInstanceDao.findStepInstance(databaseJob, "TestStep2");
		stepInstanceDaoControl.setReturnValue(databaseStep2);
		stepExecutionDao.getLastStepExecution(databaseStep2, null);
		stepExecutionDaoControl.setReturnValue(databaseStep2Exec);
		stepExecutionDao.findExecutionContext(databaseStep2Exec.getId());
		stepExecutionDaoControl.setReturnValue(executionContext);
		stepExecutionDao.getStepExecutionCount(databaseStep2);
		stepExecutionDaoControl.setReturnValue(1);
		stepExecutionDaoControl.replay();
		stepInstanceDaoControl.replay();
		
		jobDao.getJobExecutionCount(databaseJob.getId());
		jobDaoControl.setReturnValue(1);
		jobDao.findJobExecutions(databaseJob);
		jobDaoControl.setReturnValue(new ArrayList());
		jobDao.saveJobExecution(new JobExecution(databaseJob));
		jobDaoControl.setMatcher(new ArgumentsMatcher() {
			public boolean matches(Object[] expected, Object[] actual) {
				return ((JobExecution) actual[0]).getJobInstance().equals(databaseJob);
			}

			public String toString(Object[] arguments) {
				return "" + arguments[0];
			}
		});
		jobDaoControl.replay();
		JobInstance job = jobRepository.createJobExecution(jobConfiguration, jobParameters).getJobInstance();
		assertTrue(job.equals(databaseJob));
		List jobSteps = job.getStepInstances();
		Iterator it = jobSteps.iterator();
		StepInstance step = (StepInstance) it.next();
		assertTrue(step.equals(databaseStep1));
		assertTrue(step.getLastExecution().getExecutionContext().isEmpty());
		step = (StepInstance) it.next();
		assertTrue(step.getLastExecution().getExecutionContext().isEmpty());
		assertTrue(step.equals(databaseStep2));
	}

}
