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
package org.springframework.webflow;

/**
 * A command that executes arbitrary behavior and returns a logical execution
 * result a calling Flow can respond to. Actions typically delegate down to the
 * service-layer to perform business operations. They often prepare views with
 * dynamic or calculated model data to support response rendering. They act as a
 * bridge between a SWF web-tier and your middle-tier business logic layer.
 * <p>
 * When an action completes execution it signals a result event describing the
 * outcome of that execution (for example, "success", "error", "yes", "no",
 * "tryAgain", etc). In addition to providing a logical outcome the flow can
 * respond to, a result event may have payload associated with it, for example a
 * "success" return value or an "error" error code. The result event is
 * typically used as grounds for a state transition out of the current state of
 * the calling Flow.
 * <p>
 * Action implementations are often application-scoped singletons instantiated
 * and managed by a web-tier Spring application context to take advantage of
 * Spring's externalized configuration and dependency injection capabilities
 * (which is a form of Inversion of Control [IoC]). Actions may also be stateful
 * prototypes, storing conversational state as instance variables (see
 * {@link org.springframework.webflow.action.StatefulBeanInvokingAction} and
 * {@link org.springframework.webflow.action.AbstractBeanInvokingAction} for
 * more information). Action instances may also be locally scoped to a specific
 * flow definition (see use of the "import" element of the root XML flow
 * definition element.)
 * <p>
 * Note: Actions are directly instantiatable for use in a standalone test
 * environment and can be parameterized with mocks or stubs, as they are simple
 * POJOs. Action proxies may also be generated at runtime for delegating to POJO
 * business operations that have no dependency on the SWF API (see the
 * {@link org.springframework.webflow.action.LocalBeanInvokingAction} proxy
 * which is used by default when a POJO is referenced as an action from a Flow
 * definition).
 * <p>
 * Note: if an Action is a singleton managed in application scope, take care not
 * to store and/or modify caller-specific state in a unsafe manner. The Action
 * {@link #execute(RequestContext)} method runs in an independently executing
 * thread on each invocation, so make sure you deal only with local data or
 * internal, thread-safe services.
 * <p>
 * Note: an Action is not a controller like a Spring MVC controller or a Struts
 * action is a controller. <b>Flow actions are <i>commands</i></b>. Such
 * commands do not select views, they execute arbitrary command logic and then
 * return an logical command execution result. The flow that invokes an Action
 * is responsible for responding to the execution result to decide what to do
 * next. In Spring Web Flow, the flow <i>is</i> the controller.
 * 
 * @see org.springframework.webflow.ActionState
 * @see org.springframework.webflow.test.MockRequestContext
 * @see org.springframework.webflow.action.AbstractBeanInvokingAction
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface Action {

	/**
	 * Execute this action. Action execution will occur in the context of a
	 * request associated with an active flow execution.
	 * <p>
	 * More specifically, action execution is triggered in a production
	 * environment when invoked within the state of an ongoing flow execution
	 * for a specific <code>Flow</code> definition. The result of action
	 * execution, a logical outcome event, can be used as grounds for a
	 * transition out of the calling state.
	 * <p>
	 * Note: The <code>RequestContext</code> argument to this method provides
	 * access to the <b>data model</b> of the active flow execution in the
	 * context of the currently executing thread. Among other things, this
	 * allows this action to access model data set by other actions, as well as
	 * set its own attributes it wishes to expose in a given scope.
	 * <p>
	 * All attributes set in "flow scope" by Actions will exist for the life of
	 * the flow session and will be cleaned up automatically when the flow
	 * session ends. All attributes set in "request scope" exist for the life of
	 * the currently executing request only.
	 * <p>
	 * All attributes present in any scope are automatically exposed in the
	 * model for access by a view when a <code>ViewState</code> or other
	 * "interactive" state type is entered.
	 * <p>
	 * Note: the flow scope should NOT be used as a general purpose cache, but
	 * rather as a context for data needed locally by other states of the flow
	 * this action participates in. For example, it would be inappropriate to
	 * stuff large collections of objects (like those returned to support a
	 * search results view) into flow scope. Instead, put such result
	 * collections in request scope, and ensure you execute this action again
	 * each time you wish to view those results. 2nd level caches managed
	 * outside of SWF are much more general cache solutions.
	 * <p>
	 * Note: as flow scoped attributes are eligible for serialization they
	 * should be <code>Serializable</code>.
	 * 
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope" as well as obtaining other flow
	 * contextual information (e.g. action execution properties and flow
	 * execution data)
	 * @return a logical result outcome, used as grounds for a transition out of
	 * the current calling action state (e.g. "success", "error", "yes", "no",
	 * ...)
	 * @throws Exception a exception occured during action execution, either
	 * checked or unchecked; note, any <i>recoverable</i> exceptions should be
	 * caught within this method and an appropriate result outcome returned
	 * <i>or</i> be handled by the current state of the calling flow execution.
	 */
	public Event execute(RequestContext context) throws Exception;
}