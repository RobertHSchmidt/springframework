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

/**
 * A convenience support class assisting in the creation of event objects.
 * 
 * @author Keith Donald
 */
public class EventFactorySupport {

	/**
	 * The default 'success' result event identifier ("success").
	 */
	private static final String SUCCESS_EVENT_ID = "success";

	/**
	 * The default 'error' result event identifier ("error").
	 */
	private static final String ERROR_EVENT_ID = "error";

	/**
	 * The default 'yes' result event identifier ("yes").
	 */
	private static final String YES_EVENT_ID = "yes";

	/**
	 * The default 'no' result event identifier ("no").
	 */
	private static final String NO_EVENT_ID = "no";

	/**
	 * The default 'null' result event identifier ("null").
	 */
	private static final String NULL_EVENT_ID = "null";

	/**
	 * The default 'exception' event attribute name ("exception").
	 */
	private static final String EXCEPTION_ATTRIBUTE_NAME = "exception";

	/**
	 * The default 'result' event attribute name ("result").
	 */
	private static final String RESULT_ATTRIBUTE_NAME = "result";

	/**
	 * The success event identifier.
	 */
	private String successEventId = SUCCESS_EVENT_ID;

	/**
	 * The error event identifier.
	 */
	private String errorEventId = ERROR_EVENT_ID;

	/**
	 * The yes event identifier.
	 */
	private String yesEventId = YES_EVENT_ID;

	/**
	 * The no event identifier.
	 */
	private String noEventId = NO_EVENT_ID;

	/**
	 * The null event identifier.
	 */
	private String nullEventId = NULL_EVENT_ID;

	/**
	 * The exception event attribute name.
	 */
	private String exceptionAttributeName = EXCEPTION_ATTRIBUTE_NAME;

	/**
	 * The result event attribute name.
	 */
	private String resultAttributeName = RESULT_ATTRIBUTE_NAME;

	public String getSuccessEventId() {
		return successEventId;
	}

	public void setSuccessEventId(String successEventId) {
		this.successEventId = successEventId;
	}

	public String getErrorEventId() {
		return errorEventId;
	}

	public void setErrorEventId(String errorEventId) {
		this.errorEventId = errorEventId;
	}

	public String getYesEventId() {
		return yesEventId;
	}

	public void setYesEventId(String yesEventId) {
		this.yesEventId = yesEventId;
	}

	public String getNoEventId() {
		return noEventId;
	}

	public void setNoEventId(String noEventId) {
		this.noEventId = noEventId;
	}

	public String getNullEventId() {
		return nullEventId;
	}

	public void setNullEventId(String nullEventId) {
		this.nullEventId = nullEventId;
	}

	public String getResultAttributeName() {
		return resultAttributeName;
	}

	public void setResultAttributeName(String resultAttributeName) {
		this.resultAttributeName = resultAttributeName;
	}

	public String getExceptionAttributeName() {
		return exceptionAttributeName;
	}

	public void setExceptionAttributeName(String exceptionAttributeName) {
		this.exceptionAttributeName = exceptionAttributeName;
	}

	/**
	 * Returns a "success" event.
	 * @source the source of the event
	 */
	public Event success(Object source) {
		return event(source, getSuccessEventId());
	}

	/**
	 * Returns a "success" event with the provided result object as an
	 * attribute. The result object is identified by the parameter name
	 * {@link #getResultAttributeName()}.
	 * @source the source of the event
	 * @param result the action success result
	 */
	public Event success(Object source, Object result) {
		return event(source, getSuccessEventId(), getResultAttributeName(), result);
	}

	/**
	 * Returns an "error" event.
	 * @source the source of the event
	 */
	public Event error(Object source) {
		return event(source, getErrorEventId());
	}

	/**
	 * Returns an "error" event caused by the provided exception.
	 * @source the source of the event
	 * @param e the exception that caused the error event, to be put as an
	 * event attribute under the name {@link #getExceptionAttributeName()}
	 */
	public Event error(Object source, Exception e) {
		return event(source, getErrorEventId(), getExceptionAttributeName(), e);
	}

	/**
	 * Returns a "yes" event.
	 * @source the source of the event
	 */
	public Event yes(Object source) {
		return event(source, getYesEventId());
	}

	/**
	 * Returns a "no" result event.
	 * @source the source of the event
	 */
	public Event no(Object source) {
		return event(source, getNoEventId());
	}

	/**
	 * Returns an event to communicate an occurrence of a boolean expression.
	 * @source the source of the event
	 * @param booleanResult the boolean
	 * @return yes or no
	 */
	public Event event(Object source, boolean booleanResult) {
		if (booleanResult) {
			return yes(source);
		}
		else {
			return no(source);
		}
	}

	/**
	 * Returns a event with the specified identifier.
	 * @source the source of the event
	 * @param eventId the result event identifier
	 * @return the event
	 */
	public Event event(Object source, String eventId) {
		return new Event(source, eventId);
	}

	/**
	 * Returns a event with the specified identifier and the specified set of
	 * attributes.
	 * @source the source of the event
	 * @param eventId the result event identifier
	 * @param attributes the event attributes
	 * @return the action result event
	 */
	public Event event(Object source, String eventId, AttributeCollection attributes) {
		return new Event(source, eventId, attributes);
	}

	/**
	 * Returns a result event for this action with the specified identifier and
	 * a single parameter.
	 * @param eventId the result id
	 * @param attributeName the parameter name
	 * @param attributeValue the parameter value
	 * @return the action result event
	 */
	public Event event(Object source, String eventId, String attributeName, Object attributeValue) {
		AttributeMap attributes = new AttributeMap(1, 1);
		attributes.put(attributeName, attributeValue);
		return new Event(source, eventId, attributes);
	}
}