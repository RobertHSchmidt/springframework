package org.springframework.webflow.builder;

import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.internal.FlowExecutionExceptionHandler;
import org.springframework.webflow.execution.internal.RequestControlContext;

public class MyCustomStateExceptionHandler implements FlowExecutionExceptionHandler {

	public boolean handles(FlowExecutionException e) {
		return false;
	}

	public ViewSelection handle(FlowExecutionException e, RequestControlContext context) {
		return null;
	}

}
