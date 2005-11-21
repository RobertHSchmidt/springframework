package org.springframework.webflow.config;

import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.LocalBeanInvokingAction;

/**
 * Dummy implementation of a flow artifact factory that throws unsupported
 * operation exceptions for each operation.
 * <p>
 * May be subclassed to offer some lookup support.
 * 
 * @author Keith Donald
 */
public class FlowArtifactFactoryAdapter implements FlowArtifactFactory {

	/**
	 * The flow locator delegate (may be <code>null</code>).
	 */
	private FlowLocator subflowLocator;

	/**
	 * Creates an artifact factory adapter that does not natively support any
	 * artifact lookup operations.
	 */
	public FlowArtifactFactoryAdapter() {

	}

	/**
	 * Creates an artifact factory adapter that delegates to the provided flow
	 * locator for subflow resolution.
	 * @param subflowLocator the flow locator (may be <code>null</code).
	 */
	public FlowArtifactFactoryAdapter(FlowLocator subflowLocator) {
		this.subflowLocator = subflowLocator;
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		if (subflowLocator != null) {
			return subflowLocator.getFlow(id);
		}
		else {
			throw new UnsupportedOperationException("Subflow lookup is not supported by this artifact factory");
		}
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException("Action lookup is not supported by this artifact factory");
	}

	/**
	 * Helper method to the given service object into an action. If the given
	 * service object implements the <code>Action</code> interface, it is
	 * returned as is, otherwise it is wrapped in an action that can invoke a
	 * method on the service bean.
	 * @param artifact the service bean
	 * @return the action
	 */
	protected Action toAction(Object artifact) {
		if (artifact instanceof Action) {
			return (Action)artifact;
		}
		else {
			return new LocalBeanInvokingAction(artifact);
		}
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException("Attribute mapper lookup is not supported by this artifact factory");
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException("Transition criteria lookup is not supported by this artifact factory");
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException(
				"View descriptor creator lookup is not supported by this artifact factory");
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException(
				"Flow exception handler lookup is not supported by this artifact factory");
	}
}