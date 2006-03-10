package org.springframework.webflow.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.core.enums.LabeledEnum;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.support.EventFactorySupport;

/**
 * Default implementation of the event adapter interface.
 * @author Keith Donald
 */
public class DefaultResultEventFactory extends EventFactorySupport implements ResultEventFactory {

	private static final String JAVA_LANG_ENUM_CLASSNAME = "java.lang.Enum";

	private static Class java5EnumClass;

	private static Method java5EnumNameMethod;

	static {
		try {
			java5EnumClass = Class.forName(JAVA_LANG_ENUM_CLASSNAME);
			try {
				java5EnumNameMethod = java5EnumClass.getMethod("name", null);
			}
			catch (NoSuchMethodException e) {
				throw new RuntimeException("Should not happen on JDK 1.5");
			}
		}
		catch (ClassNotFoundException ex) {
		}
	}

	public Event createEvent(Object source, Object resultObject, RequestContext context) {
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
		else if (java5EnumClass != null && java5EnumClass.equals(resultObject.getClass())) {
			// handle special event adaption for enum return values
			return jdk5EnumResult(source, resultObject);
		}
		else if (resultObject instanceof LabeledEnum) {
			String resultId = ((LabeledEnum)resultObject).getLabel();
			return event(source, resultId, getResultAttributeName(), resultObject);
		}
		else {
			return event(source, String.valueOf(resultObject), getResultAttributeName(), resultObject);
		}
	}

	protected Event jdk5EnumResult(Object source, Object returnEnumValue) {
		try {
			String resultEventId = (String)java5EnumNameMethod.invoke(returnEnumValue, null);
			return event(source, resultEventId, getResultAttributeName(), returnEnumValue);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException("Should not happen on JDK 1.5");
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException("Should not happen on JDK 1.5");
		}
	}
}