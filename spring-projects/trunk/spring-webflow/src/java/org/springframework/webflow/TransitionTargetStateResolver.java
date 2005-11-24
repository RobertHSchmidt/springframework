package org.springframework.webflow;

/**
 * A strategy for calculating the target state of a transition. This facilitates
 * dynamic transition target state resolution that takes into account runtime
 * contextual information.
 * 
 * @author Keith Donald
 */
public interface TransitionTargetStateResolver {

	/**
	 * Resolve the transition's target state in the context of the current
	 * request.
	 * @param transition the transition
	 * @param context the current request context
	 * @return the transition's target state
	 */
	public State resolveTargetState(Transition transition, RequestContext context);
}