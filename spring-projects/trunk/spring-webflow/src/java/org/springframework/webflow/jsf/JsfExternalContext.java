/*
 * Copyright 2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.jsf;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.webflow.ExternalContext;

/**
 * Provides contextual information about a JSF environment that 
 * has interacted with SWF.
 * 
 * @author Keith Donald
 */
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