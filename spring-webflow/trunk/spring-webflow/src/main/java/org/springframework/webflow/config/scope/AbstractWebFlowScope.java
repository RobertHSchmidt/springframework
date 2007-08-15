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
package org.springframework.webflow.config.scope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowExecutionContextHolder;
import org.springframework.webflow.execution.FlowSession;

/**
 * Base class for {@link Scope} implementations that access a Web Flow scope from the current thread-bound
 * {@link FlowExecutionContext} object.
 * <p>
 * Subclasses simply need to implement {@link #getScope()} to return the {@link MutableAttributeMap scope map} to
 * access.
 * <p>
 * Relies on a thread-bound
 * @{link FlowExecutionContext} instance located through the
 * @{link FlowExecutionContextHolder}.
 * 
 * @see FlowExecutionContext
 * @see FlowExecutionContextHolder
 * 
 * @author Ben Hale
 * @since 1.1
 */
public abstract class AbstractWebFlowScope implements Scope {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	public Object get(String name, ObjectFactory objectFactory) {
		MutableAttributeMap scope = getScope();
		Object scopedObject = scope.get(name);
		if (scopedObject == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No scoped instance '" + name + "' found; creating new instance");
			}
			scopedObject = objectFactory.getObject();
			scope.put(name, scopedObject);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning scoped instance '" + name + "'");
			}
		}
		return scopedObject;
	}

	public Object remove(String name) {
		return getScope().remove(name);
	}

	/**
	 * Template method that returns the target scope map.
	 * @return the target scope map
	 * @see FlowExecutionContext#getConversationScope()
	 * @see FlowExecutionContext#getActiveSession()
	 * @see FlowSession#getFlashMap()
	 * @see FlowSession#getScope()
	 * @throws IllegalStateException if the scope could not be accessed
	 */
	protected abstract MutableAttributeMap getScope() throws IllegalStateException;

	/**
	 * Always returns <code>null</code> as most Spring Web Flow scopes do not have obvious conversation ids.
	 * Subclasses should override this method where conversation ids can be intelligently returned.
	 * @return always returns <code>null</code>
	 */
	public String getConversationId() {
		return null;
	}

	/**
	 * Will not register a destruction callback as Spring Web Flow does not support destruction of scoped beans.
	 * Subclasses should override this method where where destruction can adequately be accomplished.
	 * @param name the name of the bean to register the callback for
	 * @param callback the callback to execute
	 */
	public void registerDestructionCallback(String name, Runnable callback) {
		logger.warn("Destruction callback for '" + name + "' was not registered. Spring Web Flow does not "
				+ "support destruction of scoped beans.");
	}

	/**
	 * Returns the current flow execution context. Used by subclasses to easily get access to the thread-bound flow
	 * execution context.
	 * @return the current thread-bound flow execution context
	 * @throws IllegalStateException if the current flow execution context is not bound
	 */
	protected FlowExecutionContext getFlowExecutionContext() throws IllegalStateException {
		return FlowExecutionContextHolder.getFlowExecutionContext();
	}

}