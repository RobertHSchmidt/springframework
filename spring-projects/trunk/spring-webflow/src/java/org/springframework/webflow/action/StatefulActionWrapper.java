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
 * Simple action that wraps another action, which is maintained in flow scope. This
 * action can be used to work with stateful actions: action that hold modifiable
 * state in the instance members.
 * <p>
 * To use this action, just configure the "statefulActionId" as a bean property or
 * as an action execution property (which takes precedence). That id will be used
 * as a bean id to lookup a prototype action bean instance in the bean factory. The
 * id will also be used to store that action in the flow scope of the ongoing
 * flow execution. All the action execution properties for this action will be
 * available to the wrapped action, so you could have something like this in your
 * web flow XML definition:
 * <pre>
 * &lt;action-state id="doStuff"&gt;
 *    &lt;action bean="myWrapper" method="someMethod"&gt;
 *       &lt;property name="somePropForMyAction" value="someValue" /&gt;
 *       &lt;property name="statefulActionId" value="myAction" /&gt;
 *    &lt;/action&gt;
 * &lt;/action-state&gt;
 * </pre>
 * 
 * @author Erwin Vervaet
 */
public class StatefulActionWrapper extends AbstractAction implements BeanFactoryAware {
	
	/**
	 * Execution property used to specify the bean id of the stateful action
	 * in the bean factory. The bean should be configured as a prototype instance.
	 */
	public static final String STATEFUL_ACTION_ID_PROPERTY = "statefulActionId";
	
	private String statefulActionId;
	
	private BeanFactory beanFactory;
	
	/**
	 * Returns the bean id of the stateful action in the bean factory. The action
	 * bean should be configured as a prototype instance.
	 */
	public String getStatefulActionId() {
		return this.statefulActionId;
	}
	
	/**
	 * Set the bean id of the stateful action in the bean factory.
	 */
	public void setStatefulActionId(String statefulActionId) {
		this.statefulActionId = statefulActionId;
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
		Assert.hasText(beanId, "You must specify the id of the stateful action to invoke using the 'statefulActionId' property");
		if (!context.getFlowScope().containsAttribute(beanId)) {
			Assert.isTrue(!beanFactory.isSingleton(beanId),
					"The bean with id '" + beanId + "' must be configured as a prototype instance in the bean factory");
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
		if (context.getProperties().containsAttribute(STATEFUL_ACTION_ID_PROPERTY)) {
			return (String)context.getProperties().getAttribute(STATEFUL_ACTION_ID_PROPERTY);
		}
		else {
			return getStatefulActionId();
		}
	}
}
