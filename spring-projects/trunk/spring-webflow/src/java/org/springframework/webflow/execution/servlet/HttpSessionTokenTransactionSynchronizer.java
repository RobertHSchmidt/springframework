/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.execution.servlet;

import javax.servlet.http.HttpSession;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.execution.AbstractTokenTransactionSynchronizer;

/**
 * A transaction synchronizer that uses a <i>synchronizer token</i> stored in
 * the HTTP session to demarcate application transactions.
 * This implementation stores the token in the HTTP session using a name that
 * is unique for each <i>logical</i> flow execution in that HTTP session. As a
 * result you can use this transaction synchronizer with continuations based
 * flow execution storage strategies, where you have several physical copies
 * of the flow execution for a single logical flow execution.
 * 
 * @see org.springframework.webflow.execution.servlet.HttpSessionContinuationFlowExecutionStorage
 * 
 * @author Erwin Vervaet
 */
public class HttpSessionTokenTransactionSynchronizer extends AbstractTokenTransactionSynchronizer {

	private boolean createSession = true;

	/**
	 * Returns whether or not an HTTP session should be created if non
	 * exists. Defaults to true.
	 */
	public boolean isCreateSession() {
		return createSession;
	}

	/**
	 * Set whether or not an HTTP session should be created if non exists.
	 */
	public void setCreateSession(boolean createSession) {
		this.createSession = createSession;
	}

	public String getToken(RequestContext context) {
		return (String)getHttpSession(context.getSourceEvent()).getAttribute(getTokenName(context));
	}

	public void setToken(RequestContext context, String token) {
		getHttpSession(context.getSourceEvent()).setAttribute(getTokenName(context), token);
	}

	public void clearToken(RequestContext context) {
		getHttpSession(context.getSourceEvent()).removeAttribute(getTokenName(context));
	}
	
	// subclassing hooks
	
	/**
	 * Helper to get the HTTP session accessible from given event.
	 */
	protected HttpSession getHttpSession(Event event) {
		return ServletEvent.getHttpSession(event, isCreateSession());
	}
	
	/**
	 * Generate a pseudo unique token name based on the information available in
	 * given request context. The generated name is unique among all flow executions
	 * in the client's HTTP session. As a result, you can have several concurrent
	 * flow executions in the same HTTP session, all with their own transaction.
	 * @param context the flow execution request context
	 * @return the generated token name
	 */
	protected String getTokenName(RequestContext context) {
		StringBuffer tokenName = new StringBuffer();
		tokenName.append(getTransactionTokenAttributeName()).append("_");
		// use the flow execution key to uniquely identify this flow execution
		// among all other flow executions in the same HTTP session
		// note that the key always remains the same, even if
		// the flow execution gets cloned, e.g. when using continuations, so
		// it identifies the 'logical' flow execution
		tokenName.append(context.getFlowExecutionContext().getKey());
		return tokenName.toString();
	}
	
}
