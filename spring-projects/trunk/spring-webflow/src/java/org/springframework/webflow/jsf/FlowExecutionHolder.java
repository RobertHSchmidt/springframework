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
 * Simple holder class that associates a FlowExecution instance with the current
 * thread. The FlowExecution will be inherited by any child threads spawned by
 * the current thread.
 * 
 * @author Colin Sampaleanu
 */
public abstract class FlowExecutionHolder {

	/**
	 * Thread-specific storage.
	 */
	private static ThreadLocal threadedContext = new InheritableThreadLocal();

	/**
	 * Return the FlowExecution id associated with the current thread, if any.
	 * @return the current FlowExecution id, or <code>null</code> if none
	 */
	public static Serializable getFlowExecutionId() {
		return getContext().flowExecutionId;
	}

	/**
	 * Return the FlowExecution associated with the current thread, if any.
	 * @return the current FlowExecution, or <code>null</code> if none
	 */
	public static FlowExecution getFlowExecution() {
		return getContext().flowExecution;
	}

	/**
	 * Returns the source <code>JsfEvent</code> that triggered the request in
	 * process. Provides access to an external JSF context.
	 */
	public static JsfEvent getSourceEvent() {
		return getContext().event;
	}

	/**
	 * Return a boolean indicating whether or not the current flow execution has
	 * already been saved and is thus unmodifiable.
	 */
	public static boolean isFlowExecutionSaved() {
		return getContext().flowExecutionSaved;
	}

	/**
	 * Clear the FlowExecutionListener, FlowExecution id, and FlowExecution
	 * associated with the current thread
	 */
	public static void clearFlowExecution() {
		threadedContext.set(new Context());
	}

	/**
	 * Associate the given FlowExecution with the current thread. It is not
	 * legal to store a flow execution id, but no flow execution
	 * @param flowExecutionId the current FlowExecution id, or <code>null</code>
	 * to reset the thread-bound context
	 * @param flowExecution the current FlowExecution, or <code>null</code> to
	 * @param sourceEvent the source event which triggered the current execution
	 * @param flowExecutionSaved boolean indicating whether the flow execution has already
	 * been saved to storage or is still live reset the thread-bound context
	 */
	public static void setFlowExecution(Serializable flowExecutionId, FlowExecution flowExecution,
			JsfEvent sourceEvent, boolean flowExecutionSaved) {
		if (flowExecutionId != null) {
			Assert.notNull(flowExecution, "It is illegal to store a flow execution with a null flow execution id");
		}
		getContext().flowExecutionId = flowExecutionId;
		getContext().flowExecution = flowExecution;
		getContext().event = sourceEvent;
		getContext().flowExecutionSaved = flowExecutionSaved;
	}

	public static void setFlowExecutionSaved(boolean state) {
		getContext().flowExecutionSaved = state;
	}

	private static Context getContext() {
		Context context = (Context)threadedContext.get();
		if (context == null) {
			context = new Context();
			threadedContext.set(context);
		}
		return context;
	}

	private static class Context {
		public Serializable flowExecutionId;

		public FlowExecution flowExecution;

		public JsfEvent event;

		public boolean flowExecutionSaved;
	}
}