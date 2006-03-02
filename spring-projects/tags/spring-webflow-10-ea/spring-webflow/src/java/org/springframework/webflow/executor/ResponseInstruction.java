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
package org.springframework.webflow.executor;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.repository.FlowExecutionKey;
import org.springframework.webflow.support.ApplicationView;
import org.springframework.webflow.support.ConversationRedirect;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.support.FlowRedirect;

/**
 * Immutable value object that provides clients with information about a
 * response to issue.
 * <p>
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ResponseInstruction implements Serializable {

	/**
	 * Generated serialization id.
	 */
	private static final long serialVersionUID = -3787181142379347131L;

	/**
	 * The id of the flow execution.
	 */
	private final FlowExecutionKey flowExecutionKey;

	/**
	 * A state of the flow execution.
	 */
	private transient final FlowExecutionContext flowExecutionContext;

	/**
	 * The view selection that was made.
	 */
	private final ViewSelection viewSelection;

	/**
	 * Create a new response instruction for a paused flow execution.
	 * @param flowExecutionKey
	 * @param flowExecutionContext
	 * @param viewSelection
	 */
	public ResponseInstruction(FlowExecutionKey flowExecutionKey, FlowExecutionContext flowExecutionContext,
			ViewSelection viewSelection) {
		this.flowExecutionKey = flowExecutionKey;
		this.flowExecutionContext = flowExecutionContext;
		this.viewSelection = viewSelection;
	}

	/**
	 * Create a new response instruction for a ended flow execution.
	 * @param flowExecutionContext
	 * @param viewSelection
	 */
	public ResponseInstruction(FlowExecutionContext flowExecutionContext, ViewSelection viewSelection) {
		this.flowExecutionKey = null;
		this.flowExecutionContext = flowExecutionContext;
		this.viewSelection = viewSelection;
	}

	public FlowExecutionKey getFlowExecutionKey() {
		return flowExecutionKey;
	}

	public FlowExecutionContext getFlowExecutionContext() {
		return flowExecutionContext;
	}

	public ViewSelection getViewSelection() {
		return viewSelection;
	}

	public boolean isNull() {
		return viewSelection == ViewSelection.NULL_VIEW;
	}

	public boolean isApplicationView() {
		return viewSelection instanceof ApplicationView;
	}
	
	public boolean isActiveView() {
		return isApplicationView() && flowExecutionContext.isActive();
	}
	
	public boolean isConfirmationView() {
		return isApplicationView() && !flowExecutionContext.isActive();
	}
	
	public boolean isConversationRedirect() {
		return viewSelection instanceof ConversationRedirect;
	}

	public boolean isExternalRedirect() {
		return viewSelection instanceof ExternalRedirect;
	}

	public boolean isFlowRedirect() {
		return viewSelection instanceof FlowRedirect;
	}
	
	public String toString() {
		return new ToStringCreator(this).append("flowExecutionKey", flowExecutionKey).append("flowExecutionContext",
				flowExecutionContext).append("viewSelection", viewSelection).toString();
	}
}