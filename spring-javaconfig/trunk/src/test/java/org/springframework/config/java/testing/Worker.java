/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.config.java.testing;

import java.util.Date;

import org.springframework.config.java.Person;

/**
 * @author Costin Leau
 * 
 */
public class Worker extends Person {

	public enum JobTitle {
		CEO, CTO, VP, Principal, Manager, Senior, Normal
	};

	private Company company;

	private Date employmentDate;

	private JobTitle title = JobTitle.Normal;

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Date getEmploymentDate() {
		return employmentDate;
	}

	public void setEmploymentDate(Date employmentDate) {
		this.employmentDate = employmentDate;
	}

	public JobTitle getTitle() {
		return title;
	}

	public void setTitle(JobTitle title) {
		this.title = title;
	}

}
