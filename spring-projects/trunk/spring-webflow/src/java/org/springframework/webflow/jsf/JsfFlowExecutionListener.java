/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.webflow.RequestContext;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;

/**
 * FlowExecutionListener which is meant to add the HTTP request attribute map to
 * the flow request scope just before the flow event is actually submitted.
 * 
 * @author Colin Sampaleanu
 */
public class JsfFlowExecutionListener extends FlowExecutionListenerAdapter {

	private FacesContext facesContext;

	public JsfFlowExecutionListener(FacesContext facesContext) {
		this.facesContext = facesContext;
	}

	public void requestSubmitted(RequestContext context) {
		Map requestMap = facesContext.getExternalContext().getRequestMap();
		context.getRequestScope().setAttributes(requestMap);
	}
}