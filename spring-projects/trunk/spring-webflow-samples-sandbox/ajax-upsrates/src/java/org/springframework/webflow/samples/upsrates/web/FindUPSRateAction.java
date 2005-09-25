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

import org.springframework.validation.Errors;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.FormObjectAccessor;
import org.springframework.webflow.samples.upsrates.domain.UPSRateQuery;
import org.springframework.webflow.samples.upsrates.service.UPSRatesService;
import org.springframework.webflow.samples.upsrates.service.exception.UPSRateRequestException;

public class FindUPSRateAction extends AbstractAction {

	private UPSRatesService upsRatesService = null;
	private String commandName = null;
	
	public void setUpsRatesService(UPSRatesService upsRatesService) {
		this.upsRatesService = upsRatesService;
	}
	
	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
	
	public FindUPSRateAction() {
		super();
	}

	protected Event doExecute(RequestContext context) throws Exception {
		
		Errors errors = new FormObjectAccessor(context).getFormErrors(this.commandName, ScopeType.REQUEST);
		
		UPSRateQuery upsRateQuery = (UPSRateQuery)context.getFlowScope().getAttribute(this.commandName);
		try {
			double rate = this.upsRatesService.findRate(upsRateQuery);
			context.getRequestScope().setAttribute("rate", new Double(rate));
		} catch (UPSRateRequestException e) {
			errors.reject("Request rejected: " + e.getMessage() + ", error code: " + e.getCode());
			return error();
		}
		
		return success();
	}

}
