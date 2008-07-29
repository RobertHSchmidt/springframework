package org.springframework.batch.core.repository.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.util.Assert;

/**
 * Jdbc implementation of {@link StepExecutionDao}.<br/>
 * 
 * Allows customization of the tables names used by Spring Batch for step meta
 * data via a prefix property.<br/>
 * 
 * Uses sequences or tables (via Spring's {@link DataFieldMaxValueIncrementer}
 * abstraction) to create all primary keys before inserting a new row. All
 * objects are checked to ensure all fields to be stored are not null. If any
 * are found to be null, an IllegalArgumentException will be thrown. This could
 * be left to JdbcTemplate, however, the exception will be fairly vague, and
 * fails to highlight which field caused the exception.<br/>
 * 
 * @author Lucas Ward
 * @author Dave Syer
 * @author Robert Kasanicky
 * 
 * @see StepExecutionDao
 */
public class JdbcStepExecutionDao extends AbstractJdbcBatchMetadataDao implements StepExecutionDao, InitializingBean {

	private static final Log logger = LogFactory.getLog(JdbcStepExecutionDao.class);

	private static final String SAVE_STEP_EXECUTION = "INSERT into %PREFIX%STEP_EXECUTION(STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, START_TIME, "
			+ "END_TIME, STATUS, COMMIT_COUNT, ITEM_COUNT, CONTINUABLE, EXIT_CODE, EXIT_MESSAGE, READ_SKIP_COUNT, WRITE_SKIP_COUNT, ROLLBACK_COUNT) "
			+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE_STEP_EXECUTION = "UPDATE %PREFIX%STEP_EXECUTION set START_TIME = ?, END_TIME = ?, "
			+ "STATUS = ?, COMMIT_COUNT = ?, ITEM_COUNT = ?, CONTINUABLE = ? , EXIT_CODE = ?, "
			+ "EXIT_MESSAGE = ?, VERSION = ?, READ_SKIP_COUNT = ?, WRITE_SKIP_COUNT = ?, ROLLBACK_COUNT = ? where STEP_EXECUTION_ID = ? and VERSION = ?";

	private static final String GET_STEP_EXECUTION = "SELECT STEP_EXECUTION_ID, STEP_NAME, START_TIME, END_TIME, STATUS, COMMIT_COUNT,"
			+ " ITEM_COUNT, CONTINUABLE, EXIT_CODE, EXIT_MESSAGE, READ_SKIP_COUNT, WRITE_SKIP_COUNT, ROLLBACK_COUNT from %PREFIX%STEP_EXECUTION where STEP_NAME = ? and JOB_EXECUTION_ID = ?";

	private static final String CURRENT_VERSION_STEP_EXECUTION = "SELECT VERSION FROM %PREFIX%STEP_EXECUTION WHERE STEP_EXECUTION_ID=?";

	private static final int DEFAULT_EXIT_MESSAGE_LENGTH = 2500;

	private int exitMessageLength = DEFAULT_EXIT_MESSAGE_LENGTH;

	private DataFieldMaxValueIncrementer stepExecutionIncrementer;

	/**
	 * Public setter for the exit message length in database. Do not set this if
	 * you haven't modified the schema.
	 * @param exitMessageLength the exitMessageLength to set
	 */
	public void setExitMessageLength(int exitMessageLength) {
		this.exitMessageLength = exitMessageLength;
	}

	/**
	 * Save a StepExecution. A unique id will be generated by the
	 * stepExecutionIncrementor, and then set in the StepExecution. All values
	 * will then be stored via an INSERT statement.
	 * 
	 * @see StepExecutionDao#saveStepExecution(StepExecution)
	 */
	public void saveStepExecution(StepExecution stepExecution) {

		Assert.isNull(stepExecution.getId(),
				"to-be-saved (not updated) StepExecution can't already have an id assigned");
		Assert.isNull(stepExecution.getVersion(),
				"to-be-saved (not updated) StepExecution can't already have a version assigned");

		validateStepExecution(stepExecution);

		String exitDescription = truncateExitDescription(stepExecution.getExitStatus().getExitDescription());

		stepExecution.setId(new Long(stepExecutionIncrementer.nextLongValue()));
		stepExecution.incrementVersion(); // should be 0 now
		Object[] parameters = new Object[] { stepExecution.getId(), stepExecution.getVersion(),
				stepExecution.getStepName(), stepExecution.getJobExecutionId(), stepExecution.getStartTime(),
				stepExecution.getEndTime(), stepExecution.getStatus().toString(), stepExecution.getCommitCount(),
				stepExecution.getItemCount(), stepExecution.getExitStatus().isContinuable() ? "Y" : "N",
				stepExecution.getExitStatus().getExitCode(), exitDescription, stepExecution.getReadSkipCount(),
				stepExecution.getWriteSkipCount(), stepExecution.getRollbackCount() };
		getJdbcTemplate().update(
				getQuery(SAVE_STEP_EXECUTION),
				parameters,
				new int[] { Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.TIMESTAMP,
						Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.CHAR, Types.VARCHAR,
						Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER });
	}

	/**
	 * Validate StepExecution. At a minimum, JobId, StartTime, and Status cannot
	 * be null. EndTime can be null for an unfinished job.
	 * 
	 * @param jobExecution
	 * @throws IllegalArgumentException
	 */
	private void validateStepExecution(StepExecution stepExecution) {
		Assert.notNull(stepExecution);
		Assert.notNull(stepExecution.getStepName(), "StepExecution step name cannot be null.");
		Assert.notNull(stepExecution.getStartTime(), "StepExecution start time cannot be null.");
		Assert.notNull(stepExecution.getStatus(), "StepExecution status cannot be null.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.execution.repository.dao.StepExecutionDao#updateStepExecution(org.springframework.batch.core.domain.StepExecution)
	 */
	public void updateStepExecution(StepExecution stepExecution) {

		validateStepExecution(stepExecution);
		Assert.notNull(stepExecution.getId(), "StepExecution Id cannot be null. StepExecution must saved"
				+ " before it can be updated.");

		// Do not check for existence of step execution considering
		// it is saved at every commit point.

		String exitDescription = truncateExitDescription(stepExecution.getExitStatus().getExitDescription());

		// Attempt to prevent concurrent modification errors by blocking here if
		// someone is already trying to do it.
		synchronized (stepExecution) {

			Integer version = new Integer(stepExecution.getVersion().intValue() + 1);
			Object[] parameters = new Object[] { stepExecution.getStartTime(), stepExecution.getEndTime(),
					stepExecution.getStatus().toString(), stepExecution.getCommitCount(), stepExecution.getItemCount(),
					stepExecution.getExitStatus().isContinuable() ? "Y" : "N",
					stepExecution.getExitStatus().getExitCode(), exitDescription, version,
					stepExecution.getReadSkipCount(), stepExecution.getWriteSkipCount(),
					stepExecution.getRollbackCount(), stepExecution.getId(), stepExecution.getVersion() };
			int count = getJdbcTemplate().update(
					getQuery(UPDATE_STEP_EXECUTION),
					parameters,
					new int[] { Types.TIMESTAMP, Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER, Types.INTEGER,
							Types.CHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER,
							Types.INTEGER, Types.INTEGER, Types.INTEGER });

			// Avoid concurrent modifications...
			if (count == 0) {
				int curentVersion = getJdbcTemplate().queryForInt(getQuery(CURRENT_VERSION_STEP_EXECUTION),
						new Object[] { stepExecution.getId() });
				throw new OptimisticLockingFailureException("Attempt to update step execution id="
						+ stepExecution.getId() + " with wrong version (" + stepExecution.getVersion()
						+ "), where current version is " + curentVersion);
			}

			stepExecution.incrementVersion();

		}
	}

	/**
	 * Truncate the exit description if the length exceeds
	 * {@link #EXIT_MESSAGE_LENGTH}.
	 * @param description the string to truncate
	 * @return truncated description
	 */
	private String truncateExitDescription(String description) {
		if (description != null && description.length() > exitMessageLength) {
			logger.debug("Truncating long message before update of StepExecution, original message is: " + description);
			return description.substring(0, exitMessageLength);
		}
		else {
			return description;
		}
	}

	private class StepExecutionRowMapper implements RowMapper {

		private final JobExecution jobExecution;

		private final Step step;

		public StepExecutionRowMapper(JobExecution jobExecution, Step step) {
			this.jobExecution = jobExecution;
			this.step = step;
		}

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

			StepExecution stepExecution = new StepExecution(step.getName(), jobExecution, new Long(rs.getLong(1)));
			stepExecution.setStartTime(rs.getTimestamp(3));
			stepExecution.setEndTime(rs.getTimestamp(4));
			stepExecution.setStatus(BatchStatus.valueOf(rs.getString(5)));
			stepExecution.setCommitCount(rs.getInt(6));
			stepExecution.setItemCount(rs.getInt(7));
			stepExecution.setExitStatus(new ExitStatus("Y".equals(rs.getString(8)), rs.getString(9), rs.getString(10)));
			stepExecution.setReadSkipCount(rs.getInt(11));
			stepExecution.setWriteSkipCount(rs.getInt(12));
			stepExecution.setRollbackCount(rs.getInt(13));
			return stepExecution;
		}

	}

	public void setStepExecutionIncrementer(DataFieldMaxValueIncrementer stepExecutionIncrementer) {
		this.stepExecutionIncrementer = stepExecutionIncrementer;
	}

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Assert.notNull(stepExecutionIncrementer, "StepExecutionIncrementer cannot be null.");
	}

	@SuppressWarnings("unchecked")
	public StepExecution getStepExecution(JobExecution jobExecution, Step step) {
		List<StepExecution> executions = getJdbcTemplate().query(getQuery(GET_STEP_EXECUTION),
				new Object[] { step.getName(), jobExecution.getId() }, new StepExecutionRowMapper(jobExecution, step));

		Assert.state(executions.size() <= 1,
				"There can be at most one step execution with given name for single job execution");
		if (executions.isEmpty()) {
			return null;
		}
		else {
			return (StepExecution) executions.get(0);
		}
	}

}
