package org.springframework.webflow.execution;

import org.springframework.util.Assert;

/**
 * Simple holder class that associates a {@link RequestContext} instance with the current thread. The RequestContext
 * will not be inherited by any child threads spawned by the current thread.
 * <p>
 * Used as a central holder for the current RequestContext in Spring Web Flow, wherever necessary. Often used by
 * integration artifacts needing access to the current flow execution.
 * 
 * @see RequestContext
 * 
 * @author Jeremy Grelle
 */
public class RequestContextHolder {

	private static final ThreadLocal requestContextHolder = new ThreadLocal();

	/**
	 * Associate the given RequestContext with the current thread.
	 * @param requestContext the current RequestContext, or <code>null</code> to reset the thread-bound context
	 */
	public static void setRequestContext(RequestContext requestContext) {
		requestContextHolder.set(requestContext);
	}

	/**
	 * Return the RequestContext associated with the current thread, if any.
	 * @return the current RequestContext
	 * @throws IllegalStateException if no RequestContext is bound to this thread
	 */
	public static RequestContext getRequestContext() {
		Assert.state(requestContextHolder.get() != null, "No request context is bound to this thread");
		return (RequestContext) requestContextHolder.get();
	}

	// not instantiable
	private RequestContextHolder() {
	}
}
