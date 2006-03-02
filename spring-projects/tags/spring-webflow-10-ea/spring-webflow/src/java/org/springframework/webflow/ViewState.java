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

import org.springframework.core.style.ToStringCreator;

/**
 * A view state is a state in which a physical view resource should be rendered
 * to the user, for example, for soliciting form input.
 * <p>
 * To accomplish this, a <code>ViewState</code> returns a
 * {@link ViewSelection}, which contains the logical name of a view template to
 * render and all supporting model data needed to render it correctly. It is
 * expected that some sort of view resolver will map this view selection to a
 * renderable resource template (like a JSP file).
 * <p>
 * A view state can also be a <i>marker</i> state with no associated view. In
 * this case it just returns control back to the client. Marker states are
 * useful for situations where an action or custom state type has already
 * generated the response.
 * 
 * @see org.springframework.webflow.ViewSelector
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ViewState extends TransitionableState {

	/**
	 * The factory for the view selection to return when this state is entered.
	 */
	private ViewSelector viewSelector = NullViewSelector.INSTANCE;

	/**
	 * Default constructor for bean style usage.
	 * @see TransitionableState#TransitionableState()
	 * @see #setViewSelector(ViewSelector)
	 */
	public ViewState() {
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 */
	public ViewState(Flow flow, String id) throws IllegalArgumentException {
		super(flow, id);
	}

	/**
	 * Returns the strategy used to select the view to render in this view
	 * state.
	 */
	public ViewSelector getViewSelector() {
		return viewSelector;
	}

	/**
	 * Sets the strategy used to select the view to render in this view state.
	 */
	public void setViewSelector(ViewSelector viewSelector) {
		this.viewSelector = viewSelector;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method that
	 * executes behaviour specific to this state type in polymorphic fashion.
	 * <p>
	 * Returns a view selection pointing callers to a logical view resource to
	 * be displayed. The view selection also contains a model map needed when
	 * the view is rendered, for populating dynamic content.
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection containing model and view information needed to
	 * render the results of the state execution
	 * @throws StateException if an exception occurs in this state
	 */
	protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
		return selectView(context);
	}

	/**
	 * Returns the view selection that should be rendered by this state for
	 * given execution context.
	 * @param context the flow control context for the executing flow
	 * @return a view selection containing model and view information needed to
	 * render the results of the state execution
	 */
	public ViewSelection selectView(FlowExecutionControlContext context) {
		ViewSelection selection = viewSelector.makeSelection(context);
		if (logger.isDebugEnabled()) {
			logger.debug("Made view selection " + selection);
		}
		return selection;
	}

	protected void appendToString(ToStringCreator creator) {
		creator.append("viewSelector", viewSelector);
		super.appendToString(creator);
	}
}