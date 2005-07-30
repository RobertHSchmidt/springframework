package org.springframework.webflow.support;

import java.io.Serializable;

import org.springframework.webflow.RequestContext;
import org.springframework.webflow.TransitionCriteria;

/**
 * Transition criteria that negates the result of the evaluation of
 * another criteria object.
 * 
 * @author Keith Donald
 */
public class NotTransitionCriteria implements TransitionCriteria, Serializable {

	private TransitionCriteria criteria;
	
	/**
	 * Create a new transition criteria object that will negate
	 * the result of given criteria object.
	 * @param criteria the criteria to negate 
	 */
	public NotTransitionCriteria(TransitionCriteria criteria) {
		this.criteria = criteria;
	}
	
	public boolean test(RequestContext context) {
		return !criteria.test(context);
	}
	
	public String toString() {
		return "not(" + criteria + ")";
	}
}