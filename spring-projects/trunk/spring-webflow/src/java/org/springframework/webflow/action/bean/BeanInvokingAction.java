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
import org.springframework.util.CachingMapDecorator;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

/**
 * Thin action proxy that delegates to another action that is sourced from a
 * registry.
 * <p>
 * event: submit
 * 
 * before execute: get instance restore state dispatch: - method: execute
 * parameters: foo, bar after execute: save state @
 * @author Keith Donald
 */
public class BeanInvokingAction extends AbstractAction implements
		BeanFactoryAware {
	private String beanId;

	private ConversionService conversionService = new DefaultConversionService();

	private BeanFactory beanFactory;

	private CachingMapDecorator methodCache = new CachingMapDecorator(true) {
		public Object create(Object key) {
			ClassMethodKey methodKey = (ClassMethodKey) key;
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

	protected Event doExecute(RequestContext context) throws Exception {
		Object bean = getBean(context);
		MethodKey methodKey = (MethodKey) context.getProperties().getAttribute(
				"method");
		Method method = (Method) methodCache.get(new ClassMethodKey(bean
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

	protected Object getBean(RequestContext context) {
		return beanFactory.getBean((String) getActionProperty(context, "bean",
				beanId));
	}

	protected Object applyTypeConversion(Object rawParameter, Class targetType) {
		if (rawParameter == null) {
			return null;
		}
		return conversionService.getConversionExecutor(rawParameter.getClass(),
				targetType).execute(rawParameter);
	}
}