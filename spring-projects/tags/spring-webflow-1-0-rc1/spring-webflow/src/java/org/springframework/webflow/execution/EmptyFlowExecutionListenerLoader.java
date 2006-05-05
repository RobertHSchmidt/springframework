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
package org.springframework.webflow.execution;

import org.springframework.webflow.Flow;

/**
 * A flow execution listener loader that simply returns an empty listener array
 * on each invocation.
 * @author Keith Donald
 */
public class EmptyFlowExecutionListenerLoader implements FlowExecutionListenerLoader {

	/**
	 * Canonical instance of a empty flow execution listener array.
	 */
	private static final FlowExecutionListener[] EMPTY_LISTENER_ARRAY = new FlowExecutionListener[0];

	public FlowExecutionListener[] getListeners(Flow flow) {
		return EMPTY_LISTENER_ARRAY;
	}
}