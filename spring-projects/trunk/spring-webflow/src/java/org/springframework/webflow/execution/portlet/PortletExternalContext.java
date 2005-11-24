package org.springframework.webflow.execution.portlet;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.webflow.ExternalContext;

public class PortletExternalContext implements ExternalContext {

	private PortletRequest request;

	private PortletResponse response;

	public PortletExternalContext(PortletRequest request, PortletResponse response) {
		this.request = request;
		this.response = response;
	}

	public PortletRequest getRequest() {
		return request;
	}

	public PortletResponse getResponse() {
		return response;
	}

	public Map getRequestParameterMap() {
		return new PortletRequestParameterMap(request);
	}

	public Map getRequestMap() {
		return new PortletRequestMap(request);
	}

	public Map getSessionMap() {
		return new PortletSessionMap(request);
	}

	public Map getApplicationMap() {
		return new PortletContextMap(request.getPortletSession().getPortletContext());
	}

}