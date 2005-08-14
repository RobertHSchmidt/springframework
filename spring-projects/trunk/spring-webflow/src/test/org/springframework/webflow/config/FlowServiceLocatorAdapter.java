/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.config;

import org.springframework.binding.convert.ConversionService;
import org.springframework.webflow.Action;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.access.AutowireMode;
import org.springframework.webflow.access.FlowServiceLocator;
import org.springframework.webflow.access.ServiceLookupException;
import org.springframework.webflow.support.FlowConversionService;

/**
 * Simple helper adapter for the flow service locator interface. For testing.
 * 
 * @author Keith Donald
 */
public class FlowServiceLocatorAdapter implements FlowServiceLocator {
	
	public ConversionService getConversionService() {
		return new FlowConversionService();
	}

	public Action createAction(Class implementationClass,
			AutowireMode autowireMode) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Flow createFlow(AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Flow createFlow(Class implementationClass, AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public FlowAttributeMapper createFlowAttributeMapper(
			Class attributeMapperImplementationClass, AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public State createState(Class implementationClass,
			AutowireMode autowireMode) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Transition createTransition(Class implementationClass,
			AutowireMode autowireMode) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public TransitionCriteria createTransitionCriteria(
			String encodedCriteria, AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public ViewDescriptorCreator createViewDescriptorCreator(
			String encodedView, AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Action getAction(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Action getAction(String id) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public FlowAttributeMapper getFlowAttributeMapper(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public FlowAttributeMapper getFlowAttributeMapper(String id)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public State getState(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public State getState(String id) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Transition getTransition(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Transition getTransition(String id) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Flow getFlow(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Flow getFlow(String id) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Object createBean(Class implementationClass, AutowireMode autowireMode) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Object getBean(Class implementationClass) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Object getBean(String beanId) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

}