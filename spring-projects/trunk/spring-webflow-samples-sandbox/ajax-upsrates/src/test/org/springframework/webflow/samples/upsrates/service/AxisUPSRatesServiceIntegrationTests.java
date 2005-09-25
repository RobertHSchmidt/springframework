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

import java.util.Map;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.webflow.samples.upsrates.domain.UPSRateQuery;

public class AxisUPSRatesServiceIntegrationTests extends
		AbstractDependencyInjectionSpringContextTests {

	private UPSRatesService upsRatesService = null;
	
	protected String[] getConfigLocations() {
		return new String[] {
				"classpath:com/mackeybros/WebServices/ups-webservice.xml",
				"classpath:org/springframework/webflow/samples/upsrates/service/service-layer.xml"
		};
	}

	public void setUpsRatesService(UPSRatesService upsRatesService) {
		this.upsRatesService = upsRatesService;
	}
	
	public void testServiceLayerCodes() {
		Map serviceLayerCodeMap = this.upsRatesService.getServiceLevelCodes();

		assertEquals("1DM = Next Day Air Early AM", serviceLayerCodeMap.get(new Integer(0)));
		assertEquals("1DA = Next Day Air", serviceLayerCodeMap.get(new Integer(1)));
		assertEquals("1DP = Next Day Air Saver", serviceLayerCodeMap.get(new Integer(2)));
		assertEquals("2DM = 2nd Day Air Early AM", serviceLayerCodeMap.get(new Integer(3)));
		assertEquals("2DA = 2nd Day Air", serviceLayerCodeMap.get(new Integer(4)));
		assertEquals("3DS = 3 Day Select", serviceLayerCodeMap.get(new Integer(5)));
		assertEquals("GND = Ground", serviceLayerCodeMap.get(new Integer(6)));
		assertEquals("STD = Canada Standard", serviceLayerCodeMap.get(new Integer(7)));
		assertEquals("XPR = Worldwide Express", serviceLayerCodeMap.get(new Integer(8)));
		assertEquals("XDM = Worldwide Express Plus", serviceLayerCodeMap.get(new Integer(9)));
		assertEquals("XPD = Worldwide Expedited", serviceLayerCodeMap.get(new Integer(10)));
	}
	
	public void testPackageTypes() {
		Map packageTypeMap = this.upsRatesService.getPackageTypes();
	
		assertEquals("00 = Customer Packaging", packageTypeMap.get(new Integer(0)));
		assertEquals("01 = UPS Letter Envelope", packageTypeMap.get(new Integer(1)));
		assertEquals("03 = UPS Tube", packageTypeMap.get(new Integer(2)));
		assertEquals("21 = UPS Express Box", packageTypeMap.get(new Integer(3)));
		assertEquals("24 = UPS Worldwide 25 kilo", packageTypeMap.get(new Integer(4)));
		assertEquals("25 = UPS Worldwide 10 kilo", packageTypeMap.get(new Integer(5)));
	}
	
	public void testRateCharts() {
		Map rateChartMap = this.upsRatesService.getRateCharts();
	
		assertEquals("Regular Daily Pickup", rateChartMap.get(new Integer(0)));
		assertEquals("On Call Air", rateChartMap.get(new Integer(1)));
		assertEquals("One Time Pickup", rateChartMap.get(new Integer(2)));
		assertEquals("Letter Center", rateChartMap.get(new Integer(3)));
		assertEquals("Customer Counter", rateChartMap.get(new Integer(4)));
	}
	
	public void testFindUPSRateQuery() throws Exception {
		UPSRateQuery query = new UPSRateQuery();
		
		query.setPackageType(0);
		query.setPackageWeight(2);
		query.setRateChart(0);
		query.setReceiverCountryCode("US");
		query.setReceiverZipCode("90210");
		query.setResidential(false);
		query.setSenderCountryCode("US");
		query.setSenderZipCode("90250");
		query.setServiceLevelCode(1);
		
		double rate = this.upsRatesService.findRate(query);
	}
}
