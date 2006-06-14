package org.springframework.webflow.builder;

import org.springframework.webflow.RequestControlContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.ViewSelection;

public class MyCustomStateExceptionHandler implements StateExceptionHandler {

	public boolean handles(StateException e) {
		return false;
	}

	public ViewSelection handle(StateException e, RequestControlContext context) {
		return null;
	}

}
