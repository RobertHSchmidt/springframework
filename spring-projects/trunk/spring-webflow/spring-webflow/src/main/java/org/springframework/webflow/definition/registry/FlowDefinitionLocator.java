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
package org.springframework.webflow.definition.registry;

import org.springframework.webflow.definition.FlowDefinition;

/**
 * A runtime service locator interface for retrieving flow definitions by
 * <code>id</code>.
 * <p>
 * Flow locators are needed at by flow executors at runtime to retrieve
 * fully-configured Flow definitions to support launching new flow executions.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowDefinitionLocator {

	/**
	 * Lookup the flow definition with the specified <code>id</code>.
	 * @param id the flow definition id
	 * @return the flow definition
	 * @throws FlowLocatorException when a problem occured accessing the flow
	 * definition with the provided identifier
	 */
	public FlowDefinition getFlowDefinition(String id) throws NoSuchFlowDefinitionException;
}