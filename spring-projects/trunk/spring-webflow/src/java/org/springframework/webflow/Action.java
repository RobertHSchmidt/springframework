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
package org.springframework.webflow;

/**
 * A command that executes some behaivior and returns a logical execution 
 * result.  Actions typically delegate down to the service-layer to perform
 * business operations, and/or prep views with dynamic model data for rendering.
 * They act as a bridge between the web-tier client and the middle-tier
 * service layer.
 * <p>
 * When an action completes execution, it signals a single result event describing the
 * outcome of the execution ("success", "error", "yes,", "no", etc).
 * This result event is used as grounds for a state transition in the current state.
 * <p>
 * Action implementations are typically application-scoped singletons
 * instantiated and managed by a web-tier Spring application context to take
 * advantage of Spring's powerful configuration and dependency injection (ioc)
 * capabilities. Actions can also be directly instantiated for use in a standalone
 * test environment and parameterized with mocks or stubs, as they are simple POJOs.
 * In the future, first-class support for request (thread)-specific actions and
 * flow-scoped action instances may also be supported.
 * <p>
 * Note: because Actions are typically singletons managed in application scope, take
 * care not to store and/or modify caller-specific state in a unsafe manner. The
 * Action execute(RequestContext) method runs in an independently executing thread
 * on each invocation so make sure you deal only with local data or
 * internal, thread-safe services.
 * <p>
 * Note: a webflow action is not a controller like a Spring MVC controller or a Struts action is a 
 * controller.  Web flow actions are <i>commands</i>.  Such commands do not select views, they return 
 * logical execution results.  The webflow is responsible for responding to the result of the command
 * to decide what to do next.  In Spring Web Flow, the flow <i>is</i> the controller.
 * 
 * @see org.springframework.webflow.ActionState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface Action {

	/**
	 * Execute this action. Action execution will occur in the context of a
	 * request associated with an active flow execution.
	 * <p>
	 * More specifically, Action execution is triggered in a production
	 * environment when an <code>ActionState</code> is entered as part of an
	 * ongoing flow execution for a specific <code>Flow</code>
	 * definition. The result of Action execution, a logical outcome event, is
	 * used as grounds for a transition out of the calling action state.
	 * <p>
	 * Note: The <code>RequestContext</code> argument to this method provides
	 * access to the <b>data model</b> of the active flow execution in the
	 * context of the currently executing thread. Among other things, this
	 * allows this Action to access model data set by other Actions, as well as
	 * set its own attributes it wishes to expose in a given scope.
	 * <p>
	 * All attributes set in "flow scope" exist for the life of the flow session
	 * and will be cleaned up when the flow session ends. All attributes set in
	 * "request scope" exist for the life of the current executing request only.
	 * <p>
	 * All attributes present in any scope are automatically exposed in the model for
	 * convenient access by the views when a <code>ViewState</code> is entered.
	 * <p>
	 * Note: flow <code>Scope</code> should NOT be used as a general
	 * purpose cache, but rather as a context for data needed locally by other states
	 * of the flow this action participates in.  For example, it would be inappropriate
	 * to stuff large collections of objects (like those returned to support a
	 * search results view) into flow scope. Instead, put such result
	 * collections in request scope, and ensure you execute this action again
	 * each time you wish to view those results. 2nd level caches are much
	 * better cache solutions.
	 * <p>
	 * Note: as flow scoped attributes are eligible for serialization thus
	 * they should implemented <code>Serializable</code>.
	 * 
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope" as well as obtaining other flow
	 *        contextual information (e.g action execution properties and flow execution data)
	 * @return a logical result outcome, used as grounds for a transition out of the
	 *         current calling action state (e.g. "success", "error", "yes", "no")
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked; note, any <i>recoverable</i> exceptions should be
	 *         caught within this method and an appropriate result outcome
	 *         returned instead
	 */
	public Event execute(RequestContext context) throws Exception;
}