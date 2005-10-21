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

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.method.MethodInvoker;
import org.springframework.binding.method.MethodKey;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Base class for actions that delegate to methods on abritrary beans.
 * 
 * @author Keith Donald
 */
public abstract class AbstractBeanInvokingAction extends AbstractAction {

	/**
	 * The method invoker that performs the action->bean method binding.
	 */
	private MethodInvoker beanMethodInvoker = new MethodInvoker();

	/**
	 * The strategy that saves and restores stateful bean fields in flow scope.
	 */
	private BeanStatePersister statePersister = new NoOpBeanStatePersister();

	/**
	 * Returns the bean state management strategy used by this action.
	 */
	protected BeanStatePersister getStatePersister() {
		return statePersister;
	}

	/**
	 * Set the bean state management strategy.
	 */
	public void setStatePersister(BeanStatePersister stateManager) {
		this.statePersister = stateManager;
	}

	/**
	 * Set the conversion service to perform type conversion of event parameters
	 * to method arguments as neccessary.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.beanMethodInvoker.setConversionService(conversionService);
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Object bean = getBean(context);
		getStatePersister().restoreState(bean, context);
		MethodKey methodKey = (MethodKey)context.getProperties().getAttribute(MultiAction.METHOD_PROPERTY);
		if (methodKey == null) {
			throw new IllegalStateException("The method to invoke was not provided--set the '" + MultiAction.METHOD_PROPERTY
					+ "' property");
		}
		Event result = toEvent(context, beanMethodInvoker.invoke(methodKey, bean, context));
		getStatePersister().saveState(bean, context);
		return result;
	}

	/**
	 * Retrieves the bean to invoke a method on. Subclasses need to implement
	 * this method.
	 */
	protected abstract Object getBean(RequestContext context);

	/**
	 * Hook method that converts the return value of bean method invokation into
	 * a web flow event. Subclasses can override this if needed.
	 */
	protected Event toEvent(RequestContext context, Object result) {
		if (result instanceof Event) {
			return (Event)result;
		}
		else {
			String resultParameterName = (String)getActionProperty(context, RESULT_PARAMETER, RESULT_PARAMETER);
			return success(resultParameterName, result);
		}
	}

	/**
	 * State persister that doesn't take any action - default implementation.
	 * 
	 * @author Keith Donald
	 */
	private static class NoOpBeanStatePersister implements BeanStatePersister {
		public void restoreState(Object bean, RequestContext context) throws Exception {
		}

		public void saveState(Object bean, RequestContext context) throws Exception {
		}
	}
}