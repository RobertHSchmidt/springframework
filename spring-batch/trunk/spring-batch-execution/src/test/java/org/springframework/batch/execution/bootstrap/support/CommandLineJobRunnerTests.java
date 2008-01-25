/*
 * Copyright 2006-2008 the original author or authors.
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
package org.springframework.batch.execution.bootstrap.support;

import junit.framework.TestCase;

import org.springframework.batch.core.domain.Job;
import org.springframework.batch.core.domain.JobExecution;
import org.springframework.batch.core.domain.JobParameters;
import org.springframework.batch.core.executor.ExitCodeExceptionClassifier;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.execution.launch.JobLauncher;
import org.springframework.batch.repeat.ExitStatus;

/**
 * @author Lucas Ward
 *
 */
public class CommandLineJobRunnerTests extends TestCase {

	private static final String JOB = "org/springframework/batch/execution/bootstrap/support/job.xml";
	private static final String TEST_BATCH_ENVIRONMENT = "org/springframework/batch/execution/bootstrap/support/test-environment.xml";
	private static final String JOB_NAME = "test-job";
	
	private String jobPath = JOB;
	private String environmentPath = TEST_BATCH_ENVIRONMENT;
	private String jobName = JOB_NAME;
	private String jobKey = "job.Key=myKey";
	private String scheduleDate = "schedule.Date=01/23/2008";
	private String vendorId = "vendor.id=33243243";
	
	private String[] args = new String[]{jobPath, jobName, environmentPath, jobKey, scheduleDate, vendorId};
	
	private JobExecution jobExecution;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		jobExecution = new JobExecution(null, new Long(1));
		ExitStatus exitStatus = ExitStatus.FINISHED;
		jobExecution.setExitStatus(exitStatus);
	}

	public void testMain(){
		
		StubJobLauncher.jobExecution = jobExecution;
		
		CommandLineJobRunner.main(args);
		
		assertEquals(0, StubSystemExiter.getStatus());
	}
	
	public void testJobAlreadyRunning(){
		
		StubJobLauncher.throwExecutionRunningException = true;
		
		CommandLineJobRunner.main(args);
		
		assertTrue(StubExceptionClassifier.exception instanceof JobExecutionAlreadyRunningException);
	}
	
	//can't test because it will cause the system to exit.
//	public void testInvalidArgs(){
//		
//		String[] args = new String[]{jobPath, jobName};
//		CommandLineJobRunner.main(args);
//	}
	
	public void testWithNoParameters(){
		
		String[] args = new String[]{jobPath, jobName, environmentPath};
		CommandLineJobRunner.main(args);
		assertEquals(new JobParameters(), StubJobLauncher.jobParameters);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		
		StubJobLauncher.tearDown();
	}
	
	public static class StubSystemExiter implements SystemExiter {

		public static int status;

		public void exit(int status) {
			StubSystemExiter.status = status;
		}

		public static int getStatus() {
			return status;
		}
	}
	
	public static class StubJobLauncher implements JobLauncher{

		public static JobExecution jobExecution;
		public static boolean throwExecutionRunningException = false;
		public static JobParameters jobParameters;
		
		public JobExecution run(Job job, JobParameters jobParameters)
				throws JobExecutionAlreadyRunningException {
		
			StubJobLauncher.jobParameters = jobParameters;
			
			if(throwExecutionRunningException){
				throw new JobExecutionAlreadyRunningException("");
			}
			
			return jobExecution;
		}
		
		public static void tearDown(){
			jobExecution = null;
			throwExecutionRunningException = false;
			jobParameters = null;
		}
	}
	
	public static class StubExceptionClassifier implements ExitCodeExceptionClassifier{

		public static Throwable exception;
		
		public Object classify(Throwable throwable) {
			return null;
		}

		public Object getDefault() {
			return null;
		}

		public ExitStatus classifyForExitCode(Throwable throwable) {
			exception = throwable;
			return ExitStatus.FAILED;
		}
		
	}
}
