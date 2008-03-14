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

import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcJobInstanceDao;
import org.springframework.batch.core.repository.dao.JdbcStepExecutionDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * A {@link FactoryBean} that automates the creation of a {@link SimpleJobRepository}.  Requires the user
 * to describe what kind of database they are using.  
 * 
 * @author Ben Hale
 * @author Lucas Ward
 */
public class JobRepositoryFactoryBean implements FactoryBean, InitializingBean {

	private DataSource dataSource;

	private String databaseType;
	
	private DataFieldMaxValueIncrementerFactory incrementerFactory;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDatabaseType(String dbType) {
		this.databaseType = dbType;
	}
	
	public void setIncrementerFactory(
			DataFieldMaxValueIncrementerFactory incrementerFactory) {
		this.incrementerFactory = incrementerFactory;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(dataSource, "Datasource must not be null.");
		
		if(incrementerFactory == null){
			incrementerFactory = new DefaultDataFieldMaxValueIncrementerFactory(dataSource);
		}
		
		Assert.isTrue(incrementerFactory.isSupportedIncrementerType(databaseType), "Unsupported database type");
	}

	public Object getObject() throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		JobInstanceDao jobInstanceDao = createJobInstanceDao(jdbcTemplate);
		JobExecutionDao jobExecutionDao = createJobExecutionDao(jdbcTemplate);
		StepExecutionDao stepExecutionDao = createStepExecutionDao(jdbcTemplate);
		return new SimpleJobRepository(jobInstanceDao, jobExecutionDao, stepExecutionDao);
	}

	public Class getObjectType() {
		return SimpleJobRepository.class;
	}

	public boolean isSingleton() {
		return true;
	}

	private JobInstanceDao createJobInstanceDao(JdbcTemplate jdbcTemplate) throws Exception {
		JdbcJobInstanceDao dao = new JdbcJobInstanceDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setJobIncrementer(incrementerFactory.getIncrementer(databaseType, "BATCH_JOB_SEQ"));
		dao.afterPropertiesSet();
		return dao;
	}

	private JobExecutionDao createJobExecutionDao(JdbcTemplate jdbcTemplate) throws Exception {
		JdbcJobExecutionDao dao = new JdbcJobExecutionDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setJobExecutionIncrementer(incrementerFactory.getIncrementer(databaseType, "BATCH_JOB_EXECUTION_SEQ"));
		dao.afterPropertiesSet();
		return dao;
	}

	private StepExecutionDao createStepExecutionDao(JdbcTemplate jdbcTemplate) throws Exception {
		JdbcStepExecutionDao dao = new JdbcStepExecutionDao();
		dao.setJdbcTemplate(jdbcTemplate);
		dao.setStepExecutionIncrementer(incrementerFactory.getIncrementer(databaseType, "BATCH_STEP_EXECUTION_SEQ"));
		dao.afterPropertiesSet();
		return dao;
	}
}
