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
import org.springframework.util.StringUtils;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Thin action proxy that delegates to another action that is managed in flow
 * scope. This action can be used to work with stateful actions: action that
 * hold modifiable state as instance members.
 * <p>
 * To use this action, configure the <code>actionId</code> bean property on
 * this class or set the <code>actionId</code> execution property on each
 * invocation (which takes precedence). The actionId will be treated as the
 * beanId of a prototype action bean definition in the configured bean factory
 * to retrieve. Once retrieved, the <code>actionId</code> is used to store the
 * new action instance in the flow scope of the ongoing flow execution.
 * <p>
 * All action execution properties for this action will be available to the
 * wrapped action. You might have something like this in your web flow XML
 * definition:
 * 
 * <pre>
 *    &lt;action-state id=&quot;doStuff&quot;&gt;
 *       &lt;action bean=&quot;myStatefulActionProxy&quot; method=&quot;myActionMethod&quot;&gt;
 *          &lt;property name=&quot;actionId&quot; value=&quot;myStatefulMultiAction&quot; /&gt;
 *          &lt;property name=&quot;myCustomProperty&quot; value=&quot;myValue&quot; /&gt;
 *       &lt;/action&gt;
 *    &lt;/action-state&gt;
 * </pre>
 * 
 * <p>
 * Implementation note: Stateful actions should be serializable.
 * 
 * @author Erwin Vervaet
 */
public class StatefulActionProxy extends AbstractAction implements BeanFactoryAware {

	/**
	 * Execution property used to specify the bean id of the stateful action in
	 * the bean factory ("actionId"). The bean should be configured as a
	 * prototype instance.
	 */
	public static final String ACTION_ID_PROPERTY = "actionId";

	/**
	 * Execution property used to specify the attribute name the stateful action
	 * should be exposed under in flow scope.
	 */
	public static final String ACTION_ATTRIBUTE_PROPERTY = "actionAttribute";

	/**
	 * The id of the action that is stateful.
	 */
	private String actionId;

	/**
	 * The name of the attribute to expose the stateful action under in flow
	 * scope. This is optional, the default is the action id.
	 */
	private String actionAttribute;

	/**
	 * The bean factory where the action definition exists (the definition must
	 * be a prototype).
	 */
	private BeanFactory beanFactory;

	/**
	 * Returns the bean id of the stateful action in the bean factory. The
	 * action bean should be configured as a prototype instance.
	 */
	public String getActionId() {
		return actionId;
	}

	/**
	 * Set the bean id of the stateful action in the bean factory.
	 */
	public void setActionId(String statefulActionId) {
		this.actionId = statefulActionId;
	}

	/**
	 * Returns the attribute name to expose the stateful action under in flow
	 * scope.
	 */
	public String getActionAttribute() {
		return actionAttribute;
	}

	/**
	 * Sets the attribute name to expose the stateful action under in flow
	 * scope.
	 */
	public void setActionAttribute(String actionAttributeName) {
		this.actionAttribute = actionAttributeName;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		return getAction(context).execute(context);
	}

	/**
	 * Get the stateful action instance to proxy for given request context.
	 * @param context the flow execution request context
	 * @return the action to wrap
	 */
	protected Action getAction(RequestContext context) {
		String actionId = getActionId(context);
		Assert.hasText(actionId,
				"You must specify the id of the stateful action to invoke using the 'actionId' property");
		String actionAttribute = getActionAttribute(context, actionId);
		Assert.hasText(actionAttribute, "You must specify the attribute name of the stateful action in flow "
				+ "scope using the 'actionAttribute' property");
		if (!context.getFlowScope().contains(actionAttribute)) {
			context.getFlowScope().put(actionAttribute, lookupAction(actionId));
		}
		return (Action)context.getFlowScope().get(actionAttribute, Action.class);
	}

	/**
	 * Overriddable hook, useful for working with a lookup-method from a Spring
	 * bean definition.
	 * @param actionId the action id
	 * @return the stateful action
	 */
	protected Action lookupAction(String actionId) {
		Assert.isTrue(!beanFactory.isSingleton(actionId), "The stateful Action bean definition with id '" + actionId
				+ "' must be marked as a prototype in the bean factory");
		return (Action)beanFactory.getBean(actionId, Action.class);
	}

	/**
	 * Get the id of the statefull action bean. If an action property is
	 * specified, use that, otherwise use the value configured for this action.
	 * @param context the flow execution context
	 * @return the bean id
	 */
	protected String getActionId(RequestContext context) {
		return context.getAttributes().getString(ACTION_ID_PROPERTY, getActionId());
	}

	/**
	 * Get the name of the attribute the stateful action will be exposed under
	 * in flow scope. If specified as an action property, use that value,
	 * otherwise use the property value specified for this action, or the given
	 * actionId as a fallback value.
	 * @param context the flow execution context
	 * @param actionId the bean id of the stateful action
	 * @return the action attribute name
	 */
	protected String getActionAttribute(RequestContext context, String actionId) {
		String result = context.getAttributes().getString(ACTION_ATTRIBUTE_PROPERTY);
		if (result != null) {
			return result;
		}
		else {
			return StringUtils.hasText(getActionAttribute()) ? getActionAttribute() : actionId;
		}
	}
}