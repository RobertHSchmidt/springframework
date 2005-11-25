/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.execution;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

import org.springframework.webflow.ExternalContext;

/**
 * Flow execution storage that stores flow executions as <i>continuations</i>
 * in an externally managed data store.
 * <p>
 * A downside of this storage strategy (and of server-side continuations in
 * general) is that there could be many copies of the flow execution stored in
 * the data store, increasing memory/storage requirements. On this basis it is
 * possibly not suited for use with untrusted clients.
 * 
 * @author Erwin Vervaet
 * @author Colin Sampaleanu
 * 
 * @see org.springframework.webflow.execution.ClientContinuationFlowExecutionStorage
 */
public class ContinuationDataStoreFlowExecutionStorage extends DataStoreFlowExecutionStorage {

	/**
	 * Flag to turn on/off continuation compression.
	 */
	private boolean compress = false;

	/**
	 * Creates a continuation-driven data store flow execution storage.
	 */
	public ContinuationDataStoreFlowExecutionStorage() {
		super();
	}

	/**
	 * Creates a continuation-driven data store flow execution storage.
	 * @param dataStoreAccessor the data store accessor.
	 */
	public ContinuationDataStoreFlowExecutionStorage(DataStoreAccessor dataStoreAccessor) {
		super(dataStoreAccessor);
	}

	/**
	 * Returns whether or not continuations should be compressed.
	 */
	public boolean isCompress() {
		return compress;
	}

	/**
	 * Set whether or not continuations should be compressed.
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	protected FlowExecution getFlowExecution(Serializable id, ExternalContext context)
			throws FlowExecutionStorageException {
		try {
			FlowExecutionContinuation continuation = (FlowExecutionContinuation)getDataStore(context).getAttribute(
					attributeName(id));
			return continuation.readFlowExecution();
		}
		catch (IOException e) {
			throw new FlowExecutionSerializationException(this, id, null,
					"IOException thrown loading the flow execution continuation: this should not happen!", e);
		}
		catch (ClassNotFoundException e) {
			throw new FlowExecutionSerializationException(this, id, null,
					"ClassNotFoundException thrown loading the flow execution continuation:  "
							+ "This should not happen! Make sure there are no classloader issues."
							+ "For example, perhaps the Web Flow system is being loaded by a classloader "
							+ "that is a parent of the classloader loading application classes?", e);
		}
	}

	/**
	 * Associate given id with given attribute value in the data store.
	 */
	protected void setFlowExecution(Serializable id, FlowExecution flowExecution, ExternalContext context)
			throws FlowExecutionStorageException {
		try {
			getDataStore(context).setAttribute(attributeName(id),
					new FlowExecutionContinuation(flowExecution, compress));
		}
		catch (NotSerializableException e) {
			throw new FlowExecutionSerializationException(this, id, flowExecution,
					"Could not serialize flow execution '" + id + "'.  "
							+ "Make sure all objects stored in flow scope are serializable!", e);
		}
		catch (IOException e) {
			throw new FlowExecutionSerializationException(this, id, flowExecution,
					"IOException loading the flow execution continuation -- this should not happen!", e);
		}
	}

	public Serializable generateId(Serializable previousId) {
		// generate a new id for each continuation
		return createId();
	}

	public void remove(Serializable id, ExternalContext context) throws FlowExecutionStorageException {
		// nothing to do - note that we should not remove the identified flow
		// execution continuation because that id actually identifies the
		// 'previous' flow execution, not the one that has ended (because the
		// ended snapshot is never saved so doesn't even have an id!)
	}
}