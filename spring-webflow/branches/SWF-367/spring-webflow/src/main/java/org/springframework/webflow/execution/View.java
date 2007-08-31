package org.springframework.webflow.execution;

public abstract class View {

	public abstract void render();

	public abstract boolean eventSignaled();

	public abstract Event getEvent();

}
