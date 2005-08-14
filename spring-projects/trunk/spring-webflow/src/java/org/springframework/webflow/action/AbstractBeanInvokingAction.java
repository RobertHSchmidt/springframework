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

import org.springframework.binding.AttributeSource;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.method.MethodInvoker;
import org.springframework.binding.method.MethodKey;
import org.springframework.binding.support.ChainedAttributeSource;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Base class for actions that delegate to methods on abritrary beans.
 * 
 * @author Keith Donald
 */
public abstract class AbstractBeanInvokingAction extends MultiAction {

	private MethodInvoker beanMethodInvoker = new MethodInvoker();
	
	/**
	 * Set the conversion service to perform type conversion of event parameters
	 * to method arguments as neccessary.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.beanMethodInvoker.setConversionService(conversionService);
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Object bean = getBean(context);
		MethodKey methodKey = (MethodKey)context.getProperties().getAttribute(METHOD_PROPERTY);
		AttributeSource argumentSource = new ChainedAttributeSource(new AttributeSource[] { 
				context.getFlowScope(), context.getRequestScope(), context.getLastEvent() });
		return toEvent(beanMethodInvoker.invoke(methodKey, bean, argumentSource));
	}

	/**
	 * Retrieves the bean to invoke a method on. Subclasses need to
	 * implement this method.
	 */
	protected abstract Object getBean(RequestContext context);

	/**
	 * Hook method that converts the return value of bean method invokation into a
	 * web flow event. Subclasses can override this if needed.
	 */
	protected Event toEvent(Object returnValue) {
		if (returnValue instanceof Event) {
			return (Event)returnValue;
		}
		else {
			return success(returnValue);
		}
	}
}