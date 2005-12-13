package org.springframework.webflow.builder;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.Transition.TargetStateResolver;
import org.springframework.webflow.action.LocalBeanInvokingAction;

/**
 * Base implementation of a flow artifact factory that implements a minimal set
 * of the <code>FlowArtifactFactory</code> interface, throwing unsupported
 * operation exceptions for most operations.
 * <p>
 * May be subclassed to offer additional factory/lookup support.
 * @author Keith Donald
 */
public class FlowArtifactFactoryAdapter implements FlowArtifactFactory {

	public Flow getSubflow(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, Flow.class, "Subflow lookup is not supported by this artifact factory");
	}

	public Action getAction(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, Action.class, "Action lookup is not supported by this artifact factory");
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, FlowAttributeMapper.class,
				"Attribute mapper lookup is not supported by this artifact factory");
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, TransitionCriteria.class,
				"Transition criteria lookup is not supported by this artifact factory");
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, ViewSelector.class,
				"View selector lookup is not supported by this artifact factory");
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, StateExceptionHandler.class,
				"State exception handler lookup is not supported by this artifact factory");
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException {
		throw new FlowArtifactException(id, Transition.TargetStateResolver.class,
				"Transition target state resolver lookup is not supported by this artifact factory");
	}

	public Flow createFlow(String id, Map properties) throws FlowArtifactException {
		Flow flow = (Flow)newInstance(Flow.class);
		flow.setId(id);
		flow.setProperties(properties);
		return flow;
	}

	public State createState(Flow flow, String id, Class stateType, Map properties) throws FlowArtifactException {
		State state = (State)newInstance(stateType);
		state.setId(id);
		state.setFlow(flow);
		state.setProperties(properties);
		return state;
	}

	public Transition createTransition(TransitionableState sourceState, Map properties) throws FlowArtifactException {
		Transition transition = (Transition)newInstance(Transition.class);
		transition.setSourceState(sourceState);
		transition.setProperties(properties);
		return transition;
	}

	protected Object newInstance(Class artifactType) {
		return BeanUtils.instantiateClass(artifactType);
	}

	public BeanFactory getServiceRegistry() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Service registry lookup not supported by this artifact factory");
	}

	public ResourceLoader getResourceLoader() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Resource lookup not supported by this artifact factory");
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

}