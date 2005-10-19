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
package org.springframework.webflow.execution.jmx;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;

/**
 * Adapts a strongly typed flow execution to a more generically typed
 * flow execution MBean interface for purposes of standards-based JMX
 * management.
 * 
 * @author Keith Donald
 */
public class FlowExecutionMBeanAdapter implements FlowExecutionMBean {

	/**
	 * The wrapped flow execution being exported for management.
	 */
	private FlowExecution flowExecution;
	
	/**
	 * Create a new wrapper for given flow execution.
	 * @param flowExecution the flow execution to wrap
	 */
	public FlowExecutionMBeanAdapter(FlowExecution flowExecution) {
		this.flowExecution = flowExecution;
	}
	
	public String getRootFlow() {
		return flowExecution.getRootFlow().getId();
	}

	public String getActiveFlow() throws IllegalStateException {
		return flowExecution.getActiveFlow().getId();
	}

	public String getCurrentState() throws IllegalStateException {
		return flowExecution.getCurrentState().getId();
	}

	public int getActiveSessionStatus() {
		return flowExecution.getActiveSession().getStatus().getShortCode();
	}

	public Serializable getKey() {
		return flowExecution.getKey();
	}

	public String getCaption() {
		return flowExecution.getCaption();
	}
	
	public long getCreationTimestamp() {
		return flowExecution.getCreationTimestamp();
	}

	public long getUptime() {
		return flowExecution.getUptime();
	}

	public long getLastRequestTimestamp() {
		return flowExecution.getLastRequestTimestamp();
	}

	public String getLastEventId() {
		return flowExecution.getLastEventId();
	}

	public boolean isActive() {
		return flowExecution.isActive();
	}

	public boolean isRootFlowActive() {
		return flowExecution.isRootFlowActive();
	}
}