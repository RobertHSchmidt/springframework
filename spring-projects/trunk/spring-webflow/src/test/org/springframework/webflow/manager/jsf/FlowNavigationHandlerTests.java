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
package org.springframework.webflow.manager.jsf;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;

import junit.framework.TestCase;

/**
 * Test case for the FlowNavigationHandler class.
 * 
 * @author Ulrik Sandberg
 */
public class FlowNavigationHandlerTests extends TestCase {

	private FlowNavigationHandler tested;
	
	private FacesContext facesContext;

	private MyNavigationHandler navigationHandler;

	protected void setUp() throws Exception {
		super.setUp();
		navigationHandler = new MyNavigationHandler();
		facesContext = new MyFacesContext();
	}

	public void testHandleNavigationNoFlowLaunchNoFlowExecutionParticipation() {
		// perform test
		tested.handleNavigation(facesContext, "FromAction", "OutCome");
		assertTrue("delegate not called", navigationHandler.handled);
	}

	public void testHandleNavigationFlowLaunch() {
		// perform test
		tested.handleNavigation(facesContext, "FromAction", "OutCome");
		assertFalse("delegate called", navigationHandler.handled);
	}

	public void testHandleNavigationFlowExecutionParticipation() {
		// perform test
		tested.handleNavigation(facesContext, "FromAction", "OutCome");
		assertFalse("delegate called", navigationHandler.handled);
	}

	private static class MyNavigationHandler extends NavigationHandler {
		boolean handled;

		public void handleNavigation(FacesContext facesContext, String fromAction, String outcome) {
			handled = true;
		}
	}

	private static class MyFacesContext extends FacesContext {
		public Application getApplication() {
			return null;
		}

		public Iterator getClientIdsWithMessages() {
			return null;
		}

		public ExternalContext getExternalContext() {
			return new FlowNavigationHandlerTests.MyExternalContext();
		}

		public Severity getMaximumSeverity() {
			return null;
		}

		public Iterator getMessages() {
			return null;
		}

		public Iterator getMessages(String arg0) {
			return null;
		}

		public RenderKit getRenderKit() {
			return null;
		}

		public boolean getRenderResponse() {
			return false;
		}

		public boolean getResponseComplete() {
			return false;
		}

		public ResponseStream getResponseStream() {
			return null;
		}

		public void setResponseStream(ResponseStream arg0) {

		}

		public ResponseWriter getResponseWriter() {
			return null;
		}

		public void setResponseWriter(ResponseWriter arg0) {
		}

		public UIViewRoot getViewRoot() {
			UIViewRoot viewRoot = new UIViewRoot();
			viewRoot.setViewId("ViewId");
			return viewRoot;
		}

		public void setViewRoot(UIViewRoot arg0) {
		}

		public void addMessage(String arg0, FacesMessage arg1) {
		}

		public void release() {
		}

		public void renderResponse() {
		}

		public void responseComplete() {
		}
	}

	private static class MyExternalContext extends ExternalContext {
		public void dispatch(String arg0) throws IOException {
		}

		public String encodeActionURL(String arg0) {
			return null;
		}

		public String encodeNamespace(String arg0) {
			return null;
		}

		public String encodeResourceURL(String arg0) {
			return null;
		}

		public Map getApplicationMap() {
			return new HashMap();
		}

		public String getAuthType() {
			return null;
		}

		public Object getContext() {
			return null;
		}

		public String getInitParameter(String arg0) {
			return null;
		}

		public Map getInitParameterMap() {
			return null;
		}

		public String getRemoteUser() {
			return null;
		}

		public Object getRequest() {
			return null;
		}

		public String getRequestContextPath() {
			return null;
		}

		public Map getRequestCookieMap() {
			return null;
		}

		public Map getRequestHeaderMap() {
			return null;
		}

		public Map getRequestHeaderValuesMap() {
			return null;
		}

		public Locale getRequestLocale() {
			return null;
		}

		public Iterator getRequestLocales() {
			return null;
		}

		public Map getRequestMap() {
			return null;
		}

		public Map getRequestParameterMap() {
			return new HashMap();
		}

		public Iterator getRequestParameterNames() {
			return null;
		}

		public Map getRequestParameterValuesMap() {
			return null;
		}

		public String getRequestPathInfo() {
			return null;
		}

		public String getRequestServletPath() {
			return null;
		}

		public URL getResource(String arg0) throws MalformedURLException {
			return null;
		}

		public InputStream getResourceAsStream(String arg0) {
			return null;
		}

		public Set getResourcePaths(String arg0) {
			return null;
		}

		public Object getResponse() {
			return null;
		}

		public Object getSession(boolean arg0) {
			return null;
		}

		public Map getSessionMap() {
			return null;
		}

		public Principal getUserPrincipal() {
			return null;
		}

		public boolean isUserInRole(String arg0) {
			return false;
		}

		public void log(String arg0) {
		}

		public void log(String arg0, Throwable arg1) {
		}

		public void redirect(String arg0) throws IOException {
		}
	}
}