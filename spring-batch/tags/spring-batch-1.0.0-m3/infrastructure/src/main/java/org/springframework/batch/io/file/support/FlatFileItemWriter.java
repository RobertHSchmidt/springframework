/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.batch.io.file.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.springframework.batch.io.ItemWriter;
import org.springframework.batch.io.exception.BatchCriticalException;
import org.springframework.batch.io.exception.BatchEnvironmentException;
import org.springframework.batch.io.file.support.transform.Converter;
import org.springframework.batch.io.support.AbstractTransactionalIoSource;
import org.springframework.batch.item.ResourceLifecycle;
import org.springframework.batch.restart.GenericRestartData;
import org.springframework.batch.restart.RestartData;
import org.springframework.batch.restart.Restartable;
import org.springframework.batch.statistics.StatisticsProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * This class is an output target that writes data to a file or stream. The
 * writer also provides restart, statistics and transaction features by
 * implementing corresponding interfaces where possible (with a file). The
 * location of the file is defined by a {@link Resource} and must represent a
 * writable file.<br/>
 * 
 * Uses buffered writer to improve performance.<br/>
 * 
 * Use {@link #write(String)} method to output a line to an item writer.
 * 
 * @author Waseem Malik
 * @author Tomas Slanina
 * @author Robert Kasanicky
 * @author Dave Syer
 */
public class FlatFileItemWriter extends AbstractTransactionalIoSource implements 
	ItemWriter, ResourceLifecycle, Restartable, StatisticsProvider, InitializingBean,
	DisposableBean {

	/**
	 * @author dsyer
	 * 
	 */
	public static class BooleanHolder {

		public boolean value;

	}

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static final String WRITTEN_STATISTICS_NAME = "Written";

	public static final String RESTART_COUNT_STATISTICS_NAME = "Restart count";

	public static final String RESTART_DATA_NAME = "flatfileoutputtemplate.currentLine";

	private Resource resource;

	private Properties statistics = new Properties();

	private RestartData restartData = new GenericRestartData(new Properties());

	private OutputState state = new OutputState();

	private Converter converter = new Converter() {
		public Object convert(Object input) {
			return "" + input;
		}
	};

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(resource);
		File file = resource.getFile();
		Assert.state(!file.exists() || file.canWrite(), "Resource is not writable: [" + resource + "]");
	}

	/**
	 * Public setter for the converter. If not-null this will be used to convert
	 * the input data before it is output.
	 * 
	 * @param converter the converter to set
	 */
	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	/**
	 * Setter for resource. Represents a file that can be written.
	 * 
	 * @param resource
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Commit the transaction.   
	 */
	protected void transactionCommitted() {
		getOutputState().mark();
	}

	/**
	 * Rollback the transaction.
	 */
	protected void transactionRolledBack() {
		getOutputState().checkFileSize();
		resetPositionForRestart();
	}

	// This method removes any information in the file before this reset point.
	private void resetPositionForRestart() {
		getOutputState().truncate();
	}

	/**
	 * Writes out a string followed by a "new line", where the format of the new
	 * line separator is determined by the underlying operating system. If the
	 * input is not a String and a converter is available the converter will be
	 * applied and then this method recursively called with the result. If the
	 * input is an array or collection each value will be written to a separate
	 * line (recursively calling this method for each value). If no converter is
	 * supplied the input object's toString method will be used.<br/>
	 * 
	 * @param data Object (a String or Object that can be converted) to be
	 * written to output stream
	 */
	public void write(Object data) {
		convertAndWrite(data, new BooleanHolder());
	}

	/**
	 * Convert the date to a format that can be output and then write it out.
	 * @param data
	 * @param converted
	 */
	private void convertAndWrite(Object data, BooleanHolder converted) {

		if (data instanceof Collection) {
			converted.value = false;
			for (Iterator iterator = ((Collection) data).iterator(); iterator.hasNext();) {
				Object value = (Object) iterator.next();
				// (recursive)
				write(value);
			}
			return;
		}
		if (data.getClass().isArray()) {
			converted.value = false;
			Object[] array = (Object[]) data;
			for (int i = 0; i < array.length; i++) {
				Object value = array[i];
				// (recursive)
				write(value);
			}
			return;
		}
		if (data instanceof String) {
			// This is where the output stream is actually written to
			getOutputState().write(data + LINE_SEPARATOR);
		}
		else if (!converted.value) {
			// (recursive)
			converted.value = true;
			convertAndWrite(converter.convert(data), converted);
			return;
		}
		else {
			// Should not happen...
			throw new IllegalStateException(
					"Infinite loop detected - converter did not convert to String or collection/array of objects convertible to String.");
		}
	}

	/**
	 * @see ResourceLifecycle#close()
	 */
	public void close() {
		getOutputState().close();
	}

	/**
	 * Calls close to ensure that bean factories can close and always release
	 * resources.
	 * 
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		close();
	}

	/**
	 * Sets encoding for output template.
	 */
	public void setEncoding(String newEncoding) {
		getOutputState().setEncoding(newEncoding);
	}

	/**
	 * Sets buffer size for output template
	 */
	public void setBufferSize(int newSize) {
		getOutputState().setBufferSize(newSize);
	}

	/**
	 * @param shouldDeleteIfExists the shouldDeleteIfExists to set
	 */
	public void setShouldDeleteIfExists(boolean shouldDeleteIfExists) {
		getOutputState().setShouldDeleteIfExists(shouldDeleteIfExists);
	}

	/**
	 * Initialize the Output Template.
	 * @see ResourceLifecycle#open()
	 */
	public void open() {
		super.registerSynchronization();
	}

	/**
	 * @see StatisticsProvider
	 */
	public Properties getStatistics() {
		final OutputState os = getOutputState();

		statistics.setProperty(WRITTEN_STATISTICS_NAME, String.valueOf(os.linesWritten));
		statistics.setProperty(RESTART_COUNT_STATISTICS_NAME, String.valueOf(os.restartCount));
		return statistics;
	}

	/**
	 * @see Restartable#getRestartData()
	 */
	public RestartData getRestartData() {
		final OutputState os = getOutputState();

		restartData.getProperties().setProperty(RESTART_DATA_NAME, String.valueOf(os.position()));
		return restartData;
	}

	/**
	 * @see Restartable#restoreFrom(RestartData)
	 */
	public void restoreFrom(RestartData data) {
		if (data == null)
			return;

		getOutputState().restoreFrom(data.getProperties());

	}

	// Returns object representing state.
	private OutputState getOutputState() {
		return (OutputState) state;
	}

	/**
	 * Encapsulates the runtime state of the writer. All state changing
	 * operations on the writer go through this class.
	 */
	private class OutputState {
		// default encoding for writing to output files - set to UTF-8.
		private static final String DEFAULT_CHARSET = "UTF-8";

		private static final int DEFAULT_BUFFER_SIZE = 2048;

		// The bufferedWriter over the file channel that is actually written
		BufferedWriter outputBufferedWriter;

		FileChannel fileChannel;

		// this represents the charset encoding (if any is needed) for the
		// output file
		String encoding = DEFAULT_CHARSET;

		// Optional write buffer size
		int bufferSize = DEFAULT_BUFFER_SIZE;

		boolean restarted = false;

		boolean initialized = false;

		long lastMarkedByteOffsetPosition = 0;

		long linesWritten = 0;

		long restartCount = 0;

		boolean shouldDeleteIfExists = true;

		/**
		 * Return the byte offset position of the cursor in the output file as a
		 * long integer.
		 */
		public long position() {
			long pos = 0;

			if (fileChannel == null) {
				return 0;
			}

			try {
				outputBufferedWriter.flush();
				pos = fileChannel.position();
			}
			catch (IOException e) {
				throw new BatchCriticalException("An Error occured while trying to get filechannel position", e);
			}

			return pos;

		}

		/**
		 * @param properties
		 */
		public void restoreFrom(Properties properties) {
			lastMarkedByteOffsetPosition = Long.parseLong(properties.getProperty(RESTART_DATA_NAME));
			restarted = true;
		}

		/**
		 * @param shouldDeleteIfExists2
		 */
		public void setShouldDeleteIfExists(boolean shouldDeleteIfExists) {
			this.shouldDeleteIfExists = shouldDeleteIfExists;
		}

		/**
		 * @param newSize
		 */
		public void setBufferSize(int newSize) {
			bufferSize = newSize;
		}

		/**
		 * @param newEncoding
		 */
		public void setEncoding(String newEncoding) {
			encoding = newEncoding;
		}

		/**
		 * Close the open resource and reset counters.
		 */
		public void close() {
			initialized = false;
			restarted = false;
			try {
				if (outputBufferedWriter == null) {
					return;
				}
				outputBufferedWriter.close();
				fileChannel.close();
			}
			catch (IOException ioe) {
				throw new BatchEnvironmentException("Unable to close the the ItemWriter", ioe);
			}
		}

		/**
		 * @param data
		 * @param offset
		 * @param length
		 */
		public void write(String line) {
			if (!initialized) {
				initializeBufferedWriter();
			}

			try {
				outputBufferedWriter.write(line);
				outputBufferedWriter.flush();
				linesWritten++;
			}
			catch (IOException e) {
				throw new BatchCriticalException("An Error occured while trying to write to FlatFileItemWriter", e);
			}
		}

		/**
		 * Truncate the output at the last known good point.
		 */
		public void truncate() {
			try {
				fileChannel.truncate(lastMarkedByteOffsetPosition);
				fileChannel.position(lastMarkedByteOffsetPosition);
			}
			catch (Exception e) {
				throw new BatchCriticalException("An Error occured while reseting position in a file for restart", e);
			}
		}

		/**
		 * Mark the current position.
		 */
		public void mark() {
			lastMarkedByteOffsetPosition = this.position();
		}

		/**
		 * Creates the buffered writer for the output file channel based on
		 * configuration information.
		 */
		private void initializeBufferedWriter() {
			File file;

			try {
				file = resource.getFile();

				// If the output source was restarted, keep existing file.
				// If the output source was not restarted, check following:
				// - if the file should be deleted, delete it if it was exiting
				// and create blank file,
				// - if the file should not be deleted, if it already exists,
				// throw an exception,
				// - if the file was not existing, create new.
				if (!restarted) {
					if (file.exists()) {
						if (shouldDeleteIfExists) {
							file.delete();
						}
						else {
							throw new BatchEnvironmentException("Resource already exists: " + resource);
						}
					}
					String parent = file.getParent();
					if (parent!=null) {
						new File(parent).mkdirs();
					}
					file.createNewFile();
				}

			}
			catch (IOException ioe) {
				throw new DataAccessResourceFailureException("Unable to write to file resource: [" + resource + "]",
						ioe);
			}

			try {
				fileChannel = (new FileOutputStream(file.getAbsolutePath(), true)).getChannel();
			}
			catch (FileNotFoundException fnfe) {
				throw new BatchEnvironmentException("Bad filename property parameter " + file, fnfe);
			}

			outputBufferedWriter = getBufferedWriter(fileChannel, encoding, bufferSize);

			// in case of restarting reset position to last commited point
			if (restarted) {
				this.resetPosition();
			}

			initialized = true;
			linesWritten = 0;
		}

		/**
		 * Returns the buffered writer opened to the beginning of the file
		 * specified by the absolute path name contained in absoluteFileName.
		 */
		private BufferedWriter getBufferedWriter(FileChannel fileChannel, String encoding, int bufferSize) {
			try {

				BufferedWriter outputBufferedWriter = null;

				// If a buffer was requested, allocate.
				if (bufferSize > 0) {
					outputBufferedWriter = new BufferedWriter(Channels.newWriter(fileChannel, encoding), bufferSize);
				}
				else {
					outputBufferedWriter = new BufferedWriter(Channels.newWriter(fileChannel, encoding));
				}

				return outputBufferedWriter;
			}
			catch (UnsupportedCharsetException ucse) {
				throw new BatchEnvironmentException("Bad encoding configuration for output file " + fileChannel, ucse);
			}
		}

		/**
		 * Resets the file writer's current position to the point stored in the
		 * last marked byte offset position variable. It first checks to make
		 * sure the current size of the file is not less than the byte position
		 * to be moved to (if it is, throws an environment exception), then it
		 * truncates the file to that reset position, and set the cursor to
		 * start writing at that point.
		 */
		private void resetPosition() {
			checkFileSize();
			resetPositionForRestart();
		}

		/**
		 * Checks (on setState) to make sure that the current output file's size
		 * is not smaller than the last saved commit point. If it is, then the
		 * file has been damaged in some way and whole task must be started over
		 * again from the beginning.
		 */
		public void checkFileSize() {
			long size = -1;

			try {
				outputBufferedWriter.flush();
				size = fileChannel.size();
			}
			catch (Exception e) {
				throw new BatchCriticalException("An Error occured while checking file size", e);
			}

			if (size < lastMarkedByteOffsetPosition) {
				throw new BatchCriticalException("Current file size is smaller than size at last commit");
			}
		}

	}
}
