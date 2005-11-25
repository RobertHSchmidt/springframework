/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.access;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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
import org.springframework.webflow.action.LocalBeanInvokingAction;

/**
 * Helper class used to pull flow artifacts from a standard Spring
 * BeanFactory. Note that this class implements the FlowLocator interface, so it
 * can be used as a general utility to lookup flows at configuration
 * time or at runtime.
 * 
 * @author Erwin Vervaet
 */
public class FlowArtifactFactory implements FlowLocator {

	/**
	 * The Spring bean factory wrapped by this object.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a flow artifact factory that retrieves artifacts from the
	 * provided bean factory.
	 * @param beanFactory the spring bean factory, may not be null
	 */
	public FlowArtifactFactory(BeanFactory beanFactory) {
		Assert.notNull(beanFactory, "The bean factory to retrieve flow artifacts from is required");
		this.beanFactory = beanFactory;
	}
	
	/**
	 * Returns the bean factory wrapped by this object.
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}
		
	/**
	 * Returns the bean definition registry of the bean factory wrapped by this object.
	 * Can be used to register new bean definitions with the wrapped bean factory.
	 */
	public BeanDefinitionRegistry getBeanDefinitionRegistry() {
		return (BeanDefinitionRegistry)getBeanFactory();
	}
	
	public Flow getFlow(String id) throws FlowArtifactLookupException {
		return (Flow)getArtifact(id, Flow.class);
	}
	
	/**
	 * Lookup a state with specified id.
	 * @param id the state id
	 * @return the state
	 * @throws FlowArtifactLookupException when the state cannot be found
	 */
	public State getState(String id) throws FlowArtifactLookupException {
		return (State)getArtifact(id, State.class);
	}

	/**
	 * Lookup a transition with specified id.
	 * @param id the transition id
	 * @return the transition
	 * @throws FlowArtifactLookupException when the transition cannot be found
	 */
	public Transition getTransition(String id) throws FlowArtifactLookupException {
		return (Transition)getArtifact(id, Transition.class);
	}

	/**
	 * Lookup an action with specified id. If the bean retreived from the
	 * bean factory is not an Action, it will be wrapped in a LocalBeanInvokingAction.
	 * @param id the action id
	 * @return the action
	 * @throws FlowArtifactLookupException when the action cannot be found
	 * @see LocalBeanInvokingAction
	 */
	public Action getAction(String id) throws FlowArtifactLookupException {
		Object artifact;
		try {
			artifact = beanFactory.getBean(id);
		}
		catch (BeansException e) {
			throw new FlowArtifactLookupException(Action.class, id, e);
		}
		if (artifact instanceof Action) {
			return (Action)getArtifact(id, Action.class);
		}
		else {
			return new LocalBeanInvokingAction(artifact);
		}
	}

	/**
	 * Lookup a flow attribute mapper with specified id.
	 * @param id the flow attribute mapper id
	 * @return the flow attribute mapper
	 * @throws FlowArtifactLookupException when the flow model mapper cannot be found
	 */
	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactLookupException {
		return (FlowAttributeMapper)getArtifact(id, FlowAttributeMapper.class);
	}

	/**
	 * Lookup a transition criteria object with specified id.
	 * @param id the transition criteria id
	 * @return the transition criteria object
	 * @throws FlowArtifactLookupException when the transition criteria object
	 * cannot be found
	 */
	public TransitionCriteria getTransitionCriteria(String id) throws FlowArtifactLookupException {
		return (TransitionCriteria)getArtifact(id, TransitionCriteria.class);
	}

	/**
	 * Lookup a view selector with specified id.
	 * @param id the view selector id
	 * @return the view selector
	 * @throws FlowArtifactLookupException when the view selector cannot be found
	 */
	public ViewSelector getViewSelector(String id) throws FlowArtifactLookupException {
		return (ViewSelector)getArtifact(id, ViewSelector.class);
	}

	/**
	 * Lookup an exception handler with specified id
	 * @param id the exception handler id
	 * @return the exception handler
	 * @throws FlowArtifactLookupException when the exception handler cannot be found
	 */
	public StateExceptionHandler getExceptionHandler(String id) throws FlowArtifactLookupException {
		return (StateExceptionHandler)getArtifact(id, StateExceptionHandler.class);
	}
	
	//helpers
	
	/**
	 * Helper to create an artifact of specified type. The default constructor
	 * will be used.
	 * @param id the id of the artifact to create
	 * @param artifactType the expected type of the artifact
	 * @throws FlowArtifactLookupException when the artifact cannot be created
	 */
	public Object createArtifact(String id, Class artifactType) {
		try {
			return BeanUtils.instantiateClass(artifactType);
		}
		catch (BeansException e) {
			throw new FlowArtifactLookupException(artifactType, id, e);
		}
	}

	/**
	 * Helper to lookup an identified artifact of specified type.
	 * @param id the id of the artifact to lookup
	 * @param artifactType the expected type of the artifact
	 * @throws FlowArtifactLookupException when the artifact cannot be found or is not
	 * of the expected type
	 */
	public Object getArtifact(String id, Class artifactType) throws FlowArtifactLookupException {
		try {
			return beanFactory.getBean(id, artifactType);
		}
		catch (BeansException e) {
			throw new FlowArtifactLookupException(artifactType, id, e);
		}
	}
	
	/**
	 * Helper to lookup an identified artifact of specified type. If not found
	 * in the bean factory, a new instance will be created with the default
	 * constructor.
	 * @param beanId the id of the artifact bean to lookup (optional)
	 * @param id the id of the artifact to create
	 * @param artifactType the expected type of the artifact
	 * @throws FlowArtifactLookupException when the artifact cannot be found or created
	 */
	public Object getOrCreateArtifact(String beanId, String id, Class artifactType) throws FlowArtifactLookupException {
		if (StringUtils.hasText(beanId) && beanFactory.containsBean(beanId)) {
			return getArtifact(beanId, artifactType);
		}
		else {
			return createArtifact(id, artifactType);
		}
	}
}