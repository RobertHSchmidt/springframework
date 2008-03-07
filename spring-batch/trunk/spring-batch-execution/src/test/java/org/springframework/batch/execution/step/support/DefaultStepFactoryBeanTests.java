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

package org.springframework.batch.execution.step.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.batch.core.BatchListener;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.execution.job.SimpleJob;
import org.springframework.batch.execution.repository.SimpleJobRepository;
import org.springframework.batch.execution.repository.dao.MapJobExecutionDao;
import org.springframework.batch.execution.repository.dao.MapJobInstanceDao;
import org.springframework.batch.execution.repository.dao.MapStepExecutionDao;
import org.springframework.batch.execution.step.AbstractStep;
import org.springframework.batch.execution.step.ItemOrientedStep;
import org.springframework.batch.item.AbstractItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.reader.ListItemReader;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;

public class DefaultStepFactoryBeanTests extends TestCase {

	private List recovered = new ArrayList();

	private SimpleJobRepository repository = new SimpleJobRepository(new MapJobInstanceDao(), new MapJobExecutionDao(),
			new MapStepExecutionDao());

	private List processed = new ArrayList();

	private ItemWriter processor = new AbstractItemWriter() {
		public void write(Object data) throws Exception {
			processed.add((String) data);
		}
	};

	private ItemReader provider;

	private SimpleJob job = new SimpleJob();;

	protected void setUp() throws Exception {
		super.setUp();
		job.setJobRepository(repository);
		MapJobInstanceDao.clear();
		MapJobExecutionDao.clear();
		MapStepExecutionDao.clear();
	}

	private DefaultStepFactoryBean getStep(String arg) throws Exception {
		return getStep(new String[] { arg });
	}

	private DefaultStepFactoryBean getStep(String arg0, String arg1) throws Exception {
		return getStep(new String[] { arg0, arg1 });
	}

	private DefaultStepFactoryBean getStep(String[] args) throws Exception {
		DefaultStepFactoryBean factory = new DefaultStepFactoryBean();

		List items = TransactionAwareProxyFactory.createTransactionalList();
		items.addAll(Arrays.asList(args));
		provider = new ListItemReader(items);

		factory.setItemReader(provider);
		factory.setItemWriter(processor);
		factory.setJobRepository(repository);
		factory.setTransactionManager(new ResourcelessTransactionManager());
		factory.setBeanName("stepName");
		return factory;
	}

	public void testSimpleJob() throws Exception {

		job.setSteps(new ArrayList());
		AbstractStep step = (AbstractStep) getStep("foo", "bar").getObject();
		step.setName("step1");
		job.addStep(step);
		step = (AbstractStep) getStep("spam").getObject();
		step.setName("step2");
		job.addStep(step);

		JobExecution jobExecution = repository.createJobExecution(job, new JobParameters());

		job.execute(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals(3, processed.size());
		assertTrue(processed.contains("foo"));
	}

	public void testSimpleJobWithItemListeners() throws Exception {

		final List throwables = new ArrayList();

		RepeatTemplate chunkOperations = new RepeatTemplate();
		// Always handle the exception a check it is the right one...
		chunkOperations.setExceptionHandler(new ExceptionHandler() {
			public void handleException(RepeatContext context, Throwable throwable) throws RuntimeException {
				throwables.add(throwable);
				assertEquals("Error!", throwable.getMessage());
			}
		});

		/*
		 * Each message fails once and the chunk (size=1) "rolls back"; then it
		 * is recovered ("skipped") on the second attempt (see retry policy
		 * definition above)...
		 */
		DefaultStepFactoryBean factory = getStep(new String[] { "foo", "bar", "spam" });

		factory.setItemWriter(new AbstractItemWriter() {
			public void write(Object data) throws Exception {
				throw new RuntimeException("Error!");
			}
		});
		factory.setListeners(new BatchListener[] { new ItemListenerSupport() {
			public void onReadError(Exception ex) {
				recovered.add(ex);
			}

			public void onWriteError(Exception ex, Object item) {
				recovered.add(ex);
			}
		} });

		ItemOrientedStep step = (ItemOrientedStep) factory.getObject();
		step.setChunkOperations(chunkOperations);

		job.setSteps(Collections.singletonList(step));

		JobExecution jobExecution = repository.createJobExecution(job, new JobParameters());
		job.execute(jobExecution);

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals(0, processed.size());
		// provider should be exhausted
		assertEquals(null, provider.read());
		assertEquals(3, recovered.size());
	}

	public void testExceptionTerminates() throws Exception {
		DefaultStepFactoryBean factory = getStep(new String[] { "foo", "bar", "spam" });
		factory.setBeanName("exceptionStep");
		factory.setItemWriter(new AbstractItemWriter() {
			public void write(Object data) throws Exception {
				throw new RuntimeException("Foo");
			}
		});
		ItemOrientedStep step = (ItemOrientedStep) factory.getObject();
		job.setSteps(Collections.singletonList(step));

		JobExecution jobExecution = repository.createJobExecution(job, new JobParameters());
		try {
			job.execute(jobExecution);
			fail("Expected RuntimeException");
		}
		catch (RuntimeException e) {
			assertEquals("Foo", e.getMessage());
			// expected
		}
		assertEquals(BatchStatus.FAILED, jobExecution.getStatus());
	}

}
