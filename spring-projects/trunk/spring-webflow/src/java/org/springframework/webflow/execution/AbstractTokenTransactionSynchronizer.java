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
package org.springframework.webflow.execution;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.util.RandomGuid;

/**
 * Abstract base class to ease implementation of <i>synchronizer token</i>
 * based transaction synchronizers. The <i>synchronizer token</i> is a classic J2EE
 * pattern (see Core J2EE Patterns by Alur, Crupi, and Malks) that can be used
 * to monitor and control the request flow and client access to certain resources.
 * It provides an elegant way to handle situations where clients might make duplicate
 * resource requests.
 * 
 * @see org.springframework.webflow.execution.FlowScopeTokenTransactionSynchronizer
 * 
 * @author Erwin Vervaet
 */
public abstract class AbstractTokenTransactionSynchronizer implements TransactionSynchronizer {

	/**
	 * The transaction synchronizer token will be stored in the model using an
	 * attribute with this name ("txToken").
	 */
	public static final String TRANSACTION_TOKEN_ATTRIBUTE_NAME = "txToken";

	/**
	 * A client can send the transaction synchronizer token to a controller
	 * using a request parameter with this name ("_txToken").
	 */
	public static final String TRANSACTION_TOKEN_PARAMETER_NAME = "_txToken";
	
	
	private String transactionTokenAttributeName = TRANSACTION_TOKEN_ATTRIBUTE_NAME;
	
	private String transactionTokenParameterName = TRANSACTION_TOKEN_PARAMETER_NAME;
	
	private boolean secure = false;
	
	/**
	 * Get the name for the transaction token attribute. Defaults to "txToken".
	 */
	public String getTransactionTokenAttributeName() {
		return transactionTokenAttributeName;
	}
	
	/**
	 * Set the name for the transaction token attribute.
	 */
	public void setTransactionTokenAttributeName(String transactionTokenAttributeName) {
		this.transactionTokenAttributeName = transactionTokenAttributeName;
	}

	/**
	 * Get the name for the transaction token parameter in request events.
	 * Defaults to "_txToken".
	 */
	public String getTransactionTokenParameterName() {
		return transactionTokenParameterName;
	}
	
	/**
	 * Set the name for the transaction token parameter in request events.
	 */
	public void setTransactionTokenParameterName(String transactionTokenParameterName) {
		this.transactionTokenParameterName = transactionTokenParameterName;
	}

	/**
	 * Returns whether or not the transaction synchronizer tokens are
	 * cryptographically strong.
	 */
	public boolean isSecure() {
		return secure;
	}
	
	/**
	 * Set whether or not the transaction synchronizer tokens should be
	 * cryptographically strong.
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}
	
	public boolean inTransaction(RequestContext context, boolean end) {
		// we use the source event because we want to check that the
		// client request that came into the system has a transaction token!
		String tokenValue = (String)context.getSourceEvent().getParameter(getTransactionTokenParameterName());
		if (!StringUtils.hasText(tokenValue)) {
			return false;
		}
		String txToken = getToken(context);
		if (!StringUtils.hasText(txToken)) {
			return false;
		}
		if (end) {
			clearToken(context);
		}
		return txToken.equals(tokenValue);
	}
	
	public void assertInTransaction(RequestContext context, boolean end) throws IllegalStateException {
		Assert.state(inTransaction(context, end), 
				"The request is not executing in the context of an application transaction");
	}
	
	public void beginTransaction(RequestContext context) {
		setToken(context, generateToken());
	}
	
	public void endTransaction(RequestContext context) {
		clearToken(context);
	}
	
	// subclassing hooks

	/**
	 * Generate a new transaction token.
	 */
	protected String generateToken() {
		return new RandomGuid(isSecure()).toString();
	}

	/**
	 * Obtain the token value from whatever token storage is being used (e.g.
	 * from the flow scope).
	 * @param context the flow execution request context
	 * @return the retreived token, or null if no token could be found
	 */
	public abstract String getToken(RequestContext context);
	
	/**
	 * Set given token in whatever token storage is being used (e.g the flow scope).
	 * @param context the flow execution request context
	 * @param token the token value to set
	 */
	public abstract void setToken(RequestContext context, String token);
	
	/**
	 * Clear the token from whatever token storage is being used (e.g the flow
	 * scope). This will end transactional processing.
	 * @param context the flow execution request context
	 */
	public abstract void clearToken(RequestContext context);
	
}
