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
package org.springframework.webflow.action;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.method.MethodInvoker;
import org.springframework.binding.method.MethodKey;
import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Thin action proxy that delegates to an arbitrary bean managed in the Spring
 * bean factory. The bean does not have to implement any special interface to be
 * invoked.
 * <p>
 * To use this class, you configure the name of the bean you wish to invoke, and
 * what method on that bean should be invoked with what arguments.
 * 
 * @author Keith Donald
 */
public class BeanInvokingAction extends MultiAction implements
		BeanFactoryAware {

	/**
	 * The name of the default bean to invoke when this action is executed.
	 * Optional: the bean to invoke may instead be specified as an action
	 * execution property.
	 */
	private String targetBeanName;

	/**
	 * The spring binding conversion service, for performing event parameter ->
	 * method argument type conversion.
	 */
	private MethodInvoker beanMethodInvoker = new MethodInvoker();

	/**
	 * The bean factory for loading beans to invoke by <code>id</code>.
	 */
	private BeanFactory beanFactory;

	/**
	 * Set the conversion service to perform type conversion of event parameters
	 * to method arguments as neccessary.
	 * 
	 * @param conversionService
	 */
	public void setConversionService(ConversionService conversionService) {
		this.beanMethodInvoker.setConversionService(conversionService);
	}

	/**
	 * Set the name of the target bean to invoke. The bean will be looked up in
	 * the bean factory on action execution.
	 * 
	 * @param targetBeanName
	 *            the target bean name
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Object bean = getBean(context);
		MethodKey methodKey = (MethodKey) context.getProperties().getAttribute(
				METHOD_PROPERTY);
		Object returnValue = beanMethodInvoker.invoke(methodKey, bean, context.getLastEvent());
		return success(returnValue);
	}

	/**
	 * Retrieves the bean to invoke. This implementation loads the bean by name
	 * from the BeanFactory. The name to use may be specified in this class, or
	 * alternatively as a action execution property, allowing this action
	 * instance to be parameterized to invoke many different beans throughout
	 * its life.
	 */
	protected Object getBean(RequestContext context) {
		String beanName = (String) getActionProperty(context, BEAN_PROPERTY,
				this.targetBeanName);
		Assert
				.hasText(beanName,
						"The bean name to invoke was not specified: set the bean property");
		return beanFactory.getBean(beanName);
	}
}