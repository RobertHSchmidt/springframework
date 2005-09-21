/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.execution;

import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;

/**
 * A transaction synchronizer that uses a <i>synchronizer token</i> stored in
 * a data store to demarcate application transactions.
 * This implementation stores the token in the data store using a name that
 * is unique for each <i>logical</i> flow execution in that data store. As a
 * result you can use this transaction synchronizer with continuations based
 * flow execution storage strategies, where you have several physical copies
 * of the flow execution for a single logical flow execution.
 * 
 * @see org.springframework.webflow.execution.DataStoreAccessor
 * @see org.springframework.webflow.execution.ContinuationDataStoreFlowExecutionStorage
 * 
 * @author Erwin Vervaet
 */
public class DataStoreTokenTransactionSynchronizer extends AbstractTokenTransactionSynchronizer {

	private DataStoreAccessor dataStoreAccessor;

	private boolean createDataStore = true;

	/**
	 * Create a new token transaction synchronizer storing the token in given data store.
	 * @param dataStoreAccessor the data store accessor to use
	 */
	public DataStoreTokenTransactionSynchronizer(DataStoreAccessor dataStoreAccessor) {
		Assert.notNull(dataStoreAccessor,
				"The data store accessor property is required to load and save the transaction token");
		this.dataStoreAccessor = dataStoreAccessor;
	}

	/**
	 * Returns whether or not the data store should be created if it doesn't already
	 * exist. Defaults to true.
	 */
	public boolean isCreateDataStore() {
		return createDataStore;
	}

	/**
	 * Set whether or not the data store should be created if it doesn't already exist.
	 */
	public void setCreateDataStore(boolean createDataStore) {
		this.createDataStore = createDataStore;
	}

	public String getToken(RequestContext context) {
		return (String)dataStoreAccessor.getDataStore(context.getSourceEvent(), isCreateDataStore()).getAttribute(getTokenName(context));
	}

	public void setToken(RequestContext context, String token) {
		dataStoreAccessor.getDataStore(context.getSourceEvent(), isCreateDataStore()).setAttribute(getTokenName(context), token);
	}

	public void clearToken(RequestContext context) {
		dataStoreAccessor.getDataStore(context.getSourceEvent(), isCreateDataStore()).removeAttribute(getTokenName(context));
	}
	
	// subclassing hooks
	
	/**
	 * Generate a pseudo unique token name based on the information available in
	 * given request context. The generated name is unique among all flow executions
	 * in the data store. As a result, you can have several concurrent
	 * flow executions in the same data store, all with their own transaction.
	 * @param context the flow execution request context
	 * @return the generated token name
	 */
	protected String getTokenName(RequestContext context) {
		StringBuffer tokenName = new StringBuffer();
		tokenName.append(getTransactionTokenAttributeName()).append("_");
		// use the flow execution key to uniquely identify this flow execution
		// among all other flow executions in the same data store
		// note that the key always remains the same, even if
		// the flow execution gets cloned, e.g. when using continuations, so
		// it identifies the 'logical' flow execution
		tokenName.append(context.getFlowExecutionContext().getKey());
		return tokenName.toString();
	}	
}