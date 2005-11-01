package org.springframework.webflow.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.access.FlowArtifactLookupException;
import org.springframework.webflow.access.FlowLocator;
import org.springframework.webflow.access.NoSuchFlowArtifactException;
import org.springframework.webflow.action.LocalBeanInvokingAction;

/**
 * A flow artifact locator that pulls its artifacts from a standard Spring
 * BeanFactory.
 * @author Keith Donald
 */
public class BeanFactoryFlowArtifactLocator implements FlowArtifactLocator {

	/**
	 * The Spring bean factory.
	 */
	private BeanFactory beanFactory;

	/**
	 * An segregated flow locator to delegate to for retrieving subflow
	 * definitions.
	 */
	private FlowLocator subflowLocator;

	/**
	 * Creates a flow artifact locator that retrieves artifacts from the
	 * provided bean factory
	 * @param beanFactory The spring bean factory, may not be null.
	 */
	public BeanFactoryFlowArtifactLocator(BeanFactory beanFactory) {
		this(beanFactory, null);
	}

	/**
	 * Creates a flow artifact locator that retrieves artifacts from the
	 * provided bean factory
	 * @param beanFactory The spring bean factory, may not be null.
	 * @param subflowLocator The locator for loading subflows
	 */
	public BeanFactoryFlowArtifactLocator(BeanFactory beanFactory, FlowLocator subflowLocator) {
		Assert.notNull(beanFactory, "The beanFactory to retrieve flow artifacts is required");
		this.beanFactory = beanFactory;
		this.subflowLocator = subflowLocator;
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		if (subflowLocator != null) {
			return subflowLocator.getFlow(id);
		}
		else {
			return (Flow)getService(id, Flow.class);
		}
	}

	public Action getAction(String id) throws FlowArtifactLookupException {
		return toAction(getService(id, Action.class));
	}

	/**
	 * Turn the given service object into an action. If the given service object
	 * implements the <code>Action</code> interface, it is returned as is,
	 * otherwise it is wrapped in an action that can invoke a method on the
	 * service bean.
	 * @param service the service bean
	 * @return the action
	 */
	protected Action toAction(Object service) {
		if (service instanceof Action) {
			return (Action)service;
		}
		else {
			return new LocalBeanInvokingAction(service);
		}
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		return (FlowAttributeMapper)getService(id, FlowAttributeMapper.class);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		return (TransitionCriteria)getService(id, TransitionCriteria.class);
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactLookupException {
		return (ViewSelector)getService(id, ViewSelector.class);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		return (StateExceptionHandler)getService(id, StateExceptionHandler.class);
	}

	private Object getService(String id, Class serviceType) {
		try {
			return beanFactory.getBean(id);
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new NoSuchFlowArtifactException(serviceType, id, e);
		}
	}
}