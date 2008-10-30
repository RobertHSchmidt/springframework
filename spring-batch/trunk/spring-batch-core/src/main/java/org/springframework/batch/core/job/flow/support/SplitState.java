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
package org.springframework.batch.core.job.flow.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecution;
import org.springframework.batch.core.job.flow.FlowExecutionException;
import org.springframework.batch.core.job.flow.JobFlowExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

/**
 * A {@link State} implementation that splits a {@link Flow} into multiple
 * parallel subflows.
 * 
 * @author Dave Syer
 * 
 */
public class SplitState extends AbstractState {

	private final Collection<Flow> flows;

	private TaskExecutor taskExecutor = new SyncTaskExecutor();

	private FlowExecutionAggregator aggregator = new MaxValueFlowExecutionAggregator();

	/**
	 * @param name
	 */
	public SplitState(Collection<Flow> flows, String name) {
		super(name);
		this.flows = flows;
	}

	/**
	 * Public setter for the taskExecutor.
	 * @param taskExecutor the taskExecutor to set
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Execute the flows in parallel by passing them to the {@link TaskExecutor}
	 * and wait for all of them to finish before proceeding.
	 * 
	 * @see State#handle(JobFlowExecutor)
	 */
	@Override
	public String handle(final JobFlowExecutor executor) throws Exception {

		Collection<FutureTask<FlowExecution>> tasks = new ArrayList<FutureTask<FlowExecution>>();

		for (final Flow flow : flows) {

			final FutureTask<FlowExecution> task = new FutureTask<FlowExecution>(new Callable<FlowExecution>() {
				public FlowExecution call() throws Exception {
					return flow.start(executor);
				}
			});

			tasks.add(task);

			try {
				taskExecutor.execute(task);
			}
			catch (TaskRejectedException e) {
				throw new FlowExecutionException("TaskExecutor rejected task for flow=" + flow.getName());
			}

		}

		Collection<FlowExecution> results = new ArrayList<FlowExecution>();

		// TODO: could use a CompletionSerice?
		for (FutureTask<FlowExecution> task : tasks) {
			results.add(task.get());
		}

		return aggregator.aggregate(results);

	}

}
