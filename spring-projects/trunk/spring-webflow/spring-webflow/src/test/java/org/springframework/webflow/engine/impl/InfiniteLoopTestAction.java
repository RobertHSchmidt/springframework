package org.springframework.webflow.engine.impl;

import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class InfiniteLoopTestAction extends MultiAction {
	public Event method(RequestContext context) {
		return success();
	}
	
	public Event errorMethod(RequestContext context) {
		return error();
	}
}
