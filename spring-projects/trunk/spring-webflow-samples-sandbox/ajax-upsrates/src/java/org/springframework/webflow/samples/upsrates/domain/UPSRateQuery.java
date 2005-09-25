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
package org.springframework.webflow.samples.upsrates.domain;

import java.io.Serializable;

/**
 * Query class that holds query parameters filled in by users on the web site.
 * All properties are required.
 * 
 * @author Steven Devijver
 * @since Sep 22, 2005
 */
public class UPSRateQuery implements Serializable {
	
	private static final long serialVersionUID = 1611527610144031745L;
	
	private boolean residential = true;
	private String senderZipCode = null;
	private String receiverZipCode = null;
	private String senderCountryCode = null;
	private String receiverCountryCode = null;
	private int serviceLevelCode = -1;
	private int rateChart = -1;
	private int packageType = -1;
	private double packageWeight = -1;
	
	public int getPackageType() {
		return packageType;
	}
	public void setPackageType(int packageType) {
		this.packageType = packageType;
	}
	public double getPackageWeight() {
		return packageWeight;
	}
	public void setPackageWeight(double packageWeight) {
		this.packageWeight = packageWeight;
	}
	public int getRateChart() {
		return rateChart;
	}
	public void setRateChart(int rateChart) {
		this.rateChart = rateChart;
	}
	public String getReceiverCountryCode() {
		return receiverCountryCode;
	}
	public void setReceiverCountryCode(String receiverCountryCode) {
		this.receiverCountryCode = receiverCountryCode;
	}
	public String getReceiverZipCode() {
		return receiverZipCode;
	}
	public void setReceiverZipCode(String receiverZipCode) {
		this.receiverZipCode = receiverZipCode;
	}
	public boolean isResidential() {
		return residential;
	}
	public void setResidential(boolean residential) {
		this.residential = residential;
	}
	public String getSenderCountryCode() {
		return senderCountryCode;
	}
	public void setSenderCountryCode(String senderCountryCode) {
		this.senderCountryCode = senderCountryCode;
	}
	public String getSenderZipCode() {
		return senderZipCode;
	}
	public void setSenderZipCode(String senderZipCode) {
		this.senderZipCode = senderZipCode;
	}
	public int getServiceLevelCode() {
		return serviceLevelCode;
	}
	public void setServiceLevelCode(int serviceLevelCode) {
		this.serviceLevelCode = serviceLevelCode;
	}
	
	
	
}
