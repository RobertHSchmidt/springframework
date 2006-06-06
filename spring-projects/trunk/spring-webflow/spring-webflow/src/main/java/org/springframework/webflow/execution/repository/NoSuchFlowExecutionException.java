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
package org.springframework.webflow.execution.repository;

import org.springframework.webflow.execution.repository.conversation.NoSuchConversationException;

/**
 * Thrown when no logical conversation exists with the specified
 * <code>conversationId</code>. This might occur if the conversation ended,
 * expired, or was otherwise invalidated, but a client view still references it.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchFlowExecutionException extends FlowExecutionRepositoryException {

	/**
	 * The unique conversation identifier that was invalid.
	 */
	private FlowExecutionKey flowExecutionKey;

	/**
	 * Create a new flow execution lookup exception.
	 * @param conversationId the conversation id
	 */
	public NoSuchFlowExecutionException(FlowExecutionKey flowExecutionKey) {
		this(flowExecutionKey, null);
	}

	public NoSuchFlowExecutionException(FlowExecutionKey flowExecutionKey, NoSuchConversationException e) {
		super("No flow execution could be found with key '" + flowExecutionKey
				+ "' -- perhaps this executing flow has ended or expired? "
				+ "This could happen if your users are relying on browser history "
				+ "(typically via the back button) that reference ended flows.", e);
		this.flowExecutionKey = flowExecutionKey;
	}

	/**
	 * Returns the conversation id that was invalid.
	 */
	public FlowExecutionKey getFlowExecutionKey() {
		return flowExecutionKey;
	}
}