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

import java.util.Iterator;
import java.util.Map;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.samples.upsrates.service.UPSRatesService;

public class LoadPackageDetailsDataAction extends AbstractAction {

	private UPSRatesService upsRatesService = null;
	
	public void setUpsRatesService(UPSRatesService upsRatesService) {
		this.upsRatesService = upsRatesService;
	}
	
	protected Event doExecute(RequestContext context) throws Exception {
		Map serviceLevelCodeMap = this.upsRatesService.getServiceLevelCodes();
		stripCodes(serviceLevelCodeMap);
		
		Map packageTypeMap = this.upsRatesService.getPackageTypes();
		stripCodes(packageTypeMap);
		
		Map rateChartMap = this.upsRatesService.getRateCharts();
		
		context.getRequestScope().setAttribute("serviceLevelCodes", serviceLevelCodeMap);
		context.getRequestScope().setAttribute("packageTypes", packageTypeMap);
		context.getRequestScope().setAttribute("rateCharts", rateChartMap);
		
		return success();
	}

	private void stripCodes(Map map) {
		for (Iterator iter= map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry)iter.next();
			String str = entry.getValue().toString();
			int i = str.indexOf("=");
			entry.setValue(str.substring(i + 2, str.length()));			
		}
	}
}
