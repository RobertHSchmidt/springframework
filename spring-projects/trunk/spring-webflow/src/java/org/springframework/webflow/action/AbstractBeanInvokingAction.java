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

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.method.MethodInvoker;
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Base class for actions that delegate to methods on beans (plain old
 * java.lang.Objects). Acts as an adapter that adapts an {@link Object} method
 * to the SWF Action contract.
 * <p>
 * The method to invoke is determined by the value of the
 * {@link AnnotatedAction#METHOD_ATTRIBUTE} action execution property, typically
 * set when provisioning this Action's use as part of an {@link AnnotatedAction}
 * or an {@link ActionState}.
 * 
 * @author Keith Donald
 */
public abstract class AbstractBeanInvokingAction extends AbstractAction {

	/**
	 * The method invoker that performs the action-&gt;bean method binding.
	 */
	private MethodInvoker methodInvoker = new MethodInvoker();

	/**
	 * The strategy that saves and restores stateful bean fields. Some people
	 * might call what this enables memento-like <i>bijection</i>, where state
	 * is <i>injected</i> into a bean before invocation and then <i>outjected</i>
	 * after invocation.
	 */
	private BeanStatePersister beanStatePersister = new NoOpBeanStatePersister();

	/**
	 * The strategy that adapts bean method return values to Event objects.
	 */
	private ResultEventFactory resultEventFactory = new DefaultResultEventFactory();

	/**
	 * Returns the bean state management strategy used by this action.
	 */
	protected BeanStatePersister getBeanStatePersister() {
		return beanStatePersister;
	}

	/**
	 * Set the bean state management strategy.
	 */
	public void setBeanStatePersister(BeanStatePersister beanStatePersister) {
		this.beanStatePersister = beanStatePersister;
	}

	/**
	 * Returns the event adaption strategy used by this action.
	 */
	protected ResultEventFactory getResultEventFactory() {
		return resultEventFactory;
	}

	/**
	 * Set the return value-&gt;event adaption strategy.
	 */
	public void setResultEventFactory(ResultEventFactory resultEventFactory) {
		this.resultEventFactory = resultEventFactory;
	}

	/**
	 * Returns the bean method invoker helper.
	 */
	protected MethodInvoker getMethodInvoker() {
		return methodInvoker;
	}

	/**
	 * Set the conversion service to perform type conversion of event parameters
	 * to method arguments as neccessary.
	 */
	public void setConversionService(ConversionService conversionService) {
		methodInvoker.setConversionService(conversionService);
	}

	protected Event doExecute(RequestContext context) throws Exception {
		Object bean = getBeanStatePersister().restoreState(getBean(context), context);
		MethodSignature methodKey = (MethodSignature)context.getAttributes().getRequired(
				AnnotatedAction.METHOD_ATTRIBUTE, MethodSignature.class);
		Object returnValue = getMethodInvoker().invoke(methodKey, bean, context);
		exposeMethodReturnValue(returnValue, context);
		Event resultEvent = getResultEventFactory().createResultEvent(bean, returnValue, context);
		getBeanStatePersister().saveState(bean, context);
		return resultEvent;
	}

	/**
	 * Retrieves the bean to invoke a method on. Subclasses need to implement
	 * this method.
	 */
	protected abstract Object getBean(RequestContext context);

	/**
	 * Exposes the return value as an attribute in a configured scope, if
	 * necessary. Subclasses may override.
	 * @param returnValue the return value
	 * @param context the request context
	 */
	protected void exposeMethodReturnValue(Object returnValue, RequestContext context) {
		String resultName = context.getAttributes().getString(AnnotatedAction.RESULT_NAME_ATTRIBUTE, null);
		if (resultName != null) {
			ScopeType scopeType = (ScopeType)context.getAttributes().get(AnnotatedAction.RESULT_SCOPE_ATTRIBUTE,
					ScopeType.REQUEST);
			scopeType.getScope(context).put(resultName, returnValue);
		}
	}

	/**
	 * State persister that doesn't take any action - default, private
	 * implementation.
	 * 
	 * @author Keith Donald
	 */
	private static class NoOpBeanStatePersister implements BeanStatePersister {
		public Object restoreState(Object bean, RequestContext context) {
			return bean;
		}

		public void saveState(Object bean, RequestContext context) {
		}
	}
}