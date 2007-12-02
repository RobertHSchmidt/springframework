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
package org.springframework.config.java.template;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.aop.SpringAdvisor;
import org.springframework.config.java.support.ConfigurationSupport;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

public abstract class AbstractTransactionalConfiguration extends ConfigurationSupport {

	public static final String TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";

	public static final String DATASOURCE_BEAN_NAME = "dataSource";

	@Bean(autowire = Autowire.INHERITED)
	public abstract PlatformTransactionManager transactionManager();

	@Bean(autowire = Autowire.NO)
	public abstract DataSource dataSource();

	// TODO ability to use null return value can control which beans are
	// created?

	// public Jdbctemplateh

	@Bean(dependsOn = { "transactionManager" }, autowire = Autowire.NO)
	@SpringAdvisor
	protected TransactionAttributeSourceAdvisor txAdvisor() {
		TransactionAttributeSourceAdvisor txsasa = new TransactionAttributeSourceAdvisor();
		TransactionInterceptor txi = new TransactionInterceptor();
		txi.setTransactionManager(transactionManager());
		txi.setTransactionAttributeSource(createTransactionAttributeSource());
		txsasa.setTransactionInterceptor(txi);
		return txsasa;
	}

	protected AnnotationTransactionAttributeSource createTransactionAttributeSource() {
		return new AnnotationTransactionAttributeSource();
	}

}
