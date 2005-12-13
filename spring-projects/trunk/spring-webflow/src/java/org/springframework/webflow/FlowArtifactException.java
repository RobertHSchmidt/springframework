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
package org.springframework.webflow;

import org.springframework.core.NestedRuntimeException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A flow artifact exception is thrown when a service artifact required by the
 * webflow system cannot be obtained (like a flow, state, action, etc), either
 * at flow configuration time or at runtime. Can be used directly, but you are
 * encouraged to create a specific subclass for a particular use case.
 * <p>
 * Flow artifact exceptions indicate unrecoverable problems with the flow
 * definition, e.g. a required state of a flow cannot be found. They're not used
 * to signal problems related to execution of a client request. A StateException
 * is used for that.
 * 
 * @see org.springframework.webflow.StateException
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowArtifactException extends NestedRuntimeException {

	/**
	 * The type of artifact that could not be retrieved.
	 */
	private Class artifactType;

	/**
	 * The id of the artifact that could not be retrieved.
	 */
	private String artifactId;

	/**
	 * Create a new flow artifact exception.
	 * @param artifactType the expected service type
	 * @param artifactId the id of the service that cannot be found
	 */
	public FlowArtifactException(Class artifactType, String artifactId) {
		this(artifactType, artifactId, null, null);
	}

	/**
	 * Create a new flow artifact exception.
	 * @param artifactType the expected artifact type
	 * @param artifactId the id of the artifact
	 * @param cause the underlying cause of this exception
	 */
	public FlowArtifactException(Class artifactType, String artifactId, Throwable cause) {
		this(artifactType, artifactId, null, cause);
	}

	/**
	 * Create a new flow artifact exception.
	 * @param artifactType the expected artifact type
	 * @param artifactId the id of the artifact
	 * @param message descriptive message
	 */
	public FlowArtifactException(Class artifactType, String artifactId, String message) {
		this(artifactType, artifactId, message, null);
	}

	/**
	 * Create a new flow artifact lookup exception.
	 * @param artifactType the expected artifact type
	 * @param artifactId the id of the artifact
	 * @param message descriptive message
	 * @param cause the underlying cause of this exception
	 */
	public FlowArtifactException(Class artifactType, String artifactId, String message, Throwable cause) {
		super((StringUtils.hasText(message) ? message : "Unable to obtain a " + ClassUtils.getShortName(artifactType)
				+ " flow artifact with id '" + artifactId + "': make sure there is a valid [" + artifactType
				+ "] exported with this id"), cause);
		this.artifactType = artifactType;
		this.artifactId = artifactId;
	}

	/**
	 * Returns the expected service type.
	 */
	public Class getArtifactType() {
		return artifactType;
	}

	/**
	 * Returns the id of the service that cannot be found.
	 */
	public String getArtifactId() {
		return artifactId;
	}
}