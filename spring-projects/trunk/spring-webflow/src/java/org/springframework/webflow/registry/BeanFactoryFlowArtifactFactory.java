package org.springframework.webflow.registry;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
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
import org.springframework.webflow.config.FlowArtifactFactoryAdapter;

/**
 * A flow artifact locator that pulls its artifacts from a standard Spring
 * BeanFactory.
 * @author Keith Donald
 */
public class BeanFactoryFlowArtifactFactory extends FlowArtifactFactoryAdapter {

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
	public BeanFactoryFlowArtifactFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		return (Flow)getBean(id, Flow.class, true);
	}
	
	public Action getAction(String id) throws FlowArtifactLookupException {
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
	
	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		return (FlowAttributeMapper)getBean(id, FlowAttributeMapper.class, true);
	}

	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		return (TransitionCriteria)getBean(id, TransitionCriteria.class, true);
	}

	public ViewSelector getViewSelector(String id) throws FlowArtifactLookupException {
		return (ViewSelector)getBean(id, ViewSelector.class, true);
	}

	public TargetStateResolver getTargetStateResolver(String id) throws FlowArtifactLookupException {
		return (TargetStateResolver)getBean(id, Transition.TargetStateResolver.class, true);
	}

	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		return (StateExceptionHandler)getBean(id, StateExceptionHandler.class, true);
	}

	public State createState(String id, Class stateType) throws FlowArtifactLookupException {
		return (State)createPrototype(id, stateType);
	}

	public Transition createTransition(String id) throws FlowArtifactLookupException {
		return (Transition)createPrototype(id, Transition.class);
	}

	public Flow createFlow(String id) throws FlowArtifactLookupException {
		return (Flow)createPrototype(id, Flow.class);
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
			throw new FlowArtifactLookupException(artifactType, id, e);
		} catch (BeanNotOfRequiredTypeException e) {
			throw new FlowArtifactLookupException(artifactType, id, e);
		}
	}

	protected Object createPrototype(String id, Class artifactType) {
		if (StringUtils.hasText(id) && beanFactory.containsBean(id)) {
			Assert.isTrue(beanFactory.isSingleton(id), "Artifact with id '" + id + "' and type [" + artifactType
					+ "] must be a prototype");
			return beanFactory.getBean(id, artifactType);
		}
		else {
			return BeanUtils.instantiateClass(artifactType);
		}
	}

	public BeanFactory getServiceRegistry() throws UnsupportedOperationException {
		return beanFactory;
	}
	
}