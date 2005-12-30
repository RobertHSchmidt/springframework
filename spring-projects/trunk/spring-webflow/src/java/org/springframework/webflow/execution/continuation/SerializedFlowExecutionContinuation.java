package org.springframework.webflow.execution.continuation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecution;

/**
 * A continuation implementation that is based on standard java serialization.
 * @author Keith Donald
 */
public class SerializedFlowExecutionContinuation extends AbstractFlowExecutionContinuation {

	private static final long serialVersionUID = 1026250005686020025L;

	/**
	 * The serialized flow execution byte array.
	 */
	private FlowExecutionByteArray byteArray;

	public SerializedFlowExecutionContinuation(Serializable id, FlowExecutionByteArray byteArray) {
		super(id);
		Assert.notNull(byteArray, "The flow execution byte array is required");
		this.byteArray = byteArray;
	}

	public FlowExecution getFlowExecution() {
		try {
			return byteArray.deserializeFlowExecution();
		}
		catch (IOException e) {
			throw new FlowExecutionDeserializationException(
					getId(),
					"IOException thrown deserializing the flow execution stored in this continuation -- this should not happen!",
					e);
		}
		catch (ClassNotFoundException e) {
			throw new FlowExecutionDeserializationException(getId(),
					"ClassNotFoundException thrown deserializing the flow execution stored in this continuation -- "
							+ "This should not happen! Make sure there are no classloader issues."
							+ "For example, perhaps the Web Flow system is being loaded by a classloader "
							+ "that is a parent of the classloader loading application classes?", e);
		}
	}

	public byte[] toByteArray() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(byteArray.getData().length + 128);
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				oos.writeObject(this);
				oos.flush();
			}
			finally {
				oos.close();
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	protected static class FlowExecutionDeserializationException extends NestedRuntimeException {
		private Serializable continuationId;

		public FlowExecutionDeserializationException(Serializable continuationId, String message, Throwable cause) {
			super(message, cause);
			this.continuationId = continuationId;
		}

		public Serializable getContinuationId() {
			return continuationId;
		}
	}
}