package org.springframework.webflow.execution;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.util.KeyGenerator;
import org.springframework.webflow.util.RandomGuidKeyGenerator;

public class MapFlowExecutionRepository implements FlowExecutionRepository, Serializable {

	private static final long serialVersionUID = -8138465280185005691L;

	/**
	 * The map of flow execution entries in this repository.
	 */
	private Map flowExecutionEntries = new HashMap(128);

	/**
	 * The Flow Execution storage key generation strategy.
	 */
	private KeyGenerator keyGenerator = new RandomGuidKeyGenerator();

	/**
	 * Returns the flow execution key generation strategy in use.
	 */
	protected KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	/**
	 * Sets the flow execution storage key generation strategy to use.
	 */
	public void setKeyGenerator(KeyGenerator keyGenerator) {
		Assert.isInstanceOf(Serializable.class, "Must be serializable: ");
		this.keyGenerator = keyGenerator;
	}

	public FlowExecutionKey generateKey() {
		return new FlowExecutionKey(keyGenerator.generate(), keyGenerator.generate());
	}

	public FlowExecutionKey generateContinuationKey(Serializable conversationId) {
		return new FlowExecutionKey(conversationId , keyGenerator.generate());
	}

	public FlowExecution getFlowExecution(FlowExecutionKey key) {
		FlowExecutionEntry entry = (FlowExecutionEntry)flowExecutionEntries.get(key.getConversationId());
		if (entry == null) {
			throw new NoSuchFlowExecutionException(key);
		}
		else {
			if (!key.getContinuationId().equals(entry.getId())) {
				throw new NoSuchFlowExecutionException(key);
			}
			return entry.getFlowExecution();
		}
	}

	public void putFlowExecution(FlowExecutionKey key, FlowExecution flowExecution) {
		flowExecutionEntries.put(key.getConversationId(),
				new FlowExecutionEntry(key.getContinuationId(), flowExecution));
	}

	public void remove(FlowExecutionKey key) {
		flowExecutionEntries.remove(key.getConversationId());
	}

	protected static class FlowExecutionEntry implements Serializable {
		private Serializable id;

		private FlowExecution flowExecution;

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

		public String toString() {
			return new ToStringCreator(this).append("id", id).append("flowExecution", flowExecution).toString();
		}
	}
}