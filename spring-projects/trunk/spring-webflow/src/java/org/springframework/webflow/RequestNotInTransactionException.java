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
package org.springframework.webflow;

/**
 * An exception indicating a request to resume a flow execution did not occur in
 * the context of the ongoing "application transaction" associated with that
 * flow execution.
 * 
 * @author Keith Donald
 */
public class RequestNotInTransactionException extends StateException {

	/**
	 * Creates a new request not in transaction exception.
	 * @param state the state that's not executing 'in transaction'
	 */
	public RequestNotInTransactionException(State state) {
		super(state, "The current request is not executing in the context of an application transaction");
	}
}