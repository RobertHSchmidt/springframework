package org.springframework.beans.factory.java.template;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.SpringAdvisor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;


public abstract class AbstractTransactionalConfiguration extends ConfigurationSupport {
	
	public static final String TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";
	
	public static final String DATASOURCE_BEAN_NAME = "dataSource";
	
	@Bean(autowire=Autowire.INHERITED)
	public abstract PlatformTransactionManager transactionManager();
	
	@Bean(autowire=Autowire.NO)
	public abstract DataSource dataSource();
	
	// TODO ability to use null return value can control which beans are created?
	
	//public Jdbctemplateh
	
	@Bean(dependsOn = {"transactionManager"}, autowire=Autowire.NO)
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

