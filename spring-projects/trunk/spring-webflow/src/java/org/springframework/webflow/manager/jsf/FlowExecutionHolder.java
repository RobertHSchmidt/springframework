/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.manager.jsf;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;

public class FlowExecutionHolder implements Serializable {

	private FlowExecutionContinuationKey continuationKey;

	private FlowExecution flowExecution;

	public FlowExecutionHolder(FlowExecution flowExecution) {
		this.flowExecution = flowExecution;
	}

	public FlowExecutionHolder(FlowExecutionContinuationKey continuationKey, FlowExecution flowExecution) {
		this.continuationKey = continuationKey;
		this.flowExecution = flowExecution;
	}

	public FlowExecutionContinuationKey getContinuationKey() {
		return continuationKey;
	}

	public void setContinuationKey(FlowExecutionContinuationKey continuationKey) {
		this.continuationKey = continuationKey;
	}

	public FlowExecution getFlowExecution() {
		return flowExecution;
	}
}