package org.springframework.faces.webflow;

import java.util.Iterator;

import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;

import org.springframework.webflow.execution.RequestContextHolder;

public class FlowFacesContext extends FacesContext {

	/**
	 * The key for storing the responseComplete flag
	 */
	static final String RESPONSE_COMPLETE_KEY = "responseComplete";

	/**
	 * The key for storing the renderResponse flag
	 */
	static final String RENDER_RESPONSE_KEY = "renderResponse";

	/**
	 * The base FacesContext delegate
	 */
	private FacesContext delegate;

	public FlowFacesContext(FacesContext delegate) {
		this.delegate = delegate;
		FacesContext.setCurrentInstance(this);
	}

	/**
	 * TODO - This delegating method will be re-written to use SWF's internal messaging constructs
	 */
	public void addMessage(String clientId, FacesMessage message) {
		delegate.addMessage(clientId, message);
	}

	/**
	 * TODO - This delegating method will be re-written to use SWF's internal messaging constructs
	 */
	public Iterator<String> getClientIdsWithMessages() {
		return delegate.getClientIdsWithMessages();
	}

	/**
	 * TODO - This delegating method will be re-written to use SWF's internal messaging constructs
	 */
	public Severity getMaximumSeverity() {
		return delegate.getMaximumSeverity();
	}

	/**
	 * TODO - This delegating method will be re-written to use SWF's internal messaging constructs
	 */
	public Iterator<FacesMessage> getMessages() {
		return delegate.getMessages();
	}

	/**
	 * TODO - This delegating method will be re-written to use SWF's internal messaging constructs
	 */
	public Iterator<FacesMessage> getMessages(String clientId) {
		return delegate.getMessages();
	}

	public boolean getRenderResponse() {
		Boolean renderResponse = RequestContextHolder.getRequestContext().getFlashScope().getBoolean(
				RENDER_RESPONSE_KEY);
		if (renderResponse == null) {
			return false;
		}
		return renderResponse;
	}

	public boolean getResponseComplete() {
		Boolean responseComplete = RequestContextHolder.getRequestContext().getFlashScope().getBoolean(
				RESPONSE_COMPLETE_KEY);
		if (responseComplete == null) {
			return false;
		}
		return responseComplete;
	}

	public void renderResponse() {
		RequestContextHolder.getRequestContext().getFlashScope().put(RENDER_RESPONSE_KEY, Boolean.TRUE);
	}

	public void responseComplete() {
		RequestContextHolder.getRequestContext().getFlashScope().put(RESPONSE_COMPLETE_KEY, Boolean.TRUE);
	}

	// ------------------ Pass-through delegate methods ----------------------//

	public Application getApplication() {
		return delegate.getApplication();
	}

	public ELContext getELContext() {
		return delegate.getELContext();
	}

	public ExternalContext getExternalContext() {
		return delegate.getExternalContext();
	}

	public RenderKit getRenderKit() {
		return delegate.getRenderKit();
	}

	public ResponseStream getResponseStream() {
		return delegate.getResponseStream();
	}

	public ResponseWriter getResponseWriter() {
		return delegate.getResponseWriter();
	}

	public UIViewRoot getViewRoot() {
		return delegate.getViewRoot();
	}

	public void release() {
		delegate.release();
	}

	public void setResponseStream(ResponseStream responseStream) {
		delegate.setResponseStream(responseStream);
	}

	public void setResponseWriter(ResponseWriter responseWriter) {
		delegate.setResponseWriter(responseWriter);
	}

	public void setViewRoot(UIViewRoot root) {
		delegate.setViewRoot(root);
	}

	protected FacesContext getDelegate() {
		return delegate;
	}

}
