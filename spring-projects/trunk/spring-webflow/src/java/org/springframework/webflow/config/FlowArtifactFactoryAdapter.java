package org.springframework.webflow.config;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
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

/**
 * Dummy implementation of a flow artifact factory that throws unsupported
 * operation exceptions for each operation.
 * <p>
 * May be subclassed to offer some lookup support.
 * 
 * @author Keith Donald
 */
public class FlowArtifactFactoryAdapter implements FlowArtifactFactory {

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		throw new FlowArtifactLookupException(Flow.class, id,
				"Subflow lookup is not supported by this artifact factory");
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		throw new FlowArtifactLookupException(Action.class, id,
				"Action lookup is not supported by this artifact factory");
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		throw new FlowArtifactLookupException(FlowAttributeMapper.class, id,
				"Attribute mapper lookup is not supported by this artifact factory");
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		throw new FlowArtifactLookupException(TransitionCriteria.class, id,
				"Transition criteria lookup is not supported by this artifact factory");
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactLookupException {
		throw new FlowArtifactLookupException(ViewSelector.class, id,
				"View selector lookup is not supported by this artifact factory");
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		throw new FlowArtifactLookupException(StateExceptionHandler.class, id,
				"State exception handler lookup is not supported by this artifact factory");
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactLookupException {
		throw new FlowArtifactLookupException(Transition.TargetStateResolver.class, id,
				"Transition target state resolver lookup is not supported by this artifact factory");
	}

	public Flow createFlow(String id) throws FlowArtifactLookupException {
		return (Flow)newInstance(Flow.class);
	}

	public State createState(String id, Class stateType) throws FlowArtifactLookupException {
		return (State)newInstance(stateType);
	}

	public Transition createTransition(String id) throws FlowArtifactLookupException {
		return (Transition)newInstance(Transition.class);
	}

	protected Object newInstance(Class artifactType) {
		return BeanUtils.instantiateClass(artifactType);
	}

	public BeanFactory getServiceRegistry() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Service registry lookup not supported by this artifact factory");
	}

}