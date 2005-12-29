package org.springframework.webflow.execution.continuation;

import java.io.IOException;
import java.io.Serializable;

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
			throw new FlowExecutionSerializationException(getId(), null,
					"IOException thrown decoding flow execution -- this should not happen!", e);
		}
		catch (ClassNotFoundException e) {
			throw new FlowExecutionSerializationException(getId(), null,
					"ClassNotFoundException thrown decoding flow execution -- "
							+ "This should not happen! Make sure there are no classloader issues."
							+ "For example, perhaps the Web Flow system is being loaded by a classloader "
							+ "that is a parent of the classloader loading application classes?", e);
		}
	}
}