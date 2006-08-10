/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.util.Assert;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.support.LocalAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.factory.FlowExecutionFactory;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryFactory;

/**
 * The default implementation of the central facade for <i>driving</i> the
 * execution of flows within an application.
 * <p>
 * This object is responsible for creating and starting new flow executions as
 * requested by clients, as well as signaling events for processing by existing,
 * paused executions (that are waiting to be resumed in response to a user
 * event).
 * <p>
 * This object is a facade or entry point into the Spring Web Flow execution
 * system and makes the overall system easier to use. The name <i>executor</i>
 * was chosen as <i>executors drive executions</i>.
 * <p>
 * <b>Commonly used configurable properties</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>description</b></td>
 * <td><b>default</b></td>
 * </tr>
 * <tr>
 * <td>repositoryFactory</td>
 * <td>The strategy for accessing flow execution repositories that are used to
 * create, save, and store managed flow executions driven by this executor.</td>
 * <td>None</td>
 * </tr>
 * <tr>
 * <td>inputMapper</td>
 * <td>The service responsible for mapping attributes of
 * {@link ExternalContext external contexts} that request to launch new
 * {@link FlowExecution flow executions}. After mapping, the target map is then
 * passed to the FlowExecution, exposing extern context attributes as input to
 * the flow during startup.</td>
 * <td>A
 * {@link org.springframework.webflow.executor.RequestParameterInputMapper},
 * which exposes all request parameters in to the flow execution for input
 * mapping.</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @see FlowExecutionRepositoryFactory
 * @see AttributeMapper
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowExecutorImpl implements FlowExecutor {

	private static final Log logger = LogFactory.getLog(FlowExecutorImpl.class);

	/**
	 * A locator to access flow definitions registered in a central registry.
	 */
	private FlowDefinitionLocator flowLocator;

	/**
	 * An abstract factory for creating a new flow execution for a definition.
	 */
	private FlowExecutionFactory flowExecutionFactory;

	/**
	 * An abstract factory to obtain repositories to save, update, and load
	 * existing flow executions to/from a persistent store.
	 */
	private FlowExecutionRepositoryFactory repositoryFactory;

	/**
	 * The service responsible for mapping attributes of an
	 * {@link ExternalContext} to a new {@link FlowExecution} during the
	 * {@link #launch(String, ExternalContext) launch flow} operation.
	 * <p>
	 * This allows developers to control what attributes are made available in
	 * the <code>inputMap</code> to new top-level flow executions. The
	 * starting execution may then choose to map that available input into its
	 * own local scope.
	 * <p>
	 * The default implementation simply exposes all request parameters as flow
	 * execution input attributes. May be null.
	 */
	private AttributeMapper inputMapper = new RequestParameterInputMapper();

	/**
	 * Create a new flow executor that uses the repository factory to access a
	 * repository to create, save, and restore managed flow executions driven by
	 * this executor.
	 * @param repositoryFactory the repository factory
	 */
	public FlowExecutorImpl(FlowDefinitionLocator flowLocator, FlowExecutionFactory flowExecutionFactory,
			FlowExecutionRepositoryFactory repositoryFactory) {
		Assert.notNull(flowLocator, "The flow locator for loading flow definitions is required");
		Assert.notNull(flowExecutionFactory, "The execution factory for creating new flow executions is required");
		Assert.notNull(repositoryFactory,
				"The repository factory for saving and restoring flow executions is required");
		this.flowLocator = flowLocator;
		this.flowExecutionFactory = flowExecutionFactory;
		this.repositoryFactory = repositoryFactory;
	}

	/**
	 * Set the service responsible for mapping attributes of an
	 * {@link ExternalContext} to a new {@link FlowExecution} during the
	 * {@link #launch(String, ExternalContext) launch flow} operation.
	 * <p>
	 * The default implementation simply exposes all request parameters as flow
	 * execution input attributes. May be null.
	 * @see RequestParameterInputMapper
	 */
	public void setInputMapper(AttributeMapper inputMapper) {
		this.inputMapper = inputMapper;
	}

	public ResponseInstruction launch(String flowId, ExternalContext context) throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowDefinition flowDefinition = flowLocator.getFlow(flowId);
		FlowExecution flowExecution = flowExecutionFactory.createFlowExecution(flowDefinition);
		ViewSelection selectedView = flowExecution.start(createInput(context), context);
		if (flowExecution.isActive()) {
			// execution still active => store it in the repository
			FlowExecutionKey flowExecutionKey = repository.generateKey(flowExecution);
			repository.putFlowExecution(flowExecutionKey, flowExecution);
			return new ResponseInstruction(flowExecutionKey.toString(), flowExecution, selectedView);
		}
		else {
			// execution already ended => just render the selected view
			return new ResponseInstruction(flowExecution, selectedView);
		}
	}

	public ResponseInstruction signalEvent(String eventId, String flowExecutionKey, ExternalContext context)
			throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionKey repositoryKey = repository.parseFlowExecutionKey(flowExecutionKey);
		FlowExecutionLock lock = repository.getLock(repositoryKey);
		// make sure we're the only one manipulating the flow execution
		lock.lock();
		try {
			FlowExecution flowExecution = repository.getFlowExecution(repositoryKey);
			ViewSelection selectedView = flowExecution.signalEvent(new EventId(eventId), context);
			if (flowExecution.isActive()) {
				// execution still active => store it in the repository
				repositoryKey = repository.getNextKey(flowExecution, repositoryKey);
				repository.putFlowExecution(repositoryKey, flowExecution);
				return new ResponseInstruction(repositoryKey.toString(), flowExecution, selectedView);
			}
			else {
				// execution ended => remove it from the repository
				repository.removeFlowExecution(repositoryKey);
				return new ResponseInstruction(flowExecution, selectedView);
			}
		}
		finally {
			lock.unlock();
		}
	}

	public ResponseInstruction refresh(String flowExecutionKey, ExternalContext context) throws FlowException {
		FlowExecutionRepository repository = getRepository(context);
		FlowExecutionKey repositoryKey = repository.parseFlowExecutionKey(flowExecutionKey);
		FlowExecutionLock lock = repository.getLock(repositoryKey);
		// make sure we're the only one manipulating the flow execution
		lock.lock();
		try {
			FlowExecution flowExecution = repository.getFlowExecution(repositoryKey);
			ViewSelection selectedView = flowExecution.refresh(context);
			// note that we're not calling redirectOnPauseIfNecessary since this
			// is already a refresh!
			return new ResponseInstruction(repositoryKey.toString(), flowExecution, selectedView);
		}
		finally {
			lock.unlock();
		}
	}

	// helper methods

	/**
	 * Returns the repository retrieved by the configured
	 * {@link FlowExecutionRepositoryFactory}.
	 */
	protected FlowExecutionRepository getRepository(ExternalContext context) {
		return repositoryFactory.getRepository(context);
	}

	/**
	 * Factory method that creates the input attribute map for a newly created
	 * {@link FlowExecution}.
	 * @param context the external context
	 * @return the input map, or null if no input
	 */
	protected MutableAttributeMap createInput(ExternalContext context) {
		if (inputMapper != null) {
			MutableAttributeMap inputMap = new LocalAttributeMap();
			inputMapper.map(context, inputMap, null);
			return inputMap;
		}
		else {
			return null;
		}
	}
}