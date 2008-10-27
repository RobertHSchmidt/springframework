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
package org.springframework.batch.core.job.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.core.step.StepSupport;
import org.springframework.batch.flow.SimpleFlow;
import org.springframework.batch.flow.StateTransition;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;

/**
 * @author Dave Syer
 * 
 */
public class FlowJobTests {

	private FlowJob job = new FlowJob();

	private JobExecution jobExecution;

	private JobRepository jobRepository;

	@Before
	public void setUp() throws Exception {
		MapJobRepositoryFactoryBean.clear();
		MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
		factory.setTransactionManager(new ResourcelessTransactionManager());
		factory.afterPropertiesSet();
		jobRepository = (JobRepository) factory.getObject();
		job.setJobRepository(jobRepository);
		jobExecution = jobRepository.createJobExecution("job", new JobParameters());
	}

	@Test
	public void testTwoSteps() throws Exception {
		SimpleFlow<JobFlowExecutor> flow = new SimpleFlow<JobFlowExecutor>("job");
		Collection<StateTransition<JobFlowExecutor>> transitions = new ArrayList<StateTransition<JobFlowExecutor>>();
		transitions.add(StateTransition.createStateTransition(new StepState(new StubStep("step1")), "step2"));
		transitions.add(StateTransition.createEndStateTransition(new StepState(new StubStep("step2"))));
		flow.setStateTransitions(transitions);
		job.setFlow(flow);
		job.afterPropertiesSet();
		StepExecution stepExecution = job.doExecute(jobExecution);
		assertEquals(ExitStatus.FINISHED, stepExecution.getExitStatus());
		assertEquals(2, jobExecution.getStepExecutions().size());
	}

	@Test
	public void testFailedStep() throws Exception {
		SimpleFlow<JobFlowExecutor> flow = new SimpleFlow<JobFlowExecutor>("job");
		Collection<StateTransition<JobFlowExecutor>> transitions = new ArrayList<StateTransition<JobFlowExecutor>>();
		transitions.add(StateTransition.createStateTransition(new StepState(new StepSupport("step1") {
			@Override
			public void execute(StepExecution stepExecution) throws JobInterruptedException,
					UnexpectedJobExecutionException {
				stepExecution.setStatus(BatchStatus.FAILED);
				stepExecution.setExitStatus(ExitStatus.FAILED);
			}
		}), "step2"));
		transitions.add(StateTransition.createEndStateTransition(new StepState(new StubStep("step2"))));
		flow.setStateTransitions(transitions);
		job.setFlow(flow);
		job.afterPropertiesSet();
		StepExecution stepExecution = job.doExecute(jobExecution);
		assertEquals(ExitStatus.FINISHED, stepExecution.getExitStatus());
		assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
		assertEquals(2, jobExecution.getStepExecutions().size());
	}

	@Test
	public void testStoppingStep() throws Exception {
		SimpleFlow<JobFlowExecutor> flow = new SimpleFlow<JobFlowExecutor>("job");
		Collection<StateTransition<JobFlowExecutor>> transitions = new ArrayList<StateTransition<JobFlowExecutor>>();
		transitions.add(StateTransition.createStateTransition(new StepState(new StepSupport("step1") {
			@Override
			public void execute(StepExecution stepExecution) throws JobInterruptedException,
					UnexpectedJobExecutionException {
				stepExecution.setStatus(BatchStatus.STOPPED);
			}
		}), "step2"));
		transitions.add(StateTransition.createEndStateTransition(new StepState(new StubStep("step2"))));
		flow.setStateTransitions(transitions);
		job.setFlow(flow);
		job.afterPropertiesSet();
		try {
			job.doExecute(jobExecution);
			fail("Expected JobInterruptedException");
		}
		catch (JobInterruptedException e) {
			// expected
		}
		assertEquals(1, jobExecution.getStepExecutions().size());
	}

	@Test
	public void testBranching() throws Exception {
		SimpleFlow<JobFlowExecutor> flow = new SimpleFlow<JobFlowExecutor>("job");
		Collection<StateTransition<JobFlowExecutor>> transitions = new ArrayList<StateTransition<JobFlowExecutor>>();
		transitions.add(StateTransition.createStateTransition(new StepState(new StubStep("step1")), "step2"));
		transitions.add(StateTransition.createStateTransition(new StepState(new StubStep("step1")), "COMPLETED", "step3"));
		transitions.add(StateTransition.createEndStateTransition(new StepState(new StubStep("step2"))));
		transitions.add(StateTransition.createEndStateTransition(new StepState(new StubStep("step3"))));
		flow.setStateTransitions(transitions);
		job.setFlow(flow);
		job.afterPropertiesSet();
		StepExecution stepExecution = job.doExecute(jobExecution);
		assertEquals(ExitStatus.FINISHED, stepExecution.getExitStatus());
		assertEquals(2, jobExecution.getStepExecutions().size());
		assertEquals("step3", stepExecution.getStepName());
	}

	@Test
	public void testBasicFlow() throws Throwable {
		SimpleFlow<JobFlowExecutor> flow = new SimpleFlow<JobFlowExecutor>("job");
		Step step = new StubStep("step");
		flow.setStateTransitions(Collections.singleton(StateTransition.createEndStateTransition(new StepState(step),
				"*")));
		job.setFlow(flow);
		job.execute(jobExecution);
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			throw jobExecution.getAllFailureExceptions().get(0);
		}
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

	@Test
	public void testDecisionFlow() throws Throwable {
		SimpleFlow<JobFlowExecutor> flow = new SimpleFlow<JobFlowExecutor>("job");
		JobExecutionDecider decider = new JobExecutionDecider() {
			public String decide(JobExecution jobExecution) {
				return "SWITCH";
			}
		};
		Collection<StateTransition<JobFlowExecutor>> transitions = new ArrayList<StateTransition<JobFlowExecutor>>();
		transitions.add(StateTransition.createStateTransition(new StepState(new StubStep("step1")), "*", "decision"));
		transitions.add(StateTransition.createStateTransition(new DecisionState("decision", decider), "*", "step2"));
		transitions.add(StateTransition
				.createStateTransition(new DecisionState("decision", decider), "SWITCH", "step3"));
		transitions.add(StateTransition.createEndStateTransition(new StepState(new StubStep("step2")), "*"));
		transitions.add(StateTransition.createEndStateTransition(new StepState(new StubStep("step3")), "*"));
		flow.setStateTransitions(transitions);
		job.setFlow(flow);
		StepExecution stepExecution = job.doExecute(jobExecution);
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			throw jobExecution.getAllFailureExceptions().get(0);
		}
		assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
		assertEquals(2, jobExecution.getStepExecutions().size());
		assertEquals("step3", stepExecution.getStepName());
	}

	@Test
	public void testPauseFlow() throws Throwable {

		SimpleFlow<JobFlowExecutor> flow = new SimpleFlow<JobFlowExecutor>("job");
		Collection<StateTransition<JobFlowExecutor>> transitions = new ArrayList<StateTransition<JobFlowExecutor>>();
		transitions.add(StateTransition.createStateTransition(new StepState(new StubStep("step1")), "*", "pause"));
		transitions.add(StateTransition.createStateTransition(new PauseState("pause"), "*", "step2"));
		transitions.add(StateTransition.createEndStateTransition(new StepState(new StubStep("step2")), "*"));
		flow.setStateTransitions(transitions);
		job.setFlow(flow);

		job.execute(jobExecution);
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			throw jobExecution.getAllFailureExceptions().get(0);
		}
		assertEquals(BatchStatus.PAUSED, jobExecution.getStatus());
		assertEquals(1, jobExecution.getStepExecutions().size());

		job.execute(jobExecution);
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			throw jobExecution.getAllFailureExceptions().get(0);
		}
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals(2, jobExecution.getStepExecutions().size());

	}

	/**
	 * @author Dave Syer
	 * 
	 */
	private class StubStep extends StepSupport {

		private StubStep(String name) {
			super(name);
		}

		public void execute(StepExecution stepExecution) throws JobInterruptedException {
			stepExecution.setStatus(BatchStatus.COMPLETED);
			stepExecution.setExitStatus(ExitStatus.FINISHED);
			jobRepository.update(stepExecution);
		}

	}

}
