package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

import org.springframework.util.Assert;

/**
 * Represents an active, ongoing conversation between a user and the Spring Web
 * Flow system.
 * @author Keith Donald
 */
public class Conversation implements Serializable {

	private static final long serialVersionUID = 3532014613434083365L;

	/**
	 * A stack of conversation continuations. Each continuation represents a
	 * restorable snapshot of this conversation at a point in time relevant to
	 * the user.
	 */
	private LinkedList continuations = new LinkedList();

	/**
	 * The maximum number of continuations that can be created for this
	 * conversation.
	 */
	private int maxContinuations;

	/**
	 * Creates a new object representing a logical conversation between a
	 * browser and Spring Web Flow. The new conversation initially has no
	 * continuations associated with it, call
	 * {@link #addContinuation(FlowExecutionContinuation)} to add them.
	 * @param maxContinuations the maximum number of continuations allowed for
	 * this conversation.
	 */
	public Conversation(int maxContinuations) {
		Assert.isTrue(maxContinuations > 0, "'maxContinuations' must be greater than 0");
		this.maxContinuations = maxContinuations;
	}

	/**
	 * Returns the conversation continuation with the provided <code>id</code>,
	 * or <code>null</code> if no such continuation exists with that id.
	 * @param id the continuation id
	 * @return the continuation
	 */
	public FlowExecutionContinuation getContinuation(Serializable id) {
		ListIterator it = continuations.listIterator(continuations.size());
		while (it.hasPrevious()) {
			FlowExecutionContinuation continuation = (FlowExecutionContinuation)it.previous();
			if (continuation.getId().equals(id)) {
				return continuation;
			}
		}
		return null;
	}

	/**
	 * Adds the continuation to this repository. May result in the oldest
	 * continuation stored in this repository being invalidated if the
	 * {@link #maxContinuations} property is exceeded.
	 * @param continuation
	 */
	public void addContinuation(FlowExecutionContinuation continuation) {
		continuations.add(continuation);
		// remove the first continuation if them maximium number of continuations has been reached
		if (continuations.size() > maxContinuations) {
			continuations.removeFirst();
		}
	}

	/**
	 * Returns the count of continuations in this repository.
	 */
	public int getContinuationCount() {
		return continuations.size();
	}
}