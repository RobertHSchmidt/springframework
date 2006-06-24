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
package org.springframework.webflow.action;

import org.springframework.core.JdkVersion;
import org.springframework.core.enums.LabeledEnum;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.support.EventFactorySupport;

/**
 * Result object-to-event adapter interface that tries to do a 
 * sensible conversion of the result object into a web flow event.
 * It uses the following conversion table:
 * <table border="1">
 * <th>
 * <td>Result object type</td>
 * <td>Event id</td>
 * <td>Remarks</td>
 * </th>
 * <tr>
 * <td>null</td>
 * <td>{@link org.springframework.webflow.support.EventFactorySupport#getNullEventId()}</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>{@link java.lang.Boolean} or boolean</td>
 * <td>{@link org.springframework.webflow.support.EventFactorySupport#getYesEventId()}/
 * {@link org.springframework.webflow.support.EventFactorySupport#getNoEventId()}</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>{@link org.springframework.core.enums.LabeledEnum}</td>
 * <td>{@link org.springframework.core.enums.LabeledEnum#getLabel()}</td>
 * <td>The result object will included in the event as an attribute
 * named "result".</td>
 * </tr>
 * <tr>
 * <td>{@link java.lang.Enum}</td>
 * <td>{@link java.lang.Enum#name()}</td>
 * <td>The result object will included in the event as an attribute
 * named "result".</td>
 * </tr>
 * <tr>
 * <td>{@link java.lang.String}</td>
 * <td>the string</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>{@link org.springframework.webflow.Event}</td>
 * <td>result object</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>All other types</td>
 * <td>toString of result object</td>
 * <td>The result object will included in the event as an attribute
 * named "result".</td>
 * </tr>
 * </table>
 * 
 * @author Keith Donald
 */
public class ResultObjectBasedEventFactory extends EventFactorySupport implements ResultEventFactory {
	
	public Event createResultEvent(Object source, Object resultObject, RequestContext context) {
		if (resultObject == null) {
			return event(source, getNullEventId());
		}
		else if (isBoolean(resultObject.getClass())) {
			return event(source, ((Boolean)resultObject).booleanValue());
		}
		else if (isLabeledEnum(resultObject.getClass())) {
			String resultId = ((LabeledEnum)resultObject).getLabel();
			return event(source, resultId, getResultAttributeName(), resultObject);
		}
		else if (isJdk5Enum(resultObject.getClass())) {
			//java.lang.Enum.toString() return the name!
			return event(source, resultObject.toString(), getResultAttributeName(), resultObject);
		}
		else if (isString(resultObject.getClass())) {
			return event(source, (String)resultObject);
		}
		else if (resultObject instanceof Event) {
			return (Event)resultObject;
		}
		else {
			return event(source, String.valueOf(resultObject), getResultAttributeName(), resultObject);
		}
	}

	/**
	 * Check whether or not given type is mapped to a corresponding
	 * event using special mapping rules.
	 */
	public boolean isMappedValueType(Class type) {
		return isBoolean(type) || isJdk5Enum(type) || isLabeledEnum(type) || isString(type);
	}
	
	// internal helpers to determine the 'type' of a class

	private boolean isBoolean(Class type) {
		return Boolean.class.equals(type) || boolean.class.equals(type);
	}

	private boolean isJdk5Enum(Class type) {
		if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_15) {
			return type.isEnum();
		}
		else {
			return false;
		}
	}

	private boolean isLabeledEnum(Class type) {
		return LabeledEnum.class.isAssignableFrom(type);
	}

	private boolean isString(Class type) {
		return String.class.equals(type);
	}
}