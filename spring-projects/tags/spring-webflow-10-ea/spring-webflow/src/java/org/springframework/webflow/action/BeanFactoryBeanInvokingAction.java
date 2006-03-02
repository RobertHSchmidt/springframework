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
package org.springframework.webflow.action;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;

/**
 * Thin action proxy that delegates to an arbitrary bean managed in a Spring
 * bean factory. The bean does not have to implement any special interface to be
 * invoked.
 * <p>
 * To use this class, you configure the name of the bean you wish to invoke, and
 * what method on that bean should be invoked with what arguments.
 * 
 * @author Keith Donald
 */
public class BeanFactoryBeanInvokingAction extends AbstractBeanInvokingAction implements BeanFactoryAware {

	/**
	 * The bean name action execution property.
	 */
	public static final String BEAN_PROPERTY = "bean";

	/**
	 * The name of the default bean to invoke when this action is executed.
	 * Optional: the bean to invoke may instead be specified as an action
	 * execution property.
	 */
	private String targetBeanName;

	/**
	 * The bean factory for loading beans to invoke by <code>id</code>.
	 */
	private BeanFactory beanFactory;

	/**
	 * Create a bean factory bean invoking action that is expected to be
	 * parameterized with information about which bean to invoke and the bean
	 * factory to retrieve it from.
	 */
	public BeanFactoryBeanInvokingAction() {
	}

	/**
	 * Create a bean factory bean invoking action that invokes the bean with the
	 * specified name.
	 * @param targetBeanName the bean name
	 */
	public BeanFactoryBeanInvokingAction(String targetBeanName) {
		setTargetBeanName(targetBeanName);
	}

	/**
	 * Create a bean factory bean invoking action that invokes the bean with the
	 * specified name.
	 * @param targetBeanName the bean name
	 * @param beanFactory the bean factory
	 */
	public BeanFactoryBeanInvokingAction(String targetBeanName, BeanFactory beanFactory) {
		setTargetBeanName(targetBeanName);
		setBeanFactory(beanFactory);
	}

	/**
	 * Returns the target bean name.
	 */
	public String getTargetBeanName() {
		return targetBeanName;
	}

	/**
	 * Set the name of the target bean to invoke. The bean will be looked up in
	 * the bean factory on action execution.
	 * 
	 * @param targetBeanName the target bean name
	 */
	public void setTargetBeanName(String targetBeanName) {
		Assert.hasText(targetBeanName,
				"The name of the target bean to invoke cannot be null or blank -- it is required");
		this.targetBeanName = targetBeanName;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Retrieves the bean to invoke. This implementation loads the bean by name
	 * from the BeanFactory. The name to use may be specified in this class, or
	 * alternatively as a action execution property, allowing this action
	 * instance to be parameterized to invoke many different beans throughout
	 * its life.
	 */
	protected Object getBean(RequestContext context) {
		String beanName = context.getAttributes().getString(BEAN_PROPERTY, getTargetBeanName());
		Assert.hasText(beanName, "The bean name to invoke was not specified: set the bean property");
		return getBeanFactory().getBean(beanName);
	}
}