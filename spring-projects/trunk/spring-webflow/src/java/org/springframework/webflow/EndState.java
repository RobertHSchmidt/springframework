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

import java.util.Collections;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Terminates an active flow session when entered. If the terminated session is
 * the root flow session, the entire flow execution ends. If the terminated
 * session was acting as a subflow, the governing flow execution continues and
 * control is returned to the parent flow session. In that case, this state is
 * treated as an ending result event the resuming parent flow is expected to
 * respond to.
 * <p>
 * An end state may optionally be configured with the name of a view to render
 * when entered. This view will be rendered if the end state terminates the
 * entire flow execution as a kind of flow ending "confirmation page".
 * <p>
 * Note: if no <code>viewName</code> property is specified <b>and</b> this
 * end state terminates the entire flow execution, it is expected that some
 * action has already written the response (or else a blank response will
 * result). On the other hand, if no <code>viewName</code> is specified <b>and</b>
 * this end state relinquishes control back to a parent flow, view rendering
 * responsibility falls on the parent flow.
 * 
 * @see org.springframework.webflow.ViewSelector
 * @see org.springframework.webflow.SubflowState
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 */
public class EndState extends State {

	/**
	 * An optional view selector that will select a view to render if this end
	 * state terminates an executing root flow.
	 */
	private ViewSelector viewSelector;

	/**
	 * Default constructor for bean style usage.
	 * @see State#State()
	 * @see #setViewSelector(ViewSelector)
	 */
	public EndState() {
	}

	/**
	 * Create a new end state with no associated view.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 * @see State#State(Flow, String)
	 * @see #setViewSelector(ViewSelector)
	 */
	public EndState(Flow flow, String id) throws IllegalArgumentException {
		super(flow, id);
	}

	/**
	 * Returns the strategy used to select the view to render in this end state
	 * if it terminates a root flow.
	 */
	public ViewSelector getViewSelector() {
		return viewSelector;
	}

	/**
	 * Sets the strategy used to select the view to render when this end state
	 * is entered and terminates a root flow.
	 */
	public void setViewSelector(ViewSelector viewSelector) {
		this.viewSelector = viewSelector;
	}

	/**
	 * Returns true if this end state has no associated view (a "marker" end
	 * state), false otherwise.
	 */
	public boolean isMarker() {
		return viewSelector == null;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method that
	 * executes behaviour specific to this state type in polymorphic fashion.
	 * <p>
	 * This implementation pops the top (active) flow session off the execution
	 * stack, ending it, and resumes control in the parent flow (if neccessary).
	 * If the ended session is the root flow, a {@link ViewSelection} is
	 * returned (when viewName is not null, else null is returned).
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection signaling that control should be returned to the
	 * client and a view rendered
	 * @throws StateException if an exception occurs in this state
	 */
	protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
		if (context.getFlowExecutionContext().getActiveSession().isRoot()) {
			// entire flow execution is ending, return ending view if applicable
			ViewSelection selectedView;
			if (isMarker()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning a [null] ending view selection--"
							+ "assuming a response has already been written or the parent flow will care for view selection");
				}
				selectedView = null;
			}
			else {
				selectedView = viewSelector.makeSelection(context);
				if (logger.isDebugEnabled()) {
					logger.debug("Returning ending view selection " + selectedView);
				}
			}
			context.endActiveFlowSession();
			return selectedView;
		}
		else {
			// there is a parent flow that will resume
			FlowSession parentSession = context.getFlowExecutionContext().getActiveSession().getParent();
			Assert.isInstanceOf(FlowAttributeMapper.class, parentSession.getCurrentState(),
					"State in resuming flow is not an attribute mapper: ");
			FlowAttributeMapper resumingState = (FlowAttributeMapper)parentSession.getCurrentState();
			resumingState.mapSubflowOutput(context);
			context.endActiveFlowSession();
			return context.signalEvent(subflowResult(context));
		}
	}

	/**
	 * Hook method to create the subflow result event. Subclasses can override
	 * this if necessary.
	 */
	protected Event subflowResult(RequestContext context) {
		// treat this end state id as a transitional event in the resuming state
		return new Event(this, getId(), resultParameters(context));
	}

	/**
	 * Returns the subflow result event parameter map. Default implementation
	 * returns an empty map. Subclasses may override.
	 */
	private Map resultParameters(RequestContext context) {
		return Collections.EMPTY_MAP;
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("viewSelector", viewSelector);
	}
}