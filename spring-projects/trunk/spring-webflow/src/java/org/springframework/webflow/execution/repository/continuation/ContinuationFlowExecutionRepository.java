package org.springframework.webflow.execution.repository.continuation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.execution.repository.InvalidConversationContinuationException;
import org.springframework.webflow.execution.repository.NoSuchConversationException;
import org.springframework.webflow.execution.repository.SimpleFlowExecutionRepository;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * A flow execution repository implementation that stores instances of flow
 * executions representing stateful user conversations in a map structured in
 * the following manner:
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
public class ContinuationFlowExecutionRepository implements FlowExecutionRepository, Serializable {

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
	private FlowExecutionContinuationFactory continuationFactory = new SerializedFlowExecutionContinuationFactory();

	/**
	 * The uid generation strategy used to generate unique conversation and
	 * continuation identifiers.
	 */
	private UidGenerator uidGenerator = new RandomGuidUidGenerator();

	/**
	 * The maximum number of continuations that can be active per conversation.
	 */
	private int maxContinuations = 25;

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
	 * Returns the uid generator that generates unique identifiers for entries
	 * placed into this repository.
	 */
	public UidGenerator getUidGenerator() {
		return uidGenerator;
	}

	/**
	 * Sets the uid generator that generates unique identifiers for entries
	 * placed into this repository.
	 */
	public void setUidGenerator(UidGenerator uidGenerator) {
		this.uidGenerator = uidGenerator;
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

	public FlowExecutionContinuationKey generateContinuationKey(FlowExecution flowExecution) {
		return new FlowExecutionContinuationKey(uidGenerator.generateId(), uidGenerator.generateId());
	}

	public FlowExecutionContinuationKey generateContinuationKey(FlowExecution flowExecution, Serializable conversationId) {
		return new FlowExecutionContinuationKey(conversationId, uidGenerator.generateId());
	}

	public FlowExecution getFlowExecution(FlowExecutionContinuationKey key) {
		return getConversationContinuation(key).getFlowExecution();
	}

	public void putFlowExecution(FlowExecutionContinuationKey key, FlowExecution flowExecution) {
		Conversation conversation = (Conversation)getOrCreateConversation(key.getConversationId());
		conversation.addContinuation(continuationFactory.createContinuation(key.getContinuationId(), flowExecution));
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