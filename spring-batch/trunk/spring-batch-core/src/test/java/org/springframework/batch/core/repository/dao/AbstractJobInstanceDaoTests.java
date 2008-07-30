package org.springframework.batch.core.repository.dao;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.JobSupport;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractJobInstanceDaoTests extends AbstractTransactionalJUnit4SpringContextTests {

	private static final long DATE = 777;

	private JobInstanceDao dao = new MapJobInstanceDao();

	private Job fooJob = new JobSupport("foo");

	private JobParameters fooParams = new JobParametersBuilder().addString("stringKey", "stringValue").addLong(
			"longKey", Long.MAX_VALUE).addDouble("doubleKey", Double.MAX_VALUE).addDate(
			"dateKey", new Date(DATE)).toJobParameters();

	protected abstract JobInstanceDao getJobInstanceDao();

	@Before
	public void onSetUp() throws Exception {
		dao = getJobInstanceDao();
	}

	/*
	 * Create and retrieve a job instance.
	 */
	@Transactional @Test
	public void testCreateAndRetrieve() throws Exception {

		JobInstance fooInstance = dao.createJobInstance(fooJob, fooParams);
		assertNotNull(fooInstance.getId());
		assertEquals(fooJob.getName(), fooInstance.getJobName());
		assertEquals(fooParams, fooInstance.getJobParameters());

		JobInstance retrievedInstance = dao.getJobInstance(fooJob, fooParams);
		JobParameters retrievedParams = retrievedInstance.getJobParameters();
		assertEquals(fooInstance, retrievedInstance);
		assertEquals(fooJob.getName(), retrievedInstance.getJobName());
		assertEquals(fooParams, retrievedParams);
		
		assertEquals(Long.MAX_VALUE, retrievedParams.getLong("longKey"));
		assertEquals(Double.MAX_VALUE, retrievedParams.getDouble("doubleKey"), 0.001);
		assertEquals("stringValue", retrievedParams.getString("stringKey"));
		assertEquals(new Date(DATE), retrievedParams.getDate("dateKey"));
	}

	/**
	 * Trying to create instance twice for the same job+parameters causes error
	 */
	@Transactional @Test
	public void testCreateDuplicateInstance() {

		dao.createJobInstance(fooJob, fooParams);

		try {
			dao.createJobInstance(fooJob, fooParams);
			fail();
		}
		catch (IllegalStateException e) {
			// expected
		}
	}

	@Transactional @Test
	public void testCreationAddsVersion() {

		JobInstance jobInstance = new JobInstance((long) 1, new JobParameters(), "testVersionAndId");

		assertNull(jobInstance.getVersion());

		jobInstance = dao.createJobInstance(new JobSupport("testVersion"), new JobParameters());

		assertNotNull(jobInstance.getVersion());
	}

}
