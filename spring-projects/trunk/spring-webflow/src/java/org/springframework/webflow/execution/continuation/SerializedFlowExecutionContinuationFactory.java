package org.springframework.webflow.execution.continuation;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionContinuationKey;

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

	public FlowExecutionContinuation createContinuation(FlowExecutionContinuationKey key, FlowExecution flowExecution) {
		try {
			return new SerializedFlowExecutionContinuation(key.getContinuationId(), new FlowExecutionByteArray(
					flowExecution, getCompress()));
		}
		catch (NotSerializableException e) {
			throw new FlowExecutionSerializationException(null, flowExecution,
					"Could not encode flow execution--make sure all objects stored in flow scope are serializable!", e);
		}
		catch (IOException e) {
			throw new FlowExecutionSerializationException(null, flowExecution,
					"IOException thrown encoding flow execution -- this should not happen!", e);
		}
	}
}