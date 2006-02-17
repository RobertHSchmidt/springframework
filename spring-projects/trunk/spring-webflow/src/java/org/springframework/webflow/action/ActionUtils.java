package org.springframework.webflow.action;

import org.springframework.webflow.RequestContext;

/**
 * Simple static utility class holding convenience methods for action
 * implementations.
 * 
 * @author Keith Donald
 */
public abstract class ActionUtils {

	/**
	 * Cannot be instantiated - static utility class.
	 */
	private ActionUtils() {
	}

	/**
	 * Lookup the named execution property for this action from the request
	 * context.
	 * @param context the flow execution request context
	 * @param propertyName the name of the property to retrieve
	 * @param defaultValue the default value to use when the named property
	 * cannot be found
	 * @return the action property value
	 */
	public static Object getActionProperty(RequestContext context, String propertyName, Object defaultValue) {
		if (context.getProperties().containsKey(propertyName)) {
			return context.getProperties().get(propertyName);
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Lookup the named execution property for this action from the request
	 * context. Throw an exception if the property is not defined.
	 * @param context the flow execution request context
	 * @param propertyName the name of the property to get
	 * @return the property value
	 * @throws IllegalStateException when the property is not defined
	 */
	public static Object getRequiredActionProperty(RequestContext context, String propertyName)
			throws IllegalStateException {
		if (context.getProperties().containsKey(propertyName)) {
			return context.getProperties().get(propertyName);
		}
		else {
			throw new IllegalStateException("Required action execution property '" + propertyName
					+ "' not present in request context, properties present are: " + context.getProperties());
		}
	}
}