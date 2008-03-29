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
package org.springframework.samples.petclinic;

import javax.sql.DataSource;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.support.ConfigurationSupport;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.samples.petclinic.jdbc.AbstractJdbcClinic;
import org.springframework.samples.petclinic.jdbc.HsqlJdbcClinic;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * JDBC configuration.
 * 
 * @author Costin Leau
 * 
 */
@Configuration
public class JdbcConfiguration extends ConfigurationSupport {

	@Bean
	public DataSource dataSource() {
		return new DriverManagerDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost:9001", "sa", "");
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager manager = new DataSourceTransactionManager();
		manager.setDataSource(dataSource());
		return manager;
	}

	@Bean
	public Clinic clinic() {
		AbstractJdbcClinic clinic = new HsqlJdbcClinic();
		clinic.setDataSource(dataSource());
		return clinic;
	}
}
