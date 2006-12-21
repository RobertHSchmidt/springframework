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
package org.springframework.webflow.support;

import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.Event;
import org.springframework.webflow.action.AbstractAction;

/**
 * A convenience support class assisting in the creation of event objects.
 * @author Keith Donald
 */
public class EventFactorySupport {

	/**
	 * The default 'success' result event identifier ("success").
	 */
	public static final String SUCCESS_EVENT_ID = "success";

	/**
	 * The default 'error' result event identifier ("error").
	 */
	public static final String ERROR_EVENT_ID = "error";

	/**
	 * The default 'yes' result event identifier ("yes").
	 */
	public static final String YES_EVENT_ID = "yes";

	/**
	 * The default 'no' result event identifier ("no").
	 */
	public static final String NO_EVENT_ID = "no";

	/**
	 * The error event 'exception' parameter name ("exception").
	 */
	public static final String EXCEPTION_PARAMETER = "exception";

	/**
	 * The success event 'result' parameter name ("result").
	 */
	public static final String RESULT_PARAMETER = "result";

	// factory methods for creating common events

	/**
	 * Returns an "error" result event.
	 */
	protected Event error() {
		return result(ERROR_EVENT_ID);
	}

	/**
	 * Returns an "error" result event caused by the provided exception.
	 * @param e the exception that caused the error event, to be sent as an
	 * event parameter under the name {@link AbstractAction#EXCEPTION_PARAMETER}
	 */
	protected Event error(Exception e) {
		return result(ERROR_EVENT_ID, EXCEPTION_PARAMETER, e);
	}

	/**
	 * Returns a "success" result event.
	 */
	protected Event success() {
		return result(SUCCESS_EVENT_ID);
	}

	/**
	 * Returns a "success" result event with the provided result object as a
	 * parameter. The result object is identified by the parameter name
	 * {@link AbstractAction#RESULT_PARAMETER}.
	 * @param result the action success result;
	 */
	protected Event success(Object result) {
		return result(SUCCESS_EVENT_ID, RESULT_PARAMETER, result);
	}

	/**
	 * Returns a "success" result event with the provided result object as a
	 * parameter.
	 * @param resultParameterName the name of the result paramter in the created
	 * event
	 * @param result the action success result
	 */
	protected Event success(String resultParameterName, Object result) {
		return result(SUCCESS_EVENT_ID, resultParameterName, result);
	}

	/**
	 * Returns a "yes" result event.
	 */
	protected Event yes() {
		return result(YES_EVENT_ID);
	}

	/**
	 * Returns a "no" result event.
	 */
	protected Event no() {
		return result(NO_EVENT_ID);
	}

	/**
	 * Returns yes() if the boolean result is true, no() if false.
	 * @param booleanResult the boolean
	 * @return yes or no
	 */
	protected Event yesOrNo(boolean booleanResult) {
		if (booleanResult) {
			return yes();
		}
		else {
			return no();
		}
	}

	/**
	 * Returns a result event for this action with the specified identifier.
	 * Typically called as part of return, for example:
	 * 
	 * <pre>
	 *     protected Event doExecute(RequestContext context) {
	 *         // do some work
	 *         if (some condition) {
	 *             return result(&quot;success&quot;);
	 *         } else {
	 *             return result(&quot;error&quot;);
	 *         }
	 *     }
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
	 * Returns a result event for this action with the specified identifier and
	 * the specified set of parameters. Typically called as part of return, for
	 * example:
	 * 
	 * <pre>
	 *     protected Event doExecute(RequestContext context) {
	 *         // do some work
	 *         Map resultParameters = new HashMap();
	 *         resultParameters.put(&quot;parameterName&quot;, &quot;parameterValue&quot;);
	 *         if (some condition) {
	 *             return result(&quot;success&quot;, resultParameters);
	 *         } else {
	 *             return result(&quot;error&quot;, resultParameters);
	 *         }
	 *     }
	 * </pre>
	 * 
	 * Consider calling the error() or success() factory methods for returning
	 * common results.
	 * @param eventId the result event identifier
	 * @param attributes the event attributes
	 * @return the action result event
	 */
	protected Event result(String eventId, AttributeCollection attributes) {
		return new Event(this, eventId, attributes);
	}

	/**
	 * Returns a result event for this action with the specified identifier and
	 * a single parameter.
	 * @param eventId the result id
	 * @param attributeName the parameter name
	 * @param attributeValue the parameter value
	 * @return the action result event
	 */
	protected Event result(String eventId, String attributeName, Object attributeValue) {
		AttributeMap attributes = new AttributeMap(1, 1);
		attributes.put(attributeName, attributeValue);
		return new Event(this, eventId, attributes);
	}
}