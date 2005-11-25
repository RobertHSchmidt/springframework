package org.springframework.webflow.config;

import org.springframework.beans.BeanUtils;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.Transition.TargetStateResolver;
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
	 * Creates an artifact factory adapter that does not natively support any
	 * artifact lookup operations.
	 */
	public FlowArtifactFactoryAdapter() {

	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException("Subflow lookup is not supported by this artifact factory");
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
		throw new UnsupportedOperationException("View selector lookup is not supported by this artifact factory");
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException(
				"Flow exception handler lookup is not supported by this artifact factory");
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactLookupException {
		throw new UnsupportedOperationException(
				"Transition target state resolver lookup is not supported by this artifact factory");
	}

	public Flow createFlow(String id) throws FlowArtifactLookupException {
		return (Flow)BeanUtils.instantiateClass(Flow.class);
	}

	public State createState(String id, Class stateType) throws FlowArtifactLookupException {
		return (State)BeanUtils.instantiateClass(stateType);
	}

	public Transition createTransition(String id) throws FlowArtifactLookupException {
		return (Transition)BeanUtils.instantiateClass(Transition.class);
	}
}