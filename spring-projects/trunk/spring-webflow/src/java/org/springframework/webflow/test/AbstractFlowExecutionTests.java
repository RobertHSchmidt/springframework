/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.core.style.StylerUtils;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactLookupException;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.config.FlowLocator;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionImpl;

/**
 * Base class for integration tests that verify a flow executes as expected.
 * Flow execution tests captured by subclasses should test that a flow responds
 * to all supported transition criteria correctly, transitioning to the correct
 * states and producing the appropriate results on the occurence of possible
 * "external" (user) events.
 * <p>
 * More specifically, a typical flow execution test case will test:
 * <ul>
 * <li>That the flow execution starts as expected given a source event with
 * potential input request parameters (see the {@link #startFlow(Map)}
 * variants).
 * <li>That given the set of supported transition criteria for a given state,
 * that the state executes the appropriate transition when an event is signaled
 * (with potential input request parameters, see the
 * {@link #signalEvent(String, Map)} variants). A test case should be coded for
 * each logical event that can occur, where an event drives a possible path
 * through the flow. The goal should be to exercise all possible paths of the
 * flow.
 * <li>That given a transition that leads to an interactive state type (a view
 * state or an end state), that the view selection returned to the client
 * matches what was expected and the current state of the flow matches what is
 * expected.
 * </ul>
 * A flow execution test can effectively automate and validate the orchestration
 * required to drive an end-to-end business process that spans several steps
 * involving the user. Such tests are a good way to test your system top-down
 * starting at the web-tier and pushing through all the way to the DB without
 * having to deploy to a servlet or portlet container. Because these tests are
 * typically end-to-end integration tests that involve a transactional resource
 * such a database, this class subclasses Spring's
 * AbstractTransactionalSpringContextTests to take advantage of its automatic
 * transaction management and rollback capabilites. If you do not need those
 * capabilities, you must call <code>setDependencyCheck(false)</code> in your
 * test's constructor to turn off dependency checking for the transaction
 * manager property.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionTests extends AbstractTransactionalSpringContextTests {

	/**
	 * The flow locator; providing means to lookup and retrieve configured
	 * flows. Used to resolve the Flow to be tested by <code>id</code>.
	 */
	private FlowLocator flowLocator;

	/**
	 * The flow execution running the flow when the test is active (runtime
	 * object).
	 */
	private FlowExecution flowExecution;

	/**
	 * Creates a Flow execution test. This constructor disables dependency
	 * checking by default, so having Spring autowire dependencies like
	 * TransactionManager and FlowLocator is optional.
	 */
	public AbstractFlowExecutionTests() {
		setDependencyCheck(false);
	}

	/**
	 * Returns the flow locator used to resolve the Flow to be tested by
	 * <code>id</code>.
	 */
	protected FlowLocator getFlowLocator() {
		if (this.flowLocator == null) {
			this.flowLocator = createFlowLocator();
		}
		return flowLocator;
	}

	/**
	 * Sets the flow locator used to resolve the Flow definition whose execution
	 * is to be tested by this test.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Subclasses should override this method to customize the FlowLocator
	 * implementation that is returned to locate the Flow definition whose
	 * execution will be tested.
	 */
	protected FlowLocator createFlowLocator() {
		throw new IllegalStateException("Override this method to return a custom FlowLocator or "
				+ "make sure one of the 'configLocations' XML files defines a FlowLocator bean definition Spring can "
				+ "automatically inject into this test");
	}

	/**
	 * Get the singleton flow definition whose execution is being tested.
	 * @return the singleton flow definition
	 * @throws FlowArtifactLookupException if the flow identified by flowId()
	 * could not be resolved (if <code>this.flow</code> was null)
	 */
	protected Flow getFlow() throws FlowArtifactLookupException {
		return getFlowLocator().getFlow(flowId());
	}

	/**
	 * Subclasses should override to return the <code>flowId</code> whose
	 * execution should be tested.
	 * @return the flow id, whose execution is to be tested
	 */
	protected abstract String flowId();

	protected final void onSetUpInTransaction() throws Exception {
		onSetUpInTransactionalFlowTest();
	}

	/**
	 * Hook method subclasses can implement to do additional setup. Called after
	 * the transition has been activated and the flow locator has been set.
	 */
	protected void onSetUpInTransactionalFlowTest() {
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow() {
		return startFlow(null, new MockExternalContext(Collections.EMPTY_MAP));
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * @param input request parameters needed by the flow execution to start
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow(Map parameters) {
		return startFlow(null, new MockExternalContext(parameters));
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * @param stateId the state to start the flow execution in
	 * @param input request parameters needed by the flow execution to start
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow(String stateId, Map parameters) {
		return startFlow(stateId, new MockExternalContext(parameters));
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * @param context the external context providing information about the
	 * caller's environment, used by the flow execution during the start
	 * operation
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow(ExternalContext context) {
		return startFlow(null, context);
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * @param stateId the state to start the flow execution in
	 * @param context the external context providing information about the
	 * caller's environment, used by the flow execution during the start
	 * operation
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow(String stateId, ExternalContext context) {
		this.flowExecution = new FlowExecutionImpl(getFlow());
		onFlowExecutionStarting(flowExecution);
		return this.flowExecution.start(stateId, context);
	}

	/**
	 * Hook method where you can do additional setup of a flow execution before
	 * it is started, like register an execution listener.
	 * @param flowExecution the flow execution
	 */
	protected void onFlowExecutionStarting(FlowExecution flowExecution) {
	}

	/**
	 * Signal an occurence of an event in the state of the flow execution being
	 * tested.
	 * @param eventId the event that occured
	 * @param input request parameters needed by the flow execution to complete event processing
	 */
	protected ViewSelection signalEvent(String eventId, Map parameters) {
		return this.flowExecution.signalEvent(eventId, null, new MockExternalContext(parameters));
	}

	/**
	 * Signal an occurence of an event in the state of the flow execution being
	 * tested.
	 * @param eventId the event that occured
	 * @param stateId the state the event occured in
	 * @param input request parameters needed by the flow execution to complete event processing
	 */
	protected ViewSelection signalEvent(String eventId, String stateId, Map parameters) {
		return this.flowExecution.signalEvent(eventId, stateId, new MockExternalContext(parameters));
	}

	/**
	 * Signal an occurence of an event in the state of the flow execution being
	 * tested.
	 * @param eventId the event that occured
	 * @param context the external context providing information about the
	 * caller's environment, used by the flow execution during the start
	 * operation
	 */
	protected ViewSelection signalEvent(String eventId, ExternalContext context) {
		return this.flowExecution.signalEvent(eventId, null, context);
	}

	/**
	 * Signal an occurence of an event in the state of the flow execution being
	 * tested.
	 * <p>
	 * Note: signaling an event will cause state transitions to occur in a chain
	 * UNTIL control is returned to the caller. Control is returned once a
	 * "interactive" state type is entered: either a view state when the flow is
	 * paused or an end state when the flow terminates. Action states are
	 * executed without returning control, as their result always triggers
	 * another state transition, executed internally. Action states can also be
	 * executed in a chain like fashion (e.g. action state 1 (result), action
	 * state 2 (result), action state 3 (result), view state <control returns so
	 * view can be rendered>).
	 * <p>
	 * If you wish to verify expected behavior on each state transition (and not
	 * just when the view state triggers return of control back to the client),
	 * you have a few options:
	 * <p>
	 * First, you may implement a standalone unit test for your
	 * <code>Action</code> implementation. There you can verify that the
	 * action executes its core logic and responds to any exceptions it must
	 * handle. When you do this, you may mock or stub out services the Action
	 * implementation needs that are expensive to initialize. You can also
	 * verify there that the action puts everything in the flow or request scope
	 * it was supposed to (to meet its contract with the view it is prepping for
	 * display, if it's a view setup action).
	 * <p>
	 * Second, you can attach a FlowExecutionListener to the ongoing flow
	 * execution at any time within your test code, which receives callbacks on
	 * each state transition (among other points). To add a listener, call
	 * <code>getFlowExecution().getListenerList().add(myListener)</code>,
	 * where myListener is a class that implements the FlowExecutionListener
	 * interface. It is recommended you extend
	 * {@link org.springframework.webflow.execution.FlowExecutionListenerAdapter}
	 * and only override what you need.
	 * @param eventId the event that occured
	 * @param stateId the state the event occured in
	 * @param context the external context providing information about the
	 * caller's environment, used by the flow execution during the start
	 * operation
	 * @return the view selection that was made, returned once control is
	 * returned to the client (occurs when the flow enters a view state, or an
	 * end state)
	 */
	protected ViewSelection signalEvent(String eventId, String stateId, MockExternalContext context) {
		return this.flowExecution.signalEvent(eventId, stateId, context);
	}

	/**
	 * Returns the flow execution context, providing information about the
	 * ongoing flow execution being tested.
	 * @return the flow execution
	 * @throws IllegalStateException the execution has not been started
	 */
	protected FlowExecutionContext getFlowExecutionContext() throws IllegalStateException {
		if (flowExecution == null) {
			throw new IllegalStateException("The flow execution has not been started; call startFlow first");
		}
		return flowExecution;
	}

	/**
	 * Assert that the active flow session is for the flow with the provided id.
	 * @param expectedActiveFlowId the flow id that should have a session active
	 * in the tested flow execution
	 */
	protected void assertActiveFlowEquals(String expectedActiveFlowId) {
		assertEquals("The active flow id '" + getFlowExecutionContext().getActiveSession().getFlow()
				+ "' does not equal the expected active flow '" + expectedActiveFlowId + "'", expectedActiveFlowId,
				getFlowExecutionContext().getActiveSession().getFlow().getId());
	}

	/**
	 * Assert that the entire flow execution is active; that is, it has not
	 * ended and has been started.
	 */
	protected void assertFlowExecutionActive() {
		assertTrue("The flow execution is not active but it should be", flowExecution.isActive());
	}

	/**
	 * Assert that the entire flow execution has ended; that is, it is no longer
	 * active.
	 */
	protected void assertFlowExecutionEnded() {
		assertTrue("The flow execution is still active but it should have ended", !flowExecution.isActive());
	}

	/**
	 * Assert that the current state of the flow execution equals the provided
	 * state id.
	 * @param expectedCurrentStateId the expected current state
	 */
	protected void assertCurrentStateEquals(String expectedCurrentStateId) {
		assertEquals("The current state '" + getFlowExecutionContext().getActiveSession().getCurrentState().getId()
				+ "' does not equal the expected state '" + expectedCurrentStateId + "'", expectedCurrentStateId,
				getFlowExecutionContext().getActiveSession().getCurrentState().getId());
	}

	/**
	 * Assert that the last supported event that occured in the flow execution
	 * equals the provided event.
	 * @param expectedEventId the expected event
	 */
	protected void assertLastEventEquals(String expectedEventId) {
		assertEquals("The last event '" + getFlowExecutionContext().getLastEventId()
				+ "' does not equal the expected event '" + expectedEventId + "'", expectedEventId,
				getFlowExecutionContext().getLastEventId());
	}

	/**
	 * Assert that the view name equals the provided value.
	 * @param expectedViewName the expected name
	 * @param selectedView the selected view with a model attribute map to
	 * assert against
	 */
	public void assertViewNameEquals(String expectedViewName, ViewSelection selectedView) {
		assertEquals("The view name is wrong:", expectedViewName, selectedView.getViewName());
	}

	/**
	 * Assert that the selected view contains the specified model attribute with
	 * the provided expected value.
	 * @param expectedValue the expected value
	 * @param attributeName the attribute name
	 * @param selectedView the selected view with a model attribute map to
	 * assert against
	 */
	public void assertModelAttributeEquals(Object expectedValue, String attributeName, ViewSelection selectedView) {
		assertEquals("The model attribute '" + attributeName + "' value is wrong:", expectedValue,
				evaluateModelAttributeExpression(attributeName, selectedView.getModel()));
	}

	/**
	 * Assert that the selected view contains the specified collection model
	 * attribute with the provided expected size.
	 * @param expectedSize the expected size
	 * @param attributeName the collection attribute name
	 * @param selectedView the selected view with a model attribute map to
	 * assert against
	 */
	public void assertModelAttributeCollectionSize(int expectedSize, String attributeName, ViewSelection selectedView) {
		assertModelAttributeNotNull(attributeName, selectedView);
		Collection c = (Collection)evaluateModelAttributeExpression(attributeName, selectedView.getModel());
		assertEquals("The model attribute '" + attributeName + "' collection size is wrong:", expectedSize, c.size());
	}

	/**
	 * Assert that the selected view contains the specified model attribute.
	 * @param attributeName the attribute name
	 * @param selectedView the selected view with a model attribute map to
	 * assert against
	 */
	public void assertModelAttributeNotNull(String attributeName, ViewSelection selectedView) {
		assertNotNull("The model attribute '" + attributeName
				+ "' is [null] but should be NOT null, model contents are: "
				+ StylerUtils.style(selectedView.getModel()), evaluateModelAttributeExpression(attributeName,
				selectedView.getModel()));
	}

	/**
	 * Assert that the selected view does not contain the specified model
	 * attribute.
	 * @param attributeName the attribute name
	 * @param selectedView the selected view with a model attribute map to
	 * assert against
	 */
	public void assertModelAttributeNull(String attributeName, ViewSelection selectedView) {
		assertNull("The model attribute '" + attributeName + "' is NOT null but should be [null], model contents are:"
				+ StylerUtils.style(selectedView.getModel()), evaluateModelAttributeExpression(attributeName,
				selectedView.getModel()));
	}

	/**
	 * Evaluates a model attribute expression: e.g.
	 * attributeName.childAttribute.attr
	 * @param attributeName the attribute expression
	 * @param model the model map
	 * @return the attribute expression value
	 */
	protected Object evaluateModelAttributeExpression(String attributeName, Map model) {
		return ExpressionFactory.parseExpression(attributeName).evaluateAgainst(model, null);
	}
}