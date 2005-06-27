/**
 * 
 */
package org.springframework.webflow.execution.jmx;

final class GlobalStatistics implements
		GlobalStatisticsMBean {
	int activeFlowExecutionCount;

	int endedFlowExecutionCount;

	int managedFlowExecutionCount;

	int pausedFlowExecutionCount;

	int requestsInProcessCount;

	int totalFlowExecutionCount;

	int totalRequestCount;

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