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
package org.springframework;

import org.springframework.Worker.JobTitle;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.java.template.ConfigurationSupport;

/**
 * @author Costin Leau
 */
@Configuration
public class SimpleConfiguration extends ConfigurationSupport {

	@Bean
	public Company company() {
		Company company = new Company();

		company.hire(rick());
		company.hire(coliny());

		company.addOwner(rod());
		company.setName("Interface21");

		return company;
	}

	@Bean(aliases = { "ricke", "revans" })
	public Worker rick() {
		Worker rick = new Worker();
		rick.setName("Rick Evans");
		rick.setTitle(JobTitle.Senior);
		return rick;
	}

	@Bean
	public Worker coliny() {
		Worker colin = new Worker();
		colin.setName("Colin Yates");
		colin.setTitle(JobTitle.Normal);
		return colin;
	}

	@Bean
	public Owner rod() {
		Owner rod = new Owner();
		rod.setName("Rod Johnson");

		return rod;
	}
}
