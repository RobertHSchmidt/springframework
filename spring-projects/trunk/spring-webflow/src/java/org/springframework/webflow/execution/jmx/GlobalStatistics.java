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
 * Simple implementation of a global statistics MBean.
 * 
 * @author Keith Donald
 */
final class GlobalStatistics implements GlobalStatisticsMBean {

	/**
	 * The current number of actively executing (working) flow executions.
	 */
	int activeFlowExecutionCount;

	/**
	 * The total number of flow executions that have ended.
	 */
	int endedFlowExecutionCount;

	/**
	 * The current number of flow executions created and operational
	 * (includes both paused and active).
	 */
	int managedFlowExecutionCount;

	/**
	 * The current number of paused flow executions waiting for input.
	 */
	int pausedFlowExecutionCount;

	/**
	 * The current number of requests in process to the SWF system. 
	 */
	int requestsInProcessCount;

	/**
	 * The total number of flow executions that have been spawned.
	 */
	int totalFlowExecutionCount;

	/**
	 * The total number of requests that have been received.
	 */
	int totalRequestCount;

	/**
	 * Flag determing if stats collection is enabled.
	 */
	boolean statisticsEnabled;

	public int getActiveFlowExecutionCount() {
		return activeFlowExecutionCount;
	}

	public int getEndedFlowExecutionCount() {
		return endedFlowExecutionCount;
	}

	public int getManagedFlowExecutionCount() {
		return managedFlowExecutionCount;
	}

	public int getPausedFlowExecutionCount() {
		return pausedFlowExecutionCount;
	}

	public int getRequestsInProcessCount() {
		return requestsInProcessCount;
	}

	public int getTotalFlowExecutionCount() {
		return totalFlowExecutionCount;
	}

	public int getTotalRequestCount() {
		return totalRequestCount;
	}

	public boolean isStatisticsEnabled() {
		return statisticsEnabled;
	}

	public void setStatisticsEnabled(boolean statisticsEnabled) {
		this.statisticsEnabled = statisticsEnabled;
	}

	public void reset() {
		activeFlowExecutionCount = 0;
		endedFlowExecutionCount = 0;
		managedFlowExecutionCount = 0;
		pausedFlowExecutionCount = 0;
		requestsInProcessCount = 0;
		totalFlowExecutionCount = 0;
		totalRequestCount = 0;
	}
}