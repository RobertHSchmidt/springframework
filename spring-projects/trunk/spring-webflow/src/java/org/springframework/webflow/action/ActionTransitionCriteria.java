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

import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.ActionExecutionException;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.support.TransitionCriteriaChain;

/**
 * A transition criteria that will execute an action when tested and return
 * <code>true</code> if the action's result is equal to the 'trueEventId', false
 * otherwise.
 * <p>
 * This effectively adapts an <code>Action</code> to a <code>TransitionCriteria</code>.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionTransitionCriteria implements TransitionCriteria {

	/**
	 * The action to execute when the precondition is tested, annotated with usage attributes.
	 */
	private AnnotatedAction action;

	/**
	 * The result event id that should map to a <code>true</code> precondition
	 * return value.
	 */
	private String trueEventId = AbstractAction.SUCCESS_RESULT_EVENT_ID;

	/**
	 * Create a action precondition delegating to the specified action.
	 * @param action the action
	 */
	public ActionTransitionCriteria(Action action) {
		this(new AnnotatedAction(action));
	}

	/**
	 * Create a action precondition delegating to the specified action.
	 * @param action the action
	 */
	public ActionTransitionCriteria(AnnotatedAction action) {
		Assert.notNull(action, "The action is required");
		this.action = action;
	}

	/**
	 * Returns the action result <code>eventId</code> that should cause this
	 * precondition to return true (it will return false otherwise).
	 */
	public String getTrueEventId() {
		return trueEventId;
	}

	/**
	 * Sets the action result <code>eventId</code> that should cause this
	 * precondition to return true (it will return false otherwise).
	 * @param trueEventId the true result event ID
	 */
	public void setTrueEventId(String trueEventId) {
		Assert.hasText(trueEventId, "the trueEventId is required");
		this.trueEventId = trueEventId;
	}

	/**
	 * Returns the action attributes associated with this action precondition.
	 * @return the attributes
	 */
	protected AnnotatedAction getAction() {
		return action;
	}
	
	public boolean test(RequestContext context) {
		context.setProperties(action);
		try {
			Event result = this.action.getTargetAction().execute(context);
			return result != null && getTrueEventId().equals(result.getId());
		}
		catch (Exception e) {
			throw new ActionExecutionException(context.getFlowExecutionContext().getCurrentState(), action, e);
		}
		finally {
			context.setProperties(null);
		}
	}

	public static TransitionCriteria criteriaChainFor(AnnotatedAction[] actions) {
		TransitionCriteriaChain chain = new TransitionCriteriaChain();
		for (int i = 0; i < actions.length; i++) {
			chain.add(new ActionTransitionCriteria(actions[i]));
		}
		return chain;
	}
}