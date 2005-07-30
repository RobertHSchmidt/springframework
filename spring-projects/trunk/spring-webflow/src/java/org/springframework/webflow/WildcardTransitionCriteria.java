package org.springframework.webflow;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Transition criteria that always returns true.
 * 
 * @author Keith Donald
 */
public class WildcardTransitionCriteria implements TransitionCriteria,
		Serializable {

	/**
	 * Event id value ("*") that will cause the transition to match on any
	 * event.
	 */
	public static final String WILDCARD_EVENT_ID = "*";

	/**
	 * Shared instance of a TransitionCriteria that always returns true.
	 */
	public static final WildcardTransitionCriteria INSTANCE = new WildcardTransitionCriteria();

	private WildcardTransitionCriteria() {
		
	}
	
	public boolean test(RequestContext context) {
		return true;
	}

	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}

	public String toString() {
		return WILDCARD_EVENT_ID;
	}
}