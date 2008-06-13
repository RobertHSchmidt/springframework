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
package org.springframework.integration.batch.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.integration.batch.JobRepositorySupport;
import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.channel.ThreadLocalChannel;
import org.springframework.integration.dispatcher.DirectChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.message.Message;
import org.springframework.integration.message.Target;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 * 
 */
public class MessageOrientedStepTests {

	private MessageOrientedStep step = new MessageOrientedStep();

	private JobExecution jobExecution;

	private DirectChannel requestChannel;

	private MessageChannel replyChannel;

	/**
	 * @return
	 */
	@Before
	public void createStep() {
		replyChannel = new ThreadLocalChannel();
		requestChannel = new DirectChannel();
		step.setName("step");
		step.setRequestChannel(requestChannel);
		step.setReplyChannel(replyChannel);
		step.setStartLimit(10);
		step.setJobRepository(new JobRepositorySupport());
		JobInstance jobInstance = new JobInstance(0L, new JobParameters(), "job");
		jobExecution = new JobExecution(jobInstance);
	}

	/**
	 * Test method for
	 * {@link org.springframework.integration.batch.job.MessageOrientedStep#setRequestChannel(org.springframework.integration.channel.MessageChannel)}.
	 */
	@Test
	public void testSetRequestChannel() {
		Method method = ReflectionUtils.findMethod(MessageOrientedStep.class, "setRequestChannel",
				new Class<?>[] { MessageChannel.class });
		assertNotNull(method);
		Annotation[] annotations = AnnotationUtils.getAnnotations(method);
		assertEquals(1, annotations.length);
		assertEquals(Required.class, annotations[0].annotationType());
	}

	/**
	 * Test method for
	 * {@link org.springframework.integration.batch.job.MessageOrientedStep#setReplyChannel(org.springframework.integration.channel.MessageChannel)}.
	 */
	@Test
	public void testSetReplyChannel() {
		Method method = ReflectionUtils.findMethod(MessageOrientedStep.class, "setReplyChannel",
				new Class<?>[] { MessageChannel.class });
		assertNotNull(method);
		Annotation[] annotations = AnnotationUtils.getAnnotations(method);
		assertEquals(1, annotations.length);
		assertEquals(Required.class, annotations[0].annotationType());
	}

	/**
	 * Test method for
	 * {@link org.springframework.integration.batch.job.MessageOrientedStep#execute(org.springframework.batch.core.StepExecution)}.
	 * @throws Exception
	 * @throws
	 */
	@Test
	public void testExecuteWithTimeout() throws Exception {
		try {
			step.execute(jobExecution.createStepExecution(step));
			fail("Expected StepExecutionTimeoutException");
		}
		catch (StepExecutionTimeoutException e) {
			// expected
			String message = e.getMessage();
			assertTrue("Wrong message: " + message, message.contains("waiting for steps"));
		}
	}

	@Test
	public void testVanillaExecute() throws Exception {
		requestChannel.subscribe(new Target() {
			public boolean send(Message<?> message) {
				JobExecutionRequest jobExecution = (JobExecutionRequest) message.getPayload();
				jobExecution.setStatus(BatchStatus.COMPLETED);
				return replyChannel.send(message);
			}
		});
		step.execute(jobExecution.createStepExecution(step));
	}

	@Test
	public void testExecuteWithFailure() throws Exception {
		requestChannel.subscribe(new Target() {
			public boolean send(Message<?> message) {
				JobExecutionRequest jobExecution = (JobExecutionRequest) message.getPayload();
				jobExecution.registerThrowable(new RuntimeException("Planned failure"));
				return replyChannel.send(message);
			}
		});
		try {
			step.execute(jobExecution.createStepExecution(step));
			fail("Expected RuntimeException");
		}
		catch (RuntimeException e) {
			// expected
			String message = e.getMessage();
			assertEquals("Wrong message: " + message, "Planned failure", message);
		}
	}

	@Test
	public void testExecuteOnRestart() throws Exception {
		JobExecutionRequest jobExecutionRequest = new JobExecutionRequest(jobExecution);
		jobExecutionRequest.setStatus(BatchStatus.COMPLETED);
		// Send a message to the reply channel to simulate step that we were
		// waiting for when we failed on the last execution.
		replyChannel.send(new GenericMessage<JobExecutionRequest>(jobExecutionRequest));
		StepExecution stepExecution = jobExecution.createStepExecution(step);
		stepExecution.getExecutionContext().putString(MessageOrientedStep.WAITING, "true");
		step.execute(stepExecution);
	}

}
