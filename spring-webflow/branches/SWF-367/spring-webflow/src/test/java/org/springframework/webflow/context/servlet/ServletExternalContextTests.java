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

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.FlowId;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * Unit tests for {@link ServletExternalContext}.
 */
public class ServletExternalContextTests extends TestCase {

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private ServletExternalContext context;
	private StubFlowExecutor flowExecutor;

	protected void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		flowExecutor = new StubFlowExecutor();
		context = new ServletExternalContext(new MockServletContext(), request, response, flowExecutor);
	}

	public void testProcessLaunchRequest() {
		request.setMethod("GET");
		request.setPathInfo("/booking");
		context.processRequest();
		assertEquals(FlowId.valueOf("booking"), flowExecutor.flowId);
		assertEquals(0, flowExecutor.input.size());
	}

	public void testProcessLaunchRequestWithInput() {
		request.setMethod("GET");
		request.setPathInfo("/conference/46/speakers/24");
		context.processRequest();
		assertEquals(FlowId.valueOf("conference"), flowExecutor.flowId);
		assertEquals(1, flowExecutor.input.size());
	}

	public class StubFlowExecutor implements FlowExecutor {

		private FlowId flowId;
		private AttributeMap input;

		public void launchExecution(FlowId id, AttributeMap input, ExternalContext context) {
			this.flowId = id;
			this.input = input;
		}

		public void resumeExecution(String encodedKey, ExternalContext context) {

		}

	}

}
