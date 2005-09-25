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

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.webflow.samples.upsrates.service.UPSRatesService;
import org.springframework.webflow.samples.upsrates.web.command.UPSRateQueryCommand;


public class UPSRateQueryCommandValidator implements Validator {

	private UPSRatesService upsRatesService = null;
	
	public void setUpsRatesService(UPSRatesService upsRatesService) {
		this.upsRatesService = upsRatesService;
	}

	public boolean supports(Class clazz) {
		return UPSRateQueryCommand.class.isAssignableFrom(clazz);
	}
	
	public void validate(Object target, Errors errors) {
		validateCustomerType((UPSRateQueryCommand)target, errors);
		validateSender((UPSRateQueryCommand)target, errors);
		validateReceiver((UPSRateQueryCommand)target, errors);
		validatePackageDetails((UPSRateQueryCommand)target, errors);
	}
	
	public void validateCustomerType(UPSRateQueryCommand query, Errors errors) {
		if (!query.isResidentialSet()) {
			errors.rejectValue("residential", "residentialRequired", "You must select your customer profile");
		}
	}
	
	public void validateSender(UPSRateQueryCommand query, Errors errors) {
		
		if (!StringUtils.hasText(query.getSenderCountryCode()) || query.getSenderCountryCode().equals("null")) {
			errors.rejectValue("senderCountryCode", "senderCountryCodeRequired", "Sender country code is required");
		}
		
		if (!StringUtils.hasText(query.getSenderZipCode())) {
			errors.rejectValue("senderZipCode", "senderZipCodeRequired", "Sender zip code is required");
		}
	}
	
	public void validateReceiver(UPSRateQueryCommand query, Errors errors) {
		
		if (!StringUtils.hasText(query.getReceiverCountryCode()) || query.getReceiverCountryCode().equals("null")) {
			errors.rejectValue("receiverCountryCode", "receiverCountryCodeRequired", "Receiver country code is required");
		}
		
		if (!StringUtils.hasText(query.getReceiverZipCode())) {
			errors.rejectValue("receiverZipCode", "receiverZipCodeRequired", "Receiver zip code is required");
		}
	}
	
	public void validatePackageDetails(UPSRateQueryCommand query, Errors errors) {
		
		if (query.getServiceLevelCode() < 0) {
			errors.rejectValue("serviceLevelCode", "serviceLevelCodeRequired", "Service level code is required");
		}
		if (query.getServiceLevelCode() >= 0 && this.upsRatesService.getServiceLevelCode(query.getServiceLevelCode()) == null) {
			errors.rejectValue("serviceLevelCode", "serviceLevelCodeIncorrect", "Service level code is incorrect");
		}
		
		if (query.getPackageType() < 0) {
			errors.rejectValue("packageType", "packageTypeRequired", "Package type is required");
		}
		if (query.getPackageType() >= 0 && this.upsRatesService.getPackageType(query.getPackageType()) == null) {
			errors.rejectValue("packageType", "packageTypeIncorrect", "Package type is incorrect");
		}
		
		if (query.getRateChart() < 0) {
			errors.rejectValue("rateChart", "rateChartRequired", "Rate chart is required");
		}
		if (query.getRateChart() >= 0 && this.upsRatesService.getRateChart(query.getRateChart()) == null) {
			errors.rejectValue("rateChart", "rateChartIncorrect", "Rate chart is incorrect");
		}
		
		if (query.getPackageWeight() <= 0) {
			errors.rejectValue("packageWeight", "packageWeigtRequired", "Package weight is required");
		}
	}
}
