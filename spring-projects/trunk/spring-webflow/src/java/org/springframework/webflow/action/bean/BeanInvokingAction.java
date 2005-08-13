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
package org.springframework.webflow.action.bean;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.util.Assert;
import org.springframework.util.CachingMapDecorator;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

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
public class BeanInvokingAction extends AbstractAction implements
		BeanFactoryAware {

	/**
	 * The bean action executino property
	 */
	private static final String BEAN_PROPERTY = "bean";

	/**
	 * The method action execution property.
	 */
	private static final String METHOD_PROPERTY = "method";

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
	private ConversionService conversionService = new DefaultConversionService();

	/**
	 * The bean factory for loading beans to invoke by <code>id</code>.
	 */
	private BeanFactory beanFactory;

	/**
	 * A cache of invoked bean methods, keyed weakly.
	 */
	private CachingMapDecorator methodCache = new CachingMapDecorator(true) {
		public Object create(Object key) {
			TypeMethodKey methodKey = (TypeMethodKey) key;
			try {
				return methodKey.lookupMethod();
			} catch (NoSuchMethodException e) {
				throw new InvalidMethodKeyException(methodKey, e);
			}
		}
	};

	public void setConversionService(ConversionService conversionService)
			throws BeansException {
		this.conversionService = conversionService;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Object bean = getBean(context);
		MethodKey methodKey = (MethodKey) context.getProperties().getAttribute(
				METHOD_PROPERTY);
		Method method = (Method) methodCache.get(new TypeMethodKey(bean
				.getClass(), methodKey));
		Object[] args = new Object[methodKey.getArguments().size()];
		Iterator it = methodKey.getArguments().iterator();
		int i = 0;
		while (it.hasNext()) {
			Argument argument = (Argument) it.next();
			args[i] = applyTypeConversion(context.getLastEvent().getParameter(
					argument.getName()), argument.getType());
			i++;
		}
		Object returnValue = method.invoke(bean, args);
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

	/**
	 * Apply type conversion on the event parameter if neccessary
	 * 
	 * @param rawParameter
	 *            the raw event parameter
	 * @param targetType
	 *            the target type for the matching method argument
	 * @return the converted method argument
	 */
	protected Object applyTypeConversion(Object rawParameter, Class targetType) {
		if (rawParameter == null) {
			return null;
		}
		return conversionService.getConversionExecutor(rawParameter.getClass(),
				targetType).execute(rawParameter);
	}
}