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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Base action implementation that provides a number of helper methods generally
 * useful to any action command. These include:
 * <ul>
 * <li>Creating common action result events
 * <li>Inserting action pre and post execution logic (may also be done with an
 * interceptor)
 * </ul>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractAction implements Action, InitializingBean {

	/**
	 * Event id of the default 'success' result event ("success").
	 */
	public static final String SUCCESS_EVENT_ID = "success";
	
	/**
	 * Event id of the default 'error' result event ("error").
	 */
	public static final String ERROR_EVENT_ID = "error";

	/**
	 * Event id of the default 'yes' result event ("yes").
	 */
	public static final String YES_EVENT_ID = "yes";

	/**
	 * Event id of the default 'no' result event ("no").
	 */
	public static final String NO_EVENT_ID = "no";

	/**
	 * Constant for the error exception event parameter name ("exception").
	 */
	public static final String EXCEPTION_PARAMETER = "exception";

	/**
	 * Constant for the success result object event parameter name ("result").
	 */
	public static final String RESULT_PARAMETER = "result";

	/**
	 * Logger, usable in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	public void afterPropertiesSet() {
		initAction();
	}

	/**
	 * Action initializing callback, may be overriden by subclasses to perform
	 * custom initialization logic.
	 */
	protected void initAction() {
	}

	// creating common events

	/**
	 * Returns the default error event.
	 */
	protected Event error() {
		return result(ERROR_EVENT_ID);
	}

	/**
	 * Returns the default error event caused by the provided exception.
	 * @param e the exception that caused the error event, to be sent as
	 *        an event parameter under the name {@link AbstractAction#EXCEPTION_PARAMETER}
	 */
	protected Event error(Exception e) {
		return result(ERROR_EVENT_ID, EXCEPTION_PARAMETER, e);
	}

	/**
	 * Returns the default success event.
	 */
	protected Event success() {
		return result(SUCCESS_EVENT_ID);
	}

	/**
	 * Returns the default success event with the provided result object
	 * as a parameter.  The result object is identified by the parameter name
	 * {@link AbstractAction#RESULT_PARAMETER}.
	 * 
	 * @param result the action success result;
	 */
	protected Event success(Object result) {
		return result(SUCCESS_EVENT_ID, RESULT_PARAMETER, result);
	}

	/**
	 * Returns the default yes event.
	 */
	protected Event yes() {
		return result(YES_EVENT_ID);
	}

	/**
	 * Returns the default no event.
	 */
	protected Event no() {
		return result(NO_EVENT_ID);
	}

	/**
	 * Returns a result event for this action with the specified identifier.
	 * Typically called as part of return, for example:
	 * 
	 * <pre>
	 *    protected Event doExecute(RequestContext context) {
	 *      // do some work
	 *      if (some condition) {
	 *        return result(&quot;success&quot;);
	 *      } else {
	 *        return result(&quot;error&quot;);
	 *      }
	 *    }
	 * </pre>
	 * 
	 * Consider calling the error() or success() factory methods for returning
	 * common results.
	 * @param eventId the result event identifier
	 * @return the action result event
	 */
	protected Event result(String eventId) {
		return new Event(this, eventId);
	}

	/**
	 * Returns a result event for this action with the specified identifier
	 * and the specified set of parameters. Typically called as part of
	 * return, for example:
	 * 
	 * <pre>
	 *    protected Event doExecute(RequestContext context) {
	 *      // do some work
	 *      Map resultParameters = new HashMap();
	 *      resultParameters.put("parameterName", "parameterValue");
	 *      if (some condition) {
	 *        return result(&quot;success&quot;, resultParameters);
	 *      } else {
	 *        return result(&quot;error&quot;, resultParameters);
	 *      }
	 *    }
	 * </pre>
	 * 
	 * Consider calling the error() or success() factory methods for returning
	 * common results.
	 * @param eventId the result event identifier
	 * @param parameters the event parameters
	 * @return the action result event
	 */
	protected Event result(String eventId, Map parameters) {
		return new Event(this, eventId, parameters);
	}

	/**
	 * Returns a result event for this action with the specified identifier
	 * and a single parameter. Typically called as part of return, for example:
	 * <pre>
	 *   public Event makeSelection(RequestContext context) throws Exception {
	 *      try {
	 *         String selection = (String) context.getSourceEvent().getParameter("selection");
	 *         selectionSensorHelper.fireSelectionMade(selection);
	 *         return success();
	 *      } catch (NoSuchBinException e) {
	 *         return result("noSuchBin", "exception", e);
	 *      } catch (BinEmptyException e) {
	 *         return result("binEmpty", "exception", e);
	 *      } catch (NotEnoughFundsException e) {
	 *         return result("notEnoughFunds", "exception", e);
	 *      }
	 *   }
	 * </pre>
	 * @param eventId the result id
	 * @param parameterName the parameter name
	 * @param parameterValue the parameter value
	 * @return the action result event
	 */
	protected Event result(String eventId, String parameterName, Object parameterValue) {
		HashMap parameters = new HashMap(1);
		parameters.put(parameterName, parameterValue);
		return new Event(this, eventId, parameters);
	}

	/**
	 * Get a named execution property for this action from the request context.
	 * @param context the flow execution request context
	 * @param propertyName the name of the property to get
	 * @param defaultValue the default value to use when the named property cannot be
	 *        found in the execution properties
	 * @return the property value
	 */
	protected Object getActionProperty(RequestContext context, String propertyName, Object defaultValue) {
		if (context.getProperties().containsAttribute(propertyName)) {
			return (String) context.getProperties().getAttribute(propertyName);
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Get a names execution property for this action from the request context. Throw
	 * an exception if the property is not defined.
	 * @param context the flow execution request context
	 * @param propertyName the name of the property to get
	 * @return the property value
	 * @throws IllegalArgumentException when the property is not defined
	 */
	protected Object getRequredActionProperty(RequestContext context, String propertyName) throws IllegalArgumentException {
		if (context.getProperties().containsAttribute(propertyName)) {
			return (String) context.getProperties().getAttribute(propertyName);
		}
		else {
			throw new IllegalStateException(
					"Required action execution property '" + propertyName +
					"' not present in request context, properties present are: " + context.getProperties());
		}
	}

	public final Event execute(RequestContext context) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Action '" + getClass().getName() + "' beginning execution");
		}
		Event result = doPreExecute(context);
		if (result == null) {
			result = doExecute(context);
			if (logger.isDebugEnabled()) {
				logger.debug("Action '" + getClass().getName() + "' completed execution; result event is " + result);
			}
			doPostExecute(context);
			if (logger.isInfoEnabled()) {
				if (result == null) {
					logger.info("Retured action event is [null]; that's ok so long as another action associated "
							+ "with the currently executing flow state returns a valid event");
				}
			}
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("Action execution disallowed; event is " + result);
			}
		}
		return result;
	}

	/**
	 * Pre-action-execution hook, subclasses may override. If this method
	 * returns a non-<code>null</code> event, the
	 * <code>doExecute()</code> method will <b>not</b> be called and
	 * the returned event will be used to select a transition to trigger in the
	 * calling action state. If this method returns <code>null</code>,
	 * <code>doExecute()</code> will be called to obtain an action
	 * result event.
	 * <p>
	 * This implementation just returns <code>null</code>.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @return the non-<code>null</code> action result, in which case the
	 *         <code>doExecute()</code> will not be called, or
	 *         <code>null</code> if the <code>doExecute()</code>
	 *         method should be called to obtain the action result
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	protected Event doPreExecute(RequestContext context) throws Exception {
		return null;
	}

	/**
	 * Template hook method subclasses should override to encapsulate their
	 * specific action execution logic.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @return the action result event
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	protected abstract Event doExecute(RequestContext context) throws Exception;

	/**
	 * Post-action execution hook, subclasses may override.
	 * <p>
	 * This implementation does nothing.
	 * @param context the action execution context, for accessing and setting
	 *        data in "flow scope" or "request scope"
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 *         checked or unchecked
	 */
	protected void doPostExecute(RequestContext context) throws Exception {
	}
}