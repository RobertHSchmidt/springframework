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

package org.springframework.batch.item.support;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.MarkFailedException;
import org.springframework.batch.item.ResetFailedException;

/**
 * An {@link ItemReader} that delivers a list as its item, storing up objects
 * from the injected {@link ItemReader} until they are ready to be packed out as
 * a collection. This class must be used as a wrapper for a custom
 * {@link ItemReader} that can identify the record boundaries. The custom reader
 * should mark the beginning and end of records by returning an
 * {@link AggregateItem} which responds true to its query methods
 * <code>is*()</code>.<br/><br/>
 * 
 * This class is thread safe (it can be used concurrently by multiple threads)
 * as long as the {@link ItemReader} is also thread safe.
 * 
 * @see AggregateItem#isHeader()
 * @see AggregateItem#isFooter()
 * 
 * @author Dave Syer
 * 
 */
public class AggregateItemReader<T> implements ItemReader<List<T>> {

	private static final Log log = LogFactory.getLog(AggregateItemReader.class);

	private ItemReader<AggregateItem<T>> itemReader;

	/**
	 * Get the next list of records.
	 * @throws Exception
	 * 
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	public List<T> read() throws Exception {
		ResultHolder holder = new ResultHolder();

		while (process(itemReader.read(), holder)) {
			continue;
		}

		if (!holder.exhausted) {
			return holder.records;
		}
		else {
			return null;
		}
	}

	private boolean process(AggregateItem<T> value, ResultHolder holder) {
		// finish processing if we hit the end of file
		if (value == null) {
			log.debug("Exhausted ItemReader");
			holder.exhausted = true;
			return false;
		}

		// start a new collection
		if (value.isHeader()) {
			log.debug("Start of new record detected");
			return true;
		}

		// mark we are finished with current collection
		if (value.isFooter()) {
			log.debug("End of record detected");
			return false;
		}

		// add a simple record to the current collection
		log.debug("Mapping: " + value);
		holder.records.add(value.getItem());
		return true;
	}

	public void mark() throws MarkFailedException {
		itemReader.mark();
	}

	public void reset() throws ResetFailedException {
		itemReader.reset();
	}

	public void setItemReader(ItemReader<AggregateItem<T>> itemReader) {
		this.itemReader = itemReader;
	}

	/**
	 * Private class for temporary state management while item is being
	 * collected.
	 * 
	 * @author Dave Syer
	 * 
	 */
	private class ResultHolder {
		List<T> records = new ArrayList<T>();

		boolean exhausted = false;
	}

}
