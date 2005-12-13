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
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Base class for actions that delegate to methods on abritrary beans. Acts as
 * an adapter that adapts a JavaBean method to the SWF Action contract.
 * <p>
 * The method to invoke is determined by the value of the
 * {@link org.springframework.webflow.AnnotatedAction#METHOD_PROPERTY} action
 * execution property, typically set when provisioning this Action's use as part
 * of an {@link org.springframework.webflow.ActionState}.
 * 
 * @author Keith Donald
 */
public abstract class AbstractBeanInvokingAction extends AbstractAction {

	/**
	 * The method invoker that performs the action-to-bean method binding.
	 */
	private MethodInvoker methodInvoker = new MethodInvoker();

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
	public void setStatePersister(BeanStatePersister statePersister) {
		this.statePersister = statePersister;
	}

	/**
	 * Set the conversion service to perform type conversion of event parameters
	 * to method arguments as neccessary.
	 */
	public void setConversionService(ConversionService conversionService) {
		this.methodInvoker.setConversionService(conversionService);
	}

	/**
	 * Returns the bean method invoker helper.
	 */
	protected MethodInvoker getMethodInvoker() {
		return methodInvoker;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Object bean = getBean(context);
		getStatePersister().restoreState(bean, context);
		MethodKey methodKey = (MethodKey)context.getProperties().getAttribute(AnnotatedAction.METHOD_PROPERTY);
		if (methodKey == null) {
			throw new IllegalStateException("The method to invoke was not provided: please set the '"
					+ AnnotatedAction.METHOD_PROPERTY + "' property");
		}
		Object returnValue = getMethodInvoker().invoke(methodKey, bean, context);
		processMethodReturnValue(returnValue, context);
		Event resultEvent = toEvent(context, returnValue);
		getStatePersister().saveState(bean, context);
		return resultEvent;
	}

	/**
	 * Retrieves the bean to invoke a method on. Subclasses need to implement
	 * this method.
	 */
	protected abstract Object getBean(RequestContext context);

	protected void processMethodReturnValue(Object returnValue, RequestContext context) {
		String resultName = (String)getActionProperty(context, AnnotatedAction.RESULT_NAME_PROPERTY, null);
		if (resultName != null) {
			ScopeType scopeType = (ScopeType)getActionProperty(context, AnnotatedAction.RESULT_SCOPE_PROPERTY, ScopeType.REQUEST);
			scopeType.getScope(context).setAttribute(resultName, returnValue);
		}
	}

	/**
	 * Hook method that converts the return value of bean method invokation into
	 * a web flow event. Subclasses can override this if needed.
	 */
	protected Event toEvent(RequestContext context, Object returnValue) {
		if (returnValue instanceof Event) {
			return (Event)returnValue;
		}
		else {
			String resultParameterName = (String)getActionProperty(context, RESULT_PARAMETER, RESULT_PARAMETER);
			if (returnValue instanceof Boolean) {
				return yesOrNo(((Boolean)returnValue).booleanValue());
			}
			else {
				return success(resultParameterName, returnValue);
			}
		}
	}

	/**
	 * State persister that doesn't take any action - default implementation.
	 * 
	 * @author Keith Donald
	 */
	public static class NoOpBeanStatePersister implements BeanStatePersister {
		public void restoreState(Object bean, RequestContext context) throws Exception {
		}

		public void saveState(Object bean, RequestContext context) throws Exception {
		}
	}
}