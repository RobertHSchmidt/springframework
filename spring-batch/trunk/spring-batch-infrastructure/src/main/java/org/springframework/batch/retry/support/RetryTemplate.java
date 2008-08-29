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

package org.springframework.batch.retry.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.retry.ExhaustedRetryException;
import org.springframework.batch.retry.RecoveryCallback;
import org.springframework.batch.retry.RetryCallback;
import org.springframework.batch.retry.RetryContext;
import org.springframework.batch.retry.RetryException;
import org.springframework.batch.retry.RetryListener;
import org.springframework.batch.retry.RetryOperations;
import org.springframework.batch.retry.RetryPolicy;
import org.springframework.batch.retry.RetryState;
import org.springframework.batch.retry.TerminatedRetryException;
import org.springframework.batch.retry.backoff.BackOffContext;
import org.springframework.batch.retry.backoff.BackOffInterruptedException;
import org.springframework.batch.retry.backoff.BackOffPolicy;
import org.springframework.batch.retry.backoff.NoBackOffPolicy;
import org.springframework.batch.retry.policy.MapRetryContextCache;
import org.springframework.batch.retry.policy.RetryContextCache;
import org.springframework.batch.retry.policy.SimpleRetryPolicy;
import org.springframework.batch.support.Classifier;

/**
 * Template class that simplifies the execution of operations with retry
 * semantics. <br/> Retryable operations are encapsulated in implementations of
 * the {@link RetryCallback} interface and are executed using one of the
 * supplied execute methods. <br/>
 * 
 * By default, an operation is retried if is throws any {@link Exception} or
 * subclass of {@link Exception}. This behaviour can be changed by using the
 * {@link #setRetryPolicy(RetryPolicy)} method. <br/>
 * 
 * Also by default, each operation is retried for a maximum of three attempts
 * with no back off in between. This behaviour can be configured using the
 * {@link #setRetryPolicy(RetryPolicy)} and
 * {@link #setBackOffPolicy(BackOffPolicy)} properties. The
 * {@link org.springframework.batch.retry.backoff.BackOffPolicy} controls how
 * long the pause is between each individual retry attempt. <br/>
 * 
 * This class is thread-safe and suitable for concurrent access when executing
 * operations and when performing configuration changes. As such, it is possible
 * to change the number of retries on the fly, as well as the
 * {@link BackOffPolicy} used and no in progress retryable operations will be
 * affected.
 * 
 * @author Rob Harrop
 * @author Dave Syer
 */
public class RetryTemplate implements RetryOperations {

	protected final Log logger = LogFactory.getLog(getClass());

	private volatile BackOffPolicy backOffPolicy = new NoBackOffPolicy();

	private volatile RetryPolicy retryPolicy = new SimpleRetryPolicy();

	private volatile RetryListener[] listeners = new RetryListener[0];

	private RetryContextCache retryContextCache = new MapRetryContextCache();

	private Classifier<? super Throwable, Boolean> rollbackClassifier = null;

	/**
	 * Public setter for the rollback classifier. This classifier answers its
	 * default if the exception provided should not cause a rollback. I.e.
	 * anything other than the default will lead to a rollback.<br/><br/>
	 * 
	 * The decision whether to rollback or not is unrelated to that of the
	 * {@link RetryPolicy}, but the policy can be accidentally inconsistent
	 * with the rollback decision. E.g. in a stateless retry the policy might be
	 * configured to allow retry on a rollback, but that wouldn't make sense - a
	 * stateful retry should have been used. The best we can do in such
	 * circumstances is throw a {@link RetryException} from
	 * {@link #execute(RetryCallback)} or
	 * {@link #execute(RetryCallback, RecoveryCallback)}. The recovery path
	 * will not be taken in such situations.<br/><br/>
	 * 
	 * For stateless retry it is often adequate to use the default behaviour, as
	 * long as one is careful with the retry policy (exceptions which should
	 * cause rollback are still not really retryable in a transactional
	 * setting).<br/><br/>
	 * 
	 * For stateful retry adding a classifier will allow an optimisation:
	 * exceptions which are not marked for rollback can still be retried, but
	 * without paying the cost of a rollback. Effectively one is overriding the
	 * stateful quality of the retry dynamically, according to the exception
	 * type.<br/><br/>
	 * 
	 * Example usage would be for a stateful retry to specify a validation exception as not for rollback
	 * 
	 * If not set then the default is to rollback for all exceptions when the
	 * retry is stateful, and for none when it is stateless.
	 * 
	 * @param rollbackClassifier the rollback classifier to set
	 */
	public void setRollbackClassifier(Classifier<? super Throwable, Boolean> rollbackClassifier) {
		this.rollbackClassifier = rollbackClassifier;
	}

	/**
	 * Public setter for the {@link RetryContextCache}.
	 * @param retryContextCache the {@link RetryContextCache} to set.
	 */
	public void setRetryContextCache(RetryContextCache retryContextCache) {
		this.retryContextCache = retryContextCache;
	}

	/**
	 * Setter for listeners. The listeners are executed before and after a retry
	 * block (i.e. before and after all the attempts), and on an error (every
	 * attempt).
	 * @param listeners
	 * @see RetryListener
	 */
	public void setListeners(RetryListener[] listeners) {
		this.listeners = listeners;
	}

	/**
	 * Register an additional listener.
	 * @param listener
	 * @see #setListeners(RetryListener[])
	 */
	public void registerListener(RetryListener listener) {
		List<RetryListener> list = new ArrayList<RetryListener>(Arrays.asList(listeners));
		list.add(listener);
		listeners = list.toArray(new RetryListener[list.size()]);
	}

	/**
	 * Setter for {@link BackOffPolicy}.
	 * @param backOffPolicy
	 */
	public void setBackOffPolicy(BackOffPolicy backOffPolicy) {
		this.backOffPolicy = backOffPolicy;
	}

	/**
	 * Setter for {@link RetryPolicy}.
	 * 
	 * @param retryPolicy
	 */
	public void setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	/**
	 * Keep executing the callback until it either succeeds or the policy
	 * dictates that we stop, in which case the most recent exception thrown by
	 * the callback will be rethrown.
	 * 
	 * @see org.springframework.batch.retry.RetryOperations#execute(org.springframework.batch.retry.RetryCallback)
	 * 
	 * @throws TerminatedRetryException if the retry has been manually
	 * terminated by a listener.
	 */
	public final <T> T execute(RetryCallback<T> retryCallback) throws Exception {
		return doExecute(retryCallback, null, null);
	}

	/**
	 * Keep executing the callback until it either succeeds or the policy
	 * dictates that we stop, in which case the recovery callback will be
	 * executed.
	 * 
	 * @see org.springframework.batch.retry.RetryOperations#execute(org.springframework.batch.retry.RetryCallback,
	 * org.springframework.batch.retry.RecoveryCallback)
	 * 
	 * @throws TerminatedRetryException if the retry has been manually
	 * terminated by a listener.
	 */
	public final <T> T execute(RetryCallback<T> retryCallback, RecoveryCallback<T> recoveryCallback) throws Exception {
		return doExecute(retryCallback, recoveryCallback, null);
	}

	/**
	 * Execute the callback once if the policy dictates that we can, re-throwing
	 * any exception encountered.
	 * 
	 * @see org.springframework.batch.retry.RetryOperations#execute(RetryCallback,
	 * RetryState)
	 * 
	 * @throws ExhaustedRetryException if the retry has been exhausted.
	 */
	public final <T> T execute(RetryCallback<T> retryCallback, RetryState retryState) throws Exception,
			ExhaustedRetryException {
		return doExecute(retryCallback, null, retryState);
	}

	/**
	 * Execute the callback once if the policy dictates that we can, re-throwing
	 * any exception encountered.
	 * 
	 * @see org.springframework.batch.retry.RetryOperations#execute(RetryCallback,
	 * RetryState)
	 */
	public final <T> T execute(RetryCallback<T> retryCallback, RecoveryCallback<T> recoveryCallback,
			RetryState retryState) throws Exception, ExhaustedRetryException {
		return doExecute(retryCallback, recoveryCallback, retryState);
	}

	/**
	 * Execute the callback once if the policy dictates that we can, otherwise
	 * execute the recovery callback.
	 * 
	 * @see org.springframework.batch.retry.RetryOperations#execute(RetryCallback,
	 * RecoveryCallback, RetryState)
	 * @throws ExhaustedRetryException if the retry has been exhausted.
	 */
	protected <T> T doExecute(RetryCallback<T> retryCallback, RecoveryCallback<T> recoveryCallback, RetryState state)
			throws Exception, ExhaustedRetryException {

		RetryPolicy retryPolicy = this.retryPolicy;
		BackOffPolicy backOffPolicy = this.backOffPolicy;

		// Allow the retry policy to initialise itself...
		RetryContext context = open(retryPolicy, state);

		// Make sure the context is available globally for clients who need
		// it...
		RetrySynchronizationManager.register(context);

		Throwable lastException = null;

		try {

			// Give clients a chance to enhance the context...
			boolean running = doOpenInterceptors(retryCallback, context);

			if (!running) {
				throw new TerminatedRetryException("Retry terminated abnormally by interceptor before first attempt");
			}

			// Start the backoff context...
			BackOffContext backOffContext = backOffPolicy.start(context);

			/*
			 * We allow the whole loop to be skipped if the policy or context
			 * already forbid the first try. This is used in the case of
			 * external retry to allow a recovery in handleRetryExhausted
			 * without the callback processing (which would throw an exception).
			 */
			while (retryPolicy.canRetry(context) && !context.isExhaustedOnly()) {

				try {
					logger.debug("Retry: count=" + context.getRetryCount());
					// Reset the last exception, so if we are successful
					// the close interceptors will not think we failed...
					lastException = null;
					return retryCallback.doWithRetry(context);
				}
				catch (Exception e) {

					lastException = e;

					doOnErrorInterceptors(retryCallback, context, e);

					registerThrowable(retryPolicy, state, context, e);

					if (shouldRethrow(retryPolicy, context, state)) {
						logger.debug("Rethrow in retry for policy: count=" + context.getRetryCount());
						throw e;
					}

				}

				try {
					backOffPolicy.backOff(backOffContext);
				}
				catch (BackOffInterruptedException e) {
					lastException = e;
					// back off was prevented by another thread - fail the
					// retry
					logger.debug("Abort retry because interrupted: count=" + context.getRetryCount());
					throw e;
				}

				/*
				 * A stateful policy that can retry should have rethrown the
				 * exception by now - i.e. we shouldn't get this far for a
				 * stateful policy if it can retry.
				 */
			}

			logger.debug("Retry failed last attempt: count=" + context.getRetryCount());

			if (context.isExhaustedOnly()) {
				throw new ExhaustedRetryException("Retry exhausted after last attempt with no recovery path.", context
						.getLastThrowable());
			}

			return handleRetryExhausted(recoveryCallback, context, state);

		}
		finally {
			close(retryPolicy, context, state, lastException == null);
			doCloseInterceptors(retryCallback, context, lastException);
			RetrySynchronizationManager.clear();
		}

	}

	/**
	 * Clean up the cache if necessary and close the context provided (if the
	 * flag indicates that processing was successful).
	 * 
	 * @param context
	 * @param state
	 * @param succeeded
	 */
	protected void close(RetryPolicy retryPolicy, RetryContext context, RetryState state, boolean succeeded) {
		if (state != null) {
			if (succeeded) {
				retryContextCache.remove(state.getKey());
				retryPolicy.close(context);
			}
		}
		else {
			retryPolicy.close(context);
		}
	}

	/**
	 * @param retryPolicy
	 * @param state
	 * @param context
	 * @param e
	 */
	protected void registerThrowable(RetryPolicy retryPolicy, RetryState state, RetryContext context, Exception e) {
		if (state != null) {
			Object key = state.getKey();
			if (context.getRetryCount() > 0 && !retryContextCache.containsKey(key)) {
				throw new RetryException("Inconsistent state for failed item key: cache key has changed. "
						+ "Consider whether equals() or hashCode() for the key might be inconsistent, "
						+ "or if you need to supply a better key");
			}
			retryContextCache.put(key, context);
		}
		retryPolicy.registerThrowable(context, e);
	}

	/**
	 * Delegate to the {@link RetryPolicy} having checked in the cache for an
	 * existing value if the state is not null.
	 * 
	 * @param retryPolicy a {@link RetryPolicy} to delegate the context creation
	 * @return a retry context, either a new one or the one used last time the
	 * same state was encountered
	 */
	protected RetryContext open(RetryPolicy retryPolicy, RetryState state) {

		if (state == null) {
			return doOpenInternal(retryPolicy);
		}

		Object key = state.getKey();
		if (state.isForceRefresh()) {
			return doOpenInternal(retryPolicy);
		}
		else if (retryContextCache.containsKey(key)) {

			RetryContext context = retryContextCache.get(key);
			if (context == null) {
				throw new RetryException("Inconsistent state for failed item: no history found. "
						+ "Consider whether equals() or hashCode() for the item might be inconsistent, "
						+ "or if you need to supply a better ItemKeyGenerator");
			}
			return context;

		}
		else {

			// The cache is only used if there is a failure.
			return doOpenInternal(retryPolicy);

		}

	}

	/**
	 * @param retryPolicy
	 * @return
	 */
	private RetryContext doOpenInternal(RetryPolicy retryPolicy) {
		return retryPolicy.open(RetrySynchronizationManager.getContext());
	}

	/**
	 * Actions to take after final attempt has failed. If there is state clean
	 * up the cache. If there is a recovery callback, execute that and return
	 * its result. Otherwise throw an exception.
	 * 
	 * @param recoveryCallback the callback for recovery (might be null)
	 * @param context the current retry context
	 * @throws Exception if the callback does, and if there is no callback and
	 * the state is null then the last exception from the context
	 * @throws ExhaustedRetryException if the state is not null and there is no
	 * recovery callback
	 */
	protected <T> T handleRetryExhausted(RecoveryCallback<T> recoveryCallback, RetryContext context, RetryState state)
			throws Exception {
		if (state != null) {
			retryContextCache.remove(state.getKey());
		}
		if (recoveryCallback != null) {
			return recoveryCallback.recover(context);
		}
		if (state != null) {
			logger.debug("Retry exhausted after last attempt with no recovery path.");
			throw new ExhaustedRetryException("Retry exhausted after last attempt with no recovery path", context
					.getLastThrowable());
		}
		throw context.getLastThrowable();
	}

	/**
	 * Extension point for subclasses to decide on behaviour after catching an
	 * exception in a {@link RetryCallback}. Normal stateless behaviour is not
	 * to rethrow, and if there is state we rethrow.
	 * 
	 * @param retryPolicy
	 * @param context the current context
	 * 
	 * @return true if the state is not null but subclasses might choose
	 * otherwise
	 */
	protected boolean shouldRethrow(RetryPolicy retryPolicy, RetryContext context, RetryState state) {
		// Allow stateless behaviour to take over for certain exception types
		if (rollbackClassifier != null) {
			boolean rollback = rollbackClassifier.classify(context.getLastThrowable());
			if (rollback && state == null && retryPolicy.canRetry(context)) {
				throw new RetryException("Inconsistent configuration.  The retry policy says we can retry but "
						+ "the exception has been marked for rollback.", context.getLastThrowable());
			}
			return rollback;
		}
		// If no classifier is provided, just assume the all exceptions are for
		// rollback if the execution is stateful, and none otherwise.
		return state != null;
	}

	private <T> boolean doOpenInterceptors(RetryCallback<T> callback, RetryContext context) {

		boolean result = true;

		for (int i = 0; i < listeners.length; i++) {
			result = result && listeners[i].open(context, callback);
		}

		return result;

	}

	private <T> void doCloseInterceptors(RetryCallback<T> callback, RetryContext context, Throwable lastException) {
		for (int i = listeners.length; i-- > 0;) {
			listeners[i].close(context, callback, lastException);
		}
	}

	private <T> void doOnErrorInterceptors(RetryCallback<T> callback, RetryContext context, Throwable throwable) {
		for (int i = listeners.length; i-- > 0;) {
			listeners[i].onError(context, callback, throwable);
		}
	}

}
