/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.execution.scope;

import org.springframework.batch.repeat.RepeatContext;
import org.springframework.core.AttributeAccessor;

/**
 * Marker interface for beans to be injected with a {@link RepeatContext}.
 * Useful for business logic implementations that want to store some state in
 * the context, to communicate between iterations, or with an enclosing
 * interceptor.
 * 
 * @author Dave Syer
 * 
 */
public interface StepContextAware {

	/**
	 * Callback for injection of {@link RepeatContext}.
	 * 
	 * @param context the current context supplied by framework.
	 */
	void setStepScopeContext(AttributeAccessor context);
}
