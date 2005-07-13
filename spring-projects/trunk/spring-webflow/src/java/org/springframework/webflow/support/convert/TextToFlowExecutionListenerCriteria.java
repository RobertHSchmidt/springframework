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
package org.springframework.webflow.support.convert;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.FlowExecutionListenerCriteria;
import org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory;

/**
 * Converter that converts an encoded string representation of a
 * flow execution listener criteria object to an object instance.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"*" - will result in  a FlowExecutionListenerCriteria object that matches
 * with all flows ({@link org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory#allFlows()})
 * </li>
 * <li>"flowId" - will result in a FlowExecutionListenerCriteria object that
 * matches the flow with specified id
 * ({@link org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory#flow(String)})
 * </li>
 * <li>"class:&lt;classname&gt;" - will result in instantiation and usage of a custom 
 * FlowExecutionListenerCriteria implementation. The implementation must have a public
 * no-arg constructor.
 * </li>
 * </ul>
 * 
 * @see org.springframework.webflow.execution.FlowExecutionListenerCriteria
 * @see org.springframework.webflow.execution.FlowExecutionListenerCriteriaFactory
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToFlowExecutionListenerCriteria extends ConversionServiceAwareConverter {

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { FlowExecutionListenerCriteria.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws ConversionException {
		String encodedCriteria = (String)source;
		if (!StringUtils.hasText(encodedCriteria) ||
				FlowExecutionListenerCriteriaFactory.
					WildcardFlowExecutionListenerCriteria.WILDCARD_FLOW_ID.equals(encodedCriteria)) {
			// match all flows
			return FlowExecutionListenerCriteriaFactory.allFlows();
		}
		else if (encodedCriteria.startsWith(CLASS_PREFIX)) {
			// use custom criteria class
			Object o = newInstance(encodedCriteria);
			Assert.isInstanceOf(FlowExecutionListenerCriteria.class, o, "Encoded criteria class is of wrong type: ");
			return (FlowExecutionListenerCriteria)o;
		}
		else {
			// match identified flow
			return FlowExecutionListenerCriteriaFactory.flow(encodedCriteria);
		}
	}
}