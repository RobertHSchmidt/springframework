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

package org.springframework.batch.core.repository.support;

import javax.sql.DataSource;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcJobInstanceDao;
import org.springframework.batch.core.repository.dao.JdbcStepExecutionDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A {@link FactoryBean} that automates the creation of a
 * {@link SimpleJobRepository}. Requires the user to describe what kind of
 * database they are using.
 * 
 * @author Ben Hale
 * @author Lucas Ward
 */
public class JobRepositoryFactoryBean implements FactoryBean, InitializingBean {

	/**
	 * Default value for isolation level in create* method.
	 */
	private static final String DEFAULT_ISOLATION_LEVEL = "ISOLATION_SERIALIZABLE";

	private DataSource dataSource;

	private String databaseType;

	private String tablePrefix = AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

	private DataFieldMaxValueIncrementerFactory incrementerFactory;

	private PlatformTransactionManager transactionManager;

	private ProxyFactory proxyFactory;

	private String isolationLevelForCreate = DEFAULT_ISOLATION_LEVEL;

	/**
	 * Public setter for the {@link PlatformTransactionManager}.
	 * @param transactionManager the transactionManager to set
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Public setter for the isolation level to be used for the transaction when
	 * job execution entities are initially created. The default is
	 * ISOLATION_SERIALIZABLE, which prevents accidental concurrent execution of
	 * the same job (ISOLATION_REPEATABLE_READ would work as well).
	 * 
	 * @param isolationLevelForCreate the isolation level name to set
	 * 
	 * @see SimpleJobRepository#createJobExecution(org.springframework.batch.core.Job,
	 * org.springframework.batch.core.JobParameters)
	 */
	public void setIsolationLevelForCreate(String isolationLevelForCreate) {
		this.isolationLevelForCreate = isolationLevelForCreate;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDatabaseType(String dbType) {
		this.databaseType = dbType;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public void setIncrementerFactory(DataFieldMaxValueIncrementerFactory incrementerFactory) {
		this.incrementerFactory = incrementerFactory;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(dataSource, "DataSource must not be null.");
		Assert.notNull(transactionManager, "TransactionManager must not be null.");

		if (incrementerFactory == null) {
			incrementerFactory = new DefaultDataFieldMaxValueIncrementerFactory(dataSource);
		}

		Assert.isTrue(incrementerFactory.isSupportedIncrementerType(databaseType), "'" + databaseType
				+ "' is an unsupported database type.  The supported database types are "
				+ StringUtils.arrayToCommaDelimitedString(incrementerFactory.getSupportedIncrementerTypes()));

		initializeProxy();
	}

	protected void initializeProxy() throws Exception {
		proxyFactory = new ProxyFactory();
		TransactionInterceptor advice = new TransactionInterceptor(transactionManager, PropertiesConverter
				.stringToProperties("create*=PROPAGATION_REQUIRES_NEW," + isolationLevelForCreate
						+ "\n*=PROPAGATION_REQUIRED"));
		DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(advice);
		NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
		pointcut.addMethodName("*");
		advisor.setPointcut(pointcut);
		proxyFactory.addAdvisor(advisor);
		proxyFactory.setProxyTargetClass(false);
		proxyFactory.addInterface(JobRepository.class);
		proxyFactory.setTarget(getTarget());
	}

	/**
	 * @return a SimpleJobRepository
	 */
	private SimpleJobRepository getTarget() throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		JobInstanceDao jobInstanceDao = createJobInstanceDao(jdbcTemplate);
		JobExecutionDao jobExecutionDao = createJobExecutionDao(jdbcTemplate);
		StepExecutionDao stepExecutionDao = createStepExecutionDao(jdbcTemplate);
		return new SimpleJobRepository(jobInstanceDao, jobExecutionDao, stepExecutionDao);
	}

	/**
	 * Get a concrete {@link JobRepository}. The repository will be a proxy
	 * containing transaction advice using the supplied transaction manager.
	 * 
	 * @return a {@link JobRepository}
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		return proxyFactory.getProxy();
	}

	/**
	 * The type of object to be returned from {@link #getObject()}.
	 * 
	 * @return JobRepository.class
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return JobRepository.class;
	}

	public boolean isSingleton() {
		return true;
	}

	private JobInstanceDao createJobInstanceDao(JdbcTemplate jdbcTemplate) throws Exception {
		JdbcJobInstanceDao dao = new JdbcJobInstanceDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setJobIncrementer(incrementerFactory.getIncrementer(databaseType, tablePrefix + "JOB_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.afterPropertiesSet();
		return dao;
	}

	private JobExecutionDao createJobExecutionDao(JdbcTemplate jdbcTemplate) throws Exception {
		JdbcJobExecutionDao dao = new JdbcJobExecutionDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setJobExecutionIncrementer(incrementerFactory.getIncrementer(databaseType, tablePrefix
				+ "JOB_EXECUTION_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.afterPropertiesSet();
		return dao;
	}

	private StepExecutionDao createStepExecutionDao(JdbcTemplate jdbcTemplate) throws Exception {
		JdbcStepExecutionDao dao = new JdbcStepExecutionDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setStepExecutionIncrementer(incrementerFactory.getIncrementer(databaseType, tablePrefix
				+ "STEP_EXECUTION_SEQ"));
		dao.setTablePrefix(tablePrefix);
		dao.afterPropertiesSet();
		return dao;
	}
}
