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

package org.springframework.batch.execution.job;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.ItemSkipPolicy;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.JobListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.execution.repository.SimpleJobRepository;
import org.springframework.batch.execution.repository.dao.JobExecutionDao;
import org.springframework.batch.execution.repository.dao.JobInstanceDao;
import org.springframework.batch.execution.repository.dao.MapJobExecutionDao;
import org.springframework.batch.execution.repository.dao.MapJobInstanceDao;
import org.springframework.batch.execution.repository.dao.MapStepExecutionDao;
import org.springframework.batch.execution.repository.dao.StepExecutionDao;
import org.springframework.batch.execution.step.AbstractStep;
import org.springframework.batch.execution.step.support.NeverSkipItemSkipPolicy;
import org.springframework.batch.item.AbstractItemReader;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.retry.RetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Tests for DefaultJobLifecycle. MapJobDao and MapStepExecutionDao are used instead of a mock repository to test that
 * status is being stored correctly.
 * 
 * @author Lucas Ward
 */
public class SimpleJobTests extends TestCase {

	private JobRepository jobRepository;

	private JobInstanceDao jobInstanceDao;

	private JobExecutionDao jobExecutionDao;

	private StepExecutionDao stepExecutionDao;

	private List list = new ArrayList();

	private JobInstance jobInstance;

	private JobExecution jobExecution;

	private StepExecution stepExecution1;

	private StepExecution stepExecution2;

	private StubStep stepConfiguration1;

	private StubStep stepConfiguration2;

	private JobParameters jobParameters = new JobParameters();

	private SimpleJob job;

	private Step step1;

	private Step step2;

	protected void setUp() throws Exception {
		super.setUp();

		MapJobInstanceDao.clear();
		MapJobExecutionDao.clear();
		MapStepExecutionDao.clear();
		jobInstanceDao = new MapJobInstanceDao();
		jobExecutionDao = new MapJobExecutionDao();
		stepExecutionDao = new MapStepExecutionDao();
		jobRepository = new SimpleJobRepository(jobInstanceDao, jobExecutionDao, stepExecutionDao);
		job = new SimpleJob();
		job.setJobRepository(jobRepository);

		stepConfiguration1 = new StubStep("TestStep1");
		stepConfiguration1.setCallback(new Runnable() {
			public void run() {
				list.add("default");
			}
		});
		stepConfiguration2 = new StubStep("TestStep2");
		stepConfiguration2.setCallback(new Runnable() {
			public void run() {
				list.add("default");
			}
		});
		stepConfiguration1.setJobRepository(jobRepository);
		stepConfiguration2.setJobRepository(jobRepository);

		List stepConfigurations = new ArrayList();
		stepConfigurations.add(stepConfiguration1);
		stepConfigurations.add(stepConfiguration2);
		job.setName("testJob");
		job.setSteps(stepConfigurations);

		jobExecution = jobRepository.createJobExecution(job, jobParameters);
		jobInstance = jobExecution.getJobInstance();

		List steps = jobInstance.getJob().getSteps();
		step1 = (Step) steps.get(0);
		step2 = (Step) steps.get(1);
		stepExecution1 = new StepExecution(step1, jobExecution, null);
		stepExecution2 = new StepExecution(step2, jobExecution, null);

	}

	// Test to ensure the exit status returned by the last step is returned
	public void testExitStatusReturned() throws JobExecutionException {

		final ExitStatus customStatus = new ExitStatus(true, "test");

		Step testStep = new Step() {

			public void execute(StepExecution stepExecution) throws JobInterruptedException {
				stepExecution.setExitStatus(customStatus);
			}

			public String getName() {
				return "test";
			}

			public int getStartLimit() {
				return 1;
			}

			public boolean isAllowStartIfComplete() {
				return false;
			}
		};
		List steps = new ArrayList();
		steps.add(testStep);
		job.setSteps(steps);
		job.execute(jobExecution);
		assertEquals(customStatus, jobExecution.getExitStatus());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRunNormally() throws Exception {
		stepConfiguration1.setStartLimit(5);
		stepConfiguration2.setStartLimit(5);
		job.execute(jobExecution);
		assertEquals(2, list.size());
		checkRepository(BatchStatus.COMPLETED);
		assertNotNull(jobExecution.getEndTime());
		assertNotNull(jobExecution.getStartTime());
	}

	public void testRunNormallyWithListener() throws Exception {
		job.setJobListeners(new JobListenerSupport[] { new JobListenerSupport() {
			public void beforeJob(JobExecution jobExecution) {
				list.add("before");
			}

			public void afterJob(JobExecution jobExecution) {
				list.add("after");
			}
		} });
		job.execute(jobExecution);
		assertEquals(4, list.size());
	}

	public void testRunWithSimpleStepExecutor() throws Exception {

		job.setJobRepository(jobRepository);
		// do not set StepExecutorFactory...
		stepConfiguration1.setStartLimit(5);
		stepConfiguration1.setItemReader(new AbstractItemReader() {
			public Object read() throws Exception {
				list.add("1");
				return null;
			}
		});
		stepConfiguration2.setStartLimit(5);
		stepConfiguration2.setItemReader(new AbstractItemReader() {
			public Object read() throws Exception {
				list.add("2");
				return null;
			}
		});
		job.execute(jobExecution);
		assertEquals(2, list.size());
		checkRepository(BatchStatus.COMPLETED, ExitStatus.FINISHED);

	}

	public void testExecutionContextIsSet() throws Exception {
		testRunNormally();
		assertEquals(jobInstance, jobExecution.getJobInstance());
		assertEquals(2, jobExecution.getStepExecutions().size());
		assertEquals(step1.getName(), stepExecution1.getStepName());
		assertEquals(step2.getName(), stepExecution2.getStepName());
	}

	public void testInterrupted() throws Exception {
		stepConfiguration1.setStartLimit(5);
		stepConfiguration2.setStartLimit(5);
		final JobInterruptedException exception = new JobInterruptedException("Interrupt!");
		stepConfiguration1.setProcessException(exception);
		try {
			job.execute(jobExecution);
		} catch (UnexpectedJobExecutionException e) {
			assertEquals(exception, e.getCause());
		}
		assertEquals(0, list.size());
		checkRepository(BatchStatus.STOPPED, ExitStatus.INTERRUPTED);
	}

	public void testFailed() throws Exception {
		stepConfiguration1.setStartLimit(5);
		stepConfiguration2.setStartLimit(5);
		final RuntimeException exception = new RuntimeException("Foo!");
		stepConfiguration1.setProcessException(exception);
		try {
			job.execute(jobExecution);
		} catch (RuntimeException e) {
			assertEquals(exception, e);
		}
		assertEquals(0, list.size());
		checkRepository(BatchStatus.FAILED, ExitStatus.FAILED);
	}

	public void testStepShouldNotStart() throws Exception {
		// Start policy will return false, keeping the step from being started.
		stepConfiguration1.setStartLimit(0);

		try {
			job.execute(jobExecution);
			fail("Expected BatchCriticalException");
		} catch (UnexpectedJobExecutionException ex) {
			// expected
			assertTrue("Wrong message in exception: " + ex.getMessage(), ex.getMessage()
			        .indexOf("start limit exceeded") >= 0);
		}
	}

	public void testNoSteps() throws Exception {
		job.setSteps(new ArrayList());

		job.execute(jobExecution);
		ExitStatus exitStatus = jobExecution.getExitStatus();
		assertTrue("Wrong message in execution: " + exitStatus, exitStatus.getExitDescription().indexOf(
		        "No steps configured") >= 0);
	}

	// public void testNoStepsExecuted() throws Exception {
	// StepExecution completedExecution = new StepExecution("completedExecution", jobExecution);
	// completedExecution.setStatus(BatchStatus.COMPLETED);
	//
	// job.execute(jobExecution);
	// ExitStatus exitStatus = jobExecution.getExitStatus();
	// assertEquals(ExitStatus.NOOP.getExitCode(), exitStatus.getExitCode());
	// assertTrue("Wrong message in execution: " + exitStatus, exitStatus.getExitDescription().contains(
	// "steps already completed"));
	// }

	public void testNotExecutedIfAlreadyStopped() throws Exception {
		jobExecution.stop();
		try {
			job.execute(jobExecution);
		} catch (UnexpectedJobExecutionException e) {
			assertTrue(e.getCause() instanceof JobInterruptedException);
		}
		assertEquals(0, list.size());
		checkRepository(BatchStatus.STOPPED, ExitStatus.NOOP);
		ExitStatus exitStatus = jobExecution.getExitStatus();
		assertEquals(ExitStatus.NOOP.getExitCode(), exitStatus.getExitCode());
	}

	/*
	 * Check JobRepository to ensure status is being saved.
	 */
	private void checkRepository(BatchStatus status, ExitStatus exitStatus) {
		assertEquals(jobInstance, jobInstanceDao.getJobInstance(jobInstance.getJob(), jobParameters));
		// because map dao stores in memory, it can be checked directly
		JobExecution jobExecution = (JobExecution) jobExecutionDao.findJobExecutions(jobInstance).get(0);
		assertEquals(jobInstance.getId(), jobExecution.getJobId());
		assertEquals(status, jobExecution.getStatus());
		if (exitStatus != null) {
			assertEquals(jobExecution.getExitStatus().getExitCode(), exitStatus.getExitCode());
		}
	}

	private void checkRepository(BatchStatus status) {
		checkRepository(status, null);
	}

	private class StubStep extends AbstractStep {

		private Runnable runnable;
		private Exception exception;
		protected ExceptionHandler exceptionHandler;
		protected RetryPolicy retryPolicy;
		protected JobRepository jobRepository;
		protected PlatformTransactionManager transactionManager;
		protected ItemReader itemReader;
		protected ItemWriter itemWriter;
		protected ItemSkipPolicy itemSkipPolicy = new NeverSkipItemSkipPolicy();

		/**
		 * @param string
		 */
		public StubStep(String string) {
			super(string);
		}

		/**
		 * @param exception
		 */
		public void setProcessException(Exception exception) {
			this.exception = exception;
		}

		/**
		 * @param runnable
		 */
		public void setCallback(Runnable runnable) {
			this.runnable = runnable;
		}

		public void execute(StepExecution stepExecution) throws JobInterruptedException, UnexpectedJobExecutionException {
			if (exception instanceof RuntimeException) {
				stepExecution.setExitStatus(ExitStatus.FAILED);
				throw (RuntimeException) exception;
			}
			if (exception instanceof JobInterruptedException) {
				stepExecution.setExitStatus(ExitStatus.INTERRUPTED);
				throw (JobInterruptedException) exception;
			}
			if (runnable != null) {
				runnable.run();
			}
			stepExecution.setExitStatus(ExitStatus.FINISHED);
		}

		/**
		 * Set the name property. Always overrides the default value if this object is a Spring bean.
		 * 
		 * @see #setBeanName(java.lang.String)
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Public setter for the {@link RetryPolicy}.
		 * 
		 * @param retryPolicy the {@link RetryPolicy} to set
		 */
		public void setRetryPolicy(RetryPolicy retryPolicy) {
			this.retryPolicy = retryPolicy;
		}

		public void setExceptionHandler(ExceptionHandler exceptionHandler) {
			this.exceptionHandler = exceptionHandler;
		}

		/**
		 * Public setter for {@link JobRepository}.
		 * 
		 * @param jobRepository is a mandatory dependence (no default).
		 */
		public void setJobRepository(JobRepository jobRepository) {
			this.jobRepository = jobRepository;
		}

		/**
		 * Public setter for the {@link PlatformTransactionManager}.
		 * 
		 * @param transactionManager the transaction manager to set
		 */
		public void setTransactionManager(PlatformTransactionManager transactionManager) {
			this.transactionManager = transactionManager;
		}

		/**
		 * @param itemReader the itemReader to set
		 */
		public void setItemReader(ItemReader itemReader) {
			this.itemReader = itemReader;
		}

		/**
		 * @param itemWriter the itemWriter to set
		 */
		public void setItemWriter(ItemWriter itemWriter) {
			this.itemWriter = itemWriter;
		}

		public void setItemSkipPolicy(ItemSkipPolicy itemSkipPolicy) {
			this.itemSkipPolicy = itemSkipPolicy;
		}

	}
}
