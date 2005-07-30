package org.springframework.webflow.support;

import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.TransitionCriteria;

/**
 * Simple transition criteria that matches on an eventId and
 * nothing else. Specifically, if the last event that occured has id
 * ${eventId}, this criteria will return true.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class EventIdTransitionCriteria implements TransitionCriteria, Serializable {

	private String eventId;

	/**
	 * Create a new event id matching criteria object.
	 * @param eventId the event id
	 */
	public EventIdTransitionCriteria(String eventId) {
		Assert.notNull(eventId, "The event id is required");
		this.eventId = eventId;
	}

	public boolean test(RequestContext context) {
		return eventId.equals(context.getLastEvent().getId());
	}

	public String toString() {
		return "'" + eventId + "'";
	}
}