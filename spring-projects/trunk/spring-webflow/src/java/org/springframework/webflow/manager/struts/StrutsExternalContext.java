/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.manager.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.springframework.webflow.context.servlet.ServletExternalContext;

/**
 * Provides consistent access to a Struts environment from within Spring Web
 * Flow.
 * 
 * @author Keith Donald
 */
public class StrutsExternalContext extends ServletExternalContext {

	private ActionMapping actionMapping;

	private ActionForm actionForm;

	public StrutsExternalContext(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		super(request, response);
		this.actionMapping = mapping;
		this.actionForm = form;
	}

	public ActionForm getActionForm() {
		return actionForm;
	}

	public ActionMapping getActionMapping() {
		return actionMapping;
	}
}