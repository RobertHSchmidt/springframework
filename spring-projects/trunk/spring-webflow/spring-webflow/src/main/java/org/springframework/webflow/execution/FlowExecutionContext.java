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
package org.springframework.webflow.execution;

import org.springframework.webflow.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;

/**
 * Provides contextual information about a flow execution. A flow execution is
 * an instance of a flow definition. An object implementing this interface is
 * also traversable from a request context (see
 * {@link org.springframework.webflow.execution.RequestContext#getFlowExecutionContext()}).
 * <p>
 * This is an immutable interface for accessing information about exactly one
 * flow execution--an instance of a {@link FlowDefinition flow definition}.
 * <p>
 * This interface provides information that may span more than one request in a
 * thread safe manner. The {@link RequestContext} interface defines a <i>request
 * specific</i> control interface for manipulating exactly one flow execution
 * locally from exactly one request.
 * 
 * @see RequestContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionContext {

	/**
	 * Returns the root flow definition associated with this executing flow.
	 * <p>
	 * A call to this method always returns the same flow definition -- the
	 * top-level "root" -- no matter what flow may actually be active (for
	 * example, if subflows have been spawned).
	 * @return the root flow definition
	 */
	public FlowDefinition getFlowDefinition();

	/**
	 * Is the flow execution active?
	 * <p>
	 * All methods on an active flow execution context can be called
	 * successfully. If the flow execution is not active, a caller cannot access
	 * some methods such as {@link #getActiveSession()}.
	 * 
	 * @return true if active, false if flow execution has terminated
	 */
	public boolean isActive();

	/**
	 * @return
	 */
	public MutableAttributeMap getConversationScope();
	
	/**
	 * Returns the active flow session of this flow execution. The active flow
	 * session is the currently executing session--it may be the "root flow"
	 * session, or it may be a subflow session if this flow execution has
	 * spawned a subflow.
	 * @return the active flow session
	 * @throws IllegalStateException if this flow execution has not been started
	 * at all or if this execution has ended and is no longer actively executing
	 */
	public FlowSession getActiveSession() throws IllegalStateException;

}