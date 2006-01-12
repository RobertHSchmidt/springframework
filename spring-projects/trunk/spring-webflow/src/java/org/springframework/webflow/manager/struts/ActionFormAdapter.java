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

import org.springframework.web.struts.SpringBindingActionForm;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.FormObjectAccessor;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;

/**
 * Creates a flow execution listener that takes a Spring Errors instance
 * supporting POJO-based data binding in request scope under a well-defined
 * name and adapts it to the Struts ActionForm model.
 */
public class ActionFormAdapter extends FlowExecutionListenerAdapter {
	public void requestProcessed(RequestContext context) {
		if (context.getFlowExecutionContext().isActive()) {
			StrutsExternalContext strutsContext = (StrutsExternalContext)context.getExternalContext();
			if (strutsContext.getActionForm() instanceof SpringBindingActionForm) {
				SpringBindingActionForm bindingForm = (SpringBindingActionForm)strutsContext.getActionForm();
				bindingForm.expose(new FormObjectAccessor(context).getFormErrors(), strutsContext.getRequest());
			}
		}
	}
}