package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.AbstractFlowExecutionRepository;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryServices;
import org.springframework.webflow.execution.repository.InvalidConversationContinuationException;
import org.springframework.webflow.execution.repository.NoSuchConversationException;
import org.springframework.webflow.execution.repository.SimpleFlowExecutionRepository;

/**
 * Stores <i>one to many</i> flow execution continuations per conversation,
 * where each continuation represents a restorable state of an active
 * conversation captured at a point in time.
 * <p>
 * The set of all active conversations are stored in a map structured in the
 * following manner:
 * <p>
 * <ul>
 * <li>Each map entry key is an assigned conversationId, uniquely identifying
 * an ongoing conversation between a client and the Spring Web Flow system.
 * <li>Each map entry value is a {@link Conversation} object, providing the
 * details about an ongoing logical conversation or <i>application transaction</i>
 * between a client and the Spring Web Flow system. Each
 * <code>Conversation</code> maintains a stack of
 * {@link FlowExecutionContinuation} objects, where each continuation represents
 * the state of the conversation at a point in time relevant to the user <i>that
 * can be restored and continued</i>. These continuations can be restored to
 * support users going back in their browser to continue a conversation from a
 * previous point.
 * </ul>
 * <p>
 * It is important to note use of this repository <b>does</b> allow for
 * duplicate submission in conjunction with browser navigational buttons (such
 * as the back button). Specifically, if you attempt to "go back" and resubmit,
 * the continuation id stored on the page in your browser history will match the
 * continuation id of the {@link FlowExecutionContinuation} object and access to
 * the conversation will allowed.
 * <p>
 * This repository implementation also provides support for <i>conversation
 * invalidation after completion</i>, where once a logical {@link Conversation}
 * completes (by one of its FlowExecution's reaching an end state), the entire
 * conversation (including all continuations) is invalidated. This prevents the
 * possibility of duplicate submission after completion.
 * <p>
 * This repository is more elaborate than the
 * {@link SimpleFlowExecutionRepository}, offering more power (by enabling
 * multiple continuations to exist per conversatino), but incurring more
 * overhead. This repository implementation should be considered when you do
 * have to support browser navigational button use, e.g. you cannot lock down
 * the browser and require that all navigational events to be routed explicitly
 * through Spring Web Flow.
 * 
 * @author Keith Donald
 */
public class ContinuationFlowExecutionRepository extends AbstractFlowExecutionRepository implements Serializable {

	private static final long serialVersionUID = -602931852676786766L;

	/**
	 * The conversation map, where each entry key is a conversationId and each
	 * entry value is a {@link Conversation} object.
	 */
	private Map conversations = new HashMap();

	/**
	 * The continuation factory that will be used to create new continuations to
	 * be added to active conversations.
	 */
	private transient FlowExecutionContinuationFactory continuationFactory = new SerializedFlowExecutionContinuationFactory();

	/**
	 * The maximum number of continuations that can be active per conversation.
	 */
	private int maxContinuations = 25;

	/**
	 * Should this repository always use flow execution conversation scope instead of 
	 * continuation scope?
	 */
	private boolean enableConversationScope;

	/**
	 * Creates a new continuation flow execution repository.
	 * @param repositoryServices the repository services holder
	 */
	public ContinuationFlowExecutionRepository(FlowExecutionRepositoryServices repositoryServices) {
		super(repositoryServices);
	}

	/**
	 * Returns the continuation factory that encapsulates the construction of
	 * continuations stored in this repository.
	 */
	public FlowExecutionContinuationFactory getContinuationFactory() {
		return continuationFactory;
	}

	/**
	 * Sets the continuation factory that encapsulates the construction of
	 * continuations stored in this repository.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		this.continuationFactory = continuationFactory;
	}

	/**
	 * Returns the maximum number of continuations allowed per conversation in
	 * this repository.
	 */
	public int getMaxContinuations() {
		return maxContinuations;
	}

	/**
	 * Sets the maximum number of continuations allowed per conversation in this
	 * repository.
	 */
	public void setMaxContinuations(int maxContinuations) {
		this.maxContinuations = maxContinuations;
	}

	public boolean isEnableConversationScope() {
		return enableConversationScope;
	}

	public void setEnableConversationScope(boolean enableConversationScope) {
		this.enableConversationScope = enableConversationScope;
	}

	public FlowExecution getFlowExecution(FlowExecutionContinuationKey key) {
		return rehydrate(getConversationContinuation(key).getFlowExecution());
	}

	public void putFlowExecution(FlowExecutionContinuationKey key, FlowExecution flowExecution) {
		Conversation conversation = (Conversation)getOrCreateConversation(key.getConversationId());
		conversation.addContinuation(continuationFactory.createContinuation(key.getContinuationId(), flowExecution));
	}

	public FlowExecutionContinuationKey getCurrentContinuationKey(String conversationId)
			throws FlowExecutionRepositoryException {
		return new FlowExecutionContinuationKey(conversationId, getConversation(conversationId)
				.getCurrentContinuation().getId());
	}

	public ViewSelection getCurrentViewSelection(Serializable conversationId) throws FlowException {
		return getConversation(conversationId).getCurrentViewSelection();
	}

	public void setCurrentViewSelection(Serializable conversationId, ViewSelection viewSelection) throws FlowException {
		getConversation(conversationId).setCurrentViewSelection(viewSelection);
	}

	public void invalidateConversation(Serializable conversationId) {
		conversations.remove(conversationId);
	}

	private Conversation getOrCreateConversation(Serializable conversationId) {
		Conversation conversation = getConversation(conversationId);
		if (conversation == null) {
			conversation = createConversation();
			conversations.put(conversationId, conversation);
		}
		return conversation;
	}

	/**
	 * Factory method that returns a new conversation. Subclasses may override.
	 */
	protected Conversation createConversation() {
		return new Conversation(maxContinuations);
	}

	private Conversation getConversation(Serializable conversationId) {
		return (Conversation)conversations.get(conversationId);
	}

	private FlowExecutionContinuation getConversationContinuation(FlowExecutionContinuationKey key)
			throws NoSuchConversationException, InvalidConversationContinuationException {
		Conversation conversation = getConversation(key.getConversationId());
		if (conversation == null) {
			throw new NoSuchConversationException(this, key.getConversationId());
		}
		FlowExecutionContinuation continuation = conversation.getContinuation(key.getContinuationId());
		if (continuation == null) {
			throw new InvalidConversationContinuationException(this, key);
		}
		return continuation;
	}
}