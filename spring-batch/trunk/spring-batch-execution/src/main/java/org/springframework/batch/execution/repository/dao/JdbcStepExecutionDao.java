package org.springframework.batch.execution.repository.dao;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.domain.BatchStatus;
import org.springframework.batch.core.domain.JobExecution;
import org.springframework.batch.core.domain.StepExecution;
import org.springframework.batch.core.domain.StepInstance;
import org.springframework.batch.execution.repository.dao.JdbcJobExecutionDao.JobExecutionRowMapper;
import org.springframework.batch.io.exception.BatchCriticalException;
import org.springframework.batch.item.ExecutionAttributes;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
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
public class JdbcStepExecutionDao extends AbstractJdbcBatchMetadataDao 
	implements StepExecutionDao, InitializingBean {

	private static final Log logger = LogFactory.getLog(JdbcStepExecutionDao.class);

	private static final String FIND_STEP_EXECUTION_ATTRS = "SELECT TYPE_CD, KEY_NAME, STRING_VAL, DOUBLE_VAL, LONG_VAL, OBJECT_VAL "
			+ "from %PREFIX%STEP_EXECUTION_ATTRS where STEP_EXECUTION_ID = ?";

	private static final String FIND_STEP_EXECUTIONS = "SELECT STEP_EXECUTION_ID, JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, COMMIT_COUNT,"
			+ " TASK_COUNT, TASK_STATISTICS, CONTINUABLE, EXIT_CODE, EXIT_MESSAGE from %PREFIX%STEP_EXECUTION where STEP_INSTANCE_ID = ?";

	private static final String GET_STEP_EXECUTION = "SELECT STEP_EXECUTION_ID, JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, COMMIT_COUNT,"
			+ " TASK_COUNT, TASK_STATISTICS, CONTINUABLE, EXIT_CODE, EXIT_MESSAGE from %PREFIX%STEP_EXECUTION where STEP_EXECUTION_ID = ?";

	private static final String GET_STEP_EXECUTION_COUNT = "SELECT count(STEP_EXECUTION_ID) from %PREFIX%STEP_EXECUTION where "
			+ "STEP_INSTANCE_ID = ?";

	private static final String INSERT_STEP_EXECUTION_ATTRS = "INSERT into %PREFIX%STEP_EXECUTION_ATTRS(STEP_EXECUTION_ID, TYPE_CD,"
			+ " KEY_NAME, STRING_VAL, DOUBLE_VAL, LONG_VAL, OBJECT_VAL) values(?,?,?,?,?,?,?)";

	private static final String SAVE_STEP_EXECUTION = "INSERT into %PREFIX%STEP_EXECUTION(STEP_EXECUTION_ID, VERSION, STEP_INSTANCE_ID, JOB_EXECUTION_ID, START_TIME, "
			+ "END_TIME, STATUS, COMMIT_COUNT, TASK_COUNT, TASK_STATISTICS, CONTINUABLE, EXIT_CODE, EXIT_MESSAGE) "
			+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE_STEP_EXECUTION_ATTRS = "UPDATE %PREFIX%STEP_EXECUTION_ATTRS set "
			+ "TYPE_CD = ?, STRING_VAL = ?, DOUBLE_VAL = ?, LONG_VAL = ?, OBJECT_VAL = ? where STEP_EXECUTION_ID = ? and KEY_NAME = ?";

	private static final String UPDATE_STEP_EXECUTION = "UPDATE %PREFIX%STEP_EXECUTION set START_TIME = ?, END_TIME = ?, "
			+ "STATUS = ?, COMMIT_COUNT = ?, TASK_COUNT = ?, TASK_STATISTICS = ?, CONTINUABLE = ? , EXIT_CODE = ?, "
			+ "EXIT_MESSAGE = ?, VERSION = ? where STEP_EXECUTION_ID = ? and VERSION = ?";
	
	private static final String FIND_LAST_STEP_EXECUTION = "SELECT STEP_EXECUTION_ID, JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, COMMIT_COUNT,"
		+ " TASK_COUNT, TASK_STATISTICS, CONTINUABLE, EXIT_CODE, EXIT_MESSAGE from %PREFIX%STEP_EXECUTION where STEP_INSTANCE_ID = ?"
		+ " and START_TIME = (SELECT max(START_TIME) FROM %PREFIX%STEP_EXECUTION where STEP_INSTANCE_ID = ?)";


	private static final int EXIT_MESSAGE_LENGTH = 250;

	private LobHandler lobHandler = new DefaultLobHandler();

	private DataFieldMaxValueIncrementer stepExecutionIncrementer;

	private JobExecutionDao jobExecutionDao;

	public ExecutionAttributes findExecutionAttributes(final Long executionId) {

		Assert.notNull(executionId, "ExecutionId must not be null.");

		final ExecutionAttributes executionAttributes = new ExecutionAttributes();

		RowCallbackHandler callback = new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {

				String typeCd = rs.getString("TYPE_CD");
				AttributeType type = AttributeType.getType(typeCd);
				String key = rs.getString("KEY_NAME");
				if (type == AttributeType.STRING) {
					executionAttributes.putString(key, rs.getString("STRING_VAL"));
				}
				else if (type == AttributeType.LONG) {
					executionAttributes.putLong(key, rs.getLong("LONG_VAL"));
				}
				else if (type == AttributeType.DOUBLE) {
					executionAttributes.putDouble(key, rs.getDouble("DOUBLE_VAL"));
				}
				else if (type == AttributeType.OBJECT) {
					executionAttributes.putLong(key, rs.getLong("OBJECT_VAL"));
				}
				else {
					throw new BatchCriticalException("Invalid type found: [" + typeCd + "] for execution id: ["
							+ executionId + "]");
				}
			}
		};

		getJdbcTemplate().query(getQuery(FIND_STEP_EXECUTION_ATTRS), new Object[] { executionId }, callback);

		return executionAttributes;
	}

	/**
	 * Get StepExecution for the given step. Due to the nature of statistics,
	 * they will not be returned with reconstituted object.
	 * 
	 * @see StepDao#getStepExecution(Long)
	 * @throws IllegalArgumentException if id is null.
	 */
	public List findStepExecutions(final StepInstance step) {

		Assert.notNull(step, "Step cannot be null.");
		Assert.notNull(step.getId(), "Step id cannot be null.");

		RowMapper rowMapper = new StepExecutionRowMapper(step);

		return getJdbcTemplate().query(getQuery(FIND_STEP_EXECUTIONS), new Object[] { step.getId() }, rowMapper);
	}

	public StepExecution getStepExecution(Long stepExecutionId, StepInstance stepInstance) {

		Assert.notNull(stepExecutionId, "Step Execution id must not be null");

		RowMapper rowMapper = new StepExecutionRowMapper(stepInstance);

		List executions = getJdbcTemplate().query(getQuery(GET_STEP_EXECUTION), new Object[] { stepExecutionId }, rowMapper);

		StepExecution stepExecution;
		if (executions.size() == 1) {
			stepExecution = (StepExecution) executions.get(0);
		}
		else if (executions.size() == 0) {
			stepExecution = null;
		}
		else {
			throw new IncorrectResultSizeDataAccessException("Only one StepExecution may exist for given id: ["
					+ stepExecutionId + "]", 1, executions.size());
		}

		return stepExecution;
	}
	
	public StepExecution getLastStepExecution(StepInstance stepInstance) {
		Long stepInstanceId = stepInstance.getId();
		List executions = getJdbcTemplate().query(getQuery(FIND_LAST_STEP_EXECUTION),
				new Object[] { stepInstanceId, stepInstanceId }, new StepExecutionRowMapper(stepInstance));

		Assert.state(executions.size() <= 1, "There must be at most one latest execution");

		if (executions.size() == 0) {
			return null;
		}
		else {
			return (StepExecution) executions.get(0);
		}
	}

	public int getStepExecutionCount(StepInstance step) {

		Object[] parameters = new Object[] { step.getId() };

		return getJdbcTemplate().queryForInt(getQuery(GET_STEP_EXECUTION_COUNT), parameters);
	}

	/**
	 * Insert execution attributes. A lob creator must be used, since any
	 * attributes that don't match a provided type must be serialized into a
	 * blob.
	 */
	public void saveExecutionAttributes(final Long executionId, final ExecutionAttributes executionAttributes) {

		Assert.notNull(executionId, "ExecutionId must not be null.");
		Assert.notNull(executionAttributes, "The ExecutionAttributes must not be null.");

		for (Iterator it = executionAttributes.entrySet().iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			final String key = entry.getKey().toString();
			final Object value = entry.getValue();

			if (value instanceof String) {
				insertExecutionAttribute(executionId, key, value, AttributeType.STRING);
			}
			else if (value instanceof Double) {
				insertExecutionAttribute(executionId, key, value, AttributeType.DOUBLE);
			}
			else if (value instanceof Long) {
				insertExecutionAttribute(executionId, key, value, AttributeType.LONG);
			}
			else {
				insertExecutionAttribute(executionId, key, value, AttributeType.OBJECT);
			}
		}
	}

	private void insertExecutionAttribute(final Long executionId, final String key, final Object value,
			final AttributeType type) {
		PreparedStatementCallback callback = new AbstractLobCreatingPreparedStatementCallback(lobHandler) {

			protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException,
					DataAccessException {

				ps.setLong(1, executionId.longValue());
				ps.setString(3, key);
				if (type == AttributeType.STRING) {
					ps.setString(2, AttributeType.STRING.toString());
					ps.setString(4, value.toString());
					ps.setDouble(5, 0.0);
					ps.setLong(6, 0);
					lobCreator.setBlobAsBytes(ps, 7, null);
				}
				else if (type == AttributeType.DOUBLE) {
					ps.setString(2, AttributeType.DOUBLE.toString());
					ps.setString(4, null);
					ps.setDouble(5, ((Double) value).doubleValue());
					ps.setLong(6, 0);
					lobCreator.setBlobAsBytes(ps, 7, null);
				}
				else if (type == AttributeType.LONG) {
					ps.setString(2, AttributeType.LONG.toString());
					ps.setString(4, null);
					ps.setDouble(5, 0.0);
					ps.setLong(6, ((Long) value).longValue());
					lobCreator.setBlobAsBytes(ps, 7, null);
				}
				else {
					ps.setString(2, AttributeType.OBJECT.toString());
					ps.setString(4, null);
					ps.setDouble(5, 0.0);
					ps.setLong(6, 0);
					lobCreator.setBlobAsBytes(ps, 7, SerializationUtils.serialize((Serializable) value));
				}
			}
		};

		getJdbcTemplate().execute(getQuery(INSERT_STEP_EXECUTION_ATTRS), callback);
	}

	/**
	 * Save a StepExecution. A unique id will be generated by the
	 * stepExecutionIncrementor, and then set in the StepExecution. All values
	 * will then be stored via an INSERT statement.
	 * 
	 * @see StepDao#saveStepExecution(StepExecution)
	 */
	public void saveStepExecution(StepExecution stepExecution) {

		validateStepExecution(stepExecution);

		cascadeJobExecution(stepExecution.getJobExecution());

		stepExecution.setId(new Long(stepExecutionIncrementer.nextLongValue()));
		stepExecution.incrementVersion(); // should be 0 now
		Object[] parameters = new Object[] { stepExecution.getId(), stepExecution.getVersion(),
				stepExecution.getStepId(), stepExecution.getJobExecutionId(), stepExecution.getStartTime(),
				stepExecution.getEndTime(), stepExecution.getStatus().toString(), stepExecution.getCommitCount(),
				stepExecution.getTaskCount(),
				PropertiesConverter.propertiesToString(stepExecution.getExecutionAttributes().getProperties()),
				stepExecution.getExitStatus().isContinuable() ? "Y" : "N", stepExecution.getExitStatus().getExitCode(),
				stepExecution.getExitStatus().getExitDescription() };
		getJdbcTemplate().update(getQuery(SAVE_STEP_EXECUTION), parameters, new int[] { Types.INTEGER, Types.INTEGER,
				Types.INTEGER, Types.INTEGER, Types.TIMESTAMP, Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER,
				Types.INTEGER, Types.VARCHAR, Types.CHAR, Types.VARCHAR, Types.VARCHAR });
	}

	private void cascadeJobExecution(JobExecution jobExecution) {
		if (jobExecution.getId() != null) {
			// assume already saved...
			return;
		}
		jobExecutionDao.saveJobExecution(jobExecution);
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
		Assert.notNull(stepExecution.getStepId(), "StepExecution Step-Id cannot be null.");
		Assert.notNull(stepExecution.getStartTime(), "StepExecution start time cannot be null.");
		Assert.notNull(stepExecution.getStatus(), "StepExecution status cannot be null.");
	}

	/**
	 * update execution attributes. A lob creator must be used, since any
	 * attributes that don't match a provided type must be serialized into a
	 * blob.
	 * 
	 * @see {@link LobCreator}
	 */
	public void updateExecutionAttributes(final Long executionId, ExecutionAttributes executionAttributes) {

		Assert.notNull(executionId, "ExecutionId must not be null.");
		Assert.notNull(executionAttributes, "The ExecutionAttributes must not be null.");

		for (Iterator it = executionAttributes.entrySet().iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			final String key = entry.getKey().toString();
			final Object value = entry.getValue();

			if (value instanceof String) {
				updateExecutionAttribute(executionId, key, value, AttributeType.STRING);
			}
			else if (value instanceof Double) {
				updateExecutionAttribute(executionId, key, value, AttributeType.DOUBLE);
			}
			else if (value instanceof Long) {
				updateExecutionAttribute(executionId, key, value, AttributeType.LONG);
			}
			else {
				updateExecutionAttribute(executionId, key, value, AttributeType.OBJECT);
			}
		}
	}

	private void updateExecutionAttribute(final Long executionId, final String key, final Object value,
			final AttributeType type) {

		PreparedStatementCallback callback = new AbstractLobCreatingPreparedStatementCallback(lobHandler) {

			protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException,
					DataAccessException {

				ps.setLong(6, executionId.longValue());
				ps.setString(7, key);
				if (type == AttributeType.STRING) {
					ps.setString(1, AttributeType.STRING.toString());
					ps.setString(2, value.toString());
					ps.setDouble(3, 0.0);
					ps.setLong(4, 0);
					lobCreator.setBlobAsBytes(ps, 5, null);
				}
				else if (type == AttributeType.DOUBLE) {
					ps.setString(1, AttributeType.DOUBLE.toString());
					ps.setString(2, null);
					ps.setDouble(3, ((Double) value).doubleValue());
					ps.setLong(4, 0);
					lobCreator.setBlobAsBytes(ps, 5, null);
				}
				else if (type == AttributeType.LONG) {
					ps.setString(1, AttributeType.LONG.toString());
					ps.setString(2, null);
					ps.setDouble(3, 0.0);
					ps.setLong(4, ((Long) value).longValue());
					lobCreator.setBlobAsBytes(ps, 5, null);
				}
				else {
					ps.setString(1, AttributeType.OBJECT.toString());
					ps.setString(2, null);
					ps.setDouble(3, 0.0);
					ps.setLong(4, 0);
					lobCreator.setBlobAsBytes(ps, 5, SerializationUtils.serialize((Serializable) value));
				}
			}
		};

		// LobCreating callbacks always return the affect row count for SQL DML
		// statements, if less than 1 row
		// is affected, then this row is new and should be inserted.
		Integer affectedRows = (Integer) getJdbcTemplate().execute(getQuery(UPDATE_STEP_EXECUTION_ATTRS), callback);
		if (affectedRows.intValue() < 1) {
			insertExecutionAttribute(executionId, key, value, type);
		}
	}

	public void updateStepExecution(StepExecution stepExecution) {

		validateStepExecution(stepExecution);
		Assert.notNull(stepExecution.getId(), "StepExecution Id cannot be null. StepExecution must saved"
				+ " before it can be updated.");

		// Do not check for existence of step execution considering
		// it is saved at every commit point.

		String exitDescription = stepExecution.getExitStatus().getExitDescription();
		if (exitDescription != null && exitDescription.length() > EXIT_MESSAGE_LENGTH) {
			exitDescription = exitDescription.substring(0, EXIT_MESSAGE_LENGTH);
			logger.debug("Truncating long message before update of StepExecution: " + stepExecution);
		}

		// Attempt to prevent concurrent modification errors by blocking here if
		// someone is already trying to do it.
		synchronized (stepExecution) {

			Integer version = new Integer(stepExecution.getVersion().intValue() + 1);
			Object[] parameters = new Object[] { stepExecution.getStartTime(), stepExecution.getEndTime(),
					stepExecution.getStatus().toString(), stepExecution.getCommitCount(), stepExecution.getTaskCount(),
					PropertiesConverter.propertiesToString(stepExecution.getExecutionAttributes().getProperties()),
					stepExecution.getExitStatus().isContinuable() ? "Y" : "N",
					stepExecution.getExitStatus().getExitCode(), exitDescription, version, stepExecution.getId(),
					stepExecution.getVersion() };
			int count = getJdbcTemplate().update(getQuery(UPDATE_STEP_EXECUTION), parameters, new int[] { Types.TIMESTAMP,
					Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.CHAR,
					Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER });

			// Avoid concurrent modifications...
			if (count == 0) {
				throw new OptimisticLockingFailureException("Attempt to update step execution id="
						+ stepExecution.getId() + " with wrong version (" + stepExecution.getVersion() + ")");
			}

			stepExecution.incrementVersion();

		}
	}

	private class StepExecutionRowMapper implements RowMapper {

		private final StepInstance stepInstance;

		public StepExecutionRowMapper(StepInstance stepInstance) {
			this.stepInstance = stepInstance;
		}

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

			JobExecution jobExecution = (JobExecution) getJdbcTemplate().queryForObject(
					getQuery(JobExecutionRowMapper.GET_JOB_EXECUTION), new Object[] { new Long(rs.getLong(2)) },
					new JobExecutionRowMapper(stepInstance.getJobInstance()));
			StepExecution stepExecution = new StepExecution(stepInstance, jobExecution, new Long(rs.getLong(1)));
			stepExecution.setStartTime(rs.getTimestamp(3));
			stepExecution.setEndTime(rs.getTimestamp(4));
			stepExecution.setStatus(BatchStatus.getStatus(rs.getString(5)));
			stepExecution.setCommitCount(rs.getInt(6));
			stepExecution.setTaskCount(rs.getInt(7));
			stepExecution.setExecutionAttributes(new ExecutionAttributes(PropertiesConverter.stringToProperties(rs
					.getString(8))));
			stepExecution
					.setExitStatus(new ExitStatus("Y".equals(rs.getString(9)), rs.getString(10), rs.getString(11)));
			return stepExecution;
		}

	}

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	public void setStepExecutionIncrementer(DataFieldMaxValueIncrementer stepExecutionIncrementer) {
		this.stepExecutionIncrementer = stepExecutionIncrementer;
	}

	public void setJobExecutionDao(JobExecutionDao jobExecutionDao) {
		this.jobExecutionDao = jobExecutionDao;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(stepExecutionIncrementer, "StepExecutionIncrementer cannot be null.");
		Assert.notNull(jobExecutionDao, "JobDao cannot be null");
	}

	public static class AttributeType {

		private final String type;

		private AttributeType(String type) {
			this.type = type;
		}

		public String toString() {
			return type;
		}

		public static final AttributeType STRING = new AttributeType("STRING");

		public static final AttributeType LONG = new AttributeType("LONG");

		public static final AttributeType OBJECT = new AttributeType("OBJECT");

		public static final AttributeType DOUBLE = new AttributeType("DOUBLE");

		private static final AttributeType[] VALUES = { STRING, OBJECT, LONG, DOUBLE };

		public static AttributeType getType(String typeAsString) {

			for (int i = 0; i < VALUES.length; i++) {
				if (VALUES[i].toString().equals(typeAsString)) {
					return (AttributeType) VALUES[i];
				}
			}

			return null;
		}
	}

	
}
