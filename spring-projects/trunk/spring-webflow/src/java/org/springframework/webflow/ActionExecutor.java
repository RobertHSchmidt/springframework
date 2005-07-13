package org.springframework.webflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * Internal action executor, encapsulating a single action's execution and
 * result handling logic.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionExecutor {

	protected final Log logger = LogFactory.getLog(ActionExecutor.class);

	private State state;

	private Action action;

	/**
	 * Create a new action executor.
	 * @param action the action to wrap
	 */
	public ActionExecutor(State state, Action action) {
		Assert.notNull(action, "The action state's action is required");
		this.state = state;
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
	 * @return result of execution
	 */
	protected Event execute(RequestContext context) throws ActionExecutionException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("In state: " + context.getFlowExecutionContext().getCurrentState().getId() + ", executing action: " + this + "");
			}
			return action.execute(context);
		}
		catch (ActionExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ActionExecutionException(state, action, e);
		}
	}

	public String toString() {
		return action.toString();
	}
}