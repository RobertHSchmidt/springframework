/*
 * Copyright 2002-2008 the original author or authors.
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

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.support.ConfigurationSupport;
import org.springframework.config.java.testing.Worker.JobTitle;

/**
 * @author Costin Leau
 */
@Configuration
public class SimpleConfiguration extends ConfigurationSupport {

	@Bean
	public Company company() {
		Company company = new Company();

		company.hire(mark());
		company.hire(coliny());

		company.addOwner(rod());
		company.setName("SpringSource");

		return company;
	}

	@Bean(aliases = { "mfisher", "mark.fisher" })
	public Worker mark() {
		Worker rick = new Worker();
		rick.setName("Mark Fisher");
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
