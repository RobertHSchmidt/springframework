package org.springframework.webflow.config;

import org.springframework.webflow.StateContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.ViewDescriptor;

public class MyCustomStateExceptionHandler implements StateExceptionHandler {

	public boolean handles(StateException e) {
		return false;
	}

	public ViewDescriptor handle(StateException e, StateContext context) {
		return null;
	}

}
