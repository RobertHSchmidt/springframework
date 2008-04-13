/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.webflow.mvc.portlet;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.ModelAndView;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * Trivial flow handler base class that simply returns null for all operations. Subclasses should extend and override
 * which operations they need.
 * 
 * @author Keith Donald
 */
public class AbstractFlowHandler implements FlowHandler {

	public MutableAttributeMap createExecutionInputMap(PortletRequest request) {
		return null;
	}

	public String getFlowId() {
		return null;
	}

	public ModelAndView handleException(FlowException e, RenderRequest request, RenderResponse response) {
		return null;
	}

	public String handleFlowOutcome(String outcome, AttributeMap output, RenderRequest request, RenderResponse response) {
		return null;
	}

}
