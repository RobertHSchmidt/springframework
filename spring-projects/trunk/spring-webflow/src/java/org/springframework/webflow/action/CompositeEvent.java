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
