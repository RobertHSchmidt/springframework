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
import org.springframework.binding.support.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Thin action proxy that delegates to another action that is managed in flow scope. This
 * action can be used to work with stateful actions: action that hold modifiable, 
 * thread-safe state as instance members.
 * <p>
 * To use this action, configure the <code>actionId</code> bean property on this class
 * or set the <code>actionId</code> execution property on each invocation (which takes precedence).
 * The actionId will be treated as the beanId of a prototype action bean definition in the 
 * configured bean factory to retrieve.  Once retrieved, the <code>actionId</code> is used to
 * store the new action instance in the flow scope of the ongoing flow execution.
 * <p>
 * All action execution properties for this action will be available to the wrapped action.
 * You might have something like this in your web flow XML definition:
 * <pre>
 * &lt;action-state id="doStuff"&gt;
 *    &lt;action bean="myStatefulActionProxy" method="someMethod"&gt;
 *       &lt;property name="myProperty" value="myValue" /&gt;
 *       &lt;property name="actionId" value="myStatefulMultiAction" /&gt;
 *    &lt;/action&gt;
 * &lt;/action-state&gt;
 * </pre>
 * <p>
 * Implementation note: Stateful actions should be serializable.
 * </p>
 * @author Erwin Vervaet
 */
public class StatefulActionProxy extends AbstractAction implements BeanFactoryAware {
	
	/**
	 * Execution property used to specify the bean id of the stateful action
	 * in the bean factory. The bean should be configured as a prototype instance.
	 */
	public static final String ACTION_ID_PROPERTY = "actionId";
	
	/**
	 * The id of the action that is stateful. 
	 */
	private String actionId;
	
	/**
	 * The bean factory where the action definition exists (the definition must be a prototype). 
	 */
	private BeanFactory beanFactory;
	
	/**
	 * Returns the bean id of the stateful action in the bean factory. The action
	 * bean should be configured as a prototype instance.
	 */
	public String getActionId() {
		return this.actionId;
	}
	
	/**
	 * Set the bean id of the stateful action in the bean factory.
	 */
	public void setActionId(String statefulActionId) {
		this.actionId = statefulActionId;
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	protected Event doExecute(RequestContext context) throws Exception {
		return getStatefulAction(context).execute(context);
	}
	
	/**
	 * Get the stateful action instance to wrap for given request context.
	 * @param context the flow execution request context
	 * @return the action to wrap
	 */
	protected Action getStatefulAction(RequestContext context) {
		String beanId = getStatefulActionId(context);
		Assert.hasText(beanId, "You must specify the id of the stateful action to invoke using the 'actionId' property");
		if (!context.getFlowScope().containsAttribute(beanId)) {
			Assert.isTrue(!beanFactory.isSingleton(beanId),
					"The bean definition with id '" + beanId + "' must be marked as a prototype in the bean factory");
			context.getFlowScope().setAttribute(beanId, beanFactory.getBean(beanId, Action.class));
		}
		return (Action)context.getFlowScope().getAttribute(beanId, Action.class);
	}

	/**
	 * Get the id of the statefull action bean. If an action property is specified,
	 * use that, otherwise use the value configured for this action.
	 * @param context the flow execution context
	 * @return the bean id
	 */
	protected String getStatefulActionId(RequestContext context) {
		if (context.getProperties().containsAttribute(ACTION_ID_PROPERTY)) {
			return (String)context.getProperties().getAttribute(ACTION_ID_PROPERTY);
		}
		else {
			return getActionId();
		}
	}
}