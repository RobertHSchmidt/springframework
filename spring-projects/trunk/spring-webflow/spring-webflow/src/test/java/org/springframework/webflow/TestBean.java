/**
 * 
 */
package org.springframework.webflow;

import java.io.Serializable;

public class TestBean implements Serializable {
	String datum1 = "";

	int datum2;

	boolean executed;

	public void execute() {
		this.executed = true;
	}

	public String getDatum1() {
		return datum1;
	}

	public int getDatum2() {
		return datum2;
	}

	public boolean isExecuted() {
		return executed;
	}

	public void execute(String parameter) {
		this.executed = true;
		this.datum1 = parameter;
	}

	public int execute(String parameter, int parameter2) {
		this.executed = true;
		this.datum1 = parameter;
		this.datum2 = parameter2;
		return datum2;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof TestBean)) {
			return false;
		}
		TestBean other = (TestBean)obj;
		return datum1.equals(other.datum1) && datum2 == other.datum2 && executed == other.executed;
	}

	public int hashCode() {
		return (datum1.hashCode() + datum2 + (executed ? 1:0)) * 29;
	}	
}