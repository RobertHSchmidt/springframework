package org.springframework.webflow.jsf;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.webflow.ExternalContext;

public class JsfExternalContext implements ExternalContext {

	private FacesContext facesContext;
	
	private String actionId;
	
	private String outcome;

	public JsfExternalContext(FacesContext facesContext) {
		this.facesContext = facesContext;
	}

	public JsfExternalContext(FacesContext facesContext, String actionId, String outcome) {
		this.facesContext = facesContext;
		this.actionId = actionId;
		this.outcome = outcome;
	}

	public Map getRequestParameterMap() {
		return facesContext.getExternalContext().getRequestParameterMap();
	}

	public Map getRequestMap() {
		return facesContext.getExternalContext().getRequestMap();
	}

	public Map getSessionMap() {
		return facesContext.getExternalContext().getSessionMap();
	}

	public Map getApplicationMap() {
		return facesContext.getExternalContext().getApplicationMap();
	}

	public FacesContext getFacesContext() {
		return facesContext;
	}

	public String getActionId() {
		return actionId;
	}

	public String getOutcome() {
		return outcome;
	}	
}