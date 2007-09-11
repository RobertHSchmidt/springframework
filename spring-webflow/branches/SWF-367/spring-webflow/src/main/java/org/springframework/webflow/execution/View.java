package org.springframework.webflow.execution;

public interface View {

	public void render();

	public boolean eventSignaled();

	public Event getEvent();

}
