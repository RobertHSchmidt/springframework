/*
 * Copyright 2005-2006 the original author or authors.
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

package org.springframework.webflow.jsf.util;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

/**
 * Replaces original ApplicationFactory's Application with a SpringApplication
 * that wraps the orginal Application.
 * <p>
 * 
 * @author Ken Weiner
 * @author Colin Sampaleanu
 * @see SpringApplication
 */
public class SpringApplicationFactory extends ApplicationFactory {

	private ApplicationFactory originalApplicationFactory;

	public SpringApplicationFactory() {
		// Default constructor
	}

	/**
     * Replaces the original ApplicationFactory's Application with the
     * SpringApplication.
     * 
     * @param applicationFactory
     *            the original ApplicationFactory
     */
	public SpringApplicationFactory(ApplicationFactory applicationFactory) {
		this.originalApplicationFactory = applicationFactory;
		Application originalApplication = applicationFactory.getApplication();
		setApplication(originalApplication);
	}

	/**
     * Delegates the retrieval of the Application to the original
     * ApplicationFactory.
     * 
     * @return the Application from the original application factory
     */
	public Application getApplication() {
		return this.originalApplicationFactory.getApplication();
	}

	/**
     * Sets the Application in the original ApplicationFactory after wrapping it
     * in a SpringApplication if it isn't already an instance of one.
     */
	public void setApplication(Application application) {
		if (!(application instanceof SpringApplication)) {
			Application springApplication = new SpringApplication(application);
			this.originalApplicationFactory.setApplication(springApplication);
		} else {
			this.originalApplicationFactory.setApplication(application);
		}
	}
}
