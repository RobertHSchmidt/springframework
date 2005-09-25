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
package org.springframework.webflow.samples.upsrates.service;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.samples.upsrates.domain.UPSRateQuery;
import org.springframework.webflow.samples.upsrates.service.exception.UPSRateRequestException;

import com.mackeybros.WebServices.ShippingDetail;
import com.mackeybros.WebServices.UPSRatesSoap;

public class AxisUPSRatesService implements UPSRatesService {

	private UPSRatesSoap upsRatesSoap = null;
	
	public void setUpsRatesSoap(UPSRatesSoap upsRatesSoap) {
		this.upsRatesSoap = upsRatesSoap;
	}
	
	public Map getServiceLevelCodes() {
		Map result = new HashMap();
		
		String[] serviceLevelCodes = null;
		try {
			serviceLevelCodes = upsRatesSoap.possibleValues().getSvcCodes().getString();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		for (int i = 0; i < serviceLevelCodes.length; i++) {
			result.put(new Integer(i), serviceLevelCodes[i]);
		}
		
		return result;
	}

	public Map getRateCharts() {
		Map result = new HashMap();
		
		String[] rateCharts = null;
		try {
			rateCharts = upsRatesSoap.possibleValues().getRateCharts().getString();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		for (int i = 0; i < rateCharts.length; i++) {
			result.put(new Integer(i), rateCharts[i]);
		}
		
		return result;
	}

	public Map getPackageTypes() {
		Map result = new HashMap();
		
		String[] packageTypes = null;
		try {
			packageTypes = upsRatesSoap.possibleValues().getPkgTypes().getString();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		for (int i = 0; i < packageTypes.length; i++) {
			result.put(new Integer(i), packageTypes[i]);
		}
		
		return result;
	}

	public String getServiceLevelCode(int serviceLevelCode) {
		Map serviceLevelCodes = getServiceLevelCodes();
		if (serviceLevelCodes.containsKey(new Integer(serviceLevelCode))) {
			return (String)serviceLevelCodes.get(new Integer(serviceLevelCode));
		}
		return null;
	}

	public String getRateChart(int rateChart) {
		Map rateCharts = getRateCharts();
		if (rateCharts.containsKey(new Integer(rateChart))) {
			return (String)rateCharts.get(new Integer(rateChart));
		}
		return null;
	}

	public String getPackageType(int packageType) {
		Map packageTypes = getPackageTypes();
		if (packageTypes.containsKey(new Integer(packageType))) {
			return (String)packageTypes.get(new Integer(packageType));
		}
		return null;
	}

	public double findRate(UPSRateQuery upsRateQuery)
			throws UPSRateRequestException {
		String serviceLevelCode = getServiceLevelCode(upsRateQuery.getServiceLevelCode());
		String rateChart = getRateChart(upsRateQuery.getRateChart());
		String packagetType = getPackageType(upsRateQuery.getPackageType());
		
		ShippingDetail shippingDetail = null;
		
		try {
			shippingDetail = this.upsRatesSoap.getShippingRate(
					serviceLevelCode,
					rateChart,
					upsRateQuery.getSenderZipCode(),
					upsRateQuery.getReceiverZipCode(),
					upsRateQuery.getSenderCountryCode(),
					upsRateQuery.getReceiverCountryCode(),
					upsRateQuery.getPackageWeight(),
					upsRateQuery.isResidential(),
					packagetType);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		if (shippingDetail.getPrice() < 0) {
			throw new UPSRateRequestException(shippingDetail.getMessage(), shippingDetail.getResponseCode());
		} else {
			return shippingDetail.getPrice();
		}
	}

}
