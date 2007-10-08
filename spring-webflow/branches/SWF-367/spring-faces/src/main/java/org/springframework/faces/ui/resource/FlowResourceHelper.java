package org.springframework.faces.ui.resource;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.springframework.webflow.context.FlowDefinitionRequestInfo;
import org.springframework.webflow.context.RequestPath;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Helper used by Spring Faces component renderers to add links to javascript and css resources. The resource links will
 * be rendered in the correct format for the requests to be handled by Web Flow and routed to a special faces-resources
 * flow that is engineered at runtime.
 * @author Jeremy Grelle
 * 
 */
public class FlowResourceHelper {

	public void renderScriptLink(FacesContext facesContext, String scriptPath) throws IOException {

		RequestContext requestContext = RequestContextHolder.getRequestContext();

		ResponseWriter writer = facesContext.getResponseWriter();

		writer.startElement("script", null);

		writer.writeAttribute("type", "text/javascript", null);

		FlowDefinitionRequestInfo requestInfo = new FlowDefinitionRequestInfo("faces-resources", new RequestPath(
				scriptPath), null, null);
		String src = requestContext.getExternalContext().buildFlowDefinitionUrl(requestInfo);

		writer.writeAttribute("src", src, null);

		writer.endElement("script");
	}

	public void renderStyleLink(FacesContext facesContext, String cssPath) throws IOException {

		RequestContext requestContext = RequestContextHolder.getRequestContext();

		ResponseWriter writer = facesContext.getResponseWriter();

		writer.startElement("link", null);

		writer.writeAttribute("type", "text/css", null);
		writer.writeAttribute("rel", "stylesheet", null);

		FlowDefinitionRequestInfo requestInfo = new FlowDefinitionRequestInfo("faces-resources", new RequestPath(
				cssPath), null, null);
		String src = requestContext.getExternalContext().buildFlowDefinitionUrl(requestInfo);

		writer.writeAttribute("href", src, null);

		writer.endElement("link");
	}

}
