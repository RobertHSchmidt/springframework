package org.springframework.webflow.execution.repository.continuation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;
import org.springframework.webflow.FlowException;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * A flow execution repository implementation that uses no server-side state.
 * <p>
 * Specifically, instead of putting {@link FlowExecution} objects in a
 * server-side store, this repository <i>encodes</i> them directly into the
 * <code>continuationId</code> of a generated
 * {@link FlowExecutionContinuationKey}. When asked to load a flow execution by
 * its key, this repository decodes the serialized <code>continuationId</code>,
 * restoring the {@link FlowExecution} object at the state it was when it was
 * encoded.
 * <p>
 * Note: currently this repository implementation does not support
 * <i>conversation invalidation after completion</i>, which enables automatic
 * prevention of duplicate submission after a conversation is completed (note
 * the {@link #invalidateConversation(Serializable)} method is a NoOp). Support
 * for this requires tracking active <code>conversationIds</code> using some
 * centralized storage medium like a database table. This implementation will be
 * likely enhanced in future releases to provide this capability.
 * 
 * @author Keith Donald
 */
public class ClientContinuationFlowExecutionRepository implements FlowExecutionRepository {

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
	 * Returns the continuation factory.
	 */
	public FlowExecutionContinuationFactory getContinuationFactory() {
		return continuationFactory;
	}

	/**
	 * Sets the continuation factory.
	 */
	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		this.continuationFactory = continuationFactory;
	}

	/**
	 * Returns the uid generator that generates unique identifiers for this
	 * repository.
	 */
	public UidGenerator getUidGenerator() {
		return uidGenerator;
	}

	/**
	 * Sets the uid generator that generates unique identifiers for this
	 * repository.
	 */
	public void setUidGenerator(UidGenerator uidGenerator) {
		this.uidGenerator = uidGenerator;
	}

	public FlowExecutionContinuationKey generateContinuationKey(FlowExecution flowExecution) {
		return new FlowExecutionContinuationKey(uidGenerator.generateId(), encode(uidGenerator.generateId(),
				flowExecution));
	}

	public FlowExecutionContinuationKey generateContinuationKey(FlowExecution flowExecution, Serializable conversationId) {
		return new FlowExecutionContinuationKey(conversationId, encode(uidGenerator.generateId(), flowExecution));
	}

	public FlowExecution getFlowExecution(FlowExecutionContinuationKey key) {
		return decode(key.getContinuationId()).getFlowExecution();
	}

	public void putFlowExecution(FlowExecutionContinuationKey key, FlowExecution flowExecution) {
		// nothing to do by default
	}

	public ViewSelection getCurrentViewSelection(Serializable conversationId) throws FlowException {
		// nothing to do by default
		return null;
	}

	public void setCurrentViewSelection(Serializable conversationId, ViewSelection viewSelection) throws FlowException {
		// nothing to do by default
	}

	public void invalidateConversation(Serializable conversationId) {
		// nothing to do by dfault
	}

	/**
	 * Encode given flow execution object into data that can be stored on the
	 * client.
	 * <p>
	 * Subclasses can override this to change the encoding algorithm. This class
	 * just does a BASE64 encoding of the serialized flow execution.
	 * @param flowExecution the flow execution instance
	 * @return the encoded representation
	 */
	protected Serializable encode(Serializable continuationId, FlowExecution flowExecution) {
		FlowExecutionContinuation continuation = continuationFactory.createContinuation(continuationId, flowExecution);
		return new String(Base64.encodeBase64(continuation.toByteArray()));
	}

	/**
	 * Decode given data, received from the client, and return the corresponding
	 * flow execution object.
	 * <p>
	 * Subclasses can override this to change the decoding algorithm. This class
	 * just does a BASE64 decoding and then deserializes the flow execution.
	 * @param data the encode flow execution data
	 * @return the decoded flow execution instance
	 */
	protected FlowExecutionContinuation decode(Serializable data) {
		Assert.notNull(data, "The flow execution data to decode cannot be null");
		byte[] bytes = Base64.decodeBase64(String.valueOf(data).getBytes());
		try {
			return deserializeContinuation(bytes);
		}
		catch (IOException e) {
			throw new FlowExecutionContinuationDeserializationException("This should not happen", e);
		}
		catch (ClassNotFoundException e) {
			throw new FlowExecutionContinuationDeserializationException("This should not happen", e);
		}
	}

	private FlowExecutionContinuation deserializeContinuation(byte[] bytes) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
		try {
			return (FlowExecutionContinuation)ois.readObject();
		}
		finally {
			ois.close();
		}
	}

	private static class FlowExecutionContinuationDeserializationException extends NestedRuntimeException {
		public FlowExecutionContinuationDeserializationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}