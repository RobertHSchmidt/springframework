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
class ActionFormAdapter extends FlowExecutionListenerAdapter {
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