package org.springframework.webflow.execution.repository;

import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;

/**
 * A repository for storing managed flow executions. Flow execution repositories
 * are responsible for managing the storage and restoration of ongoing
 * conversations between clients and the Spring Web Flow system.
 * <p>
 * When placed in a repository, a {@link FlowExecution} object representing the
 * state of a conversation at a point in time is indexed under a unique
 * {@link FlowExecutionContinuationKey}. This key provides enough information
 * to track a single active user conversation with the server, as well as
 * provide an index into one or more snapshots taken at points in time relative
 * to the user during conversation execution.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionRepository {

	/**
	 * Generate a unique flow execution continuation key to be used as an index
	 * into a new flow execution representing the start of a new user
	 * conversation in this repository. Both the conversationId and
	 * continuationId key parts are guaranteed to be unique.
	 * @return the continuation key
	 * @throws FlowExecutionStorageException a problem occured generating the
	 * key
	 */
	public FlowExecutionContinuationKey generateContinuationKey(FlowExecution flowExecution)
			throws FlowExecutionRepositoryException;

	/**
	 * Generate a unique flow execution continuation key to be used as an index
	 * into a new flow execution continuation to be associated with an existing
	 * user conversation managed in this repository. The returned key consists
	 * of the provided conversationId provided and a new, unique continuationId.
	 * @return the continuation key
	 * @throws FlowExecutionStorageException a problem occured generating the
	 * key
	 */
	public FlowExecutionContinuationKey generateContinuationKey(FlowExecution flowExecution, Serializable conversationId)
			throws FlowExecutionRepositoryException;

	/**
	 * Return the <code>FlowExecution</code> indexed by the provided
	 * continuation key. This flow execution represents the state of a user
	 * conversation at a point in time relavent to the user.
	 * @param continuationKey the continuation key
	 * @return the flow execution
	 * @throws FlowExecutionStorageException if no flow execution was indexed
	 * with the key provided
	 */
	public FlowExecution getFlowExecution(FlowExecutionContinuationKey continuationKey)
			throws FlowExecutionRepositoryException;

	/**
	 * Place the provided <code>FlowExecution</code> in this repository,
	 * indexed under the provided continuation key.
	 * <p>
	 * If this flow execution represents the start of a new conversation, a new
	 * conversation will be created and tracked. If this flow execution
	 * represents a change in the state of an existing, still-ongoing
	 * conversation, a continuation will be created and tracked.
	 * @param continuationKey the continuation key
	 * @param flowExecution the flow execution
	 * @throws FlowExecutionStorageException the flow execution could not be
	 * stored
	 */
	public void putFlowExecution(FlowExecutionContinuationKey continuationKey, FlowExecution flowExecution)
			throws FlowExecutionRepositoryException;

	/**
	 * Invalidate the executing conversation with the specified id. This method
	 * will remove all data associated with the conversation, including any
	 * managed continuations. Any future clients that reference this
	 * conversation in a flow execution continuation key will be thrown a
	 * FlowExecutionStorageException on any access attempt.
	 * @param conversationId the conversationId
	 * @throws FlowExecutionStorageException the conversation could not be
	 * invalidated
	 */
	public void invalidateConversation(Serializable conversationId) throws FlowExecutionRepositoryException;
}