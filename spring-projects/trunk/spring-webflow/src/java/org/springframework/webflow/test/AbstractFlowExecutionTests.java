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
import java.util.Map;

import org.springframework.binding.expression.ExpressionFactory;
import org.springframework.core.style.StylerUtils;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;

/**
 * Base class for integration tests that verify a flow executes as expected.
 * Flow execution tests captured by subclasses should test that a flow responds
 * to all supported transition criteria correctly, transitioning to the correct
 * states and producing the expected results on the occurence of possible
 * external (user) events.
 * <p>
 * More specifically, a typical flow execution test case will test:
 * <ul>
 * <li>That the flow execution starts as expected given a request from an
 * external context containing potential input request parameters (see the
 * {@link #startFlow(Map)} variants).
 * <li>That given the set of supported transition criteria for a state, that
 * the state executes the appropriate transition when an event is signaled (with
 * potential input request parameters, see the {@link #signalEvent(String, Map)}
 * variants). A test case should be coded for each logical event that can occur,
 * where an event drives a possible path through the flow. The goal should be to
 * exercise all possible paths of the flow.
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
 * transaction management and rollback capabilites.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionTests extends AbstractTransactionalSpringContextTests {

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

	protected final void onSetUpInTransaction() throws Exception {
		onSetUpInTransactionalFlowTest();
	}

	/**
	 * Hook method subclasses can implement to do additional test setup.
	 */
	protected void onSetUpInTransactionalFlowTest() {
	}

	/**
	 * Start the flow execution that will be tested.
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow() {
		return startFlow(new MockExternalContext(), null);
	}

	/**
	 * Start the flow execution that will be tested. Pass in the populated
	 * request parameter map for access during execution startup.
	 * @param requestParameterMap request parameters needed by the flow
	 * execution to complete startup
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow(Map requestParameterMap) {
		return startFlow(new MockExternalContext(requestParameterMap), null);
	}

	/**
	 * Start the flow execution that will be tested.
	 * @param listener a single listener to attach to the flow execution for
	 * this test scenario.
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow(FlowExecutionListener listener) {
		return startFlow(new MockExternalContext(), new FlowExecutionListener[] { listener });
	}

	/**
	 * Start the flow execution that will be tested. Pass in the populated
	 * request parameter map for access during execution startup.
	 * @param requestParameterMap request parameters needed by the flow
	 * execution to complete startup
	 * @param listener a single listener to attach to the flow execution for
	 * this test scenario.
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow(Map requestParameterMap, FlowExecutionListener listener) {
		return startFlow(new MockExternalContext(requestParameterMap), new FlowExecutionListener[] { listener });
	}

	/**
	 * Start the flow execution that will be tested.
	 * <p>
	 * This is the most flexible of the start methods. It allows you to specify
	 * an external context that allows the flow execution being tested access to
	 * the calling environment for this request. It also allows you to specify
	 * an array of listeners that should observe the lifecycle of the flow
	 * execution being tested (and make test assertions during that lifecycle).
	 * @param context the external context providing information about the
	 * caller's environment, used by the flow execution during the start
	 * operation
	 * @param listeners an array of listeners to attach to the flow execution
	 * for this test scenario (may be null).
	 * @return the view selection made as a result of starting the flow
	 * (returned when the first interactive state (a view state or end state) is
	 * entered)
	 */
	protected ViewSelection startFlow(ExternalContext context, FlowExecutionListener[] listeners) {
		flowExecution = createFlowExecution(listeners);
		return flowExecution.start(context);
	}

	/**
	 * Create the flow execution tested by this test. Subclasses may override to
	 * customize the execution implementation, for example, to attach additional
	 * listeners.
	 * @return the flow execution for this test
	 */
	protected FlowExecution createFlowExecution(FlowExecutionListener[] listeners) {
		return new FlowExecutionImpl(getFlow(), listeners);
	}

	/**
	 * Retrieve the flow definition whose execution is to be tested by this
	 * test.
	 * @return the definition of the flow whose execution will be tested
	 * @throws FlowArtifactException if the flow identified by flowId() could
	 * not be resolved (if <code>this.flow</code> was null)
	 */
	protected abstract Flow getFlow() throws FlowArtifactException;

	/**
	 * Signal an occurence of an event in the current state of the flow
	 * execution being tested.
	 * @param eventId the event that occured
	 */
	protected ViewSelection signalEvent(String eventId) {
		return flowExecution.signalEvent(eventId, new MockExternalContext());
	}

	/**
	 * Signal an occurence of an event in the current state of the flow
	 * execution being tested.
	 * @param eventId the event that occured
	 * @param requestParameterMap request parameters needed by the flow
	 * execution to complete event processing
	 */
	protected ViewSelection signalEvent(String eventId, Map requestParameterMap) {
		return flowExecution.signalEvent(eventId, new MockExternalContext(requestParameterMap));
	}

	/**
	 * Signal an occurence of an event in the current state of the flow
	 * execution being tested.
	 * <p>
	 * Note: signaling an event will cause state transitions to occur in a chain
	 * until control is returned to the caller. Control is returned once a
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
	 * First, you may implement standalone unit tests for your
	 * {@link org.springframework.webflow.Action} implementations. There you can
	 * verify that an Action executes its logic properly in isolation. When you
	 * do this, you may mock or stub out services the Action implementation
	 * needs that are expensive to initialize. You can also verify there that
	 * the action puts everything in the flow or request scope it was expected
	 * to (to meet its contract with the view it is prepping for display, for
	 * example).
	 * <p>
	 * Second, you can attach one or more FlowExecutionListeners to the flow
	 * execution at start time within your test code, which will allow you to
	 * receive a callback on each state transition (among other points). It is
	 * recommended you extend
	 * {@link org.springframework.webflow.execution.FlowExecutionListenerAdapter}
	 * and only override the callback methods you are interested in.
	 * @param eventId the event that occured
	 * @param context the external context providing information about the
	 * caller's environment, used by the flow execution during the start
	 * operation
	 * @return the view selection that was made, returned once control is
	 * returned to the client (occurs when the flow enters a view state, or an
	 * end state)
	 */
	protected ViewSelection signalEvent(String eventId, ExternalContext context) {
		return flowExecution.signalEvent(eventId, context);
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
		assertNotNull("The model attribute '" + attributeName + "' is null but should not be; model contents are "
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
		assertNull("The model attribute '" + attributeName + "' is not null but should be; model contents are "
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