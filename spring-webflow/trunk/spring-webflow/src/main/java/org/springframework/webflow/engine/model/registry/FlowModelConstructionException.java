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
package org.springframework.webflow.engine.model.registry;

import org.springframework.webflow.core.FlowException;

/**
 * Thrown when a flow model was found during a lookup operation but could not be constructed.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Scott Andrews
 */
public class FlowModelConstructionException extends FlowException {

	/**
	 * The id of the flow that could not be constructed.
	 */
	private String flowModelId;

	/**
	 * Creates an exception indicating a flow model could not be constructed.
	 * @param flowModelId the flow model identifier
	 * @param cause the underlying cause of the exception
	 */
	public FlowModelConstructionException(String flowModelId, Throwable cause) {
		super("An exception occurred constructing the flow '" + flowModelId + "'", cause);
		this.flowModelId = flowModelId;
	}

	/**
	 * Returns the id of the flow model that could not be constructed.
	 * @return the flow id
	 */
	public String getFlowModelId() {
		return flowModelId;
	}
}