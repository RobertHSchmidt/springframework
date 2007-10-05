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

package org.springframework.batch.execution.bootstrap;

import junit.framework.TestCase;

import org.springframework.batch.core.configuration.JobConfiguration;
import org.springframework.batch.core.configuration.NoSuchJobConfigurationException;
import org.springframework.batch.core.domain.JobIdentifier;
import org.springframework.batch.core.runtime.SimpleJobIdentifierFactory;
import org.springframework.batch.execution.facade.JobExecutorFacade;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

public class SimpleJobLauncherTests extends TestCase {

	public void testStartWithNoConfiguration() throws Exception {
		final SimpleJobLauncher launcher = new SimpleJobLauncher();
		try {
			launcher.afterPropertiesSet();
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// expected
			assertTrue(e.getMessage().indexOf("required") >= 0);
		}
	}

	public void testInitializeWithNoConfiguration() throws Exception {
		final SimpleJobLauncher launcher = new SimpleJobLauncher();
		try {
			launcher.run();
			// should do nothing
			fail("Expected NoSuchJobConfigurationException");
		}
		catch (NoSuchJobConfigurationException e) {
			assertTrue("Message should mention null job name: "+e.getMessage(), e.getMessage().toLowerCase().indexOf("null")>=0);
		}
	}

	public void testRunTwiceNotFatal() throws Exception {
		SimpleJobLauncher launcher = new SimpleJobLauncher();
		launcher.setJobIdentifierFactory(new SimpleJobIdentifierFactory());
		InterruptibleFacade jobExecutorFacade = new InterruptibleFacade();
		launcher.setJobExecutorFacade(jobExecutorFacade);
		launcher.setJobConfigurationName(new JobConfiguration("foo").getName());
		launcher.run();
		assertFalse(launcher.isRunning());
		launcher.run();
		// Both jobs finished running because they were not launched in a new
		// Thread
		assertFalse(launcher.isRunning());
	}

	public void testInterruptContainer() throws Exception {
		final SimpleJobLauncher launcher = new SimpleJobLauncher();
		launcher.setJobIdentifierFactory(new SimpleJobIdentifierFactory());

		InterruptibleFacade jobExecutorFacade = new InterruptibleFacade();
		launcher.setJobExecutorFacade(jobExecutorFacade);
		launcher.setJobConfigurationName(new JobConfiguration("foo").getName());
		
		TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		Runnable launcherRunnable = new Runnable() {
			public void run() {
				try {
					launcher.run();
				} catch (NoSuchJobConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		taskExecutor.execute(launcherRunnable); 
		
		// give the thread a second to start up
		Thread.sleep(100);
		assertTrue(launcher.isRunning());
		launcher.stop();
		Thread.sleep(100);
		assertFalse(launcher.isRunning());
	}

	public void testStopOnUnranLauncher() {

		SimpleJobLauncher launcher = new SimpleJobLauncher();

		assertFalse(launcher.isRunning());
		// no exception should be thrown if stop is called on unran
		// container
		// this is to fullfill the contract outlined in Lifecycle#stop().
		launcher.stop();
	}

	private class InterruptibleFacade implements JobExecutorFacade {
		/*
		 * (non-Javadoc)
		 * @see org.springframework.batch.container.BatchContainer#run()
		 */
		public void run() {
			try {
				// 1 seconds should be long enough to allow the thread to be
				// run and for interrupt to be called;
				Thread.sleep(300);
				//return ExitStatus.FAILED;
				
			}
			catch (InterruptedException ex) {
				// thread interrupted, allow to exit normally
				//return ExitStatus.FAILED;
			}
			
			
		}

		public ExitStatus start(JobIdentifier runtimeInformation) {
			run();
			return ExitStatus.FAILED;
		}

		public void stop(JobIdentifier runtimeInformation) {
			// not needed
		}
		
		public boolean isRunning() {
			// not needed
			return false;
		}
	}

}
