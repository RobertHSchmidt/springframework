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

import java.util.Map;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.util.StringUtils;

/**
 * Converter that converts an encoded string representation of a flow execution
 * listener criteria object to an object instance.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"*" - will result in a FlowExecutionListenerCriteria object that matches
 * on all flows ({@link org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory#allFlows()})
 * </li>
 * <li>"flowId" - will result in a FlowExecutionListenerCriteria object that
 * matches the flow with specified id ({@link org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory#flow(String)})
 * </li>
 * </ul>
 * 
 * @see org.springframework.webflow.execution.FlowExecutionListenerCriteria
 * @see org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToFlowExecutionListenerCriteria extends AbstractConverter {

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { FlowExecutionListenerCriteria.class };
	}

	public FlowExecutionListenerCriteria convert(String source) {
		return (FlowExecutionListenerCriteria)convert(source);
	}

	protected Object doConvert(Object source, Class targetClass, Map context) throws ConversionException {
		String encodedCriteria = (String)source;
		if (!StringUtils.hasText(encodedCriteria)
				|| FlowExecutionListenerCriteriaFactory.WildcardFlowExecutionListenerCriteria.WILDCARD_FLOW_ID
						.equals(encodedCriteria)) {
			// match all flows
			return FlowExecutionListenerCriteriaFactory.allFlows();
		}
		else {
			// match identified flows
			return FlowExecutionListenerCriteriaFactory.flows(StringUtils
					.commaDelimitedListToStringArray(encodedCriteria));
		}
	}
}