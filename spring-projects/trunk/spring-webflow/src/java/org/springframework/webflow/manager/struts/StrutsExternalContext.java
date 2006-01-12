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