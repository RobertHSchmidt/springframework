/*
 * Copyright 2006-2008 the original author or authors.
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

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.JobSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author Lucas Ward
 * 
 */
public class JobRepositoryFactoryBeanTests {

	private JobRepositoryFactoryBean factory;

	private DataFieldMaxValueIncrementerFactory incrementerFactory;

	private DataSource dataSource;

	private PlatformTransactionManager transactionManager;

	private String tablePrefix = "TEST_BATCH_PREFIX_";

	@Before
	public void setUp() throws Exception {

		factory = new JobRepositoryFactoryBean();
		dataSource = createMock(DataSource.class);
		transactionManager = createMock(PlatformTransactionManager.class);
		factory.setDataSource(dataSource);
		factory.setTransactionManager(transactionManager);
		incrementerFactory = createMock(DataFieldMaxValueIncrementerFactory.class);
		factory.setIncrementerFactory(incrementerFactory);
		factory.setTablePrefix(tablePrefix);

	}

	@Test
	public void testNoDatabaseType() throws Exception {

		try {
			expect(incrementerFactory.isSupportedIncrementerType(null)).andReturn(false);
			expect(incrementerFactory.getSupportedIncrementerTypes()).andReturn(new String[0]);
			replay(incrementerFactory);
			factory.afterPropertiesSet();
			fail();
		}
		catch (IllegalArgumentException ex) {
			// expected
			String message = ex.getMessage();
			assertTrue("Wrong message: " + message, message.indexOf("unsupported database type") >= 0);
		}

	}

	@Test
	public void testMissingDataSource() throws Exception {

		factory.setDataSource(null);
		try {
			factory.afterPropertiesSet();
			fail();
		}
		catch (IllegalArgumentException ex) {
			// expected
			String message = ex.getMessage();
			assertTrue("Wrong message: " + message, message.indexOf("DataSource") >= 0);
		}

	}

	@Test
	public void testMissingTransactionManager() throws Exception {

		factory.setTransactionManager(null);
		try {
			expect(incrementerFactory.isSupportedIncrementerType(null)).andReturn(true);
			expect(incrementerFactory.getSupportedIncrementerTypes()).andReturn(new String[0]);
			replay(incrementerFactory);
			factory.afterPropertiesSet();
			fail();
		}
		catch (IllegalArgumentException ex) {
			// expected
			String message = ex.getMessage();
			assertTrue("Wrong message: " + message, message.indexOf("TransactionManager") >= 0);
		}

	}

	@Test
	public void testInvalidDatabaseType() throws Exception {

		factory.setDatabaseType("foo");
		try {
			expect(incrementerFactory.isSupportedIncrementerType("foo")).andReturn(false);
			expect(incrementerFactory.getSupportedIncrementerTypes()).andReturn(new String[0]);
			replay(incrementerFactory);
			factory.afterPropertiesSet();
			fail();
		}
		catch (IllegalArgumentException ex) {
			// expected
			String message = ex.getMessage();
			assertTrue("Wrong message: " + message, message.indexOf("foo") >= 0);
		}

	}

	@Test
	public void testCreateRepository() throws Exception {
		String databaseType = "foo";
		factory.setDatabaseType(databaseType);

		expect(incrementerFactory.isSupportedIncrementerType("foo")).andReturn(true);
		expect(incrementerFactory.getSupportedIncrementerTypes()).andReturn(new String[0]);
		expect(incrementerFactory.getIncrementer(databaseType, tablePrefix + "JOB_SEQ")).andReturn(new StubIncrementer());
		expect(incrementerFactory.getIncrementer(databaseType, tablePrefix + "JOB_EXECUTION_SEQ")).andReturn(new StubIncrementer());
		expect(incrementerFactory.getIncrementer(databaseType, tablePrefix + "STEP_EXECUTION_SEQ")).andReturn(new StubIncrementer());
		replay(incrementerFactory);

		factory.afterPropertiesSet();
		factory.getObject();

		verify(incrementerFactory);

	}

	@Test
	public void testTransactionAttributesForCreateMethodNullHypothesis() throws Exception {
		testCreateRepository();
		JobRepository repository = (JobRepository) factory.getObject();
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(
				DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		expect(transactionManager.getTransaction(transactionDefinition)).andReturn(null);
		replay(transactionManager);
		try {
			repository.createJobExecution(new JobSupport("job"), new JobParameters());
			// we expect an exception from the txControl because we provided the
			// wrong meta data
			fail("Expected IllegalArgumentException");
		}
		catch (AssertionError e) {
			// expected exception from txControl - wrong isolation level used in
			// comparison
			assertEquals("Unexpected method call", e.getMessage().substring(3, 25));
		}

	}

	@Test
	public void testTransactionAttributesForCreateMethod() throws Exception {

		testCreateRepository();
		JobRepository repository = (JobRepository) factory.getObject();
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(
				DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionDefinition.setIsolationLevel(DefaultTransactionDefinition.ISOLATION_SERIALIZABLE);
		expect(transactionManager.getTransaction(transactionDefinition)).andReturn(null);
		Connection conn = createNiceMock(Connection.class);
		expect(dataSource.getConnection()).andReturn(conn);
		replay(dataSource);
		replay(transactionManager);
		try {
			repository.createJobExecution(new JobSupport("job"), new JobParameters());
			// we expect an exception but not from the txControl because we
			// provided the correct meta data
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// expected exception from DataSourceUtils
			assertEquals("No Statement specified", e.getMessage());
		}

	}

	@Test
	public void testSetTransactionAttributesForCreateMethod() throws Exception {

		factory.setIsolationLevelForCreate("ISOLATION_READ_UNCOMMITTED");
		testCreateRepository();
		JobRepository repository = (JobRepository) factory.getObject();
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(
				DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionDefinition.setIsolationLevel(DefaultTransactionDefinition.ISOLATION_READ_UNCOMMITTED);
		expect(transactionManager.getTransaction(transactionDefinition)).andReturn(null);
		Connection conn = createNiceMock(Connection.class);
		expect(dataSource.getConnection()).andReturn(conn);
		replay(dataSource);
		replay(transactionManager);
		try {
			repository.createJobExecution(new JobSupport("job"), new JobParameters());
			// we expect an exception but not from the txControl because we
			// provided the correct meta data
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// expected exception from DataSourceUtils
			assertEquals("No Statement specified", e.getMessage());
		}

	}

	private static class StubIncrementer implements DataFieldMaxValueIncrementer {

		public int nextIntValue() throws DataAccessException {
			return 0;
		}

		public long nextLongValue() throws DataAccessException {
			return 0;
		}

		public String nextStringValue() throws DataAccessException {
			return null;
		}

	}

}
