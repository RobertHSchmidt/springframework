package org.springframework.webflow.builder;

import org.springframework.webflow.RequestControlContext;
import org.springframework.webflow.FlowExecutionException;
import org.springframework.webflow.FlowExecutionExceptionHandler;
import org.springframework.webflow.ViewSelection;

public class MyCustomStateExceptionHandler implements FlowExecutionExceptionHandler {

	public boolean handles(FlowExecutionException e) {
		return false;
	}

	public ViewSelection handle(FlowExecutionException e, RequestControlContext context) {
		return null;
	}

}
