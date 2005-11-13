package org.springframework.webflow.config;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.webflow.Action;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.FlowLocator;
import org.springframework.webflow.NoSuchFlowArtifactException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;

/**
 * A base class for flow artifact factories that delegate to generic service
 * registries.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowArtifactFactory extends FlowArtifactFactoryAdapter {

	/**
	 * Creates an artifact factory adapter that delegates to the provided flow
	 * locator for subflow resolution.
	 * @param flowLocator the flow locator (may be <code>null</code).
	 */
	public AbstractFlowArtifactFactory() {
	}

	/**
	 * Creates an artifact factory adapter that delegates to the provided flow
	 * locator for subflow resolution.
	 * @param flowLocator the flow locator (may be <code>null</code).
	 */
	public AbstractFlowArtifactFactory(FlowLocator subflowLocator) {
		super(subflowLocator);
	}

	public Action getAction(String id) throws FlowArtifactException {
		return toAction(getArtifact(id, Action.class));
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		return (FlowAttributeMapper)getArtifact(id, FlowAttributeMapper.class);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
		return (TransitionCriteria)getArtifact(id, TransitionCriteria.class);
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactException {
		return (ViewSelector)getArtifact(id, ViewSelector.class);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
		return (StateExceptionHandler)getArtifact(id, StateExceptionHandler.class);
	}

	private Object getArtifact(String id, Class artifactType) {
		try {
			return doGetArtifact(id, artifactType);
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new NoSuchFlowArtifactException(artifactType, id, e);
		}
	}

	protected abstract Object doGetArtifact(String id, Class artifactType);

}