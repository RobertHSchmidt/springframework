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

import org.springframework.webflow.Event;

/**
 * A event that is the union of multiple event objects. The parameter maps of
 * those event objects are merged into one map.
 * @author Keith Donald
 */
public class CompositeEvent extends Event {
	
	/**
	 * Creates a composite event.
	 * @param source the source
	 * @param id the event id
	 * @param events the individual events
	 */
	public CompositeEvent(Object source, String id, Event[] events) {
		super(source, id);
		for (int i = 0; i < events.length; i++) {
			addParameters(events[i].getParameters());
		}
	}
}
