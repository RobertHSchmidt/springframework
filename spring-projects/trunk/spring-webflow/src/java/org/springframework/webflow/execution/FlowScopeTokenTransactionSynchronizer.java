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

import org.springframework.webflow.RequestContext;

/**
 * The default transaction synchronizer implementation. The token will be stored
 * in flow scope for the duration of an application transaction.
 * This implies that there needs to be a unique flow execution for each running
 * application transaction! As a result this transaction synchronizer cannot be
 * used with continuations based flow execution storage strategies.
 * 
 * @author Erwin Vervaet
 */
public class FlowScopeTokenTransactionSynchronizer extends AbstractTokenTransactionSynchronizer {
	
	public String getTransactionId(RequestContext context) {
		return (String)getToken(context);
	}
	
	public String getToken(RequestContext context) {
		return (String)context.getFlowScope().getAttribute(getTransactionTokenAttributeName());
	}
	
	public void setToken(RequestContext context, String token) {
		context.getFlowScope().setAttribute(getTransactionTokenAttributeName(), generateToken());
	}
	
	public void clearToken(RequestContext context) {
		context.getFlowScope().removeAttribute(getTransactionTokenAttributeName());
	}

}