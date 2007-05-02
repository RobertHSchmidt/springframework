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
package org.springframework.webflow.context.scope;

import org.springframework.beans.factory.config.Scope;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowSession;

/**
 * Flow-backed {@link Scope} implementation.
 * 
 * @author Ben Hale
 * @since 1.1
 * @see FlowSession#getScope()
 */
public class FlowScope extends AbstractWebFlowScope {

	protected MutableAttributeMap getScope() {
		return getFlowExecutionContext().getActiveSession().getScope();
	}

}
