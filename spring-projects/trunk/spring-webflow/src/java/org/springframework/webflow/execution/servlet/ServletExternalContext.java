package org.springframework.webflow.execution.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.webflow.ExternalContext;

public class ServletExternalContext implements ExternalContext {

	private HttpServletRequest request;
	
	private HttpServletResponse response;
	
	public ServletExternalContext(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}
	
	public Map getRequestParameterMap() {
		return new HttpRequestParameterMap(request);
	}

	public Map getRequestMap() {
		return new HttpRequestMap(request);
	}

	public Map getSessionMap() {
		return new HttpSessionMap(request);
	}

	public Map getApplicationMap() {
		return new HttpServletContextMap(request.getSession().getServletContext());
	}

	public HttpServletRequest getRequest() {
		return request;
	}
	
	public HttpServletResponse getResponse() {
		return response;
	}
}