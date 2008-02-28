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
package org.springframework.batch.execution.step;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.domain.BatchStatus;
import org.springframework.batch.core.domain.JobInstance;
import org.springframework.batch.core.domain.JobInterruptedException;
import org.springframework.batch.core.domain.StepContribution;
import org.springframework.batch.core.domain.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.runtime.ExitStatusExceptionClassifier;
import org.springframework.batch.core.tasklet.Tasklet;
import org.springframework.batch.execution.step.support.SimpleExitStatusExceptionClassifier;
import org.springframework.batch.execution.step.support.StepInterruptionPolicy;
import org.springframework.batch.execution.step.support.ThreadStepInterruptionPolicy;
import org.springframework.batch.io.Skippable;
import org.springframework.batch.io.exception.BatchCriticalException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemRecoverer;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.KeyedItemReader;
import org.springframework.batch.item.exception.CommitFailedException;
import org.springframework.batch.item.stream.SimpleStreamManager;
import org.springframework.batch.repeat.ExitStatus;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatOperations;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.batch.retry.RetryPolicy;
import org.springframework.batch.retry.callback.ItemReaderRetryCallback;
import org.springframework.batch.retry.policy.ItemReaderRetryPolicy;
import org.springframework.batch.retry.support.RetryTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

/**
 * Simple implementation of executing the step as a set of chunks, each chunk
 * surrounded by a transaction. The structure is therefore that of two nested
 * loops, with transaction boundary around the whole inner loop. The outer loop
 * is controlled by the step operations ({@link #setStepOperations(RepeatOperations)}),
 * and the inner loop by the chunk operations ({@link #setChunkOperations(RepeatOperations)}).
 * The inner loop should always be executed in a single thread, so the chunk
 * operations should not do any concurrent execution. N.B. usually that means
 * that the chunk operations should be a {@link RepeatTemplate} (which is the
 * default).<br/>
 * 
 * Clients can use interceptors in the step operations to intercept or listen to
 * the iteration on a step-wide basis, for instance to get a callback when the
 * step is complete. Those that want callbacks at the level of an individual
 * tasks, can specify interceptors for the chunk operations.
 * 
 * @author Dave Syer
 * @author Lucas Ward
 * @author Ben Hale
 */
public class ItemOrientedStep extends AbstractStep implements InitializingBean {

	private static final Log logger = LogFactory.getLog(ItemOrientedStep.class);

	private RepeatOperations chunkOperations = new RepeatTemplate();

	private RepeatOperations stepOperations = new RepeatTemplate();

	private ExitStatusExceptionClassifier exceptionClassifier = new SimpleExitStatusExceptionClassifier();

	// default to checking current thread for interruption.
	private StepInterruptionPolicy interruptionPolicy = new ThreadStepInterruptionPolicy();

	private RetryPolicy retryPolicy = null;

	private RetryTemplate template = new RetryTemplate();

	private ItemReaderRetryCallback retryCallback;

	private int commitInterval = 0;

	private SimpleStreamManager streamManager;

	/**
	 * The {@link RepeatOperations} to use for the outer loop of the batch
	 * processing. Should be set up by the caller through a factory. Defaults to
	 * a plain {@link RepeatTemplate}.
	 * 
	 * @param stepOperations a {@link RepeatOperations} instance.
	 */
	public void setStepOperations(RepeatOperations stepOperations) {
		this.stepOperations = stepOperations;
	}

	/**
	 * The {@link RepeatOperations} to use for the inner loop of the batch
	 * processing. should be set up by the caller through a factory. defaults to
	 * a plain {@link RepeatTemplate}.
	 * 
	 * @param chunkoperations a {@link RepeatOperations} instance.
	 */
	public void setChunkOperations(RepeatOperations chunkoperations) {
		this.chunkOperations = chunkoperations;
	}

	/**
	 * Setter for the {@link StepInterruptionPolicy}. The policy is used to
	 * check whether an external request has been made to interrupt the job
	 * execution.
	 * 
	 * @param interruptionPolicy a {@link StepInterruptionPolicy}
	 */
	public void setInterruptionPolicy(StepInterruptionPolicy interruptionPolicy) {
		this.interruptionPolicy = interruptionPolicy;
	}

	/**
	 * Setter for the {@link ExitStatusExceptionClassifier} that will be used to
	 * classify any exception that causes a job to fail.
	 * 
	 * @param exceptionClassifier
	 */
	public void setExceptionClassifier(ExitStatusExceptionClassifier exceptionClassifier) {
		this.exceptionClassifier = exceptionClassifier;
	}

	/**
	 * Public setter for the retryPolicy.
	 * 
	 * @param retyPolicy the retryPolicy to set
	 */
	public void setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	public void setCommitInterval(int commitInterval) {
		this.commitInterval = commitInterval;
	}

	/**
	 * Check mandatory properties (reader and writer).
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(itemReader, "ItemReader must be provided");
		Assert.notNull(itemWriter, "ItemWriter must be provided");

		applyConfiguration();
	}

	/**
	 * Apply the configuration by inspecting it to see if it has any relevant
	 * policy information.
	 * 
	 * @param step a step
	 */
	void applyConfiguration() {

		ItemReaderRetryPolicy itemProviderRetryPolicy = new ItemReaderRetryPolicy(retryPolicy);
		template.setRetryPolicy(itemProviderRetryPolicy);

		if (retryPolicy != null) {
			Assert.state(itemReader instanceof KeyedItemReader,
					"ItemReader must be instance of KeyedItemReader to use the retry policy");
			retryCallback = new ItemReaderRetryCallback((KeyedItemReader) itemReader, itemWriter);
		}

		streamManager = new SimpleStreamManager();

		if (this.chunkOperations instanceof RepeatTemplate && commitInterval > 0) {
			((RepeatTemplate) chunkOperations).setCompletionPolicy(new SimpleCompletionPolicy(commitInterval));
		}

		if (this.stepOperations instanceof RepeatTemplate && exceptionHandler != null) {
			((RepeatTemplate) stepOperations).setExceptionHandler(exceptionHandler);
		}

	}

	/**
	 * Process the step and update its context so that progress can be monitored
	 * by the caller. The step is broken down into chunks, each one executing in
	 * a transaction. The step and its execution and execution context are all
	 * given an up to date {@link BatchStatus}, and the {@link JobRepository}
	 * is used to store the result. Various reporting information are also added
	 * to the current context (the {@link RepeatContext} governing the step
	 * execution, which would normally be available to the caller somehow
	 * through the step's {@link JobExecutionContext}.<br/>
	 * 
	 * @throws JobInterruptedException if the step or a chunk is interrupted
	 * @throws RuntimeException if there is an exception during a chunk
	 * execution
	 * @see StepExecutor#execute(StepExecution)
	 */
	public void execute(final StepExecution stepExecution) throws BatchCriticalException, JobInterruptedException {

		JobInstance jobInstance = stepExecution.getJobExecution().getJobInstance();
		StepExecution lastStepExecution = jobRepository.getLastStepExecution(jobInstance, this);

		boolean isRestart = jobRepository.getStepExecutionCount(jobInstance, this) > 0 ? true : false;

		ExitStatus status = ExitStatus.FAILED;
		final ExceptionHolder fatalException = new ExceptionHolder();

		try {

			stepExecution.setStartTime(new Date(System.currentTimeMillis()));
			// We need to save the step execution right away, before we start
			// using its ID. It would be better to make the creation atomic in
			// the caller.
			fatalException.setException(updateStatus(stepExecution, BatchStatus.STARTED));

			possiblyRegisterStreams();

			if (isRestart && lastStepExecution != null) {
				stepExecution.setExecutionContext(lastStepExecution.getExecutionContext());
			}
			else {
				stepExecution.setExecutionContext(new ExecutionContext());
			}

			// Open the stream manager *after* the execution context is fixed in
			// the step, otherwise it will not be the same reference that is
			// updated by the streams. TODO: this is a little fragile - maybe
			// StreamManager.update() should accept the context as a parameter.
			streamManager.open(stepExecution.getExecutionContext());

			status = stepOperations.iterate(new RepeatCallback() {

				public ExitStatus doInIteration(final RepeatContext context) throws Exception {

					final StepContribution contribution = stepExecution.createStepContribution();
					contribution.setExecutionContext(stepExecution.getExecutionContext());
					// Before starting a new transaction, check for
					// interruption.
					interruptionPolicy.checkInterrupted(context);

					ExitStatus result;

					TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());

					try {
						itemReader.mark();
						result = processChunk(contribution);

						contribution.incrementCommitCount();

						// If the step operations are asynchronous then we need
						// to synchronize changes to the step execution (at a
						// minimum).
						synchronized (stepExecution) {

							// Apply the contribution to the step
							// only if chunk was successful
							stepExecution.apply(contribution);

							streamManager.update(stepExecution.getExecutionContext());
							try {
								stepExecution.setStatus(BatchStatus.COMPLETED);
								jobRepository.saveOrUpdateExecutionContext(stepExecution);
							}
							catch (Exception e) {
								fatalException.setException(e);
								stepExecution.setStatus(BatchStatus.UNKNOWN);
								throw new CommitFailedException("Fatal error detected during commit", e);
							}

						}

						try {
							itemReader.mark();
							itemWriter.flush();
							transactionManager.commit(transaction);
						}
						catch (Exception e) {
							fatalException.setException(e);
							stepExecution.setStatus(BatchStatus.UNKNOWN);
							throw new CommitFailedException("Fatal error detected during commit", e);
						}

					}
					catch (CommitFailedException e) {
						throw e;
					}
					catch (Throwable t) {
						/*
						 * Any exception thrown within the transaction template
						 * will automatically cause the transaction to rollback.
						 * We need to include exceptions during an attempted
						 * commit (e.g. Hibernate flush) so this catch block
						 * comes outside the transaction.
						 */
						synchronized (stepExecution) {
							stepExecution.rollback();
						}
						try {
							itemReader.reset();
							itemWriter.clear();
							transactionManager.rollback(transaction);
						}
						catch (Exception e) {
							fatalException.setException(e);
							stepExecution.setStatus(BatchStatus.UNKNOWN);
						}

						if (itemSkipPolicy.shouldFail(t)) {
							if (t instanceof RuntimeException) {
								throw (RuntimeException) t;
							}
							else {
								throw new RuntimeException(t);
							}
						}
						else {
							logger.error("Exception should not cause step to fail", t);
						}

						result = ExitStatus.CONTINUABLE;
					}

					// Check for interruption after transaction as well, so that
					// the interrupted exception is correctly propagated up to
					// caller
					interruptionPolicy.checkInterrupted(context);

					return result;

				}
			});

			fatalException.setException(updateStatus(stepExecution, BatchStatus.COMPLETED));
		}
		catch (CommitFailedException e) {
			logger.error("Fatal error detected during commit.");
			throw e;
		}
		catch (RuntimeException e) {

			// classify exception so an exit code can be stored.
			status = exceptionClassifier.classifyForExitCode(e);
			if (e.getCause() instanceof JobInterruptedException) {
				updateStatus(stepExecution, BatchStatus.STOPPED);
				throw (JobInterruptedException) e.getCause();
			}
			else if (!fatalException.hasException()) {
				updateStatus(stepExecution, BatchStatus.FAILED);
				throw e;
			}
			else {
				logger.error("Fatal error detected during rollback caused by underlying exception: ", e);
				throw e;
			}

		}
		finally {

			stepExecution.setExitStatus(status);
			stepExecution.setEndTime(new Date(System.currentTimeMillis()));

			try {
				jobRepository.saveOrUpdate(stepExecution);
			}
			catch (RuntimeException e) {
				String msg = "Fatal error detected during final save of meta data";
				logger.error(msg, e);
				if (!fatalException.hasException()) {
					fatalException.setException(e);
				}
				throw new BatchCriticalException(msg, fatalException.getException());
			}

			try {
				streamManager.close(stepExecution.getExecutionContext());
			}
			catch (RuntimeException e) {
				String msg = "Fatal error detected during close of streams. "
						+ "The job execution completed (possibly unsuccessfully but with consistent meta-data).";
				logger.error(msg, e);
				if (!fatalException.hasException()) {
					fatalException.setException(e);
				}
				throw new BatchCriticalException(msg, fatalException.getException());
			}

			if (fatalException.hasException()) {
				throw new BatchCriticalException("Encountered an error saving batch meta data.", fatalException
						.getException());
			}

		}

	}

	/**
	 * 
	 */
	private void possiblyRegisterStreams() {
		if (itemReader instanceof ItemStream) {
			ItemStream stream = (ItemStream) itemReader;
			streamManager.register(stream);
		}
		if (itemWriter instanceof ItemStream) {
			ItemStream stream = (ItemStream) itemWriter;
			streamManager.register(stream);
		}
	}

	/**
	 * Execute a bunch of identical business logic operations all within a
	 * transaction. The transaction is programmatically started and stopped
	 * outside this method, so subclasses that override do not need to create a
	 * transaction.
	 * 
	 * @param step the current step containing the {@link Tasklet} with the
	 * business logic.
	 * @return true if there is more data to process.
	 */
	ExitStatus processChunk(final StepContribution contribution) {
		ExitStatus result = chunkOperations.iterate(new RepeatCallback() {
			public ExitStatus doInIteration(final RepeatContext context) throws Exception {
				if (contribution.isTerminateOnly()) {
					context.setTerminateOnly();
				}
				// check for interruption before each item as well
				interruptionPolicy.checkInterrupted(context);
				ExitStatus exitStatus = doProcessing(contribution);
				contribution.incrementTaskCount();
				// check for interruption after each item as well
				interruptionPolicy.checkInterrupted(context);
				return exitStatus;
			}
		});
		return result;
	}

	/**
	 * Execute the business logic, delegating to the given {@link Tasklet}.
	 * Subclasses could extend the behaviour as long as they always return the
	 * value of this method call in their superclass.<br/>
	 * 
	 * If there is an exception and the {@link Tasklet} implements
	 * {@link Skippable} then the skip method is called.
	 * 
	 * @param tasklet the unit of business logic to execute
	 * @param contribution the current step
	 * @return boolean if there is more processing to do
	 * @throws Exception if there is an error
	 */
	private ExitStatus doProcessing(StepContribution contribution) throws Exception {
		ExitStatus exitStatus = ExitStatus.CONTINUABLE;

		try {

			exitStatus = execute();

		}
		catch (Exception e) {
			if (itemSkipPolicy.shouldSkip(e, contribution.getSkipCount())) {
				contribution.incrementSkipCount();
				skip();
			}
			else {
				// Rethrow so that outer transaction is rolled back properly
				throw e;
			}
		}

		return exitStatus;
	}

	/**
	 * Read from the {@link ItemReader} and process (if not null) with the
	 * {@link ItemWriter}. The call to {@link ItemWriter} is wrapped in a
	 * stateful retry, if a {@link RetryPolicy} is provided. The
	 * {@link ItemRecoverer} is used (if provided) in the case of an exception
	 * to apply alternate processing to the item. If the stateful retry is in
	 * place then the recovery will happen in the next transaction
	 * automatically, otherwise it might be necessary for clients to make the
	 * recover method transactional with appropriate propagation behaviour
	 * (probably REQUIRES_NEW because the call will happen in the context of a
	 * transaction that is about to rollback).
	 * 
	 * @see org.springframework.batch.core.tasklet.Tasklet#execute()
	 */
	private ExitStatus execute() throws Exception {

		if (retryCallback == null) {
			Object item = null;
			try {
				item = itemReader.read();
			}
			catch (Exception ex) {
				itemFailureHandler.handleReadFailure(ex);
				throw ex;
			}
			if (item == null) {
				return ExitStatus.FINISHED;
			}
			try {
				itemWriter.write(item);
			}
			catch (Exception e) {

				itemFailureHandler.handleWriteFailure(item, e);
				// Re-throw the exception so that the surrounding transaction
				// rolls back if there is one
				throw e;
			}
			return ExitStatus.CONTINUABLE;
		}

		return new ExitStatus(template.execute(retryCallback) != null);

	}

	/**
	 * Mark the current item as skipped if possible. If there is a retry policy
	 * in action there is no need to take any action now because it will be
	 * covered by the retry in the next transaction. Otherwise if the reader and /
	 * or writer are {@link Skippable} then delegate to them in that order.
	 * 
	 * @see org.springframework.batch.io.Skippable#skip()
	 */
	private void skip() {
		if (retryCallback != null) {
			// No need to skip because the recoverer will take any action
			// necessary.
			return;
		}
		if (this.itemReader instanceof Skippable) {
			((Skippable) this.itemReader).skip();
		}
		if (this.itemWriter instanceof Skippable) {
			((Skippable) this.itemWriter).skip();
		}
	}

	/**
	 * Convenience method to update the status in all relevant places.
	 * 
	 * @param stepInstance the current step
	 * @param stepExecution the current stepExecution
	 * @param status the status to set
	 */
	private Exception updateStatus(StepExecution stepExecution, BatchStatus status) {
		stepExecution.setStatus(status);
		try {
			jobRepository.saveOrUpdate(stepExecution);
			return null;
		}
		catch (Exception e) {
			return e;
		}

	}

	/**
	 * @author Dave Syer
	 * 
	 */
	private static class ExceptionHolder {

		private Exception exception;

		public boolean hasException() {
			return exception != null;
		}

		/**
		 * @param exception
		 */
		public void setException(Exception exception) {
			this.exception = exception;
		}

		/**
		 * @return
		 */
		public Exception getException() {
			return this.exception;
		}

	}

}
