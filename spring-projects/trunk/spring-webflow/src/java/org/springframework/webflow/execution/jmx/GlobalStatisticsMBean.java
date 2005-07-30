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

/**
 * A JMX management interface for global statistics on the web flow system.
 * 
 * @author Keith Donald
 */
public interface GlobalStatisticsMBean {
	
	/**
	 * Returns the total number of flow executions that have been created since this
	 * system starting serving requests.
	 * @return the total
	 */
	public int getTotalFlowExecutionCount();

	/**
	 * Returns the total number of requests into the webflow system.
	 * @return the total
	 */
	public int getTotalRequestCount();

	/**
	 * Returns the number of requests currently being processed by the webflow system.
	 * @return the requests in process count
	 */
	public int getRequestsInProcessCount();
	
	/**
	 * Gets the current number of managed flow executions -- managed executions may be in an 
	 * active or paused state, but they exist (have not been ended).
	 * @return the managed count
	 */
	public int getManagedFlowExecutionCount();

	/**
	 * Returns the current number of paused flow executions. Paused flows are waiting on the user
	 * to do something.
	 * @return the paused count
	 */
	public int getPausedFlowExecutionCount();

	/**
	 * Returns the current number of active flow executions. Active flows are doing work -- the user is
	 * waiting on the flow to come back with a response.
	 * @return the active count
	 */
	public int getActiveFlowExecutionCount();
	
	/**
	 * Returns the number of flow executions that were created that have ended normally.
	 * @return the ended count
	 */
	public int getEndedFlowExecutionCount();
	
	/**
	 * Are global statistics enabled?
	 * @return true if yes, false otherwise
	 */
	public boolean isStatisticsEnabled();
	
	/**
	 * Turn statistics collection on/off.
	 * @param statisticsEnabled true to turn on, false to turn off
	 */
	public void setStatisticsEnabled(boolean statisticsEnabled);
	
	/**
	 * Reset these stats.
	 */
	public void reset();
}
