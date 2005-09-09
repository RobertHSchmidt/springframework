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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.core.CollectionFactory;
import org.springframework.core.style.ToStringCreator;

/**
 * A view state is a state in which a physical view resource should be rendered
 * to the user, for example, for soliciting form input.
 * <p>
 * To accomplish this, a <code>ViewState</code> returns a
 * <code>ViewDescriptor</code>, which contains the logical name of a view
 * template to render and all supporting model data needed to render it
 * correctly. It is expected that some sort of view resolver will map this view
 * name to a physical resource template (like a JSP file).
 * <p>
 * A view state can also be a <i>marker</i> state with no associated view. In
 * this case it just returns control back to the client. Marker states are
 * useful for situations where an action has already generated the response.
 * 
 * @see org.springframework.webflow.ViewDescriptorCreator
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Steven Devijver
 */
public class ViewState extends TransitionableState {

	/**
	 * The factory for the view descriptor to return when this state is entered.
	 */
	private ViewDescriptorCreator viewDescriptorCreator;
	
	/**
	 * Controllers invocations associated with this view state.
	 */
	private Collection controllerInvocations = CollectionFactory.createLinkedSetIfPossible(6);
	
	/**
	 * Default constructor for bean style usage.
	 */
	public ViewState() {
	}

	/**
	 * Create a new marker view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, Transition transition) throws IllegalArgumentException {
		super(flow, id, transition);
	}

	/**
	 * Create a new marker view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
	}

	/**
	 * Create a new marker view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @param controllerInvocation the sole controller invocation of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, Transition transition, ControllerInvocation controllerInvocation) throws IllegalArgumentException {
		this(flow, id, transition);
		// do assert on controller.
		add(controllerInvocation);
	}
	
	/**
	 * Create a new marked view state
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transitions the transitions of this state
	 * @param controllerInvocations the controller invocations of this state
	 * @throws IllegalArgumentException
	 */
	public ViewState(Flow flow, String id, Transition[] transitions, ControllerInvocation[] controllerInvocations) throws IllegalArgumentException {
		this(flow, id, transitions);
		// do assert on controllers
		addAll(controllerInvocations);
	}
	
	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param creator the factory used to produce the view to render
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, ViewDescriptorCreator creator, Transition transition) throws IllegalArgumentException {
		super(flow, id, transition);
		setViewDescriptorCreator(creator);
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param creator the factory used to produce the view to render
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, ViewDescriptorCreator creator, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
		setViewDescriptorCreator(creator);
	}

	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param creator the factory used to produce the view to render
	 * @param transition the sole transition of this state
	 * @param controllerInvocation the sole controller invocation of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, ViewDescriptorCreator creator, Transition transition, ControllerInvocation controllerInvocation) throws IllegalArgumentException {
		this(flow, id, creator, transition);
		add(controllerInvocation);
	}
	
	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param creator the factory used to produce the view to render
	 * @param transitions the transitions of this state
	 * @param controllerInvocations the controller invocations of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, ViewDescriptorCreator creator, Transition[] transitions, ControllerInvocation[] controllerInvocations) throws IllegalArgumentException {
		this(flow, id, creator, transitions);
		addAll(controllerInvocations);
	}
	
	/**
	 * Create a new view state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param creator the factory used to produce the view to render
	 * @param transitions the transitions of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public ViewState(Flow flow, String id, ViewDescriptorCreator creator, Transition[] transitions, Map properties) throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		setViewDescriptorCreator(creator);
	}
	
	/**
	 * Returns the factory to produce a descriptor for the view to render in
	 * this view state.
	 */
	public ViewDescriptorCreator getViewDescriptorCreator() {
		return viewDescriptorCreator;
	}

	/**
	 * Sets the factory to produce a descriptor for the view to render in
	 * this view state.
	 */
	public void setViewDescriptorCreator(ViewDescriptorCreator viewDescriptorCreator) {
		this.viewDescriptorCreator = viewDescriptorCreator;
	}

	/**
	 * Returns true if this view state has no associated view, false otherwise.
	 */
	public boolean isMarker() {
		return viewDescriptorCreator == null;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method
	 * that executes behaviour specific to this state type in polymorphic
	 * fashion.
	 * <p>
	 * Returns a view descriptor pointing callers to a logical view resource to
	 * be displayed. The descriptor also contains a model map needed when the
	 * view is rendered, for populating dynamic content.
	 * @param context the state context for the executing flow
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state execution
	 */
	protected ViewDescriptor doEnter(StateContext context) {
		return viewDescriptor(context);
	}
	
	/**
	 * Returns the view descriptor that should be rendered by this
	 * state for given execution context.
	 * @param context the state context for the executing flow
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state execution
	 */
	public ViewDescriptor viewDescriptor(StateContext context) {
		if (isMarker()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning control to client with a [null] view render request");
			}
			return null;
		}
		else {
			ViewDescriptor viewDescriptor = viewDescriptorCreator.createViewDescriptor(context);
			if (logger.isDebugEnabled()) {
				logger.debug("Returning view render request to client: " + viewDescriptor);
			}
			return viewDescriptor;
		}
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("viewDescriptorCreator", this.viewDescriptorCreator);
		super.createToString(creator);
	}
	
	protected ViewDescriptor invokeController(RequestContext requestContext, Event event, ControllerInvocationListener listener) {
		for (Iterator iter = this.controllerInvocations.iterator(); iter.hasNext();) {
			ControllerInvocation controllerInvocation = (ControllerInvocation)iter.next();
			if (controllerInvocation.matches(event.getId())) {
				listener.markControllerInvoked();
				return controllerInvocation.invoke(requestContext, event);
			}
		}
		return null;
	}
	
	/**
	 * Add controller invocation to this view state
	 * 
	 * @param controller invocation the controller invocation
	 */
	public void add(ControllerInvocation controllerInvocation) {
		controllerInvocation.setSourceState(this);
		this.controllerInvocations.add(controllerInvocation);
	}
	
	/**
	 * Add array of controller invocatiosn to this view state
	 * 
	 * @param controllerInvocations the controller invocations
	 */
	public void addAll(ControllerInvocation[] controllerInvocations) {
		for (int i = 0; i < controllerInvocations.length; i++) {
			add(controllerInvocations[i]);
		}
	}
	
	/**
	 * Returns the collection of controller invocations for this view state.
	 * 
	 * @return the controller invocations for this controller
	 */
	public Collection getControllerInvocations() {
		return this.controllerInvocations;
	}
}