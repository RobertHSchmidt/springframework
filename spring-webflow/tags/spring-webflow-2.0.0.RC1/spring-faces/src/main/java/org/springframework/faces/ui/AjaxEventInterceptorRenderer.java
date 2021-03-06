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
package org.springframework.faces.ui;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

import org.springframework.faces.webflow.JsfUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class AjaxEventInterceptorRenderer extends DojoRenderer {

	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		String event = (String) component.getAttributes().get("event");
		Assert.hasText(event, "The event attribute is required on " + component);
		Assert.isTrue(component.getChildCount() == 1, "Exactly one child component is required for " + component);

		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("script", component);
		writer.writeAttribute("type", "text/javascript", null);

		String processIds = (String) component.getAttributes().get("processIds");
		String renderIds = (String) component.getAttributes().get("renderIds");
		if (StringUtils.hasText(processIds) && !processIds.contains(component.getClientId(context))) {
			processIds = component.getClientId(context) + ", " + processIds;
		} else if (!StringUtils.hasText(processIds)) {
			processIds = component.getClientId(context);
		}
		if (!StringUtils.hasText(renderIds)) {
			renderIds = processIds;
		}
		StringBuffer script = new StringBuffer();
		script.append("Spring.advisors.push(new Spring.RemoteEventAdvisor({");
		script.append("event:'" + event + "'");
		script.append(", targetId: '" + ((UIComponent) component.getChildren().get(0)).getClientId(context) + "'");
		script.append(", sourceId : '" + component.getClientId(context) + "'");
		script.append(", formId : '" + RendererUtils.getFormId(context, component) + "'");
		script.append(", processIds : '" + processIds + "'");
		script.append(", renderIds : '" + renderIds + "'})");
		if (JsfUtils.isAsynchronousFlowRequest()) {
			script.append(".apply()");
		}
		script.append(");");

		writer.writeText(script.toString(), null);
		writer.endElement("script");
	}

	public void decode(FacesContext context, UIComponent component) {
		if (context.getExternalContext().getRequestParameterMap().containsKey("ajaxSource")
				&& context.getExternalContext().getRequestParameterMap().get("ajaxSource").equals(
						component.getClientId(context))) {
			component.queueEvent(new ActionEvent(component));
		}
	}
}
