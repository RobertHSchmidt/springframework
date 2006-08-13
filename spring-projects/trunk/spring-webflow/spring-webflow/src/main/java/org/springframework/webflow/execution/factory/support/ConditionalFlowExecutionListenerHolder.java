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
package org.springframework.webflow.execution.factory.support;

import java.util.Iterator;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.execution.FlowExecutionListener;

/**
 * A holder that holds a listener plus a set of criteria defining the flows in
 * which that listener applies.
 * @author Keith Donald
 */
class ConditionalFlowExecutionListenerHolder {

	/**
	 * The held listener
	 */
	private FlowExecutionListener listener;

	/**
	 * The listener criteria set.
	 */
	private Set criteriaSet = CollectionFactory.createLinkedSetIfPossible(3);

	public ConditionalFlowExecutionListenerHolder(FlowExecutionListener listener) {
		Assert.notNull(listener, "The listener is required");
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
	 * Determines if the listener held by this holder applies to the specified
	 * flow definition.
	 * @param flowDefinition the flow
	 * @return true if yes, false otherwise.
	 */
	public boolean listenerAppliesTo(FlowDefinition flowDefinition) {
		Iterator it = criteriaSet.iterator();
		while (it.hasNext()) {
			FlowExecutionListenerCriteria criteria = (FlowExecutionListenerCriteria)it.next();
			if (criteria.appliesTo(flowDefinition)) {
				return true;
			}
		}
		return false;
	}
}