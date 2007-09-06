package org.springframework.webflow.execution;

public interface ViewFactory {
	View getView(RequestContext context);
}