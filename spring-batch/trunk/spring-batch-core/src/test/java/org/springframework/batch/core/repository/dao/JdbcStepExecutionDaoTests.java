package org.springframework.batch.core.repository.dao;

import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "sql-dao-test.xml")
public class JdbcStepExecutionDaoTests extends AbstractStepExecutionDaoTests {

	protected StepExecutionDao getStepExecutionDao() {
		return (StepExecutionDao) applicationContext.getBean("stepExecutionDao");
	}

	protected JobRepository getJobRepository() {
		deleteFromTables("BATCH_EXECUTION_CONTEXT", "BATCH_STEP_EXECUTION", "BATCH_JOB_EXECUTION",
				"BATCH_JOB_PARAMS", "BATCH_JOB_INSTANCE");
		return (JobRepository) applicationContext.getBean("jobRepository");
	}

	/**
	 * Long exit descriptions are truncated on both save and update.
	 */
	@Transactional @Test
	public void testTruncateExitDescription() {
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 100; i++) {
			sb.append("too long exit description");
		}
		String longDescription = sb.toString();
		
		ExitStatus exitStatus = ExitStatus.FAILED.addExitDescription(longDescription);
		
		stepExecution.setExitStatus(exitStatus);

		((JdbcStepExecutionDao) dao).setExitMessageLength(250);
		dao.saveStepExecution(stepExecution);

		StepExecution retrievedAfterSave = dao.getStepExecution(jobExecution, step);

		assertTrue("Exit description should be truncated", retrievedAfterSave.getExitStatus().getExitDescription()
				.length() < stepExecution.getExitStatus().getExitDescription().length());

		dao.updateStepExecution(stepExecution);

		StepExecution retrievedAfterUpdate = dao.getStepExecution(jobExecution, step);

		assertTrue("Exit description should be truncated", retrievedAfterUpdate.getExitStatus().getExitDescription()
				.length() < stepExecution.getExitStatus().getExitDescription().length());
	}
}
