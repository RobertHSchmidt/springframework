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
package org.springframework.webflow.access;

import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowExecution;

/**
 * A service locator interface for retrieving flow definitions by id.
 * 
 * Flow locators are needed at two points within an application:
 * <ul>
 * <li>At runtime, to access a fully-configured Flow definition from a registry
 * to support launching a new {@link FlowExecution}.
 * <li>At configuration time, to locate a flow definition to be used as subflow
 * from within a flow that is in the process of being built.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowLocator {

	/**
	 * Lookup the flow definition with the specified id.
	 * @param id the flow definition id
	 * @return the flow definition
	 * @throws ArtifactLookupException when the flow definition with that id
	 * cannot be found
	 */
	public Flow getFlow(String id) throws ArtifactLookupException;
}