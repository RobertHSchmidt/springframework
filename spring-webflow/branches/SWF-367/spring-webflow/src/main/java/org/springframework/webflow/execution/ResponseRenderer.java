package org.springframework.webflow.execution;

import org.springframework.webflow.engine.RequestControlContext;

public interface ResponseRenderer {
	public void render(RequestControlContext context);
}
