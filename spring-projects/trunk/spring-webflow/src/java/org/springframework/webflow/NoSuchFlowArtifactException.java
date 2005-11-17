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

/**
 * Thrown when a requested artifact like a Flow, Action, TransitionCriteria,
 * ViewSelector, State, etc. could not be found.
 * @author Keith Donald
 */
public class NoSuchFlowArtifactException extends FlowArtifactException {

	/**
	 * Creates an exception indicating a flow artifact could not be found.
	 * @param artifactType the artifact type
	 * @param id the artifact id
	 * @param cause the root cause
	 */
	public NoSuchFlowArtifactException(Class artifactType, String id, Throwable cause) {
		super(artifactType, id, "No such artifact of type [" + artifactType + "] with id '" + id + "' found", cause);
	}

	/**
	 * Creates an exception indicating a flow aritfact could not be found.
	 * @param artifactType the artifact type
	 * @param id the artifact id
	 * @param message a custom message
	 * @param cause the root cause
	 */
	public NoSuchFlowArtifactException(Class artifactType, String id, String message, Throwable cause) {
		super(artifactType, id, message, cause);
	}

}