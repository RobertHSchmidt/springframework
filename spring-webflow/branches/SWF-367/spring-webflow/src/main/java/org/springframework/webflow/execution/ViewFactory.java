package org.springframework.webflow.execution;

public interface ViewFactory {

	View createView(RequestContext context);

	View restoreView(RequestContext context);

}