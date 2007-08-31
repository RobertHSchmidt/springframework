package org.springframework.webflow.execution;


public abstract class View {

	public abstract void render(RequestContext context);

	public abstract boolean eventSignaled();

	public abstract Event getEvent();

}
