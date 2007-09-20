/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.context.servlet;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.core.collection.LocalParameterMap;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * Unit tests for {@link ServletExternalContext}.
 */
public class ServletExternalContextTests extends TestCase {

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private ServletExternalContext context;
	private FlowExecutor flowExecutor;

	protected void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		flowExecutor = new StubFlowExecutor();
	}

	public void testProcessLaunchFlowRequest() throws Exception {
		request.setPathInfo("/booking");
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		assertEquals("booking", context.getFlowId());
	}

	public void testProcessLaunchFlowRequestTrailingSlash() throws Exception {
		request.setPathInfo("/booking/");
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		assertEquals("booking", context.getFlowId());
	}

	public void testProcessLaunchFlowRequestElements() throws Exception {
		request.setPathInfo("/users/1");
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		assertEquals("users", context.getFlowId());
		assertEquals(context.getRequestElements()[0], "1");
	}

	public void testProcessLaunchFlowMultipleRequestElements() throws Exception {
		request.setPathInfo("/users/1/foo/bar//baz/");
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		assertEquals("users", context.getFlowId());
		assertEquals("1", context.getRequestElements()[0]);
		assertEquals("foo", context.getRequestElements()[1]);
		assertEquals("bar", context.getRequestElements()[2]);
		assertEquals("", context.getRequestElements()[3]);
		assertEquals("baz", context.getRequestElements()[4]);
	}

	public void testProcessResumeFlowExecution() throws Exception {
		request.setPathInfo("/executions/booking/_c12345_k12345");
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		assertEquals("booking", context.getFlowId());
		assertEquals("_c12345_k12345", context.getFlowExecutionKey());
	}

	public void testExternalContextUnbound() throws Exception {
		request.setPathInfo("/executions/booking/_c12345_k12345");
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		try {
			ExternalContextHolder.getExternalContext();
			fail("Should have failed");
		} catch (IllegalStateException e) {

		}
	}

	public void testNoRequestPathInfo() {
		request.setPathInfo(null);
		try {
			context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
			fail("Should have failed");
		} catch (IllegalArgumentException e) {

		}
	}

	public void testSendFlowExecutionRedirect() throws Exception {
		request.setPathInfo("/users/1");
		flowExecutor = new FlowExecutor() {
			public void execute(ExternalContext context) {
				context.sendFlowExecutionRedirect();
				context.setPausedResult("_c12345_k12345");
			}
		};
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		assertEquals("/executions/users/_c12345_k12345", response.getRedirectedUrl());
	}

	public void testFlowExecutionRedirectAttemptOnEnd() throws Exception {
		request.setPathInfo("/users/1");
		flowExecutor = new FlowExecutor() {
			public void execute(ExternalContext context) {
				context.sendFlowExecutionRedirect();
				context.setEndedResult();
			}
		};
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		try {
			context.processRequest();
			fail("Should have failed");
		} catch (IllegalStateException e) {

		}
	}

	public void testSendFlowDefinitionRedirect() throws Exception {
		request.setPathInfo("/users/1");
		flowExecutor = new FlowExecutor() {
			public void execute(ExternalContext context) {
				Map parameters = new HashMap();
				parameters.put("foo", "bar");
				parameters.put("bar", "baz");
				context.sendFlowDefinitionRedirect("customers", new String[] { "1", "you&me" }, new LocalParameterMap(
						parameters));
				context.setEndedResult();
			}
		};
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		assertEquals("/customers/1/you%26me?foo=bar&bar=baz", response.getRedirectedUrl());
	}

	public void testSendExternalRedirect() throws Exception {
		request.setPathInfo("/users/1");
		flowExecutor = new FlowExecutor() {
			public void execute(ExternalContext context) {
				context.sendExternalRedirect("/foo/bar/baz");
				context.setEndedResult();
			}
		};
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
		context.processRequest();
		assertEquals("/foo/bar/baz", response.getRedirectedUrl());
	}

	public class StubFlowExecutor implements FlowExecutor {
		public void execute(ExternalContext context) {
			assertNotNull(ExternalContextHolder.getExternalContext());
		}
	}

}
