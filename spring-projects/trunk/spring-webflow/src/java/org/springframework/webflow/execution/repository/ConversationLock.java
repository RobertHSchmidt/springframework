package org.springframework.webflow.execution.repository;

/**
 * A interface allowing for a thread to obtain exclusive rights to a
 * conversation. Allows for preventing concurrency conflicts; for example, when
 * multiple requests from the same client session come in back-to-back.
 * 
 * @author Keith Donald
 */
public interface ConversationLock {

	/**
	 * Acquire the lock on this conversation.
	 * @throws FlowExecutionRepositoryException an exception occured acquiring
	 * the lock
	 */
	public void lock() throws FlowExecutionRepositoryException;

	/**
	 * Release the lock on this conversation.
	 * @throws FlowExecutionRepositoryException an exception occured releasing
	 * the lock
	 */
	public void unlock() throws FlowExecutionRepositoryException;
}
