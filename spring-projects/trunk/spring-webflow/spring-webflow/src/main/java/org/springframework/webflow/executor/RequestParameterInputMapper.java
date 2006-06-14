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
package org.springframework.webflow.executor;

import java.util.Map;

import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;

/**
 * Simple attribute mapper implementation that puts all entries in the
 * request parameter map of a source {@link ExternalContext} into the
 * FlowExecution inputMap. This makes request parameters available to launching
 * flows for input mapping.
 * <p>
 * Used by {@link FlowExecutorImpl} as the default AttributeMapper
 * implementation.
 * 
 * @author Keith Donald
 */
class RequestParameterInputMapper implements AttributeMapper {
	
	public void map(Object source, Object target, Map mappingContext) {
		ExternalContext context = (ExternalContext)source;
		AttributeMap inputMap = (AttributeMap)target;
		inputMap.putAll(context.getRequestParameterMap().asAttributeMap());
	}
}