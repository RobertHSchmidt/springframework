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
package org.springframework.webflow.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.style.StylerUtils;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerCriteria;
import org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory;
import org.springframework.webflow.execution.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.TextToFlowExecutionListenerCriteria;

/**
 * @author Keith Donald
 */
public class DefaultFlowExecutionListenerLoader implements FlowExecutionListenerLoader {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * A set of flow execution listeners to a list of flow execution listener
	 * criteria objects. The criteria list determines the conditions in which a
	 * single flow execution listener applies.
	 */
	private Set listenerSet = CollectionFactory.createLinkedSetIfPossible(6);

	/**
	 * Returns the array of flow execution listeners for specified flow.
	 * @param flow the flow definition associated with the execution to be
	 * listened to
	 * @return the flow execution listeners
	 */
	public FlowExecutionListener[] getListeners(Flow flow) {
		Assert.notNull(flow, "The Flow to load listeners for cannot be null");
		List listenersToAttach = new LinkedList();
		for (Iterator it = listenerSet.iterator(); it.hasNext();) {
			ConditionalFlowExecutionListenerHolder listenerHolder = (ConditionalFlowExecutionListenerHolder)it.next();
			if (listenerHolder.listenerAppliesTo(flow)) {
				listenersToAttach.add(listenerHolder.getListener());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded [" + listenersToAttach.size() + "] of possible " + listenerSet.size()
					+ " listeners to this execution request for flow '" + flow.getId()
					+ "', the listeners to attach are " + StylerUtils.style(listenersToAttach));
		}
		return (FlowExecutionListener[])listenersToAttach.toArray(new FlowExecutionListener[listenersToAttach.size()]);
	}

	/**
	 * Returns a unmodifiable map of the configured flow execution listeners and
	 * the criteria in which those listeners apply.
	 */
	public Set getListenerSet() {
		return Collections.unmodifiableSet(listenerSet);
	}

	/**
	 * Set the flow execution listener that will be notified of managed flow
	 * executions.
	 */
	public void setListener(FlowExecutionListener listener) {
		setListeners(Collections.singleton(listener));
	}

	/**
	 * Set the flow execution listener that will be notified of managed flow
	 * executions for the flows that match given criteria.
	 */
	public void setListenerCriteria(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		setListenersCriteria(Collections.singleton(listener), criteria);
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed flow
	 * executions.
	 */
	public void setListeners(Collection listeners) {
		setListenersCriteria(listeners, FlowExecutionListenerCriteriaFactory.allFlows());
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed flow
	 * executions for flows that match given criteria.
	 */
	public void setListenersCriteria(Collection listeners, FlowExecutionListenerCriteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("Setting listeners " + listeners + " with criteria " + criteria);
		}
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			FlowExecutionListener listener = (FlowExecutionListener)it.next();
			if (containsListener(listener)) {
				removeListener(listener);
			}
			addListener(listener, criteria);
		}
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed flow
	 * executions. The map keys may be individual flow execution listener
	 * instances or collections of execution listener instances. The map values
	 * can either be string encoded flow execution listener criteria or direct
	 * references to <code>FlowExecutionListenerCriteria</code> objects.
	 */
	public void setListenerMap(Map listenerCriteriaMap) {
		Iterator it = listenerCriteriaMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			FlowExecutionListenerCriteria criteria;
			if (entry.getValue() instanceof FlowExecutionListenerCriteria) {
				criteria = (FlowExecutionListenerCriteria)entry.getValue();
			}
			else {
				criteria = convertEncodedListenerCriteria((String)entry.getValue());
			}
			if (entry.getKey() instanceof Collection) {
				setListenersCriteria((Collection)entry.getKey(), criteria);
			}
			else {
				setListenerCriteria((FlowExecutionListener)entry.getKey(), criteria);
			}
		}
	}

	/**
	 * Helper that converts from text to a FlowExecutionListenerCriteria
	 * @param encodedCriteria the encoded text
	 * @return the criteria
	 */
	protected FlowExecutionListenerCriteria convertEncodedListenerCriteria(String encodedCriteria) {
		return new TextToFlowExecutionListenerCriteria().convert(encodedCriteria);
	}

	/**
	 * Add a listener that will listen to executions for all flows.
	 * @param listener the listener to add
	 */
	public void addListener(FlowExecutionListener listener) {
		addListener(listener, FlowExecutionListenerCriteriaFactory.allFlows());
	}

	/**
	 * Add a listener that will listen to executions to flows matching the
	 * specified criteria.
	 * @param listener the listener
	 * @param criteria the listener criteria
	 */
	public void addListener(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding flow execution listener " + listener + " with criteria " + criteria);
		}
		ConditionalFlowExecutionListenerHolder conditional = getHolder(listener);
		if (conditional == null) {
			conditional = new ConditionalFlowExecutionListenerHolder(listener);
			listenerSet.add(conditional);
		}
		if (criteria == null) {
			criteria = FlowExecutionListenerCriteriaFactory.allFlows();
		}
		conditional.add(criteria);
	}

	protected ConditionalFlowExecutionListenerHolder getHolder(FlowExecutionListener listener) {
		Iterator it = listenerSet.iterator();
		while (it.hasNext()) {
			ConditionalFlowExecutionListenerHolder next = (ConditionalFlowExecutionListenerHolder)it.next();
			if (next.getListener().equals(listener)) {
				return next;
			}
		}
		return null;
	}

	/**
	 * Is the listener contained by this Flow execution manager?
	 * @param listener the listener
	 * @return true if yes, false otherwise
	 */
	public boolean containsListener(FlowExecutionListener listener) {
		return listenerSet.contains(listener);
	}

	/**
	 * Remove the flow execution listener from the listener list.
	 * @param listener the listener
	 */
	public void removeListener(FlowExecutionListener listener) {
		listenerSet.remove(listener);
	}

	/**
	 * Remove the criteria for the specified listener.
	 * @param listener the listener
	 * @param criteria the criteria
	 */
	public void removeListenerCriteria(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		if (containsListener(listener)) {
			ConditionalFlowExecutionListenerHolder listenerHolder = getHolder(listener);
			listenerHolder.remove(criteria);
			if (listenerHolder.isCriteriaSetEmpty()) {
				removeListener(listener);
			}
		}
	}

	/**
	 * A holder that holds a listener plus a set of criteria defining the flows
	 * in which that listener applies.
	 * @author Keith Donald
	 */
	public static final class ConditionalFlowExecutionListenerHolder {
		private FlowExecutionListener listener;

		private Set criteriaSet = CollectionFactory.createLinkedSetIfPossible(3);

		public ConditionalFlowExecutionListenerHolder(FlowExecutionListener listener) {
			this.listener = listener;
		}

		public FlowExecutionListener getListener() {
			return listener;
		}

		public void add(FlowExecutionListenerCriteria criteria) {
			criteriaSet.add(criteria);
		}

		public void remove(FlowExecutionListenerCriteria criteria) {
			criteriaSet.remove(criteria);
		}

		public boolean isCriteriaSetEmpty() {
			return criteriaSet.isEmpty();
		}

		public boolean equals(Object o) {
			if (!(o instanceof ConditionalFlowExecutionListenerHolder)) {
				return false;
			}
			return listener.equals(((ConditionalFlowExecutionListenerHolder)o).listener);
		}

		public int hashCode() {
			return listener.hashCode();
		}

		public boolean listenerAppliesTo(Flow flow) {
			Iterator it = criteriaSet.iterator();
			while (it.hasNext()) {
				FlowExecutionListenerCriteria criteria = (FlowExecutionListenerCriteria)it.next();
				if (criteria.appliesTo(flow)) {
					return true;
				}
			}
			return false;
		}
	}
}