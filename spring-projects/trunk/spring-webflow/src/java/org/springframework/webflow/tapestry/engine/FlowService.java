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
package org.springframework.webflow.tapestry.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hivemind.util.Defense;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.engine.ILink;
import org.apache.tapestry.services.LinkFactory;
import org.apache.tapestry.services.ResponseRenderer;
import org.apache.tapestry.services.ServiceConstants;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.execution.FlowExecutionManager;
import org.springframework.webflow.tapestry.Constants;
import org.springframework.webflow.tapestry.TapestryEvent;

/**
 * Launches a new web flow execution when a FlowLink is clicked.
 * 
 * @author Keith Donald
 */
public class FlowService implements IEngineService {

	private static final String FLOW_SERVICE = "flow";

	/**
	 * The manager that will actually launch flows for us
	 */
	private FlowExecutionManager flowExecutionManager;

	private ResponseRenderer responseRenderer;

	private LinkFactory linkFactory;
	
	public void setFlowExecutionManager(FlowExecutionManager flowExecutionManager) {
		this.flowExecutionManager = flowExecutionManager;
	}

	public void setLinkFactory(LinkFactory factory) {
		this.linkFactory = factory;
	}

	public void setResponseRenderer(ResponseRenderer renderer) {
		this.responseRenderer = renderer;
	}

    public ILink getLink(IRequestCycle cycle, boolean post, Object parameter) {
		Defense.isAssignable(parameter, String.class, "parameter");
		Map parameters = new HashMap(2);
		parameters.put(ServiceConstants.SERVICE, FLOW_SERVICE);
		parameters.put(FlowExecutionManager.FLOW_ID_PARAMETER, parameter);
		return linkFactory.constructLink(cycle, post, parameters, true);
	}

	public void service(IRequestCycle cycle) throws IOException {
		ViewDescriptor firstView = flowExecutionManager.onEvent(new TapestryEvent(cycle));
		IPage firstPage = cycle.getPage(firstView.getViewName());
		firstPage.setProperty(Constants.FLOW_EXECUTION_CONTEXT_PAGE_PROPERTY, firstView
				.getAttribute(FlowExecutionManager.FLOW_EXECUTION_CONTEXT_ATTRIBUTE));
		firstPage.setProperty(Constants.MODEL_PAGE_PROPERTY, firstView.getModel());
		cycle.activate(firstPage);
		responseRenderer.renderResponse(cycle);
	}

	public String getName() {
		return FLOW_SERVICE;
	}
}