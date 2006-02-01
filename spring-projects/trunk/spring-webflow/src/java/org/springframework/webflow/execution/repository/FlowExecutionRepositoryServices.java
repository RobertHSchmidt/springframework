package org.springframework.webflow.execution.repository;

import org.springframework.webflow.execution.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.repository.continuation.ContinuationFlowExecutionRepository;
import org.springframework.webflow.executor.EmptyFlowExecutionListenerLoader;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * A factory for creating continuation-based flow execution repositories.
 * <p>
 * All properties are optional. If a property is not set, the default value set
 * within {@link ContinuationFlowExecutionRepository} be used.
 * 
 * @author Keith Donald
 */
public class FlowExecutionRepositoryServices {

	/**
	 * The flow locator strategy for retrieving a flow definition using a flow
	 * id provided by the client.
	 */
	private FlowLocator flowLocator;

	/**
	 * A set of flow execution listeners to a list of flow execution listener
	 * criteria objects. The criteria list determines the conditions in which a
	 * single flow execution listener applies.
	 */
	private FlowExecutionListenerLoader listenerLoader = new EmptyFlowExecutionListenerLoader();

	/**
	 * The uid generation strategy to use.
	 */
	private UidGenerator uidGenerator = new RandomGuidUidGenerator();

	public FlowExecutionRepositoryServices(FlowLocator flowLocator) {
		setFlowLocator(flowLocator);
	}

	/**
	 * Returns the flow locator to use for lookup of flow definitions to
	 * execute.
	 */
	public FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator to use for lookup of flow definitions to execute.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Returns the listener loader in use by this flow execution manager.
	 */
	public FlowExecutionListenerLoader getListenerLoader() {
		return listenerLoader;
	}

	/**
	 * Sets the listener loader in use by this flow execution manager.
	 */
	public void setListenerLoader(FlowExecutionListenerLoader listenerLoader) {
		this.listenerLoader = listenerLoader;
	}

	/**
	 * Returns the uid generation strategy used to generate unique conversation
	 * and continuation identifiers.
	 */
	public UidGenerator getUidGenerator() {
		return uidGenerator;
	}

	/**
	 * Sets the uid generation strategy used to generate unique conversation and
	 * continuation identifiers.
	 */
	public void setUidGenerator(UidGenerator uidGenerator) {
		this.uidGenerator = uidGenerator;
	}
}