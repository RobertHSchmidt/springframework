package org.springframework.webflow.execution;

import java.util.List;

import org.springframework.webflow.Event;
import org.springframework.webflow.State;
import org.springframework.webflow.Transition;

public class FlowSessionHistory {

	/**
	 * The state the flow session started in.
	 */
	private State startState;

	/**
	 * A list of state transition history: the key is the event that triggered a
	 * state transition, the value is the transition itself
	 */
	private List eventDrivenStateTransitions;

	/**
	 * A list of state transition history: the key is the event that triggered a
	 * state transition, the value is the transition itself
	 */
	private List navigationHistory;
	
	public FlowSessionHistory(State startState) {
		this.startState = startState;
	}

	public static class EventDrivenStateTransition {

		/**
		 * The event that triggered a state transition.
		 */
		private Event event;

		/**
		 * The transition that was executed.
		 */
		private Transition transition;

		/**
		 * Creates a 
		 * @param event the event that occured
		 * @param transition the transition that was executed as a result
		 */
		public EventDrivenStateTransition(Event event, Transition transition) {
			this.event = event;
			this.transition = transition;
		}

		/**
		 * Returns the event that occured that drove a state transition.
		 */
		public Event getEvent() {
			return event;
		}

		/**
		 * Returns the transition that was executed on the occurence of the
		 * event.
		 */
		public Transition getTransition() {
			return transition;
		}
	}
}
