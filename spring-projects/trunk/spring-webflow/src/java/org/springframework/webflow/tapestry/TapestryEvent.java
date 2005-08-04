package org.springframework.webflow.tapestry;

import java.util.Map;

import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.springframework.webflow.execution.ExternalEvent;

public class TapestryEvent extends ExternalEvent {

	public TapestryEvent(IRequestCycle cycle) {
		super(cycle);
	}
	
	public IRequestCycle getRequestCycle() {
		return (IRequestCycle)getSource();
	}
	
	public IPage getPage() {
		return getRequestCycle().getPage();
	}

	public Object getParameter(String parameterName) {
		return getRequestCycle().getParameter(parameterName);
	}

	public Map getParameters() {
		throw new UnsupportedOperationException();
	}	
}