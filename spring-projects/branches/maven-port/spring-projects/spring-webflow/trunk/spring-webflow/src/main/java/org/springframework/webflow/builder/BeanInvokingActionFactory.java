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
package org.springframework.webflow.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.method.ClassMethodKey;
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.Action;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.action.AbstractBeanInvokingAction;
import org.springframework.webflow.action.BeanFactoryBeanInvokingAction;
import org.springframework.webflow.action.LocalBeanInvokingAction;
import org.springframework.webflow.action.MementoBeanStatePersister;
import org.springframework.webflow.action.MementoOriginator;
import org.springframework.webflow.action.MethodResultSpecification;
import org.springframework.webflow.action.ResultEventFactory;
import org.springframework.webflow.action.ResultObjectBasedEventFactory;
import org.springframework.webflow.action.StatefulBeanInvokingAction;
import org.springframework.webflow.action.SuccessEventFactory;

/**
 * A factory for {@link Action} instances that invoke methods on beans.
 * <p>
 * This factory encapsulates the logic required to take an arbitrary
 * <code>java.lang.Object</code> method and adapt it to the {@link Action}
 * interface.
 * 
 * @see org.springframework.webflow.action.LocalBeanInvokingAction
 * @see org.springframework.webflow.action.BeanFactoryBeanInvokingAction
 * @see org.springframework.webflow.action.StatefulBeanInvokingAction
 * 
 * @author Keith Donald
 */
public class BeanInvokingActionFactory {

	/**
	 * Determines which result event factory should be used for each bean
	 * invoking action created by this factory.
	 */
	private ResultEventFactorySelector resultEventFactorySelector = new ResultEventFactorySelector();

	/**
	 * Returns the strategy for calcuating the result event factory to configure
	 * for each bean invoking action created by this factory.
	 */
	public ResultEventFactorySelector getResultEventFactorySelector() {
		return resultEventFactorySelector;
	}

	/**
	 * Sets the strategy to calculate the result event factory to configure for
	 * each bean invoking action created by this factory.
	 */
	public void setResultEventFactorySelector(ResultEventFactorySelector resultEventFactorySelector) {
		this.resultEventFactorySelector = resultEventFactorySelector;
	}

	/**
	 * Factory method that creates a bean invoking action, an adapter that
	 * adapts a method on an abitrary {@link Object} to the {@link Action}
	 * interface. This method is an atomic operation that returns a fully
	 * initialized Action. It encapsulates the selection of the action
	 * implementation as well as the action assembly.
	 * @param beanId the id of the bean to be adapted to an Action instance
	 * @param beanFactory the bean factory where the bean is managed
	 * @param methodSignature the method to invoke on the bean when the action
	 * is executed (required)
	 * @param resultSpecification the specification for what to do with the
	 * method return value; may be null
	 * @param conversionService the conversion service to be used to convert method
	 * parameters
	 * @param attributes attributes that may be used to affect the bean invoking
	 * action's construction
	 * @return the fully configured bean invoking action instance
	 */
	public Action createBeanInvokingAction(String beanId, BeanFactory beanFactory, MethodSignature methodSignature,
			MethodResultSpecification resultSpecification, ConversionService conversionService, AttributeCollection attributes) {
		if (!beanFactory.isSingleton(beanId)) {
			return createStatefulAction(beanId, beanFactory, methodSignature, resultSpecification, conversionService,	attributes);
		}
		else {
			Object bean = beanFactory.getBean(beanId);
			LocalBeanInvokingAction action = new LocalBeanInvokingAction(methodSignature, bean);
			configureCommonProperties(action, methodSignature, resultSpecification, bean.getClass(), conversionService);
			return action;
		}
	}

	/**
	 * Create a bean invoking action wrapping a statefull (prototype) bean.
	 * @param beanId the id of the bean to be adapted to an Action instance
	 * @param beanFactory the bean factory where the bean is managed
	 * @param methodSignature the method to invoke on the bean when the action
	 * is executed (required)
	 * @param resultSpecification the specification for what to do with the
	 * method return value; may be null
	 * @param conversionService the conversion service to be used to convert method
	 * parameters
	 * @param attributes attributes that may be used to affect the bean invoking
	 * action's construction
	 * @return the fully configured bean invoking action instance
	 */
	protected Action createStatefulAction(String beanId, BeanFactory beanFactory, MethodSignature methodSignature,
			MethodResultSpecification resultSpecification, ConversionService conversionService, AttributeCollection attributes) {
		Class beanClass = beanFactory.getType(beanId);
		if (MementoOriginator.class.isAssignableFrom(beanClass)) {
			BeanFactoryBeanInvokingAction action = new BeanFactoryBeanInvokingAction(methodSignature, beanId, beanFactory);
			action.setBeanStatePersister(new MementoBeanStatePersister());
			configureCommonProperties(action, methodSignature, resultSpecification, beanClass, conversionService);
			return action;
		}
		else {
			StatefulBeanInvokingAction action = new StatefulBeanInvokingAction(methodSignature, beanId, beanFactory);
			configureCommonProperties(action, methodSignature, resultSpecification, beanClass, conversionService);
			return action;
		}
	}
	
	// internal helpers

	/**
	 * Configure common properties of given bean invoking action.
	 */
	private void configureCommonProperties(AbstractBeanInvokingAction action, MethodSignature methodSignature,
			MethodResultSpecification resultSpecification, Class beanClass, ConversionService conversionService) {
		action.setMethodResultSpecification(resultSpecification);
		action.setResultEventFactory(resultEventFactorySelector.forMethod(methodSignature, beanClass));
		action.setConversionService(conversionService);
	}
	
	/**
	 * Helper strategy that selects the {@link ResultEventFactory} to use for each
	 * {@link AbstractBeanInvokingAction bean invoking action} that is constructed
	 * by a {@link org.springframework.webflow.builder.BeanInvokingActionFactory}.
	 * 
	 * @author Keith Donald
	 */
	public static class ResultEventFactorySelector {

		/**
		 * The event factory instance for mapping a return value to a success event.
		 */
		private SuccessEventFactory successEventFactory = new SuccessEventFactory();

		/**
		 * The event factory instance for mapping a result object to an event, using
		 * the type of the result object as the mapping criteria.
		 */
		private ResultObjectBasedEventFactory resultObjectBasedEventFactory = new ResultObjectBasedEventFactory();

		/**
		 * Select the appropriate result event factory for attempts to invoke the
		 * method on the specified bean class.
		 * @param signature the method signature
		 * @param beanClass the bean class
		 * @return the result event factory
		 */
		public ResultEventFactory forMethod(MethodSignature signature, Class beanClass) {
			ClassMethodKey key = new ClassMethodKey(beanClass, signature.getMethodName(), signature.getParameters().getTypesArray());
			if (resultObjectBasedEventFactory.isMappedValueType(key.getMethod().getReturnType())) {
				return resultObjectBasedEventFactory;
			}
			else {
				return successEventFactory;
			}
		}
	}
}