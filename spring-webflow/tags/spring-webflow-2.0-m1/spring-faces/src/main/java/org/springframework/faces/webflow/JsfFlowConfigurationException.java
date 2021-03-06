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
package org.springframework.faces.webflow;

import org.springframework.webflow.core.FlowException;

/**
 * Thrown when there is a configuration error with SWF within a JSF environment.
 * 
 * @author Keith Donald
 */
public class JsfFlowConfigurationException extends FlowException {

	public JsfFlowConfigurationException(String msg) {
		super(msg);
	}

	public JsfFlowConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
