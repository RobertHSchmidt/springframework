package org.springframework.batch.sample.item.reader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.execution.scope.StepContext;
import org.springframework.batch.execution.scope.StepContextAware;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.KeyedItemReader;
import org.springframework.batch.item.StreamContext;
import org.springframework.batch.sample.item.writer.StagingItemWriter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

public class StagingItemReader extends JdbcDaoSupport implements ItemStream, KeyedItemReader, DisposableBean,
		StepContextAware {

	// Key for buffer in transaction synchronization manager
	private static final String BUFFER_KEY = StagingItemReader.class.getName() + ".BUFFER";

	private static Log logger = LogFactory.getLog(StagingItemReader.class);

	private StepContext stepContext;

	private LobHandler lobHandler = new DefaultLobHandler();

	private Object lock = new Object();

	private volatile boolean initialized = false;

	private volatile Iterator keys;

	/**
	 * 
	 * @see org.springframework.batch.io.driving.DrivingQueryItemReader#close()
	 */
	public void close() {
		initialized = false;
		keys = null;
		if (TransactionSynchronizationManager.hasResource(BUFFER_KEY)) {
			TransactionSynchronizationManager.unbindResource(BUFFER_KEY);
		}
	}

	/**
	 * @throws Exception
	 * @see org.springframework.batch.io.driving.DrivingQueryItemReader#destroy()
	 */
	public void destroy() throws Exception {
		close();
	}

	/**
	 * 
	 * @see org.springframework.batch.io.driving.DrivingQueryItemReader#open()
	 */
	public void open() {
		// Can be called from multiple threads because of lazy initialisation...
		synchronized (lock) {
			if (keys == null) {
				keys = retrieveKeys().iterator();
				logger.info("Keys obtained for staging.");
				initialized = true;
			}
		}
	}

	/**
	 * Callback for injection of the step context.
	 * 
	 * @param stepContext the stepContext to set
	 */
	public void setStepContext(StepContext stepContext) {
		this.stepContext = stepContext;
	}

	private List retrieveKeys() {

		synchronized (lock) {

			return getJdbcTemplate().query(

			"SELECT ID FROM BATCH_STAGING WHERE JOB_ID=? AND PROCESSED=? ORDER BY ID",

			new Object[] { stepContext.getStepExecution().getJobExecution().getJobId(), StagingItemWriter.NEW },

			new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new Long(rs.getLong(1));
				}
			}

			);

		}

	}

	public Object getKey(Object item) {
		return item;
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
			open();
		}

		Long key = getBuffer().next();
		if (key == null) {
			synchronized (lock) {
				if (keys.hasNext()) {
					Long next = (Long) keys.next();
					getBuffer().add(next);
					key = next;
					logger.debug("Retrieved key from list: " + key);
					Assert.state(TransactionSynchronizationManager.isActualTransactionActive(),
							"Transaction not active for this thread.");
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemStream#isMarkSupported()
	 */
	public boolean isMarkSupported() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemStream#mark(org.springframework.batch.item.StreamContext)
	 */
	public void mark(StreamContext streamContext) {
		getBuffer().commit();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemStream#reset(org.springframework.batch.item.StreamContext)
	 */
	public void reset(StreamContext streamContext) {
		getBuffer().rollback();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.ItemStream#restoreFrom(org.springframework.batch.item.StreamContext)
	 */
	public void restoreFrom(StreamContext context) {
		// no-op
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.item.StreamContextProvider#getStreamContext()
	 */
	public StreamContext getStreamContext() {
		return new StreamContext();
	}

}
