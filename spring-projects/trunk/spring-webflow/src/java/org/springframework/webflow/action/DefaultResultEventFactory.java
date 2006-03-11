package org.springframework.webflow.action;

import org.springframework.core.enums.LabeledEnum;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.support.EventFactorySupport;

/**
 * Default implementation of the resultObject-to-event adapter interface.
 * 
 * @author Keith Donald
 */
public class DefaultResultEventFactory extends EventFactorySupport implements ResultEventFactory {

	public Event createResultEvent(Object source, Object resultObject, RequestContext context) {
		if (resultObject instanceof Event) {
			return (Event)resultObject;
		}
		if (context.getCurrentState() instanceof DecisionState) {
			return toDecisionStateEvent(source, resultObject, context);
		}
		else {
			return success(source, resultObject);
		}
	}

	/**
	 * Called when this action is invoked by a decision state - adapts the
	 * invoked method's return value to an event identifier the decision state
	 * can respond to.
	 * @param context the request context
	 * @param resultObject the return value
	 * @return the decision event
	 */
	protected Event toDecisionStateEvent(Object source, Object resultObject, RequestContext context) {
		if (resultObject == null) {
			return event(source, getNullEventId(), getResultAttributeName(), null);
		}
		else if (resultObject instanceof Boolean) {
			return event(source, ((Boolean)resultObject).booleanValue());
		}
		else if (resultObject instanceof LabeledEnum) {
			String resultId = ((LabeledEnum)resultObject).getLabel();
			return event(source, resultId, getResultAttributeName(), resultObject);
		}
		else {
			return event(source, String.valueOf(resultObject), getResultAttributeName(), resultObject);
		}
	}
}