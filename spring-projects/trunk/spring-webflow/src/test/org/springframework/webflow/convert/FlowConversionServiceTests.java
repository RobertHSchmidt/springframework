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
package org.springframework.webflow.convert;

import org.springframework.binding.convert.ConversionService;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.execution.FlowExecutionListenerCriteria;

import junit.framework.TestCase;

/**
 * Unit test for the FlowConversionService class.
 * 
 * @author Erwin Vervaet
 */
public class FlowConversionServiceTests extends TestCase {
	
	public void testDefaultConvertersRegistered() {
		ConversionService conversionService = new FlowConversionService();
		assertNotNull(conversionService.getConversionExecutor(String.class, TransitionCriteria.class));
		assertNotNull(conversionService.getConversionExecutor(String.class, ViewDescriptorCreator.class));
		assertNotNull(conversionService.getConversionExecutor(String.class, FlowExecutionListenerCriteria.class));
	}

}
