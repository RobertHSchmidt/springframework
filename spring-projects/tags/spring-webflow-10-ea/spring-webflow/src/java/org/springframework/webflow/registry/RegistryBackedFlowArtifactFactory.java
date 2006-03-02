/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.registry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.builder.DefaultFlowArtifactFactory;
import org.springframework.webflow.builder.FlowArtifactFactory;
import org.springframework.webflow.builder.FlowArtifactParameters;

/**
 * A flow artifact factory that obtains subflow definitions from a explict
 * {@link FlowRegistry} The remaining types of artifacts are sourced from a
 * standard Spring {@link BeanFactory} registry.
 * 
 * @see FlowRegistry
 * @see FlowArtifactFactory#getSubflow(String)
 * 
 * @author Keith Donald
 */
public class RegistryBackedFlowArtifactFactory extends DefaultFlowArtifactFactory implements ResourceLoaderAware {

	/**
	 * The registry for locating subflows.
	 */
	private FlowRegistry subflowRegistry;

	/**
	 * The Spring bean factory that manages configured flow artifacts.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a flow artifact factory that retrieves subflows from the provided
	 * registry and additional artifacts from the provided bean factory.
	 * @param subflowRegistry The registry for loading subflows
	 * @param beanFactory The spring bean factory
	 */
	public RegistryBackedFlowArtifactFactory(FlowRegistry subflowRegistry, BeanFactory beanFactory) {
		this.subflowRegistry = subflowRegistry;
		this.beanFactory = beanFactory;
	}

	/**
	 * Returns the flow registry used by this flow artifact factory to manage
	 * subflow definitions.
	 * @return the flow registry
	 */
	public FlowRegistry getSubflowRegistry() {
		return subflowRegistry;
	}

	/**
	 * Returns the bean factory used by this flow artifact factory to manage
	 * custom flow artifacts.
	 * @return the bean factory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public Flow getSubflow(String id) throws FlowArtifactException {
		return subflowRegistry.getFlow(id);
	}

	public Action getAction(FlowArtifactParameters actionParameters) throws FlowArtifactException {
		return toAction(getBean(actionParameters.getId(), Action.class, false), actionParameters);
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
		return (TargetStateResolver)getBean(id, TargetStateResolver.class, true);
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
		catch (BeansException e) {
			throw new FlowArtifactException(id, artifactType, e);
		}
	}

	public BeanFactory getServiceRegistry() throws UnsupportedOperationException {
		return beanFactory;
	}
}