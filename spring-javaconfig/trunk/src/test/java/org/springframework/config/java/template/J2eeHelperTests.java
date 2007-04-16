package org.springframework.config.java.template;

import javax.naming.NamingException;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.jdbcDriver;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.listener.ConfigurationListenerRegistry;
import org.springframework.config.java.listener.DefaultConfigurationListenerRegistry;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.template.AbstractTransactionalConfiguration;
import org.springframework.config.java.template.J2eeHelper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.transaction.CallCountingTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.jta.UserTransactionAdapter;

public class J2eeHelperTests extends TestCase {
	
	private ConfigurationListenerRegistry clr = new DefaultConfigurationListenerRegistry();

	private static final String DS_NAME = "java:comp/env/ds";

	public void testLookup() throws NamingException {

		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder
				.emptyActivatedContextBuilder();
		DataSource expectedDs = new DriverManagerDataSource();

		builder.bind(DS_NAME, expectedDs);
		builder.bind("java:comp/UserTransaction", new UserTransactionAdapter(
				(javax.transaction.TransactionManager) empty(javax.transaction.TransactionManager.class)));

		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(
				bf, clr);
		configurationProcessor.process(J2eeTxConfig.class);

		PlatformTransactionManager ptm = (PlatformTransactionManager) bf
				.getBean("transactionManager");
		DataSource ds = (DataSource) bf.getBean("dataSource");
		assertSame(expectedDs, ds);
	}
	
	protected Object empty(Class ... interfaces) {
		ProxyFactory pf = new ProxyFactory();
		pf.setInterfaces(interfaces);
		pf.addAdvice(new MethodInterceptor() {
			public Object invoke(MethodInvocation mi) throws Throwable {
				throw new UnsupportedOperationException();
			}
		});
		return pf.getProxy();
	}
	
	public void testAttributeDrivenTransactionManagement() {

		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(
				bf, clr);
		configurationProcessor.process(TestTxConfig.class);
		
		Object result = bf.getBean(AbstractTransactionalConfiguration.TRANSACTION_MANAGER_BEAN_NAME);
		if (result instanceof Advised) {
			fail(((Advised) result).toProxyConfigString());
		}
		

		CallCountingTransactionManager txm = (CallCountingTransactionManager) bf.getBean(AbstractTransactionalConfiguration.TRANSACTION_MANAGER_BEAN_NAME);
		
		TxAnnotated txAnnotated =  (TxAnnotated) bf.getBean("annotatedBean");
		assertTrue(AopUtils.isAopProxy(txAnnotated));
		
		assertEquals(0, txm.commits);
		txAnnotated.foo();
		assertEquals(1, txm.commits);
		
		assertFalse("Advisor is hidden so as not to pollute context", bf.containsBean("txAdvisor"));
		
	}
	
	
	public void xtestHsqlConfig() {

		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(
				bf, clr);
		configurationProcessor.process(TestHsqlConfig.class);
		
		MyDao myDao = (MyDao) bf.getBean("myDao");
		assertEquals(0, myDao.runCount() );
	}
	

	public static class J2eeTxConfig extends AbstractTransactionalConfiguration {
		
		@Override
		public PlatformTransactionManager transactionManager() {
			JtaTransactionManager jta = new JtaTransactionManager();
			jta.afterPropertiesSet();
			return jta;
		}

		@Override
		public DataSource dataSource() {
			return (DataSource) J2eeHelper.jndiObject(DS_NAME);
		}

	}
	
	public static class TestTxConfig extends AbstractTransactionalConfiguration {
		@Override
		public PlatformTransactionManager transactionManager() {
			return new CallCountingTransactionManager();
		}
		
		@Override
		public DataSource dataSource() {
			throw new UnsupportedOperationException();
		}
		
		@Bean
		public TxAnnotated annotatedBean() {
			return new TxAnnotated();
		}
	}

	public static class TxAnnotated {
		@Transactional
		public void foo() {
			
		}
	}
	
	@Configuration(defaultAutowire=Autowire.BY_TYPE)
	public static class TestHsqlConfig extends AbstractTransactionalConfiguration {
		@Override
		public PlatformTransactionManager transactionManager() {
			DataSourceTransactionManager dtm = new DataSourceTransactionManager(dataSource());
			return dtm;
		}

		@Override
		public DataSource dataSource() {
			BasicDataSource bsd = new BasicDataSource();
			bsd.setDriverClassName(jdbcDriver.class.getName());
			bsd.setUsername("sa");
			bsd.setUrl("jdbc:hsqldb:mem:xdb");
			return bsd;
		}
		
		@Bean
		public MyDao myDao() {
			return new MyDao();
		}
	}
	
	
	public static class MyDao extends SimpleJdbcDaoSupport {
		@Transactional(readOnly=true)
		public int runCount() {
			return getSimpleJdbcTemplate().queryForInt("SELECT COUNT(0) FROM USERS");
		}
	}
}
