/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.config.java.txannotations;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter;

public class TxAnnotationTests extends TestCase {

	@Configuration
	public static class SampleConfig {

		private DataSource genericDataSource;

		private String password;

		private String username;

		/**
		 * @param genericDataSource the genericDataSource to set
		 */
		public void setGenericDataSource(DataSource genericDataSource) {
			this.genericDataSource = genericDataSource;
		}

		/**
		 * @param password the password to set
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * @param username the username to set
		 */
		public void setUsername(String username) {
			this.username = username;
		}

		@Bean
		public DataSource transactionalDataSource() {
			// System.out.println("### Datasource being initialized ("
			// + genericDataSource + ", " + username + "," + password
			// + ")");

			UserCredentialsDataSourceAdapter adapter = new UserCredentialsDataSourceAdapter();
			adapter.setTargetDataSource(genericDataSource);
			adapter.setUsername(username);
			adapter.setPassword(password);

			return adapter;
		}

		@Bean
		public Object myApplicationBean() {
			// System.out.println("### Creating application bean");
			// requires transaction manager...
			transactionalDataSource();

			return new Object();
		}
	}

	public void testTxProxyingWithJavaConfigDependingOnXml() {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(
				"org/springframework/config/java/txannotations/txannotations.xml");
		ac.getBean("myApplicationBean");
	}

}
