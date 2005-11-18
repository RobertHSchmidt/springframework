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
package org.springframework.webflow.execution;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.ViewSelection;

/**
 * A strongly typed listener list class for FlowExecutionListeners. It helps
 * in managing a list of <code>FlowExecutionListener</code>s.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionListener
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionListenerList {

	/**
	 * The list of listeners that should receive event callbacks during managed
	 * flow executions (client sessions).
	 */
	private Set flowExecutionListeners = new HashSet();

	/**
	 * Add a listener.
	 * @param listener the listener to add
	 * @return true if the underlying listener list changed, false otherwise
	 */
	public boolean add(FlowExecutionListener listener) {
		if (listener == null) {
			return false;
		}
		else {
			return this.flowExecutionListeners.add(listener);
		}
	}

	/**
	 * Add a set of listeners.
	 * @param listeners the listeners to add
	 * @return true if the underlying listener list changed, false otherwise
	 */
	public boolean add(FlowExecutionListener[] listeners) {
		if (listeners == null) {
			return false;
		}
		else {
			boolean changed = false;
			for (int i = 0; i < listeners.length; i++) {
				boolean added = add(listeners[i]);
				if (added && !changed) {
					changed = true;
				}
			}
			return changed;
		}
	}

	/**
	 * Add a list of listeners.
	 * @param flowExecutionListenerList the listeners to add
	 * @return true if the underlying listener list changed, false otherwise
	 */
	public boolean add(FlowExecutionListenerList flowExecutionListenerList) {
		if (flowExecutionListenerList == null) {
			return false;
		}
		else {
			boolean changed = false;
			Iterator it = flowExecutionListenerList.iterator();
			while (it.hasNext()) {
				boolean added = add((FlowExecutionListener)it.next());
				if (added && !changed) {
					changed = true;
				}
			}
			return changed;
		}
	}

	/**
	 * Remove a listener from the list.
	 * @param listener the listener to remove
	 */
	public void remove(FlowExecutionListener listener) {
		this.flowExecutionListeners.remove(listener);
	}

	/**
	 * Remove all listeners from the list.
	 */
	public void clear() {
		this.flowExecutionListeners.clear();
	}

	/**
	 * Is at least one instance of the provided FlowExecutionListener implementation
	 * present in the listener list?
	 * @param listenerImplementationClass the flow execution listener
	 *        implementation, must be an implementation of FlowExecutionListener
	 * @return true if present, false otherwise
	 */
	public boolean isAdded(Class listenerImplementationClass) {
		Assert.isTrue(FlowExecutionListener.class.isAssignableFrom(listenerImplementationClass),
				"Listener class must be a FlowExecutionListener");
		for (Iterator it = this.flowExecutionListeners.iterator(); it.hasNext(); ) {
			if (it.next().getClass().equals(listenerImplementationClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is the provided FlowExecutionListener instance present in the listener list?
	 * @param listener the execution listener
	 * @return true if present, false otherwise
	 */
	public boolean isAdded(FlowExecutionListener listener) {
		return this.flowExecutionListeners.contains(listener);
	}

	/**
	 * Returns an iterator looping over all listeners in this list.
	 */
	public Iterator iterator() {
		return this.flowExecutionListeners.iterator();
	}

	/**
	 * Returns the number of execution listeners in this list.
	 * @return the flow execution listener count
	 */
	public int size() {
		return flowExecutionListeners.size();
	}

	/**
	 * Is this listener list empty?
	 * @return true or false
	 */
	public boolean isEmpty() {
		return flowExecutionListeners.isEmpty();
	}

	/**
	 * Returns the listeners in this list as an array.
	 */
	public FlowExecutionListener[] toArray() {
		return (FlowExecutionListener[])flowExecutionListeners.toArray(new FlowExecutionListener[size()]);
	}
	
	// methods to fire events to all listeners
	

	/**
	 * Notify all interested listeners that a flow execution was created.
	 */
	public void fireCreated(FlowExecutionContext context) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).created(context);
		}
	}
	
	/**
	 * Notify all interested listeners that a request was submitted to the flow
	 * execution.
	 */
	public void fireRequestSubmitted(RequestContext context) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).requestSubmitted(context);
		}
	}

	/**
	 * Notify all interested listeners that the flow execution finished
	 * processing a request.
	 */
	public void fireRequestProcessed(RequestContext context) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).requestProcessed(context);
		}
	}
	
	/**
	 * Notify all interested listeners that a flow execution session is starting.
	 */
	public void fireSessionStarting(RequestContext context, State startState, Map input) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).sessionStarting(context, startState, input);
		}
	}

	/**
	 * Notify all interested listeners that a flow execution session has started.
	 */
	public void fireSessionStarted(RequestContext context) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).sessionStarted(context);
		}
	}

	/**
	 * Notify all interested listeners that an event was signaled in the flow
	 * execution.
	 */
	public void fireEventSignaled(RequestContext context) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).eventSignaled(context);
		}
	}

	/**
	 * Notify all interested listeners that a state is being entered in the
	 * flow execution.
	 */
	public void fireStateEntering(RequestContext context, State nextState) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).stateEntering(context, nextState);
		}
	}

	/**
	 * Notify all interested listeners that a state was entered in the
	 * flow execution.
	 */
	public void fireStateEntered(RequestContext context, State previousState) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).stateEntered(context, previousState, context.getFlowExecutionContext().getCurrentState());
		}
	}

	/**
	 * Notify all interested listeners that a flow session was activated in the
	 * flow execution.
	 */
	public void fireResumed(RequestContext context) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).resumed(context);
		}
	}

	/**
	 * Notify all interested listeners that a flow session was paused in the
	 * flow execution.
	 */
	public void firePaused(RequestContext context, ViewSelection selectedView) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).paused(context, selectedView);
		}
	}

	/**
	 * Notify all interested listeners that the active flow execution session is ending.
	 */
	public void fireSessionEnding(RequestContext context) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).sessionEnding(context);
		}
	}

	/**
	 * Notify all interested listeners that a flow execution session has ended.
	 */
	public void fireSessionEnded(RequestContext context, FlowSession endedSession) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).sessionEnded(context, endedSession);
		}
	}
	
	/**
	 * Notify all interested listeners that a flow execution was loaded from
	 * storage.
	 */
	public void fireLoaded(FlowExecutionContext context, Serializable id) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).loaded(context, id);
		}
	}
	
	/**
	 * Notify all interested listeners that a flow execution was saved to
	 * storage.
	 */
	public void fireSaved(FlowExecutionContext context, Serializable id) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).saved(context, id);
		}
	}
	
	/**
	 * Notify all interested listeners that a flow execution was removed from
	 * storage.
	 */
	public void fireRemoved(FlowExecutionContext context, Serializable id) {
		for (Iterator it = iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).removed(context, id);
		}
	}
}