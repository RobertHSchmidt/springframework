/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.webflow.jsf;

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecution;

/**
 * Simple holder class that associates a FlowExecution instance
 * with the current thread. The FlowExecution will be inherited
 * by any child threads spawned by the current thread.
 *
 * @author Colin Sampaleanu
 * @since 1.0
 */
public abstract class FlowExecutionHolder {

	private static ThreadLocal flowExecutionIdHolder = new InheritableThreadLocal();
	private static ThreadLocal flowExecutionHolder = new InheritableThreadLocal();
	
	/**
	 * Return the FlowExecution associated with the current thread,
	 * if any.
	 * @return the current FlowExecution, or <code>null</code> if none
	 */
	public static FlowExecution getFlowExecution() {
		return (FlowExecution) flowExecutionHolder.get();
	}

	/**
	 * Return the FlowExecution id associated with the current thread,
	 * if any.
	 * @return the current FlowExecution id, or <code>null</code> if none
	 */
	public static Serializable getFlowExecutionId() {
		return (Serializable) flowExecutionIdHolder.get();
	}
	
	/**
	 * clear the FlowExecution associated with the current thread
	 */
	public static void clearFlowExecution() {
		flowExecutionIdHolder.set(null);
		flowExecutionHolder.set(null);
	}
	
	/**
	 * Associate the given FlowExecution with the current thread.
	 * @param flowExecution the current FlowExecution, or <code>null</code> to reset
	 * the thread-bound context
	 */
	public static void setFlowExecution(Serializable flowExecutionId, FlowExecution flowExecution) {
		if (flowExecutionId != null)
			Assert.notNull(flowExecution, "illegal to store flow execution id but not flow execution");
		flowExecutionHolder.set(flowExecution);
	}
}
