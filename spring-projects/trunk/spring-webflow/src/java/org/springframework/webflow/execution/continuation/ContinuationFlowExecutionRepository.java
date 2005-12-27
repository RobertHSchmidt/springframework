package org.springframework.webflow.execution.continuation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowExecutionRepository;
import org.springframework.webflow.execution.NoSuchFlowExecutionException;
import org.springframework.webflow.util.KeyGenerator;
import org.springframework.webflow.util.RandomGuidKeyGenerator;

public class ContinuationFlowExecutionRepository implements FlowExecutionRepository, Serializable {

	private static final long serialVersionUID = -602931852676786766L;

	private Map conversations = new HashMap();

	private FlowExecutionContinuationFactory continuationFactory = new SerializingFlowExecutionContinuationFactory();

	private KeyGenerator keyGenerator = new RandomGuidKeyGenerator();

	public FlowExecutionContinuationFactory getContinuationFactory() {
		return continuationFactory;
	}

	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		this.continuationFactory = continuationFactory;
	}

	public KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(KeyGenerator keyGenerator) {
		Assert.isInstanceOf(Serializable.class, "Must be serializable: ");
		this.keyGenerator = keyGenerator;
	}

	public FlowExecutionKey generateKey() {
		return new FlowExecutionKey(keyGenerator.generate(), keyGenerator.generate());
	}

	public FlowExecutionKey generateContinuationKey(Serializable conversationId) {
		return new FlowExecutionKey(conversationId, keyGenerator.generate());
	}

	public FlowExecution getFlowExecution(FlowExecutionKey key) {
		return getContinuation(key).getFlowExecution();
	}

	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution) {
		Conversation conversation = (Conversation)conversations.get(key.getConversationId());
		if (conversation != null) {
			conversation.addContinuation(continuationFactory.createContinuation(key, flowExecution));
		}
		else {
			conversation = new Conversation();
			conversation.addContinuation(continuationFactory.createContinuation(key, flowExecution));
			conversations.put(key.getConversationId(), conversation);
		}
	}

	public void remove(FlowExecutionKey key) {
		conversations.remove(key.getConversationId());
	}

	private FlowExecutionContinuation getContinuation(FlowExecutionKey key) throws NoSuchFlowExecutionException {
		Conversation conversation = getConversation(key.getConversationId());
		if (conversation == null) {
			throw new NoSuchFlowExecutionException(key);
		}
		FlowExecutionContinuation continuation = conversation.getContinuation(key.getContinuationId());
		if (continuation == null) {
			throw new NoSuchFlowExecutionException(key);
		}
		return continuation;
	}

	private Conversation getConversation(Serializable conversationId) {
		return (Conversation)conversations.get(conversationId);
	}
}