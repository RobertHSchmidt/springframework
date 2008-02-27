package org.springframework.batch.sample.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.batch.core.domain.BatchStatus;
import org.springframework.batch.core.domain.JobExecution;
import org.springframework.batch.core.domain.JobParameters;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.sample.tasklet.JobSupport;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class JdbcJobRepositoryTests extends AbstractTransactionalDataSourceSpringContextTests {

	private JobRepository repository;

	private JobSupport jobConfiguration;

	private Set jobExecutionIds = new HashSet();

	private Set jobIds = new HashSet();

	private List list = new ArrayList();

	public void setRepository(JobRepository repository) {
		this.repository = repository;
	}

	protected String[] getConfigLocations() {
		return new String[] { "simple-container-definition.xml" };
	}

	protected void onSetUpInTransaction() throws Exception {
		jobConfiguration = new JobSupport("test-job");
		jobConfiguration.setRestartable(true);
	}

	protected void onSetUpBeforeTransaction() throws Exception {
		startNewTransaction();
		getJdbcTemplate().update("DELETE FROM BATCH_JOB_EXECUTION");
		getJdbcTemplate().update("DELETE FROM BATCH_JOB_INSTANCE");
		setComplete();
		endTransaction();
	}

	protected void onTearDownAfterTransaction() throws Exception {
		startNewTransaction();
		for (Iterator iterator = jobExecutionIds.iterator(); iterator.hasNext();) {
			Long id = (Long) iterator.next();
			getJdbcTemplate().update("DELETE FROM BATCH_JOB_EXECUTION where JOB_EXECUTION_ID=?", new Object[] { id });
		}
		for (Iterator iterator = jobIds.iterator(); iterator.hasNext();) {
			Long id = (Long) iterator.next();
			getJdbcTemplate().update("DELETE FROM BATCH_JOB_INSTANCE where JOB_INSTANCE_ID=?", new Object[] { id });
		}
		setComplete();
		endTransaction();
		for (Iterator iterator = jobIds.iterator(); iterator.hasNext();) {
			Long id = (Long) iterator.next();
			int count = getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE where JOB_INSTANCE_ID=?", new Object[] { id });
			assertEquals(0, count);
		}
	}

	public void testFindOrCreateJob() throws Exception {
		jobConfiguration.setName("foo");
		int before = getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
		JobExecution execution = repository.createJobExecution(jobConfiguration, new JobParameters());
		int after = getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
		assertEquals(before + 1, after);
		assertNotNull(execution.getId());
	}

	public void testFindOrCreateJobConcurrently() throws Exception {

		jobConfiguration.setName("bar");

		int before = getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
		assertEquals(0, before);

		endTransaction();
		startNewTransaction();

		JobExecution execution = null;
		long t0 = System.currentTimeMillis();
		try {
			execution = doConcurrentStart();
			fail("Expected JobExecutionAlreadyRunningException");
		}
		catch (JobExecutionAlreadyRunningException e) {
			// expected
		}
		long t1 = System.currentTimeMillis();

		if (execution == null) {
			execution = (JobExecution) list.get(0);
		}

		assertNotNull(execution);

		int after = getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
		assertNotNull(execution.getId());
		assertEquals(before + 1, after);

		logger.info("Duration: " + (t1 - t0)
				+ " - the second transaction did not block if this number is less than about 1000.");
	}

	public void testFindOrCreateJobConcurrentlyWhenJobAlreadyExists() throws Exception {

		jobConfiguration.setName("spam");

		JobExecution execution = repository.createJobExecution(jobConfiguration, new JobParameters());
		cacheJobIds(execution);
		execution.setEndTime(new Timestamp(System.currentTimeMillis()));
		repository.saveOrUpdate(execution);
		execution.setStatus(BatchStatus.FAILED);
		setComplete();
		endTransaction();

		startNewTransaction();

		int before = getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
		assertEquals(1, before);

		endTransaction();
		startNewTransaction();

		long t0 = System.currentTimeMillis();
		try {
			execution = doConcurrentStart();
			fail("Expected JobExecutionAlreadyRunningException");
		}
		catch (JobExecutionAlreadyRunningException e) {
			// expected
		}
		long t1 = System.currentTimeMillis();

		int after = getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE");
		assertNotNull(execution.getId());
		assertEquals(before, after);

		logger.info("Duration: " + (t1 - t0)
				+ " - the second transaction did not block if this number is less than about 1000.");
	}

	private void cacheJobIds(JobExecution execution) {
		if (execution == null)
			return;
		jobExecutionIds.add(execution.getId());
		jobIds.add(execution.getJobId());
	}

	private JobExecution doConcurrentStart() throws InterruptedException, JobExecutionAlreadyRunningException {
		new Thread(new Runnable() {
			public void run() {
				try {
					new TransactionTemplate(transactionManager).execute(new TransactionCallback() {
						public Object doInTransaction(org.springframework.transaction.TransactionStatus status) {
							try {
								JobExecution execution = repository.createJobExecution(jobConfiguration, new JobParameters());
								cacheJobIds(execution);
								list.add(execution);
								Thread.sleep(1000);
							}
							catch (Exception e) {
								list.add(e);
							}
							return null;
						}
					});
				}
				catch (RuntimeException e) {
					list.add(e);
				}

			}
		}).start();

		Thread.sleep(400);
		JobExecution execution = repository.createJobExecution(jobConfiguration, new JobParameters());
		cacheJobIds(execution);

		int count = 0;
		while (list.size() == 0 && count++ < 10) {
			Thread.sleep(200);
		}

		assertEquals("Timed out waiting for JobExecution to be created", 1, list.size());
		assertTrue("JobExecution not created in thread", list.get(0) instanceof JobExecution);
		return (JobExecution) list.get(0);
	}

}
