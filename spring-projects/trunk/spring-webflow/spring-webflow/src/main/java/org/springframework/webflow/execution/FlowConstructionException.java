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

/**
 * Thrown when a flow definition was found during a lookup operation
 * but could not be constructed.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowConstructionException extends FlowLocatorException {
	
	/**
	 * Creates an exception indicating a flow definition could not be found.
	 * @param flowId the flow id
	 * @param registeredFlowIds all flow ids known to the registry generating this exception
	 */
	public FlowConstructionException(String flowId, Throwable cause) {
		super(flowId, "An exception occured constructing the flow with id '" + flowId + "'", cause);
	}	
}