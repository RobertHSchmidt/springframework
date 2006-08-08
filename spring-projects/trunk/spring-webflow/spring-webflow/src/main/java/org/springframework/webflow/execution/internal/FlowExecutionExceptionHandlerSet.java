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
package org.springframework.webflow.execution.internal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.style.StylerUtils;
import org.springframework.webflow.collection.support.CollectionUtils;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.ViewSelection;

/**
 * A typed set of state exception handlers, mainly for use internally by
 * artifacts that can apply state exception handling logic.
 * 
 * @see Flow#getExceptionHandlerSet()
 * @see State#getExceptionHandlerSet()
 * 
 * @author Keith Donald
 */
public class FlowExecutionExceptionHandlerSet {

	/**
	 * The set of exception handlers.
	 */
	private List exceptionHandlers = new LinkedList();

	/**
	 * Add a state exception handler to this set.
	 * @param exceptionHandler the exception handler to add
	 * @return true if this set's contents changed as a result of the add
	 * operation
	 */
	public boolean add(FlowExecutionExceptionHandler exceptionHandler) {
		if (contains(exceptionHandler)) {
			return false;
		}
		return exceptionHandlers.add(exceptionHandler);
	}

	/**
	 * Add a collection of state exception handler instances to this set.
	 * @param exceptionHandlers the exception handlers to add
	 * @return true if this set's contents changed as a result of the add
	 * operation
	 */
	public boolean addAll(FlowExecutionExceptionHandler[] exceptionHandlers) {
		return CollectionUtils.addAllNoDuplicates(this.exceptionHandlers, exceptionHandlers);
	}

	/**
	 * Tests if this state exception handler is in this set.
	 * @param exceptionHandler the exception handler
	 * @return true if the state exception handler is contained in this set,
	 * false otherwise
	 */
	public boolean contains(FlowExecutionExceptionHandler exceptionHandler) {
		return exceptionHandlers.contains(exceptionHandler);
	}

	/**
	 * Remove the exception handler instance from this set.
	 * @param exceptionHandler the exception handler to add
	 * @return true if this set's contents changed as a result of the remove
	 * operation
	 */
	public boolean remove(FlowExecutionExceptionHandler exceptionHandler) {
		return exceptionHandlers.remove(exceptionHandler);
	}

	/**
	 * Returns the size of this state exception handler set.
	 * @return the exception handler set size.
	 */
	public int size() {
		return exceptionHandlers.size();
	}

	/**
	 * Convert this list to a typed state exception handler array.
	 * @return the exception handler list, as a typed array
	 */
	public FlowExecutionExceptionHandler[] toArray() {
		return (FlowExecutionExceptionHandler[])exceptionHandlers.toArray(new FlowExecutionExceptionHandler[exceptionHandlers.size()]);
	}

	/**
	 * Handle an exception that occured during the context of the current flow
	 * execution request.
	 * <p>
	 * This implementation iterates over the ordered set of exception handler
	 * objects, delegating to each handler in the set until one handles the
	 * exception that occured and selects a non-null error view.
	 * @param exception the exception that occured
	 * @param context the flow execution control context
	 * @return the selected error view, or <code>null</code> if no handler
	 * matched or returned a non-null view selection
	 */
	public ViewSelection handleException(FlowExecutionException exception, RequestControlContext context) {
		Iterator it = exceptionHandlers.iterator();
		while (it.hasNext()) {
			FlowExecutionExceptionHandler handler = (FlowExecutionExceptionHandler)it.next();
			if (handler.handles(exception)) {
				ViewSelection result = handler.handle(exception, context);
				if (result != null) {
					return result;
				}
				// else continue with next handler
			}
		}
		return null;
	}

	public String toString() {
		return StylerUtils.style(exceptionHandlers);
	}
}