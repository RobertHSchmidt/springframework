/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.execution.repository.support;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecution;

/**
 * A support strategy used by repositories that serialize flow executions to
 * restore transient execution state after deserialization.
 * 
 * @author Keith Donald
 */
public interface FlowExecutionStateRestorer {

	/**
	 * Restore the transient state of the flow execution.
	 * @param flowExecution the (potentially deserialized) flow execution
	 * @param conversationScope the execution's conversation scope, which is
	 * typically not part of the serialized form since it could be shared
	 * by multiple physical flow execution <i>copies</i> all sharing the
	 * same logical conversation
	 * @return the restored flow execution
	 */
	public FlowExecution restoreState(FlowExecution flowExecution, MutableAttributeMap conversationScope);
}