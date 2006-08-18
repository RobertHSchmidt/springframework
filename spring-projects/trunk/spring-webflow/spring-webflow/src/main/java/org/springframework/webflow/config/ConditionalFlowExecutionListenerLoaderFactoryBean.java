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
package org.springframework.webflow.config;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.ConditionalFlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.FlowExecutionListenerCriteria;
import org.springframework.webflow.execution.factory.FlowExecutionListenerCriteriaFactory;

/**
 * A factory bean that creates a {@link ConditionalFlowExecutionListenerLoader}.
 * To create the listener loader, you must pass in a map that has keys of
 * listener instances and values of the criteria for each one of the listeners.
 * 
 * @author Ben Hale
 */
class ConditionalFlowExecutionListenerLoaderFactoryBean implements FactoryBean {

	private ConditionalFlowExecutionListenerLoader flowExecutionListenerLoader;

	private Map listenersAndCriteria;

	private FlowExecutionListenerCriteriaFactory executionListenerCriteriaFactory = new FlowExecutionListenerCriteriaFactory();

	public ConditionalFlowExecutionListenerLoaderFactoryBean(Map listenersAndCriteria) {
		Assert.notNull(listenersAndCriteria, "A map containing listeners and criteria is a required argument");
		this.listenersAndCriteria = listenersAndCriteria;
	}

	public synchronized Object getObject() throws Exception {
		if (flowExecutionListenerLoader == null) {
			flowExecutionListenerLoader = new ConditionalFlowExecutionListenerLoader();
			for (Iterator i = listenersAndCriteria.entrySet().iterator(); i.hasNext();) {
				addListener(flowExecutionListenerLoader, (Entry)i.next());
			}
		}
		return flowExecutionListenerLoader;
	}

	public Class getObjectType() {
		return ConditionalFlowExecutionListenerLoader.class;
	}

	public boolean isSingleton() {
		return true;
	}

	private void addListener(ConditionalFlowExecutionListenerLoader flowExecutionListenerLoader, Entry entry) {
		FlowExecutionListener listener = (FlowExecutionListener)entry.getKey();
		FlowExecutionListenerCriteria criteria = getCriteria((String)entry.getValue());
		flowExecutionListenerLoader.addListener(listener, criteria);
	}

	private FlowExecutionListenerCriteria getCriteria(String value) {
		if ("*".equals(value)) {
			return executionListenerCriteriaFactory.allFlows();
		}
		else {
			return executionListenerCriteriaFactory.flows(StringUtils.commaDelimitedListToStringArray(value));
		}
	}
}