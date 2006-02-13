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
package org.springframework.webflow.executor;

import java.io.Serializable;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.repository.FlowExecutionKey;

/**
 * Immutable value object that provides clients with information about a
 * response to issue.
 * <p>
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ResponseInstruction implements Serializable {

	/**
	 * The id of the flow execution.
	 */
	private final FlowExecutionKey flowExecutionKey;

	/**
	 * A state of the flow execution.
	 */
	private final FlowExecutionContext flowExecutionContext;

	/**
	 * The view selection that was made.
	 */
	private final ViewSelection viewSelection;

	/**
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
	 * @param flowExecutionKey
	 * @param flowExecutionContext
	 * @param viewSelection
	 */
	public ResponseInstruction(FlowExecutionContext flowExecutionContext, ViewSelection viewSelection) {
		this.flowExecutionKey = null;
		this.flowExecutionContext = flowExecutionContext;
		this.viewSelection = viewSelection;
	}

	public String getViewName() {
		return viewSelection.getViewName();
	}

	public Map getModel() {
		return viewSelection.getModel();
	}

	public boolean isNull() {
		return viewSelection.isNull();
	}

	public boolean isRedirect() {
		return viewSelection.isRedirect();
	}
	
	public boolean isRestart() {
		return false;
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

	public String toString() {
		return new ToStringCreator(this).append("flowExecutionKey", flowExecutionKey).append("flowExecutionContext",
				flowExecutionContext).append("viewSelection", viewSelection).toString();
	}
}