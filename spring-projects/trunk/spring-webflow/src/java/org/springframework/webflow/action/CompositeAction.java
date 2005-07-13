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
package org.springframework.webflow.action;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * A action that will execute an ordered chain of other actions when executed.
 * @author Keith Donald
 */
public class CompositeAction extends AbstractAction {

	/**
	 * The actions to execute.
	 */
	private Action[] actions;

	/**
	 * Should execution stop if one action returns an error event?
	 */
	private boolean stopOnError;
	
	/**
	 * The error result event identifier. 
	 */
	private String errorEventId = ERROR_RESULT_EVENT_ID;
	
	/**
	 * Create a action precondition delegating to the specified action.
	 * @param action the action
	 */
	public CompositeAction(Action[] actions) {
		Assert.notEmpty(actions, "At least one action is required");
		this.actions = actions;
	}

	/**
	 * Returns the action attributes associated with this action precondition.
	 * @return the attributes
	 */
	protected Action[] getActions() {
		return actions;
	}

	/**
	 * Retruns the stop on error flag.
	 */
	public boolean isStopOnError() {
		return stopOnError;
	}

	/**
	 * Sets the stop on error flag.
	 */
	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}
	
	/**
	 * Returns the error event id.
	 */
	public String getErrorEventId() {
		return errorEventId;
	}

	/**
	 * Sets the error event id.
	 */
	public void setErrorEventId(String errorEventId) {
		this.errorEventId = errorEventId;
	}
	
	public Event doExecute(RequestContext context) throws Exception {
		Action[] actions = getActions();
		Event result = null;
		for (int i = 0; i < actions.length; i++) {
			Action action = actions[i];
			result = action.execute(context);
			if (isStopOnError() && result != null && result.getId().equals(getErrorEventId())) {
				return result;
			}
		}
		return result;
	}
	
	public String toString() {
		return new ToStringCreator(this).append("actions", getActions()).append("stopOnError", stopOnError).
			append("errorEventId", errorEventId).toString();
	}
}