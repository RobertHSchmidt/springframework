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

import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.config.BeanFactoryFlowServiceLocator;
import org.springframework.webflow.config.FlowBuilder;
import org.springframework.webflow.config.FlowFactoryBean;
import org.springframework.webflow.config.FlowLocator;
import org.springframework.webflow.config.ServiceLookupException;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.impl.FlowExecutionImpl;

/**
 * Base class for integration tests that verify a flow executes as expected.  Flow execution tests
 * captured by subclasses should test that a flow responds to all supported transition criteria correctly,
 * transitioning to the correct states and producing the appropriate results on the occurence of 
 * possible "external" (user) events. 
 * <p>
 * More specifically, a typical flow execution test case will test:
 * <ul>
 * <li>that the flow execution starts as expected given a source event with potential input parameters
 * (see startFlow(Event))
 * <li>that given the set of supported transition criteria for a given state, that the state executes 
 * the appropriate transition when an event is signaled (with potential input parameters).  A test case
 * should be coded for each logical event that can occur, where an event drives a possible 
 * path through the flow.  The goal should be to exercise all possible paths of the flow.
 * <li>that given a transition that leads to an interactive state type (a view state or an end state),
 * that the view descriptor returned to the client matches what was expected and the current
 * state of the flow matches what is expected.
 * </ul>
 * A flow execution test can effectively automate and validate the orchestration required to drive a 
 * end-to-end business process thats spans several steps involving the user.  Such tests area good way to
 * test your system top-down starting at the web-tier and pushing through all the way to the DB without
 * having to deploy to a servlet or portlet container.  Because these tests are typically end-to-end
 * integration tests that involve a transactional resource such a database, this class subclasses Spring's 
 * AbstractTransactionalSpringContextTests to take advantage of its automatic transaction management
 * and rollback capabilites.  If you do not need those capabilities, you must call
 * <code>setDependencyCheck(false)</code> in your test's constructor to turn off dependency
 * checking for the transaction manager property.
 * @author Keith Donald
 */
public abstract class AbstractFlowExecutionTests extends AbstractTransactionalSpringContextTests {

	/**
	 * The flow definition whose execution is being tested (configuration object).
	 */
	private Flow flow;

	/**
	 * The flow execution running the flow when the test is active (runtime object).
	 */
	private FlowExecution flowExecution;

	/**
	 * The flow service locator; providing means to lookup and retrieve configured flows.
	 * Used to resolve the Flow to be tested by <code>id</code>.
	 */
	private FlowLocator flowLocator;

	/**
	 * Returns the flow locator used to resolve the Flow to be tested by <code>id</code>.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	protected final void onSetUpInTransaction() throws Exception {
		this.flowLocator = new BeanFactoryFlowServiceLocator(this.applicationContext);
		onSetUpInTransactionalFlowTest();
	}
	
	/**
	 * Hook method subclasses can implement to do additional setup. Called
	 * after the transition has been activated and the flow locator has been set.
	 */
	protected void onSetUpInTransactionalFlowTest() {
	}
	
	/**
	 * Get the singleton flow definition whose execution is being tested.
	 * @return the singleton flow definition
	 * @throws ServiceLookupException if the flow identified by flowId()
	 *         could not be resolved (if <code>this.flow</code> was null)
	 */
	protected Flow getFlow() throws ServiceLookupException {
		if (this.flow == null) {
			setFlow(getFlowLocator().getFlow(flowId()));
		}
		return this.flow;
	}

	/**
	 * Set the flow definition whose execution is being tested.
	 * @param flow the singleton flow definition
	 */
	protected void setFlow(Flow flow) {
		Assert.notNull(flow, "The flow definition whose execution will be tested is required");
		this.flow = flow;
	}

	/**
	 * Subclasses should override to return the <code>flowId</code> whose
	 * execution should be tested.
	 * @return the flow id, whose execution is to be tested.
	 */
	protected abstract String flowId();

	/**
	 * Set the flow definition to be tested to the Flow built by the specified
	 * builder.
	 * @param flowBuilder the flow builder
	 */
	protected void setFlowBuilder(FlowBuilder flowBuilder) {
		setFlow(new FlowFactoryBean(flowBuilder).getFlow());
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * @return the model and view returned as a result of starting the flow
	 *         (returned when the first view state is entered)
	 */
	protected ViewDescriptor startFlow() {
		return startFlow(event("start"));
	}

	/**
	 * Convenience factory method that returns an event instance for this
	 * test client with the specified id.
	 * @param eventId the event id
	 * @return the event
	 */
	protected Event event(String eventId) {
		return new Event(this, eventId);
	}

	/**
	 * Convenience factory method that returns an event instance for this
	 * test client with the specified id and parameters
	 * @param eventId the event id
	 * @param parameters the event parameters
	 * @return the event
	 */
	protected Event event(String eventId, Map parameters) {
		return new Event(this, eventId, parameters);
	}

	/**
	 * Start a new flow execution for the flow definition that is being tested.
	 * @param event the starting event
	 * @return the model and view returned as a result of starting the flow
	 *         (returned when the first view state is entered)
	 */
	protected ViewDescriptor startFlow(Event event) {
		this.flowExecution = new FlowExecutionImpl(getFlow());
		onSetupFlowExecution(flowExecution);
		return this.flowExecution.start(event);
	}

	/**
	 * Hook method where you can do additional setup of a flow execution before
	 * it is started, like register an execution listener.
	 * @param flowExecution the flow execution
	 */
	protected void onSetupFlowExecution(FlowExecution flowExecution) {
	}

	/**
	 * Signal an occurence of an event in the current state of the flow
	 * execution being tested.
	 * <p>
	 * Note: signaling an event will cause state transitions to occur in a chain
	 * UNTIL control is returned to the caller. Control will be returned once a
	 * view state is entered or an end state is entered and the flow terminates.
	 * Action states are executed without returning control, as their result
	 * always triggers another state transition, executed internally. Action
	 * states can also be executed in a chain like fashion (e.g. action state 1
	 * (result), action state 2 (result), action state 3 (result), view state
	 * <control returns so view can be rendered>).
	 * <p>
	 * If you wish to verify expected behavior on each state transition (and not
	 * just when the view state triggers return of control back to the client),
	 * you have a few options:
	 * <p>
	 * First, you can always write a standalone unit test for the
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
	 * interface (It is recommended you extend
	 * {@link org.springframework.webflow.support.FlowExecutionListenerAdapter}
	 * and only override what you need.
	 * 
	 * @param event the event to signal
	 * @return the model and view, returned once control is returned to the
	 *         client (occurs when the flow enters a view state, or an end
	 *         state)
	 */
	protected ViewDescriptor signalEvent(Event event) {
		return this.flowExecution.signalEvent(event);
	}

	/**
	 * Returns the ongoing flow execution for this test.
	 * @return the flow execution
	 * @throws IllegalStateException the execution has not been started
	 */
	protected FlowExecutionContext getFlowContext() throws IllegalStateException {
		if (flowExecution == null) {
			throw new IllegalStateException("The flow execution has not been started; call startFlow first");
		}
		return flowExecution;
	}

	/**
	 * Assert that the active flow session is for the flow with the provided id.
	 * @param expectedActiveFlowId the flow id that should have a session active
	 *        in the tested flow execution
	 */
	protected void assertActiveFlowEquals(String expectedActiveFlowId) {
		assertEquals(
				"The active flow id '" + getFlowContext().getActiveSession().getFlow()
				+ "' does not equal the expected active flow '" + expectedActiveFlowId + "'",
				expectedActiveFlowId, getFlowContext().getActiveSession().getFlow().getId());
	}

	/**
	 * Assert that the current state of the flow execution equals the provided
	 * state id.
	 * @param expectedCurrentStateId the expected current state
	 */
	protected void assertCurrentStateEquals(String expectedCurrentStateId) {
		assertEquals(
				"The current state '" + getFlowContext().getActiveSession().getCurrentState().getId()
				+ "' does not equal the expected state '" + expectedCurrentStateId + "'",
				expectedCurrentStateId, getFlowContext().getActiveSession().getCurrentState().getId());
	}

	/**
	 * Assert that the last supported event that occured in the flow execution
	 * equals the provided event.
	 * @param expectedEventId the expected event
	 */
	protected void assertLastEventEquals(String expectedEventId) {
		assertEquals(
				"The last event '" + getFlowContext().getLastEventId()
				+ "' does not equal the expected event '" + expectedEventId	+ "'",
				expectedEventId, getFlowContext().getLastEventId());
	}

	/**
	 * Assert that the view name equals the provided value.
	 * @param expectedViewName the expected name
	 * @param viewDescriptor the view descriptor to assert
	 */
	public void assertViewNameEquals(String expectedViewName, ViewDescriptor viewDescriptor) {
		assertEquals("The view name is wrong:", expectedViewName, viewDescriptor.getViewName());
	}
	
	/**
	 * Assert that the view descriptor contains the specified model attribute
	 * with the provided expected value.
	 * @param expectedValue the expected value
	 * @param attributeName the attribute name
	 * @param viewDescriptor the view descriptor to assert
	 */
	public void assertModelAttributeEquals(String expectedValue, String attributeName, ViewDescriptor viewDescriptor) {
		assertEquals("The model attribute value is wrong:", expectedValue, viewDescriptor.getAttribute(attributeName));
	}

	/**
	 * Assert that the view descriptor contains the specified collection model
	 * attribute with the provided expected size.
	 * @param expectedSize the expected size
	 * @param attributeName the collection attribute name
	 * @param viewDescriptor the view descriptor to assert
	 */
	public void assertModelAttributeCollectionSize(int expectedSize, String attributeName,
			ViewDescriptor viewDescriptor) {
		assertModelAttributeNotNull(attributeName, viewDescriptor);
		Collection c = (Collection)viewDescriptor.getAttribute(attributeName);
		assertEquals("The model attribute collection size is wrong:", expectedSize, c.size());
	}
	/**
	 * Assert that the view descriptor contains the specified model attribute.
	 * @param attributeName the attribute name
	 * @param viewDescriptor the view descriptor to assert
	 */
	public void assertModelAttributeNotNull(String attributeName, ViewDescriptor viewDescriptor) {
		assertNotNull("The model attribute is [null] but should be NOT null:",
				viewDescriptor.getAttribute(attributeName));
	}	

	/**
	 * Assert that the view descriptor does not contain the specified model attribute.
	 * @param attributeName the attribute name
	 * @param viewDescriptor the view descriptor to assert
	 */
	public void assertModelAttributeNull(String attributeName, ViewDescriptor viewDescriptor) {
		assertNull("The model attribute is NOT null but should be [null]:",
				viewDescriptor.getAttribute(attributeName));
	}	
}