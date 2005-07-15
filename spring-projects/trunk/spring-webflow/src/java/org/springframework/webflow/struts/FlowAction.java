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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.struts.ActionSupport;
import org.springframework.web.struts.SpringBindingActionForm;
import org.springframework.web.util.WebUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.action.FormObjectAccessor;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.execution.servlet.ServletFlowExecutionManager;
import org.springframework.webflow.support.FlowExecutionListenerAdapter;

/**
 * Struts Action that acts a front controller entry point into the web flow
 * system. Typically, a single FlowAction manages all flow executions by
 * parameterization with the appropriate <code>flowId</code> in views that
 * start new flow executions. Alternatively, a FlowAction may exist per
 * top-level (root) flow definition in the application.
 * <p>
 * Requests are managed by and delegated to a
 * {@link FlowExecutionManager}, allowing reuse of common front flow
 * controller logic in other environments. Consult the JavaDoc of that class for
 * more information on how requests are processed.
 * <p>
 * On each request received by this action, a StrutsEvent object is created as input 
 * to the webflow system.  This external source event provides access to the 
 * action form, action mapping, and other struts-specific constructs.  As a 
 * convenience, the "actionForm" attribute is also exposed in requestScope after
 * a request is submitted.  This means you may access your action form from your
 * webflow artifacts as follows:
 * <code>context.getRequestScope().getAttribute("actionForm")</code>.
 * <p>
 * This class also is aware of the <code>SpringBindingActionForm</code> adapter,
 * which adapts Spring's data binding infrastructure (based on POJO binding, a
 * standard Errors interface, and property editor type conversion) to the Struts
 * action form model. This option gives backend web-tier developers full support for
 * POJO-based binding with minimal hassel, while still providing consistency to
 * view developers who already have a lot of experience with Struts for markup
 * and request dispatching.
 * <p>
 * Below is an example <code>struts-config.xml</code> configuration for a
 * FlowAction that fronts a single top-level flow:
 * 
 * <pre>
 * &lt;action path=&quot;/userRegistration&quot;
 *    	type=&quot;org.springframework.web.flow.struts.FlowAction&quot;
 *     	name=&quot;springBindingActionForm&quot; scope=&quot;request&quot; 
 *     	className=&quot;org.springframework.web.flow.struts.FlowActionMapping&quot;&gt;
 *     	&lt;set-property property=&quot;flowId&quot; value=&quot;user.Registration&quot; /&gt;
 * &lt;/action&gt;
 * </pre>
 * 
 * This example associates the logical request URL
 * <code>/userRegistration.do</code> with the <code>Flow</code> indentified
 * by the id <code>user.Registration</code>. Alternatively, the
 * <code>flowId</code> could have been left blank and provided in dynamic
 * fashion by the views (allowing a single <code>FlowAction</code> to manage
 * any number of flow executions). A Spring binding action form instance is set in
 * request scope, acting as an adapter enabling POJO-based binding and
 * validation with Spring.
 * <p>
 * Other notes regarding Struts web-flow integration:
 * <ul>
 * <li>Logical view names returned when <code>ViewStates</code> and
 * <code>EndStates</code> are entered are mapped to physical view templates
 * using standard Struts action forwards (typically global forwards).
 * <li>Use of the <code>SpringBindingActionForm</code> requires no special setup
 * in <code>struts-config.xml</code>: simply declare a form bean in request scope 
 * of the class <code>org.springframework.web.struts.SpringBindingActionForm</code> and
 * use it with your FlowAction(s).
 * <p>
 * The benefits here are substantial: developers now have a powerful web flow
 * capability integrated with Struts, with a consistent-approach to POJO-based
 * binding and validation that addresses the proliferation of
 * <code>ActionForm</code> classes found in traditional Struts-based apps.
 * 
 * @see org.springframework.webflow.struts.FlowActionMapping
 * @see org.springframework.webflow.execution.FlowExecutionManager
 * @see org.springframework.web.struts.SpringBindingActionForm
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAction extends ActionSupport {

	public static final String FLOW_EXECUTION_MANAGER_BEAN_NAME = "flowExecutionManager";

	public static final String ACTION_FORM_ATTRIBUTE = "actionForm";
	
	private FlowExecutionManager flowExecutionManager;
	
	protected void onInit() {
		try {
			this.flowExecutionManager = (FlowExecutionManager)getWebApplicationContext().getBean(FLOW_EXECUTION_MANAGER_BEAN_NAME, FlowExecutionManager.class);
		} catch (NoSuchBeanDefinitionException e) {
			this.flowExecutionManager = new ServletFlowExecutionManager();
			this.flowExecutionManager.setBeanFactory(getWebApplicationContext());
		}
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		FlowExecutionListener actionFormAdapter = createActionFormAdapter(request, form);
		Event event = createEvent(mapping, form, request, response);
		ViewDescriptor viewDescriptor =	flowExecutionManager.onEvent(event, actionFormAdapter);
		return toActionForward(viewDescriptor, mapping, request);
	}
	
	/**
	 * Creates a Struts event based on given information. Subclasses can override this
	 * to return a specialized event object.
	 * @param mapping the action mapping
	 * @param form the action form
	 * @param request the current request
	 * @param response the current response
	 */
	protected StrutsEvent createEvent(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		return new StrutsEvent(mapping, form, request, response);
	}

	/**
	 * Creates a flow execution listener that takes a Spring Errors instance
	 * supporting POJO-based data binding in request scope under a well-defined
	 * name and adapts it to the Struts Action form model.
	 * @param request the request
	 * @param form the action form
	 * @return the adapter
	 */
	protected FlowExecutionListener createActionFormAdapter(final HttpServletRequest request, final ActionForm form) {
		return new FlowExecutionListenerAdapter() {
			public void requestSubmitted(RequestContext context) {
				context.getRequestScope().setAttribute(ACTION_FORM_ATTRIBUTE, form);
			}
			
			public void requestProcessed(RequestContext context) {
				if (context.getFlowExecutionContext().isActive()) {
					if (form instanceof SpringBindingActionForm) {
						SpringBindingActionForm bindingForm = (SpringBindingActionForm)form;
						bindingForm.expose(new FormObjectAccessor(context).getFormErrors(), request);
					}
				}
			}
		};
	}

	/**
	 * Return a Struts ActionForward given a ViewDescriptor. We need to add all
	 * attributes from the ViewDescriptor as request attributes.
	 */
	private ActionForward toActionForward(ViewDescriptor viewDescriptor, ActionMapping mapping,
			HttpServletRequest request) {
		if (viewDescriptor != null) {
			WebUtils.exposeRequestAttributes(request, viewDescriptor.getModel());
			ActionForward forward = mapping.findForward(viewDescriptor.getViewName());
			if (forward != null) {
				forward = new ActionForward(forward);
			}
			else {
				forward = new ActionForward(viewDescriptor.getViewName());
			}
			forward.setRedirect(viewDescriptor.isRedirect());
			forward.freeze();
			return forward;
		}
		else {
			return null;
		}
	}
}