/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * Worker object that performs an action execution, encapsulating common logging and
 * exception handling logic. This is an internal helper class that is not normally
 * used by application code.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionExecutor {

	private final Log logger = LogFactory.getLog(ActionExecutor.class);

	/**
	 * The action that will be executed.
	 */
	private Action action;

	/**
	 * Create a new action executor.
	 * @param action the action to execute
	 */
	public ActionExecutor(Action action) {
		Assert.notNull(action, "The action to execute is required");
		this.action = action;
	}

	/**
	 * Returns the wrapped action.
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Execute the wrapped action.
	 * @param context the flow execution request context
	 * @return result of action execution
	 * @throws ActionExecutionException if the action threw an exception while executing
	 */
	public Event execute(RequestContext context) throws ActionExecutionException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing action [" + action + "] in state '" + context.getFlowExecutionContext().getCurrentState().getId() + "'");
			}
			return action.execute(context);
		}
		catch (ActionExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			// wrap the action as an ActionExecutionException
			throw new ActionExecutionException(context.getFlowExecutionContext().getCurrentState(), action, e);
		}
	}
	
	public String toString() {
		return action.toString();
	}
}