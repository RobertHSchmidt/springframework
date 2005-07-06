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

import org.springframework.webflow.FlowExecutionStatistics;

/**
 * Managed bean interface for a flow execution. This interface provides
 * a management view on the information exposed by the <code>FlowExecutionContext</code>
 * interface.
 * 
 * @see org.springframework.webflow.FlowExecutionContext
 * 
 * @author Erwin Vervaet
 */
public interface FlowExecutionMBean extends FlowExecutionStatistics {
	
	/**
	 * Returns the root (top-level) flow.
	 * @return the root flow id
	 */
	public String getRootFlow();

	/**
	 * Returns the current active flow.
	 * @return the active flow id
	 */
	public String getActiveFlow() throws IllegalStateException;

	/**
	 * Returns the current state of the active flow.
	 * @return the current state id
	 */
	public String getCurrentState() throws IllegalStateException;

	/**
	 * Returns the current status of this flow execution.
	 * @return the status code
	 */
	public int getStatus();
}