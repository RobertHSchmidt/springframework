package org.springframework.webflow.builder;

import org.springframework.webflow.engine.FlowExecutionExceptionHandler;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.ViewSelection;

public class MyCustomStateExceptionHandler implements FlowExecutionExceptionHandler {

	public boolean handles(FlowExecutionException e) {
		return false;
	}

	public ViewSelection handle(FlowExecutionException e, RequestControlContext context) {
		return null;
	}

}
