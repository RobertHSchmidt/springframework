/**
 * 
 */
package org.springframework.webflow.action;

public class TestBean {
	String datum1;

	Integer datum2;

	boolean executed;

	public void execute() {
		this.executed = true;
	}

	public void execute(String parameter) {
		this.executed = true;
		this.datum1 = parameter;
	}

	public void execute(String parameter, Integer parameter2) {
		this.executed = true;
		this.datum1 = parameter;
		this.datum2 = parameter2;
	}
}