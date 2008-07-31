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
package org.springframework.batch.core;

/**
 * Provide callbacks at specific points in the lifecycle of a {@link Job}.
 * Implementations can be stateful if they are careful to either ensure thread
 * safety, or to use one instance of a listener per job, assuming that job
 * instances themselves are not used by more than one thread.
 * 
 * @author Dave Syer
 * 
 */
public interface JobExecutionListener {

	/**
	 * Initialise the state of the listener with the {@link JobExecution} from
	 * the current scope.
	 * @param jobExecution the current {@link JobExecution}
	 */
	void beforeJob(JobExecution jobExecution);

	/**
	 * Callback after successful completion of a job.
	 * @param jobExecution the current {@link JobExecution}
	 */
	void afterJob(JobExecution jobExecution);
	
	/**
	 * Callback on job failure owing to the throwable provided.
	 * @param jobExecution the current {@link JobExecution}
	 * @param e the exception that caused the job to terminate
	 */
	void onError(JobExecution jobExecution, Throwable e);

	/**
	 * Callback when a job is interrupted or stopped manually.
	 * @param jobExecution the current {@link JobExecution}
	 */
	void onInterrupt(JobExecution jobExecution);
}