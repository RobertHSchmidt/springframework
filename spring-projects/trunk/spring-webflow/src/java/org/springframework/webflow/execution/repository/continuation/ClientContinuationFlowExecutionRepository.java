package org.springframework.webflow.execution.repository.continuation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.repository.FlowExecutionContinuationKey;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.util.RandomGuidUidGenerator;
import org.springframework.webflow.util.UidGenerator;

/**
 * A flow execution repository implementation that uses no server-side state.
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

	public FlowExecutionContinuationFactory getContinuationFactory() {
		return continuationFactory;
	}

	public void setContinuationFactory(FlowExecutionContinuationFactory continuationFactory) {
		this.continuationFactory = continuationFactory;
	}

	public UidGenerator getUidGenerator() {
		return uidGenerator;
	}

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
		
	}

	public void invalidateConversation(Serializable conversationId) {

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