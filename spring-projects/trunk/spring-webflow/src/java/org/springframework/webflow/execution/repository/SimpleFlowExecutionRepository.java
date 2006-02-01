package org.springframework.webflow.execution.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;

/**
 * A simple flow execution repository implementation that stores single
 * instances of flow executions representing stateful user conversations in map
 * entries structured in the following manner:
 * <p>
 * <ul>
 * <li>Each entry key is an assigned conversationId, uniquely identifying an
 * ongoing conversation between a client and the Spring Web Flow system in this
 * repository.
 * <li>Each entry value is a {@link FlowExecutionEntry} object, consisting of a
 * {@link FlowExecution} representing the state and behavior of a conversation
 * plus a <code>continuationId</code> acting as a token required for accessing
 * that conversation.
 * </ul>
 * <p>
 * It is important to note use of this repository <b>does not</b> allow for
 * duplicate submission in conjunction with browser navigational buttons (such
 * as the back button). Specifically, if you attempt to "go back" and resubmit,
 * the continuation id stored on the page in your browser history will <b>not</b>
 * match the continuation id of the {@link FlowExecutionEntry} object and access
 * to the conversation will be disallowed. This is because the
 * <code>continuationId</code> changes on each request to consistently prevent
 * the possibility of duplicate submission.
 * <p>
 * This repository is specifically designed to be 'simple': incurring minimal
 * resources and overhead, as only one {@link FlowExecution} is stored <i>per
 * user conversation</i>. This repository implementation should only be used
 * when you do not have to support browser navigational button use, e.g. you
 * lock down the browser and require that all navigational events to be routed
 * explicitly through Spring Web Flow.
 * 
 * @author Keith Donald
 */
public class SimpleFlowExecutionRepository extends AbstractFlowExecutionRepository implements Serializable {

	private static final long serialVersionUID = -8138465280185005691L;

	/**
	 * The map of flow execution entries in this repository.
	 */
	private Map flowExecutionEntries = new HashMap(128);
	
	public SimpleFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices) {
		super(repositoryServices);
	}

	public FlowExecution getFlowExecution(FlowExecutionContinuationKey key) {
		FlowExecutionEntry entry = getFlowExecutionEntry(key.getConversationId());
		// assert that the provided continuationId matches the entry's
		// continuationId
		// if they do not match, access to the conversation is not allowed.
		if (!key.getContinuationId().equals(entry.getId())) {
			throw new InvalidConversationContinuationException(this, key, "The continuation id '"
					+ key.getContinuationId() + "' associated with conversation '" + key.getConversationId()
					+ "' is invalid.  This will happen when accessing browser history "
					+ "(typically via the back button) that references a previously used continuation id, "
					+ "as this simple repository implementation does not support multiple continuations.  "
					+ "Consider using another repository implementation or "
					+ "restrict use of the browser back button.");
		}
		return rehydrate(entry.getFlowExecution());
	}

	private FlowExecutionEntry getFlowExecutionEntry(Serializable conversationId) {
		FlowExecutionEntry entry = (FlowExecutionEntry)flowExecutionEntries.get(conversationId);
		if (entry == null) {
			throw new NoSuchConversationException(this, conversationId);
		}
		return entry;
	}

	public void putFlowExecution(FlowExecutionContinuationKey key, FlowExecution flowExecution) {
		flowExecutionEntries.put(key.getConversationId(),
				new FlowExecutionEntry(key.getContinuationId(), flowExecution));
	}

	public ViewSelection getCurrentViewSelection(Serializable conversationId) throws FlowException {
		return getFlowExecutionEntry(conversationId).getCurrentViewSelection();
	}

	public void setCurrentViewSelection(Serializable conversationId, ViewSelection viewSelection) throws FlowException {
		getFlowExecutionEntry(conversationId).setCurrentViewSelection(viewSelection);
	}

	public void invalidateConversation(Serializable conversationId) {
		flowExecutionEntries.remove(conversationId);
	}

	/**
	 * A holder for a flow execution representing a user conversation with
	 * Spring Web Flow. Is also assigned an <code>id</code> used as a key for
	 * accessing the conversation.
	 * @author Keith Donald
	 */
	protected static class FlowExecutionEntry implements Serializable {

		/**
		 * The key required to continue the conversation.
		 */
		private Serializable id;

		/**
		 * The flow execution representing the state of a conversation.
		 */
		private FlowExecution flowExecution;

		/**
		 * The last (current) view selection made by the conversation.
		 */
		private ViewSelection currentViewSelection;

		public FlowExecutionEntry(Serializable id, FlowExecution flowExecution) {
			this.id = id;
			this.flowExecution = flowExecution;
		}

		public Serializable getId() {
			return id;
		}

		public FlowExecution getFlowExecution() {
			return flowExecution;
		}

		public ViewSelection getCurrentViewSelection() {
			return currentViewSelection;
		}

		public void setCurrentViewSelection(ViewSelection viewSelection) {
			this.currentViewSelection = viewSelection;
		}

		public String toString() {
			return new ToStringCreator(this).append("id", id).append("flowExecution", flowExecution).toString();
		}
	}
}