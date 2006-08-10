package org.springframework.webflow.test.execution.engine;

import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.impl.FlowExecutionImpl;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.FlowExecutionFactory;

/**
 * A simple flow execution factory that delegates to subclass template
 * methods to obtain Flow definition information to create FlowExecutions
 * with optional execution listeners attached.
 * @author Keith Donald
 */
public class MockFlowExecutionFactory implements FlowExecutionFactory {

	/**
	 * The listeners to attach.
	 */
	private FlowExecutionListener[] listeners;

	/**
	 * Creates a new flow execution factory.
	 */
	public MockFlowExecutionFactory() {
	}

	/**
	 * Creates a new flow execution factory that will attach the listener to
	 * newly created flow executions.
	 * @param listener the execution listener
	 */
	public MockFlowExecutionFactory(FlowExecutionListener listener) {
		this(new FlowExecutionListener[] { listener });
	}

	/**
	 * Creates a new flow execution factory that will attach the listener
	 * list to newly created flow executions.
	 * @param listeners the execution listener list
	 */
	public MockFlowExecutionFactory(FlowExecutionListener[] listeners) {
		this.listeners = listeners;
	}

	public FlowExecution createFlowExecution(FlowDefinition flowDefinition) {
		return new FlowExecutionImpl((Flow)flowDefinition, listeners);
	}
}