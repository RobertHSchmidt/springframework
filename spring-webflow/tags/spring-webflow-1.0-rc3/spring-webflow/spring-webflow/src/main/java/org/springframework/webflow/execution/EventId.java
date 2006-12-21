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
package org.springframework.webflow.execution;

/**
 * A value object describing an event that occured within a flow execution.
 * 
 * @author Keith Donald
 */
public class EventId {
	
	/**
	 * The value of the event identifier.
	 */
	private String value;
	
	/**
	 * Constructs a new event id.
	 * @param value the string id value
	 */
	public EventId(String value) {
		this.value = value;
	}
	
	/**
	 * Returns the raw string event id value.
	 * @return the string id value
	 */
	public String getValue() {
		return value;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof EventId)) {
			return false;
		}
		EventId other = (EventId)o;
		return value.equals(other.value);
	}
	
	public int hashCode() {
		return value.hashCode();
	}
	
	public String toString() {
		return value;
	}
}