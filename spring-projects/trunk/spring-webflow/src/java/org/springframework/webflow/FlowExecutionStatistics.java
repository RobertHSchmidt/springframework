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

import java.io.Serializable;

/**
 * A generically typed flow execution statistics interface for use by management clients.
 * These stats would typically be exported for management via JMX. References to strongly-typed
 * web flow classes (e.g Flow, State) should not go here -- put them in the FlowExecutionContext
 * subinterface.
 *
 * @see org.springframework.webflow.FlowExecutionContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionStatistics {
	
	/**
	 * Returns a key that uniquely identifies this flow execution as
	 * a logical entity. It is possible that the same <i>logical</i>
	 * flow execution is physically represented as several different
	 * objects. However, each of those objects will have the same key.
	 * As such, the key identifies a group of related flow execution
	 * representations for the same logical flow execution.
	 * <p>
	 * This key will remain unchanged during the entire lifecycle of
	 * the flow execution and can safely be used to identify the
	 * flow execution among all other flow executions.
	 * @return the flow execution identification key
	 */
	public Serializable getKey();
	
	/**
	 * Returns a display string suitable for logging/printing in a console
	 * containing info about this executing flow.
	 * @return the flow execution caption
	 */
	public String getCaption();

	/**
	 * Returns the time at which this flow started executing, represented
	 * as the number of milliseconds since January 1, 1970, 00:00:00 GMT.
	 * @return the creation timestamp
	 */
	public long getCreationTimestamp();

	/**
	 * Returns the time in milliseconds this flow execution has been active.
	 * @return the flow execution up time
	 */
	public long getUptime();

	/**
	 * Returns the timestamp noting when the last request to manipulate this
	 * executing flow was received, represented as the number of milliseconds
	 * since January 1, 1970, 00:00:00 GMT.
	 * @return the timestamp of the last client request
	 */
	public long getLastRequestTimestamp();

	/**
	 * Returns the id of the last event that occured in this executing flow.
	 * @return the last event id
	 */
	public String getLastEventId();

	/**
	 * Is the flow execution active?
	 * @return true if active, false if flow execution has terminated
	 */
	public boolean isActive();
	
	/**
	 * Is the root flow of the flow execution currently active?
	 * @return true if so, false otherwise
	 */
	public boolean isRootFlowActive();
}