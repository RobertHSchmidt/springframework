/*
 * Copyright 2004-2005 the original author or authors.
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
package org.springframework.webflow.samples.upsrates.web;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.webflow.Event;
import org.springframework.webflow.samples.upsrates.domain.UPSRateQuery;
import org.springframework.webflow.samples.upsrates.service.UPSRatesService;
import org.springframework.webflow.samples.upsrates.service.exception.UPSRateRequestException;
import org.springframework.webflow.test.MockRequestContext;

public class FindUPSRatesActionTests extends TestCase {


	public void testExecuteSuccess() throws Exception {
		UPSRateQuery query = new UPSRateQuery();
		MockControl serviceMockControl = MockControl.createControl(UPSRatesService.class);
		UPSRatesService service = (UPSRatesService)serviceMockControl.getMock();
		serviceMockControl.expectAndReturn(service.findRate(query), 10);
		serviceMockControl.replay();
				
		String commandName = "query";
		MockRequestContext context = new MockRequestContext();
		FindUPSRateAction action = new FindUPSRateAction();
		Errors errors = new BindException(query, commandName);
		action.setUpsRatesService(service);
		action.setCommandName(commandName);
		context.getFlowScope().setAttribute(BindException.ERROR_KEY_PREFIX + commandName, errors);
		context.getFlowScope().setAttribute(commandName, query);
		
		Event event = action.execute(context);
		
		serviceMockControl.verify();
		assertFalse(errors.hasErrors());
		assertEquals("success", event.getId());
	}
	
	public void testExecuteFail() throws Exception {
		UPSRateQuery query = new UPSRateQuery();
		MockControl serviceMockControl = MockControl.createControl(UPSRatesService.class);
		UPSRatesService service = (UPSRatesService)serviceMockControl.getMock();
		serviceMockControl.expectAndThrow(service.findRate(query), new UPSRateRequestException("Some message", "someCode"));
		serviceMockControl.replay();
				
		String commandName = "query";
		MockRequestContext context = new MockRequestContext();
		FindUPSRateAction action = new FindUPSRateAction();
		Errors errors = new BindException(query, commandName);
		action.setUpsRatesService(service);
		action.setCommandName(commandName);
		context.getFlowScope().setAttribute(BindException.ERROR_KEY_PREFIX + commandName, errors);
		context.getFlowScope().setAttribute(commandName, query);
		
		Event event = action.execute(context);
		
		serviceMockControl.verify();
		assertTrue(errors.hasErrors());
		assertEquals("error", event.getId());
	}
}
