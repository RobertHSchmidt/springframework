package org.springframework.batch.io.stax;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.springframework.batch.io.InputSource;
import org.springframework.batch.io.Skippable;
import org.springframework.batch.repeat.synch.BatchTransactionSynchronizationManager;
import org.springframework.batch.restart.GenericRestartData;
import org.springframework.batch.restart.RestartData;
import org.springframework.batch.restart.Restartable;
import org.springframework.batch.statistics.StatisticsProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.util.Assert;

/**
 * Input source for reading XML input based on StAX.
 * 
 * It extracts fragments from the input XML document which correspond to records
 * for processing. The fragments are wrapped with StartDocument and EndDocument
 * events so that the fragments can be further processed like standalone XML
 * documents.
 * 
 * @author Robert Kasanicky
 */
public class StaxEventReaderInputSource implements InputSource, Skippable, Restartable, StatisticsProvider, InitializingBean, DisposableBean {

	public static final String READ_COUNT_STATISTICS_NAME = "StaxEventReaderInputSource.readCount";

	private static final String RESTART_DATA_NAME = "StaxEventReaderInputSource.recordcount";

	private FragmentEventReader fragmentReader;
	
	private TransactionalEventReader txReader;

	private FragmentDeserializer fragmentDeserializer;

	private Resource resource;

	private InputStream inputStream;

	private String fragmentRootElementName;

	private boolean initialized = false;

	private TransactionSynchronization synchronization = new StaxEventReaderInputSourceTransactionSychronization();

	private long lastCommitPointRecordCount = 0;

	private long currentRecordCount = 0;

	private List skipRecords = new ArrayList();

	public Object read() {
		if (!initialized) {
			open();
		}
		Object item = null;

		do {
			currentRecordCount++;
			if (moveCursorToNextFragment(fragmentReader)) {
				fragmentReader.markStartFragment();
				item = fragmentDeserializer.deserializeFragment(fragmentReader);
				fragmentReader.markFragmentProcessed();
			}
		} while (skipRecords.contains(new Long(currentRecordCount)));
		
		if (item == null) {
			currentRecordCount--;
		}
		return item;
	}

	// TODO make sure exception stack is not lost in any case.
	public void close() {
		try {
			initialized = false;
			fragmentReader.close();
		}
		catch (XMLStreamException e) {
			throw new DataAccessResourceFailureException("Error while closing event reader", e);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (IOException e) {
				throw new DataAccessResourceFailureException("Error while closing input stream", e);
			}
		}
	}

	public void open() {
		registerSynchronization();
		try {
			inputStream = resource.getInputStream();
			txReader = new DefaultTransactionalEventReader(XMLInputFactory
					.newInstance().createXMLEventReader(inputStream));
			fragmentReader = new DefaultFragmentEventReader(txReader);
		}
		catch (XMLStreamException xse) {
			throw new DataAccessResourceFailureException("Unable to create XML reader", xse);
		}
		catch (IOException ioe) {
			throw new DataAccessResourceFailureException("Unable to get input stream", ioe);
		}
		initialized = true;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @param fragmentDeserializer maps xml fragments corresponding to records to
	 * objects
	 */
	public void setFragmentDeserializer(FragmentDeserializer fragmentDeserializer) {
		this.fragmentDeserializer = fragmentDeserializer;
	}

	/**
	 * @param fragmentRootElementName name of the root element of the fragment 
	 * TODO String can be ambiguous due to namespaces, use QName?
	 */
	public void setFragmentRootElementName(String fragmentRootElementName) {
		this.fragmentRootElementName = fragmentRootElementName;
	}

	public void skip() {
		skipRecords.add(new Long(currentRecordCount));
	}

	/**
	 * @return Properties wrapper for the count of records read so far.
	 */
	public Properties getStatistics() {
		Properties statistics = new Properties();
		statistics.setProperty(READ_COUNT_STATISTICS_NAME, String.valueOf(currentRecordCount));
		return statistics;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(resource);
		Assert.state(resource.exists(), "Input resource does not exist: [" + resource + "]");
		Assert.notNull(fragmentDeserializer);
	}

	/**
	 * @return wrapped count of records read so far.
	 */
	public RestartData getRestartData() {
		Properties restartData = new Properties();

		restartData.setProperty(RESTART_DATA_NAME, String.valueOf(currentRecordCount));

		return new GenericRestartData(restartData);
	}

	/**
	 * Rereads (skips) the number of records extracted from restart data.
	 */
	public void restoreFrom(RestartData data) {
		if (data == null || data.getProperties() == null || 
				data.getProperties().getProperty(RESTART_DATA_NAME) == null) {
			return;
		}

		if (!initialized) {
			open();
		}

		long restoredRecordCount = Long.parseLong(data.getProperties().getProperty(RESTART_DATA_NAME));
		int REASONABLE_ADHOC_COMMIT_FREQUENCY = 10000;
		while (currentRecordCount <= restoredRecordCount) {
			currentRecordCount++;
			if (currentRecordCount % REASONABLE_ADHOC_COMMIT_FREQUENCY == 0) {
				txReader.onCommit(); // reset the history buffer
			}
			fragmentReader.next();
			moveCursorToNextFragment(fragmentReader);
		}
		txReader.onCommit(); // reset the history buffer
	}
	
	/**
	 * Responsible for moving the cursor before the StartElement of the fragment root.
	 * 
	 * This implementation simply looks for the next corresponding element, it does not care
	 * about element nesting. You will need to override this method to correctly handle 
	 * composite fragments.
	 * 
	 * @return <code>true</code> if next fragment was found, <code>false</code> otherwise.
	 */
	protected boolean moveCursorToNextFragment(XMLEventReader reader) {
		try {
			while (true) {
				while (reader.peek() != null && !reader.peek().isStartElement()) {
					reader.nextEvent();
				}
				if (reader.peek() == null) {
					return false;
				}
				QName startElementName = ((StartElement) reader.peek()).getName();
				if (startElementName.getLocalPart().equals(fragmentRootElementName)) {
					return true;
				} else {
					reader.nextEvent();
				}
			}
		}
		catch (XMLStreamException e) {
			throw new DataAccessResourceFailureException("Error while reading from event reader", e);
		}
	}
	
	// package visibility method for simulating transaction events
	TransactionSynchronization getSynchronization() {
		return synchronization;
	}
	
	/**
	 * Encapsulates transaction events for the StaxEventReaderInputSource.
	 */
	private class StaxEventReaderInputSourceTransactionSychronization extends TransactionSynchronizationAdapter {

		/**
		 * @param status
		 * @see org.springframework.transaction.support.TransactionSynchronizationAdapter#afterCompletion(int)
		 */
		public void afterCompletion(int status) {
			if (status == TransactionSynchronization.STATUS_COMMITTED) {
				lastCommitPointRecordCount = currentRecordCount;
				txReader.onCommit();
				skipRecords = new ArrayList();
			}
			else if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
				currentRecordCount = lastCommitPointRecordCount;
				txReader.onRollback();
				fragmentReader.reset();
			}
		}
		
	}

	public void destroy() throws Exception {
		close();
	}
	
	private void registerSynchronization() {
		BatchTransactionSynchronizationManager.registerSynchronization(synchronization);
	}

}
