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
package org.springframework.batch.core.step.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.job.JobSupport;
import org.springframework.batch.core.listener.SkipListenerSupport;
import org.springframework.batch.core.repository.dao.MapJobExecutionDao;
import org.springframework.batch.core.repository.dao.MapJobInstanceDao;
import org.springframework.batch.core.repository.dao.MapStepExecutionDao;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.core.step.AbstractStep;
import org.springframework.batch.item.AbstractItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.retry.RetryException;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Dave Syer
 * 
 */
public class StatefulRetryStepFactoryBeanTests extends TestCase {

	protected final Log logger = LogFactory.getLog(getClass());

	private StatefulRetryStepFactoryBean factory = new StatefulRetryStepFactoryBean();

	private List recovered = new ArrayList();

	private List processed = new ArrayList();

	int count = 0;

	private SimpleJobRepository repository = new SimpleJobRepository(new MapJobInstanceDao(), new MapJobExecutionDao(),
			new MapStepExecutionDao());

	JobExecution jobExecution;

	private ItemWriter processor = new AbstractItemWriter() {
		public void write(Object data) throws Exception {
			processed.add((String) data);
		}
	};

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {

		MapJobInstanceDao.clear();
		MapJobExecutionDao.clear();
		MapStepExecutionDao.clear();

		factory.setBeanName("step");

		factory.setItemReader(new ListItemReader(new ArrayList()));
		factory.setItemWriter(processor);
		factory.setJobRepository(repository);
		factory.setTransactionManager(new ResourcelessTransactionManager());
		factory.setRetryableExceptionClasses(new Class[] { Exception.class });

		JobSupport job = new JobSupport("jobName");
		job.setRestartable(true);
		JobParameters jobParameters = new JobParametersBuilder().addString("statefulTest", "make_this_unique")
				.toJobParameters();
		jobExecution = repository.createJobExecution(job, jobParameters);
		jobExecution.setEndTime(new Date());

	}

	public void testType() throws Exception {
		assertEquals(Step.class, factory.getObjectType());
	}

	public void testDefaultValue() throws Exception {
		assertTrue(factory.getObject() instanceof Step);
	}

	/**
	 * N.B. this doesn't really test retry, since the retry is only on write
	 * failures, but it does test that read errors are re-presented for another
	 * try when the retryLimit is high enough (it is used to build an exception
	 * handler).
	 * 
	 * @throws Exception
	 */
	public void testSuccessfulRetryWithReadFailure() throws Exception {
		List items = TransactionAwareProxyFactory.createTransactionalList();
		items.addAll(Arrays.asList(new String[] { "a", "b", "c" }));
		ItemReader provider = new ListItemReader(items) {
			public Object read() {
				Object item = super.read();
				count++;
				if (count == 2) {
					throw new RuntimeException("Temporary error - retry for success.");
				}
				return item;
			}
		};
		factory.setItemReader(provider);
		factory.setRetryLimit(10);
		factory.setSkippableExceptionClasses(new Class[0]);
		AbstractStep step = (AbstractStep) factory.getObject();

		StepExecution stepExecution = new StepExecution(step.getName(), jobExecution);
		step.execute(stepExecution);

		assertEquals(0, stepExecution.getSkipCount());

		// b is processed twice, plus a, plus c, plus the null at end
		assertEquals(5, count);
	}

	public void testSkipAndRetry() throws Exception {
		factory.setSkippableExceptionClasses(new Class[] { Exception.class });
		factory.setSkipLimit(2);
		List items = TransactionAwareProxyFactory.createTransactionalList();
		items.addAll(Arrays.asList(new String[] { "a", "b", "c", "d", "e", "f" }));
		ItemReader provider = new ListItemReader(items) {
			public Object read() {
				Object item = super.read();
				count++;
				if ("b".equals(item) || "d".equals(item)) {
					throw new RuntimeException("Read error - planned but skippable.");
				}
				return item;
			}
		};
		factory.setItemReader(provider);
		factory.setRetryLimit(10);
		AbstractStep step = (AbstractStep) factory.getObject();

		StepExecution stepExecution = new StepExecution(step.getName(), jobExecution);
		step.execute(stepExecution);

		assertEquals(2, stepExecution.getSkipCount());
		// b is processed once and skipped, plus 1, plus c, plus the null at end
		assertEquals(7, count);
	}

	public void testSkipAndRetryWithWriteFailure() throws Exception {

		factory.setSkippableExceptionClasses(new Class[] { RetryException.class });
		factory.setListeners(new StepListener[] { new SkipListenerSupport() {
			public void onSkipInWrite(Object item, Throwable t) {
				recovered.add(item);
				assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			}
		} });
		factory.setSkipLimit(2);
		List items = TransactionAwareProxyFactory.createTransactionalList();
		items.addAll(Arrays.asList(new String[] { "a", "b", "c", "d", "e", "f" }));
		ItemReader provider = new ListItemReader(items) {
			public Object read() {
				Object item = super.read();
				logger.debug("Read Called! Item: [" + item + "]");
				count++;
				return item;
			}
		};

		ItemWriter itemWriter = new AbstractItemWriter() {
			public void write(Object item) throws Exception {
				logger.debug("Write Called! Item: [" + item + "]");
				if ("b".equals(item) || "d".equals(item)) {
					throw new RuntimeException("Read error - planned but skippable.");
				}
			}
		};
		factory.setItemReader(provider);
		factory.setItemWriter(itemWriter);
		factory.setRetryLimit(5);
		factory.setRetryableExceptionClasses(new Class[] { RuntimeException.class });
		AbstractStep step = (AbstractStep) factory.getObject();
		step.setName("mytest");
		StepExecution stepExecution = new StepExecution(step.getName(), jobExecution);
		step.execute(stepExecution);

		assertEquals(2, recovered.size());
		assertEquals(2, stepExecution.getSkipCount());
		assertEquals(2, stepExecution.getWriteSkipCount().intValue());
		// each item once, plus 5 failed retries each for b and d, plus the null
		// terminator
		assertEquals(17, count);
	}
}
