package org.springframework.webflow.execution;

import org.springframework.webflow.action.MultiAction;

public class InfiniteLoopTestAction extends MultiAction {
	public Event method(RequestContext context) {
		return success();
	}
	
	public Event errorMethod(RequestContext context) {
		return error();
	}
}
