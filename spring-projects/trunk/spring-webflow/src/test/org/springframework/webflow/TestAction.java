package org.springframework.webflow;

import org.springframework.webflow.action.AbstractAction;

public class TestAction extends AbstractAction {

	private boolean executed;
	
	private int executionCount;
	
	
	public boolean isExecuted() {
		return executed;
	}


	public int getExecutionCount() {
		return executionCount;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		executed = true;
		executionCount++;
		return success();
	}
}