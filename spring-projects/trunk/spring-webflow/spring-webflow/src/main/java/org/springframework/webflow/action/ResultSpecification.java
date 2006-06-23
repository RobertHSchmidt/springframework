/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.action;

import org.springframework.webflow.RequestContext;

/**
 * Strategy encapsulating how a bean method result value should be exposed
 * to an executing flow.
 * 
 * @see org.springframework.webflow.action.AbstractBeanInvokingAction
 * @see org.springframework.webflow.RequestContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface ResultSpecification {

	/**
	 * Expose given bean method return value in given flow execution request
	 * context.
	 * @param returnValue the return value
	 * @param context the request context
	 */
	public void exposeResult(Object returnValue, RequestContext context);
}
