package org.springframework.webflow.execution.continuation;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;

/**
 * A factory that creates new instances of flow execution continuations based on
 * standard java serialization.
 * 
 * @author Keith Donald
 */
public class SerializedFlowExecutionContinuationFactory implements FlowExecutionContinuationFactory, Serializable {

	/**
	 * Flag to turn on/off continuation compression.
	 */
	private boolean compress = false;

	/**
	 * Returns whether or not continuations should be compressed.
	 */
	public boolean getCompress() {
		return compress;
	}

	/**
	 * Set whether or not continuations should be compressed.
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public FlowExecutionContinuation createContinuation(Serializable continuationId, FlowExecution flowExecution) {
		try {
			return new SerializedFlowExecutionContinuation(continuationId, new FlowExecutionByteArray(flowExecution,
					getCompress()));
		}
		catch (NotSerializableException e) {
			throw new FlowExecutionSerializationException(continuationId, flowExecution,
					"Could not serialize flow execution; make sure all objects stored in flow scope are serializable",
					e);
		}
		catch (IOException e) {
			throw new FlowExecutionSerializationException(continuationId, flowExecution,
					"IOException thrown serializing flow execution -- this should not happen!", e);
		}
	}
}