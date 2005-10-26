package org.springframework.webflow.config;

import org.springframework.webflow.FlowExceptionHandler;
import org.springframework.webflow.StateContext;
import org.springframework.webflow.ViewDescriptor;

public class MyCustomFlowExceptionHandler implements FlowExceptionHandler {

	public boolean handles(Exception e) {
		return false;
	}

	public ViewDescriptor handle(Exception e, StateContext context) {
		return null;
	}

}
