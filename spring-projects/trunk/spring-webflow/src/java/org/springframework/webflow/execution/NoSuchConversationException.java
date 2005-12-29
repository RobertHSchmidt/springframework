/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.execution;

import java.io.Serializable;

/**
 * Thrown when no flow execution exists by the specified
 * <code>flowExecutionId</code>. This might occur if the flow execution timed
 * out, but a client view still references it.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchConversationException extends FlowExecutionStorageException {

	/**
	 * Create a new flow execution lookup exception.
	 * @param flowExecutionId id of the flow execution that cannot be found
	 */
	public NoSuchConversationException(Serializable conversationId) {
		this(conversationId, null);
	}

	/**
	 * Create a new flow execution lookup exception.
	 * @param flowExecutionId id of the flow execution that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public NoSuchConversationException(Serializable conversationId, Throwable cause) {
		super(conversationId, null, "No conversation could be found with id '" + conversationId
				+ "' -- perhaps this executing flow has ended or expired? "
				+ "This could happen if your users are relying on browser history "
				+ "(typically via the back button) that reference ended flows.", cause);
	}
}