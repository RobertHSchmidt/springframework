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

package org.springframework.batch.repeat.support;

import junit.framework.TestCase;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSet;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.support.AbstractItemWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Base class for simple tests with small trade data set.
 *
 * @author Dave Syer
 *
 */
public abstract class AbstractTradeBatchTests extends TestCase {

	public static final int NUMBER_OF_ITEMS = 5;

	Resource resource = new ClassPathResource("trades.csv", getClass());

	protected TradeWriter processor = new TradeWriter();

	protected TradeItemReader provider;

	protected void setUp() throws Exception {
		super.setUp();
		provider = new TradeItemReader(resource);
		provider.open(new ExecutionContext());
	}

	protected static class TradeItemReader extends FlatFileItemReader<Trade> {

		protected TradeItemReader(Resource resource) throws Exception {
			super();
			setResource(resource);
			setFieldSetMapper(new TradeMapper());
			afterPropertiesSet();
		}

	}

	protected static class TradeMapper implements FieldSetMapper<Trade> {
		public Trade mapLine(FieldSet fs, int lineNum) {
			return new Trade(fs);
		}
	}

	protected static class TradeWriter extends AbstractItemWriter<Trade> {
		int count = 0;

		// This has to be synchronized because we are going to test the state
		// (count) at the end of a concurrent batch run.
		public synchronized void write(Trade data) {
			count++;
			System.out.println("Executing trade '" + data + "'");
		}
	}

}
