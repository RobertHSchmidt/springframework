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
package org.springframework.webflow.action;

import java.lang.reflect.Method;

/**
 * Helper strategy that selects the {@link ResultEventFactory} to use for
 * actions that invoke methods and produce result values.
 * 
 * @author Keith Donald
 */
public class ResultEventFactorySelector {

	/**
	 * The event factory instance for mapping a return value to a success event.
	 */
	private SuccessEventFactory successEventFactory = new SuccessEventFactory();

	/**
	 * The event factory instance for mapping a result object to an event, using
	 * the type of the result object as the mapping criteria.
	 */
	private ResultObjectBasedEventFactory resultObjectBasedEventFactory = new ResultObjectBasedEventFactory();

	/**
	 * Select the appropriate result event factory for attempts to invoke the
	 * method.
	 * @param method the method
	 * @return the result event factory
	 */
	public ResultEventFactory forMethod(Method method) {
		if (resultObjectBasedEventFactory.isMappedValueType(method.getReturnType())) {
			return resultObjectBasedEventFactory;
		}
		else {
			return successEventFactory;
		}
	}

	/**
	 * Select the appropriate result event factory for the given result.
	 * @param the result
	 * @return the result event factory
	 */
	public ResultEventFactory forResult(Object result) {
		if (result != null && resultObjectBasedEventFactory.isMappedValueType(result.getClass())) {
			return resultObjectBasedEventFactory;
		}
		else {
			return successEventFactory;
		}
	}
}