package org.springframework.webflow.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;

/**
 * A flow artifact locator that pulls its artifacts from a standard Spring
 * BeanFactory.
 * @author Keith Donald
 */
public class DefaultFlowArtifactFactory extends FlowArtifactFactoryAdapter {

	/**
	 * The Spring bean factory.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a flow artifact locator that retrieves artifacts from the
	 * provided bean factory
	 * @param beanFactory The spring bean factory, may not be null.
	 * @param subflowLocator The locator for loading subflows
	 */
	public DefaultFlowArtifactFactory(FlowLocator subflowLocator, BeanFactory beanFactory) {
		super(subflowLocator);
		Assert.notNull(beanFactory, "The beanFactory to retrieve flow artifacts is required");
		this.beanFactory = beanFactory;
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		return toAction(getArtifact(id, Action.class));
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		return (FlowAttributeMapper)getArtifact(id, FlowAttributeMapper.class);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		return (TransitionCriteria)getArtifact(id, TransitionCriteria.class);
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactLookupException {
		return (ViewSelector)getArtifact(id, ViewSelector.class);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		return (StateExceptionHandler)getArtifact(id, StateExceptionHandler.class);
	}

	private Object getArtifact(String id, Class artifactType) {
		try {
			return doGetArtifact(id, artifactType);
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new FlowArtifactLookupException(artifactType, id, e);
		}
	}

	protected Object doGetArtifact(String id, Class artifactType) {
		return beanFactory.getBean(id);
	}
}