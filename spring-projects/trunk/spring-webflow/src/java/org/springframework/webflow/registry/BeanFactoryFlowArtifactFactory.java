package org.springframework.webflow.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.Transition.TargetStateResolver;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.builder.AbstractFlowArtifactFactory;

/**
 * A flow artifact locator that obtains its artifacts by delegating to a
 * standard Spring BeanFactory.
 * @author Keith Donald
 */
public class BeanFactoryFlowArtifactFactory extends AbstractFlowArtifactFactory implements ResourceLoaderAware {

	/**
	 * The Spring bean factory that manages configured flow artifacts.
	 */
	private BeanFactory beanFactory;

	/**
	 * An optional resource loader that can load resources.
	 */
	private ResourceLoader resourceLoader;

	/**
	 * Creates a flow artifact locator that retrieves artifacts from the
	 * provided bean factory
	 * @param beanFactory The spring bean factory that manages configured flow
	 * artifacts.
	 */
	public BeanFactoryFlowArtifactFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Flow getSubflow(String id) throws FlowArtifactException {
		return (Flow)getBean(id, Flow.class, true);
	}

	public Action getAction(String id) throws FlowArtifactException {
		return toAction(getBean(id, Action.class, false));
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

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		return (FlowAttributeMapper)getBean(id, FlowAttributeMapper.class, true);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactException {
		return (TransitionCriteria)getBean(id, TransitionCriteria.class, true);
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactException {
		return (ViewSelector)getBean(id, ViewSelector.class, true);
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactException {
		return (TargetStateResolver)getBean(id, Transition.TargetStateResolver.class, true);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactException {
		return (StateExceptionHandler)getBean(id, StateExceptionHandler.class, true);
	}

	private Object getBean(String id, Class artifactType, boolean enforceTypeCheck) {
		try {
			if (enforceTypeCheck) {
				return beanFactory.getBean(id, artifactType);
			}
			else {
				return beanFactory.getBean(id);
			}
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new FlowArtifactException(artifactType, id, e);
		}
		catch (BeanNotOfRequiredTypeException e) {
			throw new FlowArtifactException(artifactType, id, e);
		}
	}

	public BeanFactory getServiceRegistry() throws UnsupportedOperationException {
		return beanFactory;
	}

	public ResourceLoader getResourceLoader() throws UnsupportedOperationException {
		return resourceLoader;
	}
}