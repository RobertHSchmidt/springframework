package org.springframework.webflow.jsf;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.webflow.execution.ExternalEvent;

public class JsfEvent extends ExternalEvent {

	private String actionId;
	
	public JsfEvent(String outcome, FacesContext context, String actionId) {
		super(context);
		setId(outcome);
		this.actionId = actionId;
		initParameters(context, null);
	}
	
	public JsfEvent(String outcome, FacesContext context, String actionId, Map parameters) {
		super(context);
		setId(outcome);
		this.actionId = actionId;
		initParameters(context, parameters);
	}
	
	protected void initParameters(FacesContext context, Map parameters) {
		addParameters(context.getExternalContext().getRequestParameterMap());
		addParameters(parameters);
	}
	
	public FacesContext getContext() {
		return (FacesContext)getSource();
	}
	
	public String getActionId() {
		return actionId;
	}
}