package org.springframework.batch.sample.item.reader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.domain.StepExecution;
import org.springframework.batch.core.domain.StepListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.exception.StreamException;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.batch.sample.item.writer.StagingItemWriter;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

public class StagingItemReader extends JdbcDaoSupport implements ItemStream, ItemReader, StepListener {

	// Key for buffer in transaction synchronization manager
	private static final String BUFFER_KEY = StagingItemReader.class.getName() + ".BUFFER";

	private static Log logger = LogFactory.getLog(StagingItemReader.class);

	private StepExecution stepExecution;

	private LobHandler lobHandler = new DefaultLobHandler();

	private Object lock = new Object();

	private volatile boolean initialized = false;

	private volatile Iterator keys;
	
	/**
	 * Public setter for the {@link LobHandler}.
	 * @param lobHandler the {@link LobHandler} to set (defaults to {@link DefaultLobHandler}).
	 */
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	/**
	 * 
	 * @see org.springframework.batch.io.driving.DrivingQueryItemReader#close(ExecutionContext)
	 */
	public void close(ExecutionContext executionContext) {
		initialized = false;
		keys = null;
		if (TransactionSynchronizationManager.hasResource(BUFFER_KEY)) {
			TransactionSynchronizationManager.unbindResource(BUFFER_KEY);
		}
	}

	/**
	 * 
	 * @see org.springframework.batch.io.driving.DrivingQueryItemReader#open()
	 */
	public void open(ExecutionContext executionContext) {
		// Can be called from multiple threads because of lazy initialisation...
		synchronized (lock) {
			if (keys == null) {
				keys = retrieveKeys().iterator();
				logger.info("Keys obtained for staging.");
				initialized = true;
			}
		}
	}

	private List retrieveKeys() {

		synchronized (lock) {

			return getJdbcTemplate().query(

			"SELECT ID FROM BATCH_STAGING WHERE JOB_ID=? AND PROCESSED=? ORDER BY ID",

			new Object[] { stepExecution.getJobExecution().getJobId(), StagingItemWriter.NEW },

			new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new Long(rs.getLong(1));
				}
			}

			);

		}

	}

	public Object read() throws Exception {
		Long id = doRead();

		if (id == null) {
			return null;
		}
		Object result = getJdbcTemplate().queryForObject("SELECT VALUE FROM BATCH_STAGING WHERE ID=?",
				new Object[] { id }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						byte[] blob = lobHandler.getBlobAsBytes(rs, 1);
						return SerializationUtils.deserialize(blob);
					}
				});
		// Update now - changes will rollback if there is a problem later.
		int count = getJdbcTemplate().update("UPDATE BATCH_STAGING SET PROCESSED=? WHERE ID=? AND PROCESSED=?",
				new Object[] { StagingItemWriter.DONE, id, StagingItemWriter.NEW });
		if (count != 1) {
			throw new OptimisticLockingFailureException("The staging record with ID=" + id
					+ " was updated concurrently when trying to mark as complete (updated " + count + " records.");
		}
		return result;
	}

	private Long doRead() {
		if (!initialized) {
			throw new StreamException("ItemStream must be open before it can be read.");
		}

		Long key = getBuffer().next();
		if (key == null) {
			synchronized (lock) {
				if (keys.hasNext()) {
					Assert.state(TransactionSynchronizationManager.isActualTransactionActive(),
							"Transaction not active for this thread.");
					Long next = (Long) keys.next();
					getBuffer().add(next);
					key = next;
					logger.debug("Retrieved key from list: " + key);
				}
			}
		}
		else {
			logger.debug("Retrieved key from buffer: " + key);
		}
		return key;

	}

	private StagingBuffer getBuffer() {
		if (!TransactionSynchronizationManager.hasResource(BUFFER_KEY)) {
			TransactionSynchronizationManager.bindResource(BUFFER_KEY, new StagingBuffer());
		}
		return (StagingBuffer) TransactionSynchronizationManager.getResource(BUFFER_KEY);
	}

	public boolean recover(Object data, Throwable cause) {
		return false;
	}

	private class StagingBuffer {

		private List list = new ArrayList();

		private Iterator iter = new ArrayList().iterator();

		public Long next() {
			if (iter.hasNext()) {
				return (Long) iter.next();
			}
			return null;
		}

		public void add(Long next) {
			list.add(next);
		}

		public void rollback() {
			logger.debug("Resetting buffer on rollback: " + list);
			iter = new ArrayList(list).iterator();
		}

		public void commit() {
			logger.debug("Clearing buffer on commit: " + list);
			list.clear();
			iter = new ArrayList().iterator();
		}

		public String toString() {
			return "list=" + list + "; iter.hasNext()=" + iter.hasNext();
		}
	}

	/**
	 * Mark is supported in a multi- as well as a single-threaded environment.
	 * The state backing the mark is a buffer, and access is synchronized, so
	 * multiple threads can be accommodated. Buffers are stored as transaction
	 * resources (using
	 * {@link TransactionSynchronizationManager#bindResource(Object, Object)}),
	 * so they are thread bound.
	 * 
	 * @see org.springframework.batch.item.ItemReader#mark()
	 */
	public void mark() {
		getBuffer().commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemStream#reset(org.springframework.batch.item.ExecutionContext)
	 */
	public void reset() {
		getBuffer().rollback();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ExecutionContextProvider#getExecutionContext()
	 */
	public void update(ExecutionContext executionContext) {
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.domain.StepListener#afterStep(StepExecution)
	 */
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.domain.StepListener#beforeStep(org.springframework.batch.core.domain.StepExecution)
	 */
	public void beforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.domain.StepListener#onErrorInStep(java.lang.Throwable)
	 */
	public ExitStatus onErrorInStep(Throwable e) {
		return null;
	}

}
