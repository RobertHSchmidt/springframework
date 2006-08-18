package org.springframework.webflow.config;

import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.engine.impl.FlowExecutionImplStateRestorer;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.DefaultFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.FlowExecutionStateRestorer;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorFactory;
import org.springframework.webflow.executor.FlowExecutorImpl;

/**
 * The default flow executor factory implementation.
 * 
 * @author Keith Donald
 */
public class DefaultFlowExecutorFactory implements FlowExecutorFactory {

	/**
	 * The locator the executor will use to access flow definitions registered
	 * in a central registry. Required.
	 */
	private FlowDefinitionLocator definitionLocator;

	/**
	 * The factory the executor will use to create new flow executions.
	 */
	private FlowExecutionImplFactory executionFactory = new FlowExecutionImplFactory();

	/**
	 * The strategy used by the repository to restore transient flow execution
	 * state.
	 */
	private FlowExecutionImplStateRestorer executionStateRestorer;

	/**
	 * Creates a new {@link FlowExecutor flow executor} factory.
	 * @param definitionLocator The locator the executor will use to access flow
	 * definitions registered in a central registry.
	 */
	public DefaultFlowExecutorFactory(FlowDefinitionLocator definitionLocator) {
		Assert.notNull(definitionLocator, "The flow definition locator is required");
		this.definitionLocator = definitionLocator;
		this.executionStateRestorer = new FlowExecutionImplStateRestorer(definitionLocator);
	}

	/**
	 * Sets the system attributes that apply to flow executions launched by the
	 * executor created by this factory. Execution attributes may affect flow
	 * execution behavior.
	 * @param executionAttributes flow execution system attributes
	 */
	public void setExecutionAttributes(AttributeMap executionAttributes) {
		this.executionFactory.setExecutionAttributes(executionAttributes);
		this.executionStateRestorer.setAttributes(executionAttributes);
	}

	/**
	 * Convenience setter that sets a single listener that always applys to flow
	 * executions launched by the executor created by this factory.
	 * @param executionListener the flow execution listener
	 */
	public void setExecutionListener(FlowExecutionListener executionListener) {
		executionFactory.setExecutionListener(executionListener);
		executionStateRestorer.setListener(executionListener);
	}

	/**
	 * Convenience setter that sets a list of listeners that always apply to
	 * flow executions launched by the executor created by this factory.
	 * @param executionListeners the flow execution listeners
	 */
	public void setExecutionListeners(FlowExecutionListener[] executionListeners) {
		executionFactory.setExecutionListeners(executionListeners);
		executionStateRestorer.setListeners(executionListeners);
	}

	/**
	 * Sets the strategy for loading the listeners that will observe executions
	 * of a flow definition. Allows full control over what listeners should
	 * apply to executions of a flow definition launched by the executor created
	 * by this factory.
	 */
	public void setExecutionListenerLoader(FlowExecutionListenerLoader executionListenerLoader) {
		executionFactory.setExecutionListenerLoader(executionListenerLoader);
		executionStateRestorer.setListenerLoader(executionListenerLoader);
	}

	public FlowExecutor createFlowExecutor() {
		return new FlowExecutorImpl(definitionLocator, executionFactory,
				createExecutionRepository(executionStateRestorer));
	}

	/**
	 * Factory method for creating the flow execution repository for saving and
	 * loading executing flows. Subclasses may override to customize the
	 * repository implementation used.
	 * @param executionStateRestorer the execution state restorer strategy
	 * @return the flow execution repository
	 */
	protected FlowExecutionRepository createExecutionRepository(FlowExecutionStateRestorer executionStateRestorer) {
		return new DefaultFlowExecutionRepository(executionStateRestorer);
	}
}