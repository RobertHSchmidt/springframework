/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.struts;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.web.struts.ActionSupport;
import org.springframework.web.struts.SpringBindingActionForm;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.action.FormObjectAccessor;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.servlet.ServletExternalContext;

/**
 * Point of integration between Struts and Spring Web Flow: a Struts Action that
 * acts a front controller entry point into the web flow system. A single
 * FlowAction may launch any new FlowExecution. In addition, a single Flow
 * Action may signal events in any existing/restored FlowExecutions.
 * <p>
 * Requests are managed by and delegated to a {@link FlowExecutionManager},
 * allowing reuse of common front flow controller logic in other environments.
 * Consult the JavaDoc of that class for more information on how requests are
 * processed.
 * <p>
 * <li>To have this controller launch a new flow execution (conversation), have
 * the client send a {@link FlowExecutionManager#getFlowIdParameterName()}
 * request parameter indicating the flow definition to launch.
 * <li>To have this controller participate in an existing flow execution
 * (conversation), have the client send a
 * {@link FlowExecutionManager#getFlowExecutionIdParameterName()} request
 * parameter identifying the conversation to participate in.
 * <p>
 * On each request received by this action, a {@link StrutsExternalContext}
 * object is created as input to the web flow system. This external source event
 * provides access to the action form, action mapping, and other struts-specific
 * constructs.
 * <p>
 * This class also is aware of the <code>SpringBindingActionForm</code>
 * adapter, which adapts Spring's data binding infrastructure (based on POJO
 * binding, a standard Errors interface, and property editor type conversion) to
 * the Struts action form model. This option gives backend web-tier developers
 * full support for POJO-based binding with minimal hassel, while still
 * providing consistency to view developers who already have a lot of experience
 * with Struts for markup and request dispatching.
 * <p>
 * Below is an example <code>struts-config.xml</code> configuration for a
 * FlowAction:
 * 
 * <pre>
 *     &lt;action path=&quot;/userRegistration&quot;
 *         type=&quot;org.springframework.webflow.struts.FlowAction&quot;
 *         name=&quot;springBindingActionForm&quot; scope=&quot;request&quot;&gt;
 *     &lt;/action&gt;
 * </pre>
 * 
 * This example associates the logical request URL
 * <code>/userRegistration.do</code> as a Flow controller. It is expected that
 * flows to launch be provided in a dynamic fashion by the views (allowing this
 * single <code>FlowAction</code> to manage any number of flow executions). A
 * Spring binding action form instance is set in request scope, acting as an
 * adapter enabling POJO-based binding and validation with Spring.
 * <p>
 * Other notes regarding Struts web-flow integration:
 * <ul>
 * <li>Logical view names returned when <code>ViewStates</code> and
 * <code>EndStates</code> are entered are mapped to physical view templates
 * using standard Struts action forwards (typically global forwards).
 * <li>Use of the <code>SpringBindingActionForm</code> requires no special
 * setup in <code>struts-config.xml</code>: simply declare a form bean in
 * request scope of the class
 * <code>org.springframework.web.struts.SpringBindingActionForm</code> and use
 * it with your FlowAction.
 * </ul>
 * <p>
 * The benefits here are substantial: developers now have a powerful web flow
 * capability integrated with Struts, with a consistent-approach to POJO-based
 * binding and validation that addresses the proliferation of
 * <code>ActionForm</code> classes found in traditional Struts-based apps.
 * 
 * @see org.springframework.webflow.execution.FlowExecutionManager
 * @see org.springframework.webflow.struts.StrutsEvent
 * @see org.springframework.web.struts.SpringBindingActionForm
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAction extends ActionSupport {

	/**
	 * The flow execution manager will be retreived from the application context
	 * using this bean name if no manager is explicitly set.
	 */
	public static final String FLOW_EXECUTION_MANAGER_BEAN_NAME = "flowExecutionManager";

	/**
	 * The manager responsible for launching and signaling struts-originating
	 * events in flow executions.
	 */
	private FlowExecutionManager flowExecutionManager;

	/**
	 * Returns the flow execution manager used by this controller.
	 * @return the flow execution manager
	 */
	protected FlowExecutionManager getFlowExecutionManager() {
		return flowExecutionManager;
	}

	/**
	 * Configures the flow execution manager implementation to use.
	 */
	public void setFlowExecutionManager(FlowExecutionManager flowExecutionManager) {
		this.flowExecutionManager = flowExecutionManager;
	}

	protected void onInit() {
		if (getFlowExecutionManager() == null) {
			setFlowExecutionManager((FlowExecutionManager)getWebApplicationContext().getBean(
					FLOW_EXECUTION_MANAGER_BEAN_NAME, FlowExecutionManager.class));
		}
		getFlowExecutionManager().addListener(new ActionFormAdapter());
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ExternalContext context = new StrutsExternalContext(mapping, form, request, response);
		ViewSelection viewDescriptor = getFlowExecutionManager().onEvent(context);
		return toActionForward(viewDescriptor, mapping, request);
	}

	/**
	 * Return a Struts ActionForward given a ViewSelection. Adds all attributes
	 * from the ViewSelection as request attributes.
	 */
	protected ActionForward toActionForward(ViewSelection selectedView, ActionMapping mapping,
			HttpServletRequest request) {
		if (selectedView != null) {
			WebUtils.exposeRequestAttributes(request, selectedView.getModel());
			ActionForward forward = mapping.findForward(selectedView.getViewName());
			if (forward != null) {
				// the 1.2.1 copy constructor would ideally be better to use,
				// but it is not Struts 1.1 compatible
				forward = new ActionForward(forward.getName(), forward.getPath(), selectedView.isRedirect());
			}
			else {
				if (selectedView.isRedirect()) {
					forward = new ActionForward(buildRedirectUrlPath(selectedView), true);
				}
				else {
					forward = new ActionForward(selectedView.getViewName(), false);
				}
			}
			forward.freeze();
			return forward;
		}
		else {
			return null;
		}
	}

	/**
	 * Takes the view name of the selected view and appends the model properties
	 * as query parameters.
	 * @param selectedView the selected view
	 * @return the relative url path to redirect to
	 */
	protected String buildRedirectUrlPath(ViewSelection selectedView) {
		StringBuffer path = new StringBuffer(selectedView.getViewName());
		if (selectedView.getModel().size() > 0) {
			// append model attributes as redirect query parameters
			path.append('?');
			Iterator it = selectedView.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				path.append(entry.getKey()).append('=').append(entry.getValue());
				if (it.hasNext()) {
					path.append('&');
				}
			}
		}
		return path.toString();
	}

	/**
	 * Creates a flow execution listener that takes a Spring Errors instance
	 * supporting POJO-based data binding in request scope under a well-defined
	 * name and adapts it to the Struts ActionForm model.
	 */
	protected static class ActionFormAdapter extends FlowExecutionListenerAdapter {
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

	public static class StrutsExternalContext extends ServletExternalContext {

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
}