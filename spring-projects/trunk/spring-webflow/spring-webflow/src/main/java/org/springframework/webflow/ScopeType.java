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
package org.springframework.webflow;

import org.springframework.core.enums.StaticLabeledEnum;

/**
 * The core scope types of Spring Web Flow.
 * <p>
 * Defines a type of "bucket" (or map) for storing custom user attributes within
 * a flow execution. Different scope types have different semantics in terms of
 * how long attributes placed in those scope maps remain valid.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class ScopeType extends StaticLabeledEnum {

	/**
	 * Constant indicating request scope. Data in request scope exists for the
	 * life of a request submitted to a flow execution for processing. When that
	 * request ends any data in request scope goes out of scope.
	 */
	public static final ScopeType REQUEST = new ScopeType(0, "Request") {
		public AttributeMap getScope(RequestContext context) {
			return context.getRequestScope();
		}
	};

	/**
	 * Constant indicating flow scope. Data in flow scope is shared by all
	 * artifacts of exactly one flow definition (actions, view, states, etc.)
	 * and lives locally for the life of a executing flow session. When the flow
	 * session ends an data in flow scope goes out of scope.
	 */
	public static final ScopeType FLOW = new ScopeType(1, "Flow") {
		public AttributeMap getScope(RequestContext context) {
			return context.getFlowScope();
		}
	};

	/**
	 * Constant indicating conversation scope. Data in conversation scope is
	 * shared by all flow sessions associated with a flow execution and lives
	 * for the life of the entire flow execution (representing a single logical
	 * conversation). When the overall execution ends, any data in conversation
	 * scope goes out of scope.
	 */
	public static final ScopeType CONVERSATION = new ScopeType(2, "Conversation") {
		public AttributeMap getScope(RequestContext context) {
			return context.getConversationScope();
		}
	};

	/**
	 * Private constructor because this is a typesafe enum!
	 */
	private ScopeType(int code, String label) {
		super(code, label);
	}

	public Class getType() {
		return ScopeType.class;
	}

	/**
	 * Returns the mutable attribute map for this scope type for an executing
	 * flow execution request.
	 * @param context a flow execution request context
	 * @return the scope map, allowing for attributes to be accessed and set
	 */
	public abstract AttributeMap getScope(RequestContext context);
}