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
package org.springframework.webflow.execution;

import java.util.Collection;
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

/**
 * Default implementation of a listener loader that stores listeners in a
 * list-backed data structure and allows for configuration of which listeners
 * should apply to which flow definitions.
 * 
 * @author Keith Donald
 */
public class ConditionalFlowExecutionListenerLoader implements FlowExecutionListenerLoader {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The list of flow execution listeners containing
	 * @{link {@link ConditionalFlowExecutionListenerHolder} objects. The list
	 * determines the conditions in which a single flow execution listener
	 * applies.
	 */
	private List listeners = new LinkedList();

	/**
	 * Set the flow execution listener that will be notified of <i>all</i>
	 * managed flow executions.
	 */
	public void setListener(FlowExecutionListener listener) {
		setListeners(new FlowExecutionListener[] { listener });
	}

	/**
	 * Sets the flow execution listeners that will be notified of <i>all</i>
	 * managed flow executions.
	 */
	public void setListeners(FlowExecutionListener[] listeners) {
		setListenersCriteria(listeners, FlowExecutionListenerCriteriaFactory.allFlows());
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
				Collection collection = (Collection)entry.getKey();
				FlowExecutionListener[] listeners = (FlowExecutionListener[])collection
						.toArray(new FlowExecutionListener[collection.size()]);
				setListenersCriteria(listeners, criteria);
			}
			else {
				setListenerCriteria((FlowExecutionListener)entry.getKey(), criteria);
			}
		}
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
			listeners.add(conditional);
		}
		if (criteria == null) {
			criteria = FlowExecutionListenerCriteriaFactory.allFlows();
		}
		conditional.add(criteria);
	}

	/**
	 * Returns the array of flow execution listeners for specified flow.
	 * @param flow the flow definition associated with the execution to be
	 * listened to
	 * @return the flow execution listeners that apply
	 */
	public FlowExecutionListener[] getListeners(Flow flow) {
		Assert.notNull(flow, "The Flow to load listeners for cannot be null");
		List listenersToAttach = new LinkedList();
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			ConditionalFlowExecutionListenerHolder listenerHolder = (ConditionalFlowExecutionListenerHolder)it.next();
			if (listenerHolder.listenerAppliesTo(flow)) {
				listenersToAttach.add(listenerHolder.getListener());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded [" + listenersToAttach.size() + "] of possible " + listeners.size()
					+ " listeners to this execution request for flow '" + flow.getId()
					+ "', the listeners to attach are " + StylerUtils.style(listenersToAttach));
		}
		return (FlowExecutionListener[])listenersToAttach.toArray(new FlowExecutionListener[listenersToAttach.size()]);
	}

	/**
	 * Lookup the listener criteria holder for the listener provided.
	 * @param listener the listener
	 * @return the holder
	 */
	protected ConditionalFlowExecutionListenerHolder getHolder(FlowExecutionListener listener) {
		Iterator it = listeners.iterator();
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
		return listeners.contains(listener);
	}

	/**
	 * Remove the flow execution listener from the listener list.
	 * @param listener the listener
	 */
	public void removeListener(FlowExecutionListener listener) {
		listeners.remove(listener);
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
	 * Helper that converts from text to a FlowExecutionListenerCriteria
	 * @param encodedCriteria the encoded text
	 * @return the criteria
	 */
	protected FlowExecutionListenerCriteria convertEncodedListenerCriteria(String encodedCriteria) {
		return new TextToFlowExecutionListenerCriteria().convert(encodedCriteria);
	}

	private void setListenerCriteria(FlowExecutionListener listener, FlowExecutionListenerCriteria criteria) {
		setListenersCriteria(new FlowExecutionListener[] { listener }, criteria);
	}

	private void setListenersCriteria(FlowExecutionListener[] listeners, FlowExecutionListenerCriteria criteria) {
		for (int i = 0; i < listeners.length; i++) {
			FlowExecutionListener listener = (FlowExecutionListener)listeners[i];
			if (containsListener(listener)) {
				removeListener(listener);
			}
			addListener(listener, criteria);
		}
	}

	/**
	 * A holder that holds a listener plus a set of criteria defining the flows
	 * in which that listener applies.
	 * @author Keith Donald
	 */
	private static final class ConditionalFlowExecutionListenerHolder {

		/**
		 * The held listener
		 */
		private FlowExecutionListener listener;

		/**
		 * The listener criteria set.
		 */
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

		/**
		 * Determines if the listener held by this holder applies to the
		 * specified flow definition.
		 * @param flow the flow
		 * @return true if yes, false otherwise.
		 */
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