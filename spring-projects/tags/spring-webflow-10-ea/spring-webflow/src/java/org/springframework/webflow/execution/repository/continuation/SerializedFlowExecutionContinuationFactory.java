/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.execution.repository.continuation;

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
public class SerializedFlowExecutionContinuationFactory implements FlowExecutionContinuationFactory {

	/**
	 * Flag to turn on/off continuation compression.
	 */
	private boolean compress = false;

	/**
	 * Returns whether or not the continuations should be compressed.
	 */
	public boolean getCompress() {
		return compress;
	}

	/**
	 * Set whether or not the continuations should be compressed.
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