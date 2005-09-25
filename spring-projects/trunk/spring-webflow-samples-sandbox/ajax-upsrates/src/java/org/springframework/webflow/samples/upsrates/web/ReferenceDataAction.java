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

import java.util.Map;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.samples.upsrates.service.UPSRatesService;
import org.springframework.webflow.samples.upsrates.web.command.UPSRateQueryCommand;

public class ReferenceDataAction extends AbstractAction {

	private UPSRatesService upsRatesService = null;
	private String commandName = "query";
	
	public void setUpsRatesService(UPSRatesService upsRatesService) {
		this.upsRatesService = upsRatesService;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
	
	protected Event doExecute(RequestContext context) throws Exception {
		UPSRateQueryCommand query = (UPSRateQueryCommand)context.getFlowScope().getAttribute(this.commandName);
		Map countries = (Map)context.getRequestScope().getAttribute("countries");
	
		context.getRequestScope().setAttribute("senderCountry", countries.get(query.getSenderCountryCode()));
		context.getRequestScope().setAttribute("receiverCountry", countries.get(query.getReceiverCountryCode()));
		context.getRequestScope().setAttribute("serviceLevel", stripCode(this.upsRatesService.getServiceLevelCode(query.getServiceLevelCode())));
		context.getRequestScope().setAttribute("packageType", stripCode(this.upsRatesService.getPackageType(query.getPackageType())));
		context.getRequestScope().setAttribute("rateChart", this.upsRatesService.getRateChart(query.getRateChart()));
		
		return success();
	}
	
	private String stripCode(Object o) {
		String str = o.toString();
		int i = str.indexOf("=");
		return str.substring(i + 2, str.length());
	}
}
