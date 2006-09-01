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

import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.util.Assert;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.support.ExternalContextHolder;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.execution.EventId;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.execution.repository.FlowExecutionLock;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;

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
 * <td>definitionLocator</td>
 * <td>The service locator responsible for loading flow definitions to execute.</td>
 * <td>None</td>
 * </tr>
 * <tr>
 * <td>executionFactory</td>
 * <td>The factory responsible for creating new flow executions.</td>
 * <td>None</td>
 * </tr>
 * <tr>
 * <td>executionRepository</td>
 * <td>The repository responsible for managing flow execution persistence.</td>
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
 * {@link org.springframework.webflow.executor.RequestParameterInputMapper request parameter mapper},
 * which exposes all request parameters in to the flow execution for input
 * mapping.</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @see FlowDefinitionLocator
 * @see FlowExecutionFactory
 * @see FlowExecutionRepository
 * @see AttributeMapper
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class FlowExecutorImpl implements FlowExecutor {

	/**
	 * A locator to access flow definitions registered in a central registry.
	 */
	private FlowDefinitionLocator definitionLocator;

	/**
	 * An abstract factory for creating a new execution of a flow definition.
	 */
	private FlowExecutionFactory executionFactory;

	/**
	 * An repository used to save, update, and load existing flow executions
	 * to/from a persistent store.
	 */
	private FlowExecutionRepository executionRepository;

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
	 * Create a new flow executor.
	 * @param definitionLocator the locator for accessing flow definitions to
	 * execute
	 * @param executionFactory the factory for creating executions of flow
	 * definitions
	 * @param executionRepository the repository for persisting paused flow
	 * executions
	 */
	public FlowExecutorImpl(FlowDefinitionLocator definitionLocator, FlowExecutionFactory executionFactory,
			FlowExecutionRepository executionRepository) {
		Assert.notNull(definitionLocator, "The locator for accessing flow definitions is required");
		Assert.notNull(executionFactory, "The execution factory for creating new flow executions is required");
		Assert.notNull(executionRepository, "The repository for persisting flow executions is required");
		this.definitionLocator = definitionLocator;
		this.executionFactory = executionFactory;
		this.executionRepository = executionRepository;
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

	/**
	 * Exposes the configured flow definition locator to subclasses and
	 * privileged accessors.
	 * @return the flow definition locator
	 */
	protected FlowDefinitionLocator getDefinitionLocator() {
		return definitionLocator;
	}

	/**
	 * Exposes the configured execution factory to subclasses and privileged
	 * accessors.
	 * @return the execution factory
	 */
	protected FlowExecutionFactory getExecutionFactory() {
		return executionFactory;
	}

	/**
	 * Exposes the execution repository to subclasses and privileged accessors.
	 * @return the execution repository
	 */
	protected FlowExecutionRepository getExecutionRepository() {
		return executionRepository;
	}

	/**
	 * Exposes the configured input mapper to subclasses and privileged
	 * accessors.
	 * @return the input mapper
	 */
	protected AttributeMapper getInputMapper() {
		return inputMapper;
	}

	public ResponseInstruction launch(String flowId, ExternalContext context) throws FlowException {
		// expose external context as a thread-bound service
		ExternalContextHolder.setExternalContext(context);
		try {
			FlowDefinition flowDefinition = definitionLocator.getFlowDefinition(flowId);
			FlowExecution flowExecution = executionFactory.createFlowExecution(flowDefinition);
			ViewSelection selectedView = flowExecution.start(createInput(context), context);
			if (flowExecution.isActive()) {
				// execution still active => store it in the repository
				FlowExecutionKey key = executionRepository.generateKey(flowExecution);
				executionRepository.putFlowExecution(key, flowExecution);
				return new ResponseInstruction(key.toString(), flowExecution, selectedView);
			}
			else {
				// execution already ended => just render the selected view
				return new ResponseInstruction(flowExecution, selectedView);
			}
		}
		finally {
			ExternalContextHolder.setExternalContext(null);
		}
	}

	public ResponseInstruction signalEvent(String eventId, String flowExecutionKey, ExternalContext context)
			throws FlowException {
		// expose external context as a thread-bound service
		ExternalContextHolder.setExternalContext(context);
		try {
			FlowExecutionKey key = executionRepository.parseFlowExecutionKey(flowExecutionKey);
			FlowExecutionLock lock = executionRepository.getLock(key);
			// make sure we're the only one manipulating the flow execution
			lock.lock();
			try {
				FlowExecution flowExecution = executionRepository.getFlowExecution(key);
				ViewSelection selectedView = flowExecution.signalEvent(new EventId(eventId), context);
				if (flowExecution.isActive()) {
					// execution still active => store it in the repository
					key = executionRepository.getNextKey(flowExecution, key);
					executionRepository.putFlowExecution(key, flowExecution);
					return new ResponseInstruction(key.toString(), flowExecution, selectedView);
				}
				else {
					// execution ended => remove it from the repository
					executionRepository.removeFlowExecution(key);
					return new ResponseInstruction(flowExecution, selectedView);
				}
			}
			finally {
				lock.unlock();
			}
		}
		finally {
			ExternalContextHolder.setExternalContext(null);
		}
	}

	public ResponseInstruction refresh(String flowExecutionKey, ExternalContext context) throws FlowException {
		// expose external context as a thread-bound service
		ExternalContextHolder.setExternalContext(context);
		try {
			FlowExecutionKey key = executionRepository.parseFlowExecutionKey(flowExecutionKey);
			// do we need to lock here?
			FlowExecutionLock lock = executionRepository.getLock(key);
			// make sure we're the only one manipulating the flow execution
			lock.lock();
			try {
				FlowExecution flowExecution = executionRepository.getFlowExecution(key);
				ViewSelection selectedView = flowExecution.refresh(context);
				return new ResponseInstruction(key.toString(), flowExecution, selectedView);
			}
			finally {
				lock.unlock();
			}
		}
		finally {
			ExternalContextHolder.setExternalContext(null);
		}
	}

	// helper methods

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