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
package org.springframework.webflow.execution.continuation;

import org.springframework.webflow.execution.RepositoryFlowExecutionStorage;

/**
 * A subclass of
 * {@link RepositoryFlowExecutionStorage} that simply uses a
 * {@link ContinuationFlowExecutionRepository continuation-based flow execution repository factory}
 * by default.
 * <p>
 * This is a convenience implementation that makes it easy to use a server-side
 * continuation-based flow execution storage strategy with a
 * {@link org.springframework.webflow.execution.FlowExecutionManager}.
 * 
 * @see ContinuationFlowExecutionRepositoryFactory
 * 
 * @author Keith Donald
 */
public class ContinuationRepositoryFlowExecutionStorage extends RepositoryFlowExecutionStorage {
	public ContinuationRepositoryFlowExecutionStorage() {
		setRepositoryFactory(new ContinuationFlowExecutionRepositoryFactory());
	}
}