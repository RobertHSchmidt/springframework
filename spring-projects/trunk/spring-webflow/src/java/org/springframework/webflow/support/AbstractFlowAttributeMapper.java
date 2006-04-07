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
package org.springframework.webflow.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.UnmodifiableAttributeMap;

/**
 * @author Keith Donald
 */
public abstract class AbstractFlowAttributeMapper implements FlowAttributeMapper, Serializable {

	/**
	 * Logger, usable in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());;

	protected abstract AttributeMapper getInputMapper();
	
	protected abstract AttributeMapper getOutputMapper();
	
	public AttributeMap createSubflowInput(RequestContext context) {
		if (getInputMapper() != null) {
			AttributeMap input = new AttributeMap();
			// map from request context to input map
			getInputMapper().map(context, input, getMappingContext(context));
			return input;
		}
		else {
			// an empty, but modifiable map
			return new AttributeMap();
		}
	}

	public void mapSubflowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		if (getOutputMapper() != null && subflowOutput != null) {
			// map from request context to parent flow scope
			getOutputMapper().map(subflowOutput, context.getFlowExecutionContext().getActiveSession().getScope(),
					getMappingContext(context));
		}
	}

	/**
	 * Returns a map of contextual data available during mapping.
	 */
	protected Map getMappingContext(RequestContext context) {
		return Collections.EMPTY_MAP;
	}	
}